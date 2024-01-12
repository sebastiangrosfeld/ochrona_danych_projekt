package ee.sebgros.secureapplication.config;

import ee.sebgros.secureapplication.repository.entity.LoginLog;
import ee.sebgros.secureapplication.repository.entity.User;
import ee.sebgros.secureapplication.repository.repositories.LoginLogRepository;
import ee.sebgros.secureapplication.repository.repositories.UserRepository;
import ee.sebgros.secureapplication.services.IpService;
import ee.sebgros.secureapplication.services.LoginAttemptService;
import ee.sebgros.secureapplication.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.Date;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {
	private final IpService ipService;
	private final UserRepository userRepository;
	private final CustomWebAuthenticationSource customWebAuthenticationSource;

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl(ipService);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(11, new SecureRandom());
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new CustomAuthenticationProvider(userRepository);
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.headers()
				.addHeaderWriter(new StaticHeadersWriter("Server", ""))
				.and()
				.formLogin()
				.authenticationDetailsSource(customWebAuthenticationSource)
				.loginPage("/login")
				.failureUrl("/login-error")
				.and()
				.logout()
				.logoutUrl("/perform_logout")
				.logoutSuccessUrl("/")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.and()
				.authorizeRequests()
				.antMatchers("/", "/login", "/register", "/perform_logout", "/login-error",
						"/activate/*", "/restore-passwd", "/reset_passwd/*", "/js/**", "/static/**",
						"/resources/**").permitAll()
				.anyRequest().authenticated()
				.and().httpBasic();
		return http.build();
	}

	@Component
	public static class AuthenticationFailureListener implements
			ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

		@Autowired
		private HttpServletRequest request;

		@Autowired
		private LoginAttemptService loginAttemptService;

		@Autowired
		private UserRepository userRepository;

		@Autowired
		private LoginLogRepository loginLogRepository;

		@Autowired
		private IpService ipService;

		@SneakyThrows
		@Override
		public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
			try {
				if ("POST".equalsIgnoreCase(request.getMethod())) {
					String user = request.getParameter("username");
					User loadUser = userRepository.findFirstByUsernameIgnoreCase(user).orElse(null);
					if (loadUser != null) {
						LoginLog loginLog = new LoginLog();
						loginLog.setIp(ipService.getClientIP());
						loginLog.setOperation("login");
						loginLog.setResult("FAILURE");
						loginLog.setUser(loadUser);
						loginLog.setDate(new Date().toString());
						loginLogRepository.save(loginLog);
					}
				}
			} catch (Exception ex) {
				log.error(ex.toString());
			}

			final String xfHeader = request.getHeader("X-Forwarded-For");
			if (xfHeader == null) {
				loginAttemptService.loginFailed(request.getRemoteAddr());
			} else {
				loginAttemptService.loginFailed(xfHeader.split(",")[0]);
			}
		}
	}

	@Component
	public static class AuthenticationSuccessEventListener implements
			ApplicationListener<AuthenticationSuccessEvent> {

		@Autowired
		private HttpServletRequest request;

		@Autowired
		private LoginAttemptService loginAttemptService;

		@Autowired
		private UserRepository userRepository;

		@Autowired
		private LoginLogRepository loginLogRepository;


		@Autowired
		private IpService ipService;

		@Override
		public void onApplicationEvent(final AuthenticationSuccessEvent e) {
			final String xfHeader = request.getHeader("X-Forwarded-For");

			try {
				if ("POST".equalsIgnoreCase(request.getMethod())) {
					String user = request.getParameter("username");
					User loadUser = userRepository.findFirstByUsernameIgnoreCase(user).orElse(null);
					if (loadUser != null) {
						LoginLog loginLog = new LoginLog();
						loginLog.setIp(ipService.getClientIP());
						loginLog.setOperation("login");
						loginLog.setResult("SUCCESS");
						loginLog.setUser(loadUser);
						loginLog.setDate(new Date().toString());
						loginLogRepository.save(loginLog);
					}
				}
			} catch (Exception ex) {
				log.error(ex.toString());
			}

			if (xfHeader == null) {
				loginAttemptService.loginSucceeded(request.getRemoteAddr());
			} else {
				loginAttemptService.loginSucceeded(xfHeader.split(",")[0]);
			}
		}
	}

}
