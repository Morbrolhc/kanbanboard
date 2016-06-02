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

package ch.fhnw.imvs.kanban.persistence.impl;

import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.persistence.TaskRepositoryCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Component
public class TaskRepositoryImpl implements TaskRepositoryCustom {

    private static Logger log = LoggerFactory.getLogger(TaskRepositoryImpl.class);


    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<Task> findEndingOnDate(Date date) {

        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), zoneId);
        ZonedDateTime start = zdt.toLocalDate().atStartOfDay(zoneId);
        ZonedDateTime end = start.plusDays(1);


        log.debug("Date: {}", start.toString());
        Criteria criteria =
                Criteria.where("dueDate").gte(Date.from(start.toLocalDateTime().atZone(zoneId).toInstant()))
                .andOperator(
                        Criteria.where("dueDate").lt(Date.from(end.toLocalDateTime().atZone(zoneId).toInstant())));

        return mongoTemplate.find(Query.query(criteria), Task.class);
    }
}
