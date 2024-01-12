package ee.sebgros.secureapplication.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender sender;

    public void sendMail(String subject, String text, String destAdr) {
        final var email = createMail(subject, text, destAdr);
        sender.send(email);
    }

    private SimpleMailMessage createMail(String subject, String text, String destAdr) {
        final var email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(text);
        email.setTo(destAdr);
        email.setFrom("secureapp@mail.com");
        return email;
    }
}
