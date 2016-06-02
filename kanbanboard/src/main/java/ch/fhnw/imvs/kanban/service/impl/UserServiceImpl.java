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

import ch.fhnw.imvs.kanban.dto.UserCreateDto;
import ch.fhnw.imvs.kanban.dto.UserDetailChangeDto;
import ch.fhnw.imvs.kanban.exception.InvalidTokenException;
import ch.fhnw.imvs.kanban.model.*;
import ch.fhnw.imvs.kanban.persistence.BoardRepository;
import ch.fhnw.imvs.kanban.persistence.TaskRepository;
import ch.fhnw.imvs.kanban.persistence.UserRepository;
import ch.fhnw.imvs.kanban.service.EMailService;
import ch.fhnw.imvs.kanban.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl extends AuthenticatedService implements UserService {

    private final static Logger log = LoggerFactory.getLogger(UserService.class);
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EMailService eMailService;
    private final AccountStatusUserDetailsChecker detailsChecker;
    private final PasswordEncoder encoder;
    private final SecureRandom random;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, EMailService eMailService,
                           PasswordEncoder encoder, BoardRepository boardRepository,
                           TaskRepository taskRepository, SecureRandom random) {

        Assert.notNull(userRepository, "User repository null");
        Assert.notNull(eMailService, "EMail service null");
        Assert.notNull(encoder, "password encoder null");
        Assert.notNull(boardRepository, "boardRepository null");
        Assert.notNull(taskRepository, "taskRepository null");
        Assert.notNull(random, "SecureRandom null");

        this.userRepository = userRepository;
        this.eMailService = eMailService;
        this.encoder = encoder;
        this.boardRepository = boardRepository;
        this.taskRepository = taskRepository;
        this.random = random;

        detailsChecker = new AccountStatusUserDetailsChecker();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final User loadUserByUsername(String username) {
        final User user = userRepository.findByUsername(username);
        if (user == null) {
            log.info("User {} not found.", username);
            throw new UsernameNotFoundException("User with username " + username + " not found.");
        }
        detailsChecker.check(user);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final User loadUserByEmail(String email) {
        final User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User with email " + email + " not found.");
        }
        detailsChecker.check(user);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User registerNewUserAccount(UserCreateDto userDto) {

        Pattern pattern = Pattern.compile("[a-zA-Z0-9] ");
        Matcher matcher = pattern.matcher(userDto.getUsername());

        if (matcher.find()) {
            throw new IllegalArgumentException("Username must only contain letters and alphanumeric characters.");
        }

        try {
            InternetAddress emailAddr = new InternetAddress(userDto.getEmail());
            emailAddr.validate();
        } catch (AddressException e) {
            log.info("Bad mail format: {}", userDto.getEmail());
            throw new IllegalArgumentException("Bad Mail format.");
        }

        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new IllegalStateException("Mail already taken.");
        }
        if (userRepository.findByUsername(userDto.getUsername()) != null) {
            throw new IllegalStateException("Username taken.");
        }

        User user = new User(userDto.getUsername(), userDto.getDisplayname(), userDto.getEmail().toLowerCase());

        // Token validity 7 days
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 7);
        Token token = new Token(Date.from(c.toInstant()));
        log.debug("Activationtoken: {}" + token.getToken());

        user.setPassword(encoder.encode(userDto.getPassword()));
        user.grantRole(UserRole.USER);
        user.setAccountEnabled(false);
        user.setActivationToken(token);
        user.setLanguage(userDto.getLanguage());
        user.setCreationDate(new Date());

        user = userRepository.save(user);
        try {
            eMailService.sendActivation(user);
        } catch (InvalidTokenException e) {
            log.error("Token was generated but not found. This should never happen", e.getCause());
            throw new InternalError("Token was generated but not found. This should never happen", e.getCause());
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> findUsersLike(String like) {
        return userRepository.findByUsernameLikeOrEmailLikeOrDisplaynameLikeAllIgnoreCase(like, like, like);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User createPasswordResetTokenForUser(String userInput) {

        User user;

        if (userInput.contains("@")) {
            user = loadUserByEmail(userInput.toLowerCase());
        } else {
            user = loadUserByUsername(userInput);
        }

        String token = new BigInteger(130, random).toString(32);
        log.debug("Resettoken:" + token);

        // Validity one day
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);

        Token resetToken = new Token(token, c.getTime());

        user.setPasswordResetToken(resetToken);
        userRepository.save(user);
        log.debug("Created password reset token for " + user.toString());
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verifyPasswordResetToken(String username, String token) throws InvalidTokenException {

        User user = loadUserByUsername(username);

        if (user.getPasswordResetToken() == null) {
            throw new InvalidTokenException("User has no token");
        }

        if (!user.getPasswordResetToken().getToken().equals(token)) {
            throw new InvalidTokenException("Token does not match");
        }
        if (user.getPasswordResetToken().getExpiryDate().before(new Date())) {
            log.error("Token {} expired. Removing token.", token);
            user.setPasswordResetToken(null);
            userRepository.save(user);
            throw new InvalidTokenException("Token expired");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changePasswordWithToken(String username, String token, String newPassword) throws InvalidTokenException {
        if (verifyPasswordResetToken(username, token)) {
            log.debug("Changing password for user " + username);
            User user = userRepository.findByUsername(username);
            user.setPassword(encoder.encode(newPassword));
            user.setPasswordResetToken(null);
            userRepository.save(user);
            return true;
        }
        log.debug("Could not change password for user " + username + ". Unknown user or invalid token.");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(String username) {
        User user = loadUserByUsername(username);

        final List<Board> ownedBoards = boardRepository.findByOwner(user);
        if (ownedBoards.size() > 0) {
            throw new IllegalStateException("User is the owner of " + ownedBoards.size() +
                    " boards. Reduce to 0 to remove user.");
        }

        final List<Task> ownedTasks = taskRepository.findByCreator(user);
        if (ownedTasks.size() > 0) {
            // TODO handle owner change to board owner
            throw new IllegalStateException("User is the owner of " + ownedTasks.size() +
                    " tasks. Reduce to 0 to remove user.");
        }

        List<Task> memberTasks = taskRepository.findByAssigned(user);
        memberTasks.forEach(memberTask -> memberTask.remove(user));
        taskRepository.save(memberTasks);

        List<Board> memberBoards = boardRepository.findByUsers(user);
        memberBoards.forEach(board -> board.removeUser(user));
        boardRepository.save(memberBoards);

        userRepository.delete(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean activateAccount(String username, String token) throws InvalidTokenException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.debug("Unknown user: " + username);
            throw new UsernameNotFoundException("Unknown user");
        }
        if (user.getActivationToken() == null) {
            throw new InvalidTokenException("User has no token");
        }
        if (!user.getActivationToken().getToken().equals(token)) {
            throw new InvalidTokenException("Token does not match");
        }
        if (user.getActivationToken().getExpiryDate().before(new Date())) {

            log.error("Activation token {} expired. Removing token from user.", token);
            user.setActivationToken(null);
            userRepository.save(user);
            throw new InvalidTokenException("Token expired");
        }

        user.setActivationToken(null);
        user.setAccountEnabled(true);
        userRepository.save(user);
        log.debug("User " + username + " activated.");

        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void changeDetails(User user, UserDetailChangeDto userDetailChangeDto) {

        if (!encoder.matches(userDetailChangeDto.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("Old password wrong");
        }

        if (userDetailChangeDto.getDisplayname() != null
                && !"".equals(userDetailChangeDto.getDisplayname())
                && !user.getDisplayname().equals(userDetailChangeDto.getDisplayname())) {
            log.debug("Changing display name from " + user.getDisplayname() + " to " + userDetailChangeDto.getDisplayname());
            user.setDisplayname(userDetailChangeDto.getDisplayname());
        }

        if (userDetailChangeDto.getPassword() != null && !"".equals(userDetailChangeDto.getPassword())) {
            log.debug("Changed password for user " + user.getUsername());
            user.setPassword(encoder.encode(userDetailChangeDto.getPassword()));
        }

        if (userDetailChangeDto.getLanguage() != null && !"".equals(userDetailChangeDto.getLanguage())) {
            log.debug("Changed language for user " + user.getUsername());
            user.setLanguage(userDetailChangeDto.getLanguage());
        }

        userRepository.save(user);
    }


}
