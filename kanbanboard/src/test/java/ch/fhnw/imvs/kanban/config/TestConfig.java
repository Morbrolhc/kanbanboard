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

package ch.fhnw.imvs.kanban.config;

import ch.fhnw.imvs.kanban.model.*;
import ch.fhnw.imvs.kanban.persistence.BoardRepository;
import ch.fhnw.imvs.kanban.persistence.TaskRepository;
import ch.fhnw.imvs.kanban.persistence.UserRepository;
import ch.fhnw.imvs.kanban.persistence.impl.FileStoreImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Configuration
public class TestConfig {

    private static Logger log = LoggerFactory.getLogger(TestConfig.class);

    @Bean
    public InitializingBean insertDefaultUsers() {
        return new InitializingBean() {
            @Autowired
            private UserRepository userRepository;

            @Autowired
            private MongoTemplate mongoTemplate;

            @Autowired
            private BoardRepository boardRepository;

            @Autowired
            private TaskRepository taskRepository;

            @Override
            public void afterPropertiesSet() {
                mongoTemplate.remove(new Query(), "boards");
                mongoTemplate.remove(new Query(), "tasks");
                mongoTemplate.remove(new Query(), "users");
                mongoTemplate.remove(new Query(), "fs.chunks");
                mongoTemplate.remove(new Query(), "fs.files");

                final User admin = addUser("admin", "admin");
                final User user = addUser("user", "user");
                final User user2 = addUser("user2", "password2");

                addInactiveUser("userInactive", "inactivePass");
                addPasswordTokenUser("userPass", "changeMe");

                addHugeBoard("HugeBoard");
                addBoard("testboard 01", admin);
                addBoard("testboard 02", user);

                addFile();

            }

            private User addUser(String username, String password) {
                User user = new User(username, username, username + "@example.com");
                user.setPassword(new BCryptPasswordEncoder().encode(password));
                user.grantRole(username.equals("admin") ? UserRole.ADMIN : UserRole.USER);
                user.setAccountEnabled(true);
                user.setLanguage("DE");
                user.setCreationDate(new Date());
                userRepository.save(user);
                return user;
            }

            private void addInactiveUser(String username, String password) {
                User user = new User(username, username, username + "@example.com");
                user.setPassword(new BCryptPasswordEncoder().encode(password));
                user.grantRole(username.equals("admin") ? UserRole.ADMIN : UserRole.USER);
                user.setAccountEnabled(false);
                user.setLanguage("DE");
                user.setCreationDate(new Date());

                Token token = new Token();
                log.debug("Activation token:" + token.getToken());
                user.setActivationToken(token);
                userRepository.save(user);
            }

            private void addPasswordTokenUser(String username, String password) {
                User user = new User(username, username, username + "@example.com");
                user.setPassword(new BCryptPasswordEncoder().encode(password));
                user.grantRole(username.equals("admin") ? UserRole.ADMIN : UserRole.USER);
                user.setAccountEnabled(true);
                user.setLanguage("DE");
                user.setCreationDate(new Date());

                SecureRandom random = new SecureRandom();
                String plainToken = new BigInteger(130, random).toString(32);
                log.debug("Reset token:" + plainToken);

                Token token = new Token(plainToken);
                user.setPasswordResetToken(token);
                userRepository.save(user);
            }

            private void addBoard(String name, User owner) {
                Board board = new Board(name);
                board.setOwner(owner);
                board.addUser(userRepository.findByUsername("user"));
                board.addUser(userRepository.findByUsername("userDemo0"));
                board.addUser(userRepository.findByUsername("userDemo1"));

                board = boardRepository.save(board);

                Task task = new Task("Task 1", Task.Category.TODO);
                task.setDescription("Test description");
                User user = userRepository.findByUsername("user");
                task.assign(user);
                task.setCreator(user);
                task.setCreationDate(new Date());
                task.setDueDate(new Date());
                task.setContainingBoardId(board.getId());
                task = taskRepository.save(task);
                board.addTask(task);

                Task task2 = new Task("Task 2", Task.Category.DOING);
                task2.setDescription("Test description 2");
                task2.setCreator(user);
                task2.setCreationDate(new Date());
                task2.assign(userRepository.findByUsername("admin"));
                task2.setDueDate(new Date());
                task2.setContainingBoardId(board.getId());

                task2 = taskRepository.save(task2);

                board.addTask(task2);

                boardRepository.save(board);
            }

            private void addHugeBoard(String name) {
                Board board = new Board(name);

                User user = userRepository.findByUsername("user");
                board.setOwner(user);

                List<User> users = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    User tmp = addUser("userDemo" + i, "passDemo" + i);
                    users.add(tmp);
                    board.addUser(tmp);
                }

                board = boardRepository.save(board);

                for (int i = 0; i < 20; i++) {
                    Task task = new Task("TaskTodo " + i, Task.Category.TODO);

                    task.setCreator(users.get(i % 10));
                    task.setCreationDate(new Date());

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, i);
                    task.setDueDate(cal.getTime());
                    task.setContainingBoardId(board.getId());

                    taskRepository.save(task);
                    board.addTask(task);
                }

                for (int i = 0; i < 30; i++) {
                    Task task = new Task("TaskDoing " + i, Task.Category.DOING);

                    task.setCreator(users.get(i % 10));
                    task.setCreationDate(new Date());

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, i);
                    task.setDueDate(cal.getTime());
                    task.setContainingBoardId(board.getId());

                    taskRepository.save(task);
                    board.addTask(task);
                }

                for (int i = 0; i < 40; i++) {
                    Task task = new Task("TaskD " + i, Task.Category.DONE);

                    task.setCreator(users.get(i % 10));
                    task.setCreationDate(new Date());

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -i);
                    task.setDueDate(cal.getTime());
                    task.setContainingBoardId(board.getId());

                    taskRepository.save(task);
                    board.addTask(task);
                }

                boardRepository.save(board);
            }

            private void addFile() {
                FileStoreImpl fileStore = new FileStoreImpl();
                try (FileInputStream fis = new FileInputStream("test.txt")) {
                    FSFile file = fileStore.store(fis, "test.txt", "text/plain");
                    Task task = taskRepository.findAll().get(0);
                    task.addFile(file);
                    taskRepository.save(task);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
