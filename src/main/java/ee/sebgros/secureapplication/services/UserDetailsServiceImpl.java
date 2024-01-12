package ee.sebgros.secureapplication.services;

import ee.sebgros.secureapplication.repository.entity.User;
import ee.sebgros.secureapplication.repository.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LoginAttemptService loginAttemptService;

	private final IpService ipService;

	@SneakyThrows
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {

		String ip = ipService.getClientIP();
		if (loginAttemptService.isBlocked(ip)) {
			log.error("USER IS BLOCKED");
			throw new UsernameNotFoundException("Could not find user");
		}

		User user = userRepository.findFirstByUsernameIgnoreCase(username).orElse(null);
		Thread.sleep(3000);
		if (user == null) {
			throw new UsernameNotFoundException("Could not find user");
		}

		return user;
	}


}
