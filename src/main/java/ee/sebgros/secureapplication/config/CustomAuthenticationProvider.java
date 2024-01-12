package ee.sebgros.secureapplication.config;

import ee.sebgros.secureapplication.repository.entity.User;
import ee.sebgros.secureapplication.repository.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;


@RequiredArgsConstructor
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication auth)
            throws AuthenticationException {
        String verificationCode
                = ((CustomWebAuthenticationDetails) auth.getDetails())
                .getVerificationCode();
        User user = userRepository.findFirstByUsernameIgnoreCase(auth.getName()).orElse(null);
        if ((user == null)) {
            throw new BadCredentialsException("Invalid username or password");
        }
        Totp totp = new Totp(user.getTotpSecret());
        if (!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
            throw new BadCredentialsException("Invalid verfication code");
        }

        Authentication result = super.authenticate(auth);
        return new UsernamePasswordAuthenticationToken(
                user, result.getCredentials(), result.getAuthorities());
    }

    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
