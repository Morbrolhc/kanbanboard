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

import ch.fhnw.imvs.kanban.dto.LoginResponseDto;
import ch.fhnw.imvs.kanban.dto.UserLoginDto;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.security.TokenHandler;
import ch.fhnw.imvs.kanban.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "${api.prefix}",
        produces = "application/json")
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private TokenHandler tokenHandler;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginController() {

    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody @Valid final UserLoginDto login) throws ServletException {
        if (login == null || login.getUsername() == null) {
            log.info("Invalid login");
            throw new AccessDeniedException("Invalid login");
        }

        User user = userService.loadUserByUsername(login.getUsername());
        if (passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            final String token = tokenHandler.createTokenForUser(user);

            HttpHeaders headers = new HttpHeaders();
            // INFO token returned as JSON and not cookie bacause browser is too slow in setting cookie by itself.
            //headers.add("Set-Cookie", "token=" + token);
            return new ResponseEntity<>(new LoginResponseDto(token), headers, HttpStatus.OK);

        } else {
            throw new AccessDeniedException("Invalid login");
        }

    }

    /**
     * Dummy answer for the logout since the token is not stored on the server.
     *
     * @return always returns {@link HttpStatus#OK}
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> logout() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.info("Authentication is null, user not logged in.");
            throw new AccessDeniedException("Not logged in");
        }

        log.debug("Requesting user is {}.", authentication.toString());
        try {
            User user =  (User) authentication.getDetails();
            log.info("{} logged out.", user.getUsername());
        } catch (ClassCastException e) {
            log.warn("User not logged in: {}", authentication.toString());
            throw new AccessDeniedException("Not logged in");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
