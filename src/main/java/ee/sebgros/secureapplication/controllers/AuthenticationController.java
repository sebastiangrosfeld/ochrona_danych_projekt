package ee.sebgros.secureapplication.controllers;

import ee.sebgros.secureapplication.data.LoginRequest;
import ee.sebgros.secureapplication.data.RegisterRequest;
import ee.sebgros.secureapplication.data.ResetPasswdRequest;
import ee.sebgros.secureapplication.data.RestorePasswordRequest;
import ee.sebgros.secureapplication.repository.entity.ActivateAccount;
import ee.sebgros.secureapplication.repository.entity.LoginLog;
import ee.sebgros.secureapplication.repository.entity.RestorePasswd;
import ee.sebgros.secureapplication.repository.entity.User;
import ee.sebgros.secureapplication.repository.repositories.ActivateAccRepository;
import ee.sebgros.secureapplication.repository.repositories.LoginLogRepository;
import ee.sebgros.secureapplication.repository.repositories.RestorePasswdRepository;
import ee.sebgros.secureapplication.repository.repositories.UserRepository;
import ee.sebgros.secureapplication.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Date;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final ActivateAccRepository activateAccRepository;
	private final RestorePasswdRepository restorePasswdRepository;
	private final DateService dateService;
	private final LoginAttemptService loginAttemptService;
	private final IpService ipService;
	private final LoginLogRepository loginLogRepository;
	private final EmailService emailService;
	private final QRService qrService;

	@GetMapping("/login")
	public String getLogin(Model model) {
		model.addAttribute("login_request", new LoginRequest());
		return "login";
	}

	@GetMapping("/login-error")
	public String loginError(Model model) {
		model.addAttribute("loginError", true);
		if (loginAttemptService.isBlocked(ipService.getClientIP())) {
			model.addAttribute("isBlocked", true);
		}
		return "login";
	}

	@GetMapping("/perform_logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/";
	}

	@GetMapping("/register")
	public String getRegister(Model model) {
		model.addAttribute("registerRequest", new RegisterRequest());
		return "register";
	}

	@PostMapping("/register")
	public String postCreateNote(@Valid RegisterRequest registerRequest, Errors errors, Model model) throws InterruptedException {
		if (loginAttemptService.isBlocked(ipService.getClientIP())) {
			model.addAttribute("registerError", true);
			return "register";
		}

		if (registerRequest.getIsNotOk() != null && registerRequest.getIsNotOk()) {
			log.error("BOT");
			loginAttemptService.block(ipService.getClientIP());
		}

		if (errors.hasErrors()) {
			return "register";
		}
		Thread.sleep(3000);

		if (userRepository.findFirstByUsernameIgnoreCase(registerRequest.getUsername()).isPresent()
				|| userRepository.findFirstByEmailIgnoreCase(registerRequest.getEmail()).isPresent()) {
			model.addAttribute("registerError", true);
			return "register";
		}
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setIsValid(false);
		user.setTotpSecret(Base32.random());
		user = userRepository.save(user);
		try {
			ActivateAccount activateAcc = new ActivateAccount();
			activateAcc.setUser(user);
			activateAcc.setValidTime(dateService.getValidDate());
			activateAcc = activateAccRepository.save(activateAcc);
			model.addAttribute("uuid", "/activate/" + activateAcc.getId());

			final var baseAddressUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			final var activeUrl = baseAddressUrl + "/activate/" + activateAcc.getId() + "\n" + "Totp Code: " + user.getTotpSecret();

			String qr = qrService.generateQRUrl(user);
			model.addAttribute("qrCode", qr);
			model.addAttribute("isQrGenerated", true);

			emailService.sendMail("Activation of account in secure notes app", activeUrl, registerRequest.getEmail());
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
			user.setIsValid(true);
			userRepository.save(user);
		}

		return "login";
	}

	@GetMapping("/activate/{id}")
	public String activateAccount(@PathVariable("id") String uid) {
		ActivateAccount activateAcc = activateAccRepository.findById(uid).orElse(null);
		if (activateAcc == null) {
			return "redirect:/register";
		}
		if (activateAcc.getValidTime().compareTo(new Date()) < 0) {
			return "redirect:/register";
		}
		User a = activateAcc.getUser();
		a.setIsValid(true);
		userRepository.save(a);
		activateAccRepository.delete(activateAcc);


		LoginLog loginLog = new LoginLog();
		loginLog.setIp(ipService.getClientIP());
		loginLog.setOperation("activate");
		loginLog.setResult("SUCCESS");
		loginLog.setUser(a);
		loginLog.setDate(new Date().toString());
		loginLogRepository.save(loginLog);

		return "redirect:/login";
	}

	@GetMapping("/restore-passwd")
	public String getResetPassword(Model model) {
		model.addAttribute("restorePasswordRequest", new RestorePasswordRequest());
		return "restore_passwd";
	}

	@PostMapping("/restore-passwd")
	public String postResetPassword(Model model, RestorePasswordRequest restorePasswordRequest) throws InterruptedException {
		Thread.sleep(3000);
		if (loginAttemptService.isBlocked(ipService.getClientIP())) {
			model.addAttribute("registerError", true);
			return "register";
		}

		if (restorePasswordRequest.getShoeSize() != null && restorePasswordRequest.getShoeSize()) {
			log.error("BOT");
			loginAttemptService.block(ipService.getClientIP());
		}


		User user = userRepository.findFirstByEmailIgnoreCase(restorePasswordRequest.getEmail()).orElse(null);
		if (user == null) {
			model.addAttribute("error", true);
			return "restore_passwd";
		}

		restorePasswdRepository.deleteAll(restorePasswdRepository.findAllByUserId(user.getId()));

		try {
			RestorePasswd restorePasswd = new RestorePasswd();
			restorePasswd.setUser(user);
			restorePasswd.setValidTime(dateService.getValidDate());
			restorePasswdRepository.save(restorePasswd);
			model.addAttribute("reset", "/reset_passwd/" + restorePasswd.getId());

			final var baseAddressUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			final var activeUrl = baseAddressUrl + "/reset_passwd/" + restorePasswd.getId();
			emailService.sendMail("Password Restore", activeUrl, user.getEmail());
		} catch (Exception e) {
			model.addAttribute("error", true);
			return "restore_passwd";
		}

		LoginLog loginLog = new LoginLog();
		loginLog.setOperation("Reset_passwd_request");
		loginLog.setIp(ipService.getClientIP());
		loginLog.setResult("REQUESTED");
		loginLog.setUser(user);
		loginLog.setDate(new Date().toString());
		loginLogRepository.save(loginLog);

		return "login";
	}

	@GetMapping("/reset_passwd/{id}")
	public String getResetHardPasswordForm(Model model, @PathVariable("id") String uid) {
		model.addAttribute("uuid", uid);
		model.addAttribute("resetPasswdRequest", new ResetPasswdRequest());
		return "reset_passwd";
	}

	@PostMapping("/reset_passwd/{id}")
	public String postResetHardPasswordForm(@Valid ResetPasswdRequest resetPasswdRequest, Errors errors, Model model, @PathVariable("id") String uid) throws InterruptedException {

		if (loginAttemptService.isBlocked(ipService.getClientIP())) {
			model.addAttribute("registerError", true);
			return "register";
		}

		if (resetPasswdRequest.getShoeSize() != null && resetPasswdRequest.getShoeSize()) {
			log.error("BOT");
			loginAttemptService.block(ipService.getClientIP());
		}

		model.addAttribute("uuid", uid);
		model.addAttribute("resetPasswdRequest", new ResetPasswdRequest());
		if (errors.hasErrors()) {
			return "reset_passwd";
		}

		Thread.sleep(3000);

		RestorePasswd restorePasswd = restorePasswdRepository.findById(uid).orElse(null);
		if (restorePasswd == null) {
			model.addAttribute("error", true);
			return "reset_passwd";
		}
		if (restorePasswd.getValidTime().compareTo(new Date()) < 0) {
			model.addAttribute("error", true);
			return "reset_passwd";
		}

		User user = restorePasswd.getUser();
		user.setPassword(passwordEncoder.encode(resetPasswdRequest.getPassword()));
		userRepository.save(user);

		restorePasswdRepository.delete(restorePasswd);

		LoginLog loginLog = new LoginLog();
		loginLog.setIp(ipService.getClientIP());
		loginLog.setOperation("reset_passwd");
		loginLog.setResult("SUCCESS");
		loginLog.setUser(user);
		loginLog.setDate(new Date().toString());
		loginLogRepository.save(loginLog);

		return "redirect:/login";
	}

	@GetMapping("/logs")
	public String getLogs(Model model, @AuthenticationPrincipal User user) {

		model.addAttribute("loginLog", loginLogRepository.findAllByUserId(user.getId()));
		return "loggin_log";
	}
}
