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
import ch.fhnw.imvs.kanban.dto.UserLoginDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LoginControllerIT extends SpringIntegrationTest {

    private Logger log = LoggerFactory.getLogger(LoginControllerIT.class);

    @Override
    public void setUp() {

    }

    @Override
    public void tearDown() {

    }


    @Test
    public void loginSuccessfullAdmin() throws Exception {

        final UserLoginDto user = new UserLoginDto("admin", "admin");

        final MvcResult result = mockMvc.perform(post(PREFIX + "/login")
                .contentType(contentType).content(this.json(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").exists())
                .andReturn();

        final String body = result.getResponse().getContentAsString();

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree(body);
        final String token = node.get("token").asText();

        log.debug("Token: " + token);

        final Jws<Claims> claims = Jwts.parser()
                .setSigningKey(TEST_KEY)
                .parseClaimsJws(token);

        assertEquals("admin", claims.getBody().getSubject());
    }

    @Test
    public void loginSuccessfullUser() throws Exception {

        final UserLoginDto user = new UserLoginDto("user", "user");

        final MvcResult result = mockMvc.perform(post(PREFIX + "/login")
                .contentType(contentType).content(this.json(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").exists())
                .andReturn();

        final String body = result.getResponse().getContentAsString();

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree(body);
        final String token = node.get("token").asText();

        log.debug("Token: " + token);

        final Jws<Claims> claims = Jwts.parser()
                .setSigningKey(TEST_KEY)
                .parseClaimsJws(token);

        assertEquals("user", claims.getBody().getSubject());
    }

    @Test
    public void loginWrongPassword() throws Exception {

        final UserLoginDto user = new UserLoginDto("admin", "admina");

        mockMvc.perform(post(PREFIX + "/login").contentType(contentType).content(this.json(user)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());
    }

    @Test
    public void loginUnknownUsername() throws Exception {
        final UserLoginDto user = new UserLoginDto("admina", "admina");

        mockMvc.perform(post(PREFIX + "/login").contentType(contentType).content(this.json(user)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());
    }

    @Test
    public void loginAccountDisabled() throws Exception {
        final UserLoginDto user = new UserLoginDto("userInactive", "inactivePass");

        mockMvc.perform(post(PREFIX + "/login").contentType(contentType).content(this.json(user)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("token").doesNotExist());
    }

    @Test
    public void logout() throws Exception {
        String token = createUserToken();

        mockMvc.perform(post(PREFIX + "/logout").contentType(contentType).header("Cookie", token))
                .andExpect(status().isOk());
    }

    @Test
    public void logoutNotLoggedIn() throws Exception {
        mockMvc.perform(post(PREFIX + "/logout").contentType(contentType))
                .andExpect(status().isUnauthorized());
    }
}
