package ee.sebgros.secureapplication.services;
import ee.sebgros.secureapplication.repository.entity.User;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class QRService {
    public static String QR_PREFIX =
            "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=";
    public static String APP_NAME = "SecureNotesApp";

    public String generateQRUrl(User user) {
        return QR_PREFIX + URLEncoder.encode(String.format(
                        "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                        APP_NAME, user.getEmail(), user.getTotpSecret(), APP_NAME),
                StandardCharsets.UTF_8);
    }
}
