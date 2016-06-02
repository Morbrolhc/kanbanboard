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

package ch.fhnw.imvs.kanban.persistence;

import ch.fhnw.imvs.kanban.KanbanApplication;
import ch.fhnw.imvs.kanban.model.Task;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KanbanApplication.class)
@WebIntegrationTest(randomPort = true)
@TestPropertySource(locations="classpath:test.properties")
public class TaskRepositoryUT {

    private static Logger log = LoggerFactory.getLogger(TaskRepositoryUT.class);

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void findEndingOnDateToday() throws Exception {

        final Date today = new Date();
        final List<Task> endingOnDate = taskRepository.findEndingOnDate(today);

        assertNotNull(endingOnDate);
        assertTrue(endingOnDate.size() > 0);

        for (Task t : endingOnDate) {
            log.debug(t.getTitle());
            log.debug(t.getDueDate().toString());

            assertEquals(DateUtils.truncate(today, Calendar.DATE), DateUtils.truncate(t.getDueDate(), Calendar.DATE));
        }
    }

    @Test
    public void findEndingOnDateTomorrow() throws Exception {

        final Date tomorrow = Date.from(LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        final List<Task> endingOnDate = taskRepository.findEndingOnDate(tomorrow);

        assertNotNull(endingOnDate);

        for (Task t : endingOnDate) {
            log.debug(t.getTitle());
            log.debug(t.getDueDate().toString());
            assertEquals(DateUtils.truncate(tomorrow, Calendar.DATE), DateUtils.truncate(t.getDueDate(), Calendar.DATE));
        }
    }
}
