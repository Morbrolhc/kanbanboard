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

package ch.fhnw.imvs.kanban.service;

import ch.fhnw.imvs.kanban.dto.UserCreateDto;
import ch.fhnw.imvs.kanban.dto.UserDetailChangeDto;
import ch.fhnw.imvs.kanban.exception.InvalidTokenException;
import ch.fhnw.imvs.kanban.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    /**
     * {@inheritDoc}
     */
    User loadUserByUsername(String username);

    User loadUserByEmail(String email);

    /**
     * Registers a new a account in deactivated state. The account has to be activated before anything can be done.
     *
     * @param userDto The data to create the new user.
     * @return The new created user.
     */
    User registerNewUserAccount(UserCreateDto userDto);

    /**
     * Adds an already initialized user to the repository.
     *
     * @param user The user to add.
     */
    void addUser(User user);

    /**
     * Finds all uaer which username, displayname or email contains the search term.
     *
     * @param like the search term.
     * @return All found users.
     */
    List<User> findUsersLike(String like);

    /**
     * Adds a password reset token to a user. Finds the user with the supplied user input.
     * @param userInput the username or the email address of the user.
     * @return the user with the new token.
     */
    User createPasswordResetTokenForUser(String userInput);

    /**
     * Verifies if the password reset token of a user is valid. Removes the token if it is invalid.
     *
     * @param username the username of the user
     * @param token the content of the token
     * @return true if the token is
     * @throws InvalidTokenException if the user does not have a token assigned
     */
    boolean verifyPasswordResetToken(String username, String token) throws InvalidTokenException;

    boolean changePasswordWithToken(String username, String token, String newPassword) throws InvalidTokenException;

    void deleteUser(String username);

    boolean activateAccount(String username, String token) throws InvalidTokenException;

    void changeDetails(User user, UserDetailChangeDto userDetailChangeDto);
}
