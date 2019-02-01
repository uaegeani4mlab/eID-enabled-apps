/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teem.loginapp.serviceImpl;

import teem.loginapp.model.dao.EmailContentsRepository;
import teem.loginapp.model.dmo.EmailContentsMngDMO;
import teem.loginapp.pojo.SwellrtEvent;
import teem.loginapp.service.MailService;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikos
 */
@Service
public class MailServiceImpl implements MailService {

//    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private EmailContentsRepository emailRepo;

    @Autowired
    private MailContentBuilder mailContentBuilder;

    @Value("${mail.password}")
    private String mailPass;

    private final static String MAIL_HOST = "smtp.aegean.gr";
    private final static String MAIL_FRIENDLY_NAME = "UAegean Online Communities";
    private final static String MAIL_SERVER_FROM = "@aegean.gr";

    private static Logger log = LoggerFactory.getLogger(MailService.class);

    private final String FROM = "onlinecommunities@aegean.gr";

    @Inject
    public MailServiceImpl(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
        this.mailSender.setHost(MAIL_HOST);
        this.mailSender.setPort(587);
        this.mailSender.setUsername("onlinecommunities@aegean.gr");
        this.mailSender.setPassword(mailPass);
//        this.mailSender.setProtocol("smtp");
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        this.mailSender.setJavaMailProperties(props);
    }

    @Override
    public String prepareAndSend(String recipient, String subject, String userName, String displayName, String password) {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        EmailContentsMngDMO emailContents = emailRepo.findBySubject(subject);
//        StringBuilder from = new StringBuilder();
//        from.append(emailContents.getUser())
//                .append(MAIL_SERVER_FROM);

        try {
            helper.setTo(recipient);
//            helper.setBcc(bcc);
            helper.setFrom(new InternetAddress(FROM, MAIL_FRIENDLY_NAME));
            helper.setSubject(emailContents.getSubject());

            String content = mailContentBuilder.build(userName, displayName, password);

            helper.setText(content, true);

            mailSender.send(message);

            return "OK";
        } catch (Exception e) {
            log.error("Error sending mail", e.getMessage());
            log.error(e.getMessage());
            return "ERROR";
        }
    }

    @Override
    public String sendEventMail(String recipient, SwellrtEvent evt) {
//        mailSender.setHost(MAIL_HOST);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

//        StringBuilder from = new StringBuilder();
//        from.append("teem").append(MAIL_SERVER_FROM);
        try {
            helper.setTo(recipient);
            helper.setFrom(new InternetAddress(FROM, MAIL_FRIENDLY_NAME));
            helper.setSubject(evt.getData().getTitle() + "-" + evt.getData().getSummaryText());
            String content = mailContentBuilder.buildEventContent(evt);
            helper.setText(content, true);
            mailSender.send(message);
            return "OK";
        } catch (Exception e) {
            log.error("Error sending mail", e.getMessage());
            return "ERROR";
        }
    }

    @Override
    public String sendEmailsForEvent(SwellrtEvent event, List<String> participants) {
//        mailSender.setHost(MAIL_HOST);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
//        StringBuilder from = new StringBuilder();
//        from.append("teem").append(MAIL_SERVER_FROM);
        String[] recipientsArray = participants.stream().toArray(String[]::new);
        try {
            helper.setBcc(recipientsArray);
            helper.setFrom(new InternetAddress(FROM, MAIL_FRIENDLY_NAME));
            helper.setSubject(event.getData().getTitle() + "-" + event.getData().getSummaryText());
            String content = mailContentBuilder.buildEventContent(event);
            helper.setText(content, true);
            mailSender.send(message);
            return "OK";
        } catch (Exception e) {
            log.error("Error sending mail");
            log.error(e.getMessage());
            return "ERROR";
        }

    }

}
