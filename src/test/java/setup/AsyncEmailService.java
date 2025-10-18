package setup;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailService {
    private final EmailSession emailSession;
    
    @Async
    public void sendEmail(Email email) {
        sendEmail(email, 0);
    }

    @Async
    public void sendEmail(Email email, int delayInMillis) {
        final Session session = emailSession.create();
        try {
            Thread.sleep(delayInMillis);
            final MimeMessage message = createMessage(email, session);
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException("Authentication error occurred:", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted:", e);
        } catch (MessagingException e) {
            throw new RuntimeException("Messaging error occurred:", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding error occurred:", e);
        }
    }

    private MimeMessage createMessage(Email email, Session session) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(email.from().address(), email.from().personal());
        helper.setSubject(email.subject());
        helper.setTo(email.to().address());
        helper.setText(email.body(), true);
        return message;
    }
}
