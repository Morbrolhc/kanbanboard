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

import ch.fhnw.imvs.kanban.dto.*;
import ch.fhnw.imvs.kanban.exception.InvalidTokenException;
import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.model.UserRole;
import ch.fhnw.imvs.kanban.security.TokenHandler;
import ch.fhnw.imvs.kanban.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "${api.prefix}/users",
        produces = "application/json")
public class UserController {

    private static Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private TokenAuthenticationService authenticationService;
    @Autowired
    private UserService userService;
    @Autowired
    private EMailService eMailService;
    @Autowired
    private BoardService boardService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TokenHandler tokenHandler;
    @Autowired
    private TokenAuthenticationService tokenService;

    public UserController() {
    }

    @RequestMapping(value = "/findusers/{username}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> findUsersLike(@PathVariable("username") String username) throws ServletException {

        final List<User> usersLike = userService.findUsersLike(username);
        final ArrayList<UserDto> collect = usersLike.stream().map(UserDto::new).collect(Collectors.toCollection(ArrayList::new));

        return new ResponseEntity<>(collect, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody @Valid final UserCreateDto newUser) throws ServletException {

        try {
            User user = userService.registerNewUserAccount(newUser);
            return new ResponseEntity<>(new UserDto(user), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new ResultDto("Username/email does already exist"), HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    @ResponseBody
    public UserDto currentUser(HttpServletRequest request) {

        final Authentication authentication = authenticationService.getAuthentication(request);

        if (authentication == null) {
            return new UserDto("unknown", "unknown", "unknown");
        } else {
            return new UserDto((User) authentication.getDetails());
        }
    }

    @RequestMapping(value = "/me", method = RequestMethod.PUT,
            consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> changeUserDetails(HttpServletRequest request,
                                               @RequestBody final UserDetailChangeDto dto) {

        final Authentication authentication = authenticationService.getAuthentication(request);

        if (authentication == null) {
            throw new AccessDeniedException("Invalid token");
        }
        User user = (User) authentication.getDetails();

        userService.changeDetails(user, dto);
        final String token = tokenHandler.createTokenForUser(user);

        HttpHeaders headers = new HttpHeaders();
        // INFO token returned as JSON and not cookie bacause browser is too slow in setting cookie by itself.
        //headers.add("Set-Cookie", "token=" + token);
        return new ResponseEntity<>(new LoginResponseDto(token), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/resetPassword/", method = RequestMethod.POST,
            consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> createResetPasswordToken(@RequestBody UserPasswordResetRequestDto dto) {

        if (dto.getText() == null) {
            return new ResponseEntity<>(new ResultDto("no mail/username provided"), HttpStatus.BAD_REQUEST);
        }
        try {
            User user = userService.createPasswordResetTokenForUser(dto.getText());
            eMailService.sendPasswordReset(user);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (InvalidTokenException e) {
            // This should never happen, we just created the token
            log.error("Token created but not accessible", e.getCause());
            throw new InternalError(e.getCause());
        }

        // always send OK to hide registered accounts
    }

    @RequestMapping(value = "/{username}/resetPassword/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> verifyPasswordResetToken(@PathVariable("username") String username,
                                                      @RequestParam("token") String token) {

        try {
            userService.verifyPasswordResetToken(username, token);
        } catch (InvalidTokenException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/{username}/resetPassword/", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@PathVariable("username") String username,
                                           @RequestBody UserNewPasswordResetDto userPwDto) {

        try {
            userService.changePasswordWithToken(username, userPwDto.getToken(), userPwDto.getPassword());
            return new ResponseEntity<>(new ResultDto("Ok"), HttpStatus.OK);

        } catch (InvalidTokenException e) {
            return new ResponseEntity<>(new ResultDto("Unauthorized"), HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/{username}/activate/", method = RequestMethod.POST,
            consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> activateAccount(@PathVariable("username") String username,
                                             @RequestBody Map<String, String> content) {


        try {
            log.debug("Activating account for {}", username);
            userService.activateAccount(username, content.get("token"));
            return new ResponseEntity<>(new ResultDto("Account activated"), HttpStatus.OK);
        } catch (UsernameNotFoundException | InvalidTokenException e) {
            return new ResponseEntity<>(new ResultDto("Unknown username or invalid token"), HttpStatus.UNAUTHORIZED);
        }

    }

    @RequestMapping(value = "/{username}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable("username") String username, HttpServletRequest request) {
        final Authentication authentication = tokenService.getAuthentication(request);
        if (authentication == null) {
            throw new AccessDeniedException("Invalid token");
        }
        final User user = (User) authentication.getDetails();


        if (!user.hasRole(UserRole.ADMIN) && !user.getUsername().equals(username)) {
            return new ResponseEntity<>(new ResultDto("Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        try {
            userService.deleteUser(username);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResultDto("Unauthorized"), HttpStatus.UNAUTHORIZED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new ResultDto(e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}/boards")
    @ResponseBody
    public ResponseEntity<?> getBoardsForUser(@PathVariable("id") String username) {

        List<BoardDto> boards = boardService.getBoardsForUser(username);
        return new ResponseEntity<>(boards, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}/cards", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getTasksForUser(@PathVariable("id") String userId,
                                             @RequestParam(value = "page", required = false) Integer page,
                                             @RequestParam(value = "pagesize", required = false) Integer pageSize,
                                             @RequestParam(value = "search", required = false) String search) {

        if (pageSize == null || pageSize <= 0) {
            log.debug("Defaulting to pagesize 20.");
            pageSize = 20;
        }

        if (page == null || page < 0) {
            log.debug("Defaulting to page 0.");
            page = 0;
        }

        if (search == null) {
            log.debug("no search term, filtering disabled");
            search = "";
        }

        final String searchTerm = search;

        try {
            List<Task> tasks = taskService.getTasksForUser(userId);
            List<TaskDto> taskDtos = tasks.stream()
                    .filter(task -> task.getTitle().contains(searchTerm) || task.getDescription().contains(searchTerm))
                    .skip(page * pageSize).limit(pageSize)
                    .map(TaskDto::new)
                    .collect(Collectors.toList());
            PagingDto dto = new PagingDto(page, pageSize, 1 + taskDtos.size() / pageSize, taskDtos);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            log.warn("User {} not found.", userId);
            return new ResponseEntity<>(new ResultDto(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}
