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

import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.persistence.BoardRepository;
import ch.fhnw.imvs.kanban.persistence.TaskRepository;
import ch.fhnw.imvs.kanban.persistence.UserRepository;
import ch.fhnw.imvs.kanban.service.impl.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserServiceUT {


    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private EMailService eMailService;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SecureRandom secureRandom;

    @Before
    public void init() {
        initMocks(this);

    }

    @Test
    public void loadUserByUsername() throws Exception {
        User load = new User("admin", "admin", "admin@example.com");
        load.setAccountEnabled(true);
        when(userRepository.findByUsername("admin")).thenReturn(load);
        User user = userService.loadUserByUsername("admin");

        assertNotNull(user);
        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    public void loadUserByUsernameFail() {
        when(userRepository.findByUsername("nonExistent")).thenReturn(null);

        try {
            userService.loadUserByUsername("nonExistent");
            fail();
        } catch (UsernameNotFoundException e) {
            // everything ok
        }

        verify(userRepository, times(1)).findByUsername("nonExistent");
    }


    @Test
    public void loadUserByEmail() throws Exception {

        User load = new User("admin", "admin", "admin@example.com");
        load.setAccountEnabled(true);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(load);
        User user = userService.loadUserByEmail("admin@example.com");

        assertEquals("admin", user.getUsername());
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }
}
