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

package ch.fhnw.imvs.kanban.web;

import ch.fhnw.imvs.kanban.SpringIntegrationTest;
import ch.fhnw.imvs.kanban.dto.UserCreateDto;
import ch.fhnw.imvs.kanban.dto.UserPasswordResetRequestDto;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.persistence.UserRepository;
import ch.fhnw.imvs.kanban.service.PeriodicService;
import ch.fhnw.imvs.kanban.service.TaskService;
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * This class is for all tests who send mail.
 */
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class MailIT extends SpringIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(MailIT.class);

    private GreenMail mailServer;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private PeriodicService periodicService;
    @Autowired
    private TaskService taskService;

    @Value("${mail.sender}")
    private String mailSender;

    @Override
    public void setUp() {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory.class.getName());
        mailServer = new GreenMail(ServerSetupTest.SMTPS);
        mailServer.start();
    }

    @Override
    public void tearDown() {
        mailServer.stop();
    }

    @Test
    @DirtiesContext
    public void registerNewUser() throws Exception {
        String testname = "testName";
        String testDisplay = "testDisplay";
        String testmail = "demouser@example.com";
        String password = "12345";
        String language = "DE";

        UserCreateDto user = new UserCreateDto(testname, testDisplay, testmail, password, language);

        mockMvc.perform(post(PREFIX + "/users").contentType(contentType).content(this.json(user)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());

        User fromSave = userRepository.findByUsername(testname);


        assertNotNull(fromSave);

        assertFalse(fromSave.isAccountEnabled());
        assertTrue(fromSave.isAccountNonLocked());
        assertTrue(fromSave.isCredentialsNonExpired());

        assertNotNull(fromSave.getActivationToken());
        assertNull(fromSave.getPasswordResetToken());

        assertEquals(testDisplay, fromSave.getDisplayname());
        assertEquals(testname, fromSave.getUsername());
        assertEquals(testmail, fromSave.getEmail());

        assertTrue(encoder.matches(password, fromSave.getPassword()));
        assertEquals(language, fromSave.getLanguage());


        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(1, messages.length);
        MimeMessage m = messages[0];

        assertEquals(mailSender, m.getFrom()[0].toString());
        assertEquals(InternetAddress.parse(testmail, false)[0], m.getAllRecipients()[0]);

        log.debug("Message: {}" , m.getContent().toString());
    }

    @Test
    @DirtiesContext
    public void registerNewUserUsernameTaken() throws Exception {
        String testname = "user";
        String testDisplay = "testDisplay";
        String testmail = "demouser@example.com";
        String password = "12345";
        String language = "DE";

        UserCreateDto user = new UserCreateDto(testname, testDisplay, testmail, password, language);

        mockMvc.perform(post(PREFIX + "/users").contentType(contentType).content(this.json(user)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());

        User fromSave = userRepository.findByUsername(testname);

        assertNotNull(fromSave);

        assertNotEquals(testDisplay, fromSave.getDisplayname());
        assertEquals(testname, fromSave.getUsername());
        assertNotEquals(testmail, fromSave.getEmail());

        assertFalse(encoder.matches(password, fromSave.getPassword()));

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @DirtiesContext
    public void registerNewUserUsernameWithSpaces() throws Exception {
        String testname = "us er";
        String testDisplay = "test Display";
        String testmail = "demoaauser@example.com";
        String password = "12345";
        String language = "DE";

        UserCreateDto user = new UserCreateDto(testname, testDisplay, testmail, password, language);

        mockMvc.perform(post(PREFIX + "/users").contentType(contentType).content(this.json(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());

        User fromSave = userRepository.findByUsername(testname);

        assertNull(fromSave);

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @DirtiesContext
    public void registerNewUserMailTaken() throws Exception {
        String testname = "testName";
        String testDisplay = "testDisplay";
        String testmail = "user@example.com";
        String password = "12345";
        String language = "DE";

        UserCreateDto user = new UserCreateDto(testname, testDisplay, testmail, password, language);

        mockMvc.perform(post(PREFIX + "/users").contentType(contentType).content(this.json(user)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());

        User fromSave = userRepository.findByUsername(testname);
        assertNull(fromSave);

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @DirtiesContext
    public void registerNewUserShortUsername() throws Exception {
        String testname = "aa";
        String testDisplay = "testDisplay";
        String testmail = "user@example.com";
        String password = "12345";
        String language = "DE";

        UserCreateDto user = new UserCreateDto(testname, testDisplay, testmail, password, language);

        mockMvc.perform(post(PREFIX + "/users").contentType(contentType).content(this.json(user)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());

        User fromSave = userRepository.findByUsername(testname);
        assertNull(fromSave);

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @DirtiesContext
    public void registerNewUserInvalidMail() throws Exception {
        String testname = "aadasasdf";
        String testDisplay = "testDisplay";
        String testmail = "useratexample.com";
        String password = "12345";
        String language = "DE";

        UserCreateDto user = new UserCreateDto(testname, testDisplay, testmail, password, language);

        mockMvc.perform(post(PREFIX + "/users").contentType(contentType).content(this.json(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());

        User fromSave = userRepository.findByUsername(testname);
        assertNull(fromSave);

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @DirtiesContext
    public void registerNewUserMailTakenCaseSensitive() throws Exception {
        String testname = "user";
        String testDisplay = "testDisplay";
        String testmail = "USEr@example.com";
        String password = "12345";
        String language = "DE";

        UserCreateDto user = new UserCreateDto(testname, testDisplay, testmail, password, language);

        mockMvc.perform(post(PREFIX + "/users").contentType(contentType).content(this.json(user)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());

        User fromSave = userRepository.findByUsername(testname);


        assertNotNull(fromSave);

        assertNotEquals(testDisplay, fromSave.getDisplayname());
        assertEquals(testname, fromSave.getUsername());
        assertNotEquals(testmail, fromSave.getEmail());

        assertFalse(encoder.matches(password, fromSave.getPassword()));


        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @DirtiesContext
    public void sendPasswordResetByUsername() throws Exception {

        String username = "user";

        User user = userRepository.findByUsername(username);

        assertNull(user.getPasswordResetToken());


        UserPasswordResetRequestDto dto = new UserPasswordResetRequestDto(username);


        mockMvc.perform(post(PREFIX + "/users/resetPassword/")
                .contentType(contentType).content(this.json(dto)))
                .andExpect(status().isNoContent());


        User userWithToken = userRepository.findByUsername(username);
        assertNotNull(userWithToken.getPasswordResetToken());

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(1, messages.length);
        MimeMessage m = messages[0];

        assertTrue(String.valueOf(m.getContent()).contains(userWithToken.getPasswordResetToken().getToken()));

        log.debug("Message: {}" , m.getContent().toString());
    }

    @Test
    @DirtiesContext
    public void sendPasswordResetByMail() throws Exception {

        String username = "user";

        User user = userRepository.findByUsername(username);

        assertNull(user.getPasswordResetToken());


        UserPasswordResetRequestDto dto = new UserPasswordResetRequestDto(user.getEmail());


        mockMvc.perform(post(PREFIX + "/users/resetPassword/")
                .contentType(contentType).content(this.json(dto)))
                .andExpect(status().isNoContent());


        User userWithToken = userRepository.findByUsername(username);
        assertNotNull(userWithToken.getPasswordResetToken());

        MimeMessage[] messages = mailServer.getReceivedMessages();
        assertNotNull(messages);
        assertEquals(1, messages.length);
        MimeMessage m = messages[0];

        assertTrue(String.valueOf(m.getContent()).contains(userWithToken.getPasswordResetToken().getToken()));
    }

    @Test
    public void remindFinishingToday() throws Exception {

        periodicService.remindFinishingToday();

        final int size = taskService.getEndingToday().size();

        MimeMessage[] messages = mailServer.getReceivedMessages();

        MimeMessage m = messages[0];
        log.debug("Message: {}" , m.getContent().toString());
    }


}
