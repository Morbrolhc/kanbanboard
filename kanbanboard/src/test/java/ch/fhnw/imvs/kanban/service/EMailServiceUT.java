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

import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.service.impl.EMailServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class EMailServiceUT {

    @InjectMocks
    private EMailServiceImpl eMailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private User user;
    @Mock
    private User user2;

    @Mock
    private Task task;
    @Mock
    private Task task2;

    @Before
    public void setup() {

        initMocks(this);

        when(user.getUsername()).thenReturn("user");
        when(user.getDisplayname()).thenReturn("User");
        when(user.getEmail()).thenReturn("user@example.com");

        when(user2.getUsername()).thenReturn("user2");
        when(user2.getDisplayname()).thenReturn("User2");
        when(user2.getEmail()).thenReturn("user2@example.com");
    }

    @Test
    public void sendReminder() throws Exception {

        when(task.getAssigned()).thenReturn(Collections.singletonList(user));
        List<Task> tasks = Collections.singletonList(task);

        eMailService.sendReminder(tasks);

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    public void sendReminderTwoAssigned() throws Exception {
        when(task.getAssigned()).thenReturn(Arrays.asList(user, user2));
        List<Task> tasks = Collections.singletonList(task);

        eMailService.sendReminder(tasks);

        ArgumentCaptor<MimeMessagePreparator> messageCaptor = ArgumentCaptor.forClass(MimeMessagePreparator.class);

        verify(mailSender, times(2)).send(messageCaptor.capture());
    }

    @Test
    public void sendReminderOneAssignedTwoTasks() throws Exception {

        when(task.getAssigned()).thenReturn(Collections.singletonList(user));
        when(task2.getAssigned()).thenReturn(Collections.singletonList(user));
        List<Task> tasks = Arrays.asList(task, task2);

        eMailService.sendReminder(tasks);

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    public void sendReminderTwoAssignedTwoTasks() throws Exception {
        when(task.getAssigned()).thenReturn(Arrays.asList(user, user2));
        when(task2.getAssigned()).thenReturn(Collections.singletonList(user));
        List<Task> tasks = Arrays.asList(task, task2);

        eMailService.sendReminder(tasks);

        ArgumentCaptor<MimeMessagePreparator> messageCaptor = ArgumentCaptor.forClass(MimeMessagePreparator.class);

        verify(mailSender, times(2)).send(messageCaptor.capture());
    }
}
