/*
 * MIT License
 *
 * Copyright (c) 2016 Maurice Gschwind
 * Copyright (c) 2016 Samuel Merki
 * Copyright (c) 2016 Joel Wasmer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ch.fhnw.imvs.kanban.service.impl;

import ch.fhnw.imvs.kanban.exception.InvalidTokenException;
import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.service.EMailService;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.util.Assert;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.util.*;

@Service
public class EMailServiceImpl extends AuthenticatedService implements EMailService {

    private static Logger log = LoggerFactory.getLogger(EMailService.class);

    @Value("${mail.sender}")
    private String mailFromAddress;

    @Value("${kanbanboard.hostname}")
    private String hostname;

    private JavaMailSender mailSender;

    @Autowired
    private VelocityEngine velocityEngine;
    @Autowired
    MessageSource messageSource;


    @Autowired
    public EMailServiceImpl(JavaMailSender mailSender) {
        Assert.notNull(mailSender);
        this.mailSender = mailSender;
    }

    @Override
    public void sendActivation(User user) throws InvalidTokenException {

        if (user.getActivationToken() == null) {
            throw new InvalidTokenException("No activation token found for " + user.toString());
        }

        Map<String, Object> model = getBaseModel(user);
        model.put("url", hostname + "#/activation/" + user.getUsername() + "/" + user.getActivationToken().getToken());

        final String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "templates/sendActivation.vm", "UTF-8", model);

        MimeMessagePreparator preparator = mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setFrom(new InternetAddress(mailFromAddress));
            mimeMessage.setSubject("Kanbanboard WGM Accountaktivierung");
            mimeMessage.setText(body, "UTF-8", "html");
        };

        try {
            mailSender.send(preparator);
            log.info("Activation Mail sent to "+ user.getEmail());
        } catch (MailException e) {
            log.error("Could not send activation mail to " + user.getEmail() + ". The error was :", e);
        }
    }

    @Override
    public void sendPasswordReset(User user) throws InvalidTokenException {

        if (user.getPasswordResetToken() == null) {
            throw new InvalidTokenException("No password reset token found for " + user.toString());
        }

        final Map<String, Object> model = getBaseModel(user);
        model.put("url", hostname + "#/reset/" + user.getUsername() + "/" + user.getPasswordResetToken().getToken());

        final String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "templates/sendPasswordReset.vm", "UTF-8", model);

        MimeMessagePreparator preparator = mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setFrom(new InternetAddress(mailFromAddress));
            mimeMessage.setSubject("Kanbanboard WGM Passwort Reset");
            mimeMessage.setText(body, "UTF-8", "html");
        };

        try {
            mailSender.send(preparator);
            log.info("Reset Mail sent to {}", user.getEmail());
        } catch (MailException e) {
            log.error("Could not send mail to " + user.getEmail() + ". The error was :", e);
        }
    }

    @Override
    public void sendReminder(List<Task> tasks) {

        Map<User, List<Task>> taskMap = new HashMap<>();

        for (Task t : tasks) {
            for (User u : t.getAssigned()) {
                if (taskMap.containsKey(u)) {
                    taskMap.get(u).add(t);
                } else {
                    taskMap.put(u, new ArrayList<>());
                    taskMap.get(u).add(t);
                }
            }
        }

        taskMap.forEach(this::remindUser);
    }

    private void remindUser(User user, List<Task> tasks) {



        MimeMessagePreparator preparator = mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            mimeMessage.setFrom(new InternetAddress(mailFromAddress));

            StringBuilder sb = new StringBuilder();
            sb.append("<ul>");
            for (Task task : tasks) {
                sb.append("<li>").append(task.getCategory()).append(": ").append(task.getTitle()).append("</li>");
            }
            sb.append("</ul>");
            final String tasktext = sb.toString();

            final Map<String, Object> model = getBaseModel(user);
            model.put("tasktext", tasktext);
            final String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "templates/sendReminder.vm", "UTF-8", model);

            mimeMessage.setSubject("Kanbanboard WGM Task Reminder");
            mimeMessage.setText(body, "UTF-8", "html");
        };

        try {
            mailSender.send(preparator);
            log.info("Reminder Mail sent to {} containing {} tasks.", user.getEmail(), tasks.size());
        } catch (MailException e) {
            log.error("Could not send mail to {}. The error was :", user.getEmail(), e);
        }
    }

    private Map<String, Object> getBaseModel(User user) {
        Map<String, Object> model = new HashMap<>();
        model.put("messages", messageSource);
        model.put("locale", new Locale(user.getLanguage()));
        model.put("user", user);
        return model;
    }
}
