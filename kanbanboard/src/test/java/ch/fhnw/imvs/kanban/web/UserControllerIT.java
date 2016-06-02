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
import ch.fhnw.imvs.kanban.dto.PagingDto;
import ch.fhnw.imvs.kanban.dto.UserDetailChangeDto;
import ch.fhnw.imvs.kanban.dto.UserDto;
import ch.fhnw.imvs.kanban.dto.UserNewPasswordResetDto;
import ch.fhnw.imvs.kanban.model.Token;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.persistence.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class UserControllerIT extends SpringIntegrationTest {

    private Logger log = LoggerFactory.getLogger(UserControllerIT.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void setUp() {

    }

    @Override
    public void tearDown() {

    }


    @Test
    public void currentUserAdmin() throws Exception {

        final String token = createAdminToken();

        mockMvc.perform(get(PREFIX + "/users/me").contentType(contentType).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist())
                .andExpect(jsonPath("username", is("admin")))
                .andExpect(jsonPath("email", is("admin@example.com")));
    }

    @Test
    public void currentUserUser() throws Exception {

        final String token = createUserToken();

        mockMvc.perform(get(PREFIX + "/users/me").contentType(contentType).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist())
                .andExpect(jsonPath("username", is("user")))
                .andExpect(jsonPath("email", is("user@example.com")));
    }

    @Test
    public void isResetTokenOk() throws Exception {

        String username = "userPass";

        final String token = userRepository.findByUsername(username).getPasswordResetToken().getToken();

        mockMvc.perform(get(PREFIX + "/users/ " + username + "/resetPassword/?token="+ token).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));
    }

    @Test
    public void isResetTokenOkInvalidToken() throws Exception {
        String username = "userPass";

        final String token = "someRandomInvalidResetString";

        mockMvc.perform(get(PREFIX + "/users/ " + username + "/resetPassword/?token=" + token).contentType(contentType))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType));
    }

    @Test
    @DirtiesContext
    public void isResetTokenOkExpiredToken() throws Exception {

        String username = "expiredUser";

        User user = new User(username, "displayExpired", "expired@example.com");
        user.setPassword(encoder.encode("12345"));
        user.setAccountEnabled(true);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date expiryDate = cal.getTime();
        final Token token = new Token(expiryDate);
        user.setPasswordResetToken(token);
        userRepository.save(user);

        mockMvc.perform(get(PREFIX + "/users/ " + username + "/resetPassword/?token=" + token.getToken())
                .contentType(contentType))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType));


        User afterAction = userRepository.findByUsername(username);

        assertTrue(afterAction.isAccountEnabled());
        assertNull(afterAction.getPasswordResetToken());
        assertTrue(encoder.matches("12345", afterAction.getPassword()));
    }

    @Test
    public void changePasswordWithResetToken() throws Exception {
        String username = "userPass";

        final String token = userRepository.findByUsername(username).getPasswordResetToken().getToken();

        final String newPass = "newPass12345";

        UserNewPasswordResetDto dto = new UserNewPasswordResetDto(newPass, token);

        mockMvc.perform(post(PREFIX + "/users/ " + username + "/resetPassword/")
                .contentType(contentType).content(this.json(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));

        final User afterPwChange = userRepository.findByUsername(username);
        assertTrue(encoder.matches(newPass, afterPwChange.getPassword()));
    }

    @Test
    public void changePasswordWithResetTokenInvalidToken() throws Exception {
        String username = "userPass";

        final String token = "randomStringAsToken";

        final String newPass = "newPass12345";

        UserNewPasswordResetDto dto = new UserNewPasswordResetDto(newPass, token);

        mockMvc.perform(post(PREFIX + "/users/ " + username + "/resetPassword/")
                .contentType(contentType).content(this.json(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType));

        final User afterPwChange = userRepository.findByUsername(username);
        assertFalse(encoder.matches(newPass, afterPwChange.getPassword()));
    }

    @Test
    @DirtiesContext
    public void changePasswordWithResetTokenExpiredToken() throws Exception {

        String username = "expiredUser";

        User user = new User(username, "displayExpired", "expired@example.com");
        user.setPassword(encoder.encode("12345"));
        user.setAccountEnabled(true);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date expiryDate = cal.getTime();
        final Token token = new Token(expiryDate);
        user.setPasswordResetToken(token);
        userRepository.save(user);

        final String newPass = "newPass12345";

        UserNewPasswordResetDto dto = new UserNewPasswordResetDto(newPass, token.getToken());

        mockMvc.perform(post(PREFIX + "/users/ " + username + "/resetPassword/")
                .contentType(contentType).content(this.json(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType));


        User afterAction = userRepository.findByUsername(username);

        assertNull(afterAction.getPasswordResetToken());
        assertTrue(encoder.matches("12345", afterAction.getPassword()));
    }

    @Test
    @DirtiesContext
    public void changePassword() throws Exception {
        final String token = createUserToken();

        UserDetailChangeDto userChange = new UserDetailChangeDto("", "12345", "DE", "user");

        final User userBefore = userRepository.findByUsername("user");
        assertFalse(encoder.matches("12345", userBefore.getPassword()));
        assertEquals(userBefore.getDisplayname(), "user");

        mockMvc.perform(put(PREFIX + "/users/me").contentType(contentType).header("Cookie", token).content(this.json(userChange)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));


        User userAfter = userRepository.findByUsername("user");
        assertTrue(encoder.matches("12345", userAfter.getPassword()));
        assertEquals(userAfter.getDisplayname(), "user");
    }


    @Test
    @DirtiesContext
    public void activateUser() throws Exception {

        final String username = "userInactive";

        final User inactiveUser = userRepository.findByUsername(username);

        final String token = inactiveUser.getActivationToken().getToken();

        assertFalse(inactiveUser.isAccountEnabled());

        Map<String, String> input = new HashMap<>();
        input.put("token", token);

        log.debug("Input: {}", this.json(input));

        MvcResult result = mockMvc.perform(post(PREFIX + "/users/ " + username + "/activate/")
                .contentType(contentType).content(this.json(input)))
                .andExpect(status().isOk())
//                .andExpect(content().contentType(contentType))
                .andReturn();

        log.debug("Result: {} ", result.getResponse().getContentAsString());


        final User activatedUser = userRepository.findByUsername(username);

        assertTrue(activatedUser.isAccountEnabled());
        assertNull(activatedUser.getActivationToken());

    }

    @Test
    @DirtiesContext
    public void changeDisplayname() throws Exception {
        final String token = createUserToken();
        log.debug("Token: " + token);

        final String newName = "UserNewName";
        final String oldPassword = "user";

        UserDetailChangeDto userChange = new UserDetailChangeDto(newName, "", "DE", oldPassword);

        final User userBefore = userRepository.findByUsername("user");
        assertTrue(encoder.matches("user", userBefore.getPassword()));
        assertEquals(userBefore.getDisplayname(), "user");

        MvcResult result = mockMvc.perform(put(PREFIX + "/users/me").contentType(contentType).header("Cookie", token).content(this.json(userChange)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug(result.getResponse().getContentAsString());

        User userAfter = userRepository.findByUsername("user");
        assertTrue(encoder.matches(oldPassword, userAfter.getPassword()));
        assertEquals(newName, userAfter.getDisplayname());
    }

    @Test
    @DirtiesContext
    public void changeDisplaynameWrongOldPassword() throws Exception {
        final String token = createUserToken();
        log.debug("Token: " + token);

        final String newName = "UserNewName";
        final String newPassword = "12345";

        UserDetailChangeDto userChange = new UserDetailChangeDto(newName, newPassword, "DE", "blabla");

        final User userBefore = userRepository.findByUsername("user");
        assertTrue(encoder.matches("user", userBefore.getPassword()));
        assertEquals(userBefore.getDisplayname(), "user");

        MvcResult result = mockMvc.perform(put(PREFIX + "/users/me").contentType(contentType).header("Cookie", token).content(this.json(userChange)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", result.getResponse().getContentAsString());

        User userAfter = userRepository.findByUsername("user");
        assertTrue(encoder.matches("user", userAfter.getPassword()));
        assertEquals("user", userAfter.getDisplayname());
    }


    @Test
    @DirtiesContext
    public void changeDisplaynameAndPassword() throws Exception {
        final String token = createUserToken();

        final String newName = "UserNewName";
        final String newPassword = "12345";

        UserDetailChangeDto userChange = new UserDetailChangeDto(newName, newPassword, "DE", "user");

        final User userBefore = userRepository.findByUsername("user");
        assertTrue(encoder.matches("user", userBefore.getPassword()));
        assertEquals(userBefore.getDisplayname(), "user");

        mockMvc.perform(put(PREFIX + "/users/me").contentType(contentType).header("Cookie", token).content(this.json(userChange)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));


        User userAfter = userRepository.findByUsername("user");
        assertTrue(encoder.matches("12345", userAfter.getPassword()));
        assertEquals(userAfter.getDisplayname(), "UserNewName");

    }

    @Test
    @DirtiesContext
    public void changeLanguage() throws Exception {
        final String token = createUserToken();
        log.debug("Token: " + token);

        final String newName = "";
        final String oldPassword = "user";
        final String newLanguage = "EN";

        UserDetailChangeDto userChange = new UserDetailChangeDto(newName, "", newLanguage, oldPassword);

        final User userBefore = userRepository.findByUsername("user");
        assertTrue(encoder.matches("user", userBefore.getPassword()));
        assertEquals(userBefore.getDisplayname(), "user");

        MvcResult result = mockMvc.perform(put(PREFIX + "/users/me").contentType(contentType).header("Cookie", token).content(this.json(userChange)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug(result.getResponse().getContentAsString());

        User userAfter = userRepository.findByUsername("user");
        assertTrue(encoder.matches(oldPassword, userAfter.getPassword()));
        assertEquals("user", userAfter.getDisplayname());
        assertEquals(newLanguage, userAfter.getLanguage());
    }

    @Test
    public void getBoardsForUser() throws Exception {
        final String token = createUserToken();


        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/users/user/boards").contentType(contentType).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        log.debug("Boards for user: " + content);

    }

    @Test
    @DirtiesContext
    public void deleteUser() throws Exception {
        final String username = "user2";
        final String token = createToken(username, "password2");

        assertNotNull(userRepository.findByUsername(username));

        final MvcResult mvcResult = mockMvc.perform(delete(PREFIX + "/users/" + username)
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isNoContent())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        log.debug("Result: " + content);

        assertNull(userRepository.findByUsername(username));
    }

    @Test
    @DirtiesContext
    public void deleteUserUnauthorized() throws Exception {
        final String username = "user2";
        final String token = createToken("user", "user");

        assertNotNull(userRepository.findByUsername(username));

        final MvcResult mvcResult = mockMvc.perform(delete(PREFIX + "/users/" + username)
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").exists())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        log.debug("Result: " + content);

        assertNotNull(userRepository.findByUsername(username));
    }

    @Test
    @DirtiesContext
    public void deleteUserInvalidUser() throws Exception {
        final String username = "dasuser2";
        final String token = createAdminToken();

        assertNull(userRepository.findByUsername(username));

        final MvcResult mvcResult = mockMvc.perform(delete(PREFIX + "/users/" + username)
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").exists())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        log.debug("Result: " + content);

        assertNull(userRepository.findByUsername(username));
    }


    @Test
    public void findBySearchterm() throws Exception {
        final String token = createUserToken();

        String search = "demo";

        List<User> users = userRepository.findByUsernameLikeOrEmailLikeOrDisplaynameLikeAllIgnoreCase(search, search, search);


        log.debug("Num users: " + users.size());

        for (User u : users) {
            log.debug(u.toString());
        }

        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/users/findusers/" + search)
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

        ObjectMapper mapper = new ObjectMapper();
        final List<UserDto> userDtos = mapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<UserDto>>(){});

        assertEquals(userDtos.size(), users.size());
    }

    @Test
    public void findBySearchtermNotInDb() throws Exception {
        final String token = createUserToken();

        String search = "demoIsNotinDbnfs1234542";

        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/users/findusers/" + search)
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

        ObjectMapper mapper = new ObjectMapper();
        final List<UserDto> userDtos = mapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<UserDto>>(){});

        assertEquals(0, userDtos.size());
    }

    @Test
    @DirtiesContext
    public void getTasksForUser() throws Exception {
        final String username = "user";

        final String token = createUserToken();

        final User user = userRepository.findByUsername(username);

        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/users/" + user.getUsername() + "/cards")
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.pagesize").exists())
                .andExpect(jsonPath("$.pagecount").exists())
                .andExpect(jsonPath("$.content").exists())
                .andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DirtiesContext
    public void getTasksForUserPaging() throws Exception {

        int pagesize = 5;
        int page = 0;

        final String username = "user";

        final String token = createUserToken();

        final User user = userRepository.findByUsername(username);


        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/users/" + user.getUsername() + "/cards")
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.pagesize").exists())
                .andExpect(jsonPath("$.pagecount").exists())
                .andExpect(jsonPath("$.content").exists())
                .andReturn();

        log.debug("Result Full: {}", mvcResult.getResponse().getContentAsString());

        ObjectMapper mapper = new ObjectMapper();
        PagingDto dto = mapper.readValue(mvcResult.getResponse().getContentAsString(), PagingDto.class);

        int numTasks = dto.getContent().size();
        log.debug("numtasks: {}.", numTasks);

        final MvcResult mvcResultPage0 = mockMvc.perform(get(PREFIX + "/users/" + user.getUsername() + "/cards?page=" + page + "&pagesize=" + pagesize)
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.pagesize").value(pagesize))
                .andExpect(jsonPath("$.pagecount").exists())
                .andExpect(jsonPath("$.content").exists())
                .andReturn();

        log.debug("Result Page 0: {}", mvcResultPage0.getResponse().getContentAsString());

        PagingDto resultPage0 = mapper.readValue(mvcResultPage0.getResponse().getContentAsString(), new TypeReference<PagingDto>(){});

        assertTrue(resultPage0.getContent().size() <= pagesize);
        assertTrue(resultPage0.getContent().size() == pagesize || resultPage0.getContent().size() == numTasks);


    }

    @Test
    @DirtiesContext
    public void getTasksForUserUnauthorizedRequester() throws Exception {

        final String username = "user2";

        final String token = createUserToken();

        final User user = userRepository.findByUsername(username);


        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/users/" + user.getId() + "/cards")
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.result").exists())
                .andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    @DirtiesContext
    public void getTasksForUserUnknownUser() throws Exception {

        String userId = "invalidId";


        final String token = createAdminToken();


        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/users/" + userId + "/cards")
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.result").exists())
                .andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

    }

    @Test
    @DirtiesContext
    public void getTasksForUserTaskSummary() throws Exception {

        int pagesize = 5;
        int page = 0;

        final String username = "user";

        final String token = createUserToken();

        final User user = userRepository.findByUsername(username);


        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/users/" + user.getUsername() + "/cards")
                .contentType(contentType)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.tasks").exists())
                .andExpect(jsonPath("$[?(@.tasks[0] == 2)]").exists())
                .andExpect(jsonPath("$[?(@.tasks[1] == 0)]").exists())
                .andExpect(jsonPath("$[?(@.tasks[2] == 0)]").exists())
                .andReturn();

        log.debug("Result Full: {}", mvcResult.getResponse().getContentAsString());
    }

}
