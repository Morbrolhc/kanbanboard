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

import ch.fhnw.imvs.kanban.exception.InvalidTokenException;
import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.model.User;

import java.util.List;

public interface EMailService {

    /**
     * Sends an activation mail to the user. The user must already have an activation token assigned.
     *
     * @param user A user with an activation token.
     * @exception InvalidTokenException if the user has no password reset token assigned.
     */
    void sendActivation(User user) throws InvalidTokenException;

    /**
     * Sends a password reset mail to the user. The user must already have a password reset token assigned.
     *
     * @param user A user with a password reset token.
     * @exception InvalidTokenException if the user has no password reset token assigned.
     */
    void sendPasswordReset(User user) throws InvalidTokenException;

    /**
     * Sends a reminder to all assigned users of the given tasks.
     *
     * @param tasks The tasks which all assigned users will be sent an email.
     */
    void sendReminder(List<Task> tasks);
}
