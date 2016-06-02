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

import ch.fhnw.imvs.kanban.dto.TaskDto;
import ch.fhnw.imvs.kanban.exception.BoardNotFoundException;
import ch.fhnw.imvs.kanban.exception.TaskNotFoundException;
import ch.fhnw.imvs.kanban.model.Board;
import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.model.UserRole;
import ch.fhnw.imvs.kanban.persistence.BoardRepository;
import ch.fhnw.imvs.kanban.persistence.TaskRepository;
import ch.fhnw.imvs.kanban.service.TaskService;
import ch.fhnw.imvs.kanban.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class TaskServiceImpl extends AuthenticatedService implements TaskService{

    private static Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private BoardRepository boardRepository;
    private TaskRepository taskRepository;
    private final UserService userService;

    @Autowired
    public TaskServiceImpl(BoardRepository boardRepository, TaskRepository taskRepository, UserService userService) {
        Assert.notNull(boardRepository);
        Assert.notNull(taskRepository);
        Assert.notNull(userService);

        this.boardRepository = boardRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Task createNewTask(String boardId, TaskDto taskDto) {

        User user = getAuthorizedUser();
        Board board = boardRepository.findOne(boardId);

        if (board == null) {
            log.info("Board with id {} not found.", boardId);
            throw new BoardNotFoundException("Board " + boardId + " not found");
        }

        final Set<User> users = board.getUsers();

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !users.contains(user)) {
            log.warn("User {} is not allowed to add a task to board {} with id {}.", user.getUsername(), board.getName(), board.getId());
            throw new AccessDeniedException("User is not allowed to add tasks to this board.");
        }

        Task task = new Task(taskDto.getName(), taskDto.getState());
        task.setDescription(taskDto.getDescription());
        task.setCreationDate(new Date());
        task.setDueDate(taskDto.getDuedate());
        task.setCreator(user);
        task.setContainingBoardId(board.getId());

        if (taskDto.getAssigned() != null) {
            List<User> assigned = taskDto.getAssigned().stream()
                    .map(u -> userService.loadUserByUsername(u.getUsername()))
                    .filter(user1 -> board.getOwner().equals(user1) || board.getUsers().contains(user1))
                    .collect(Collectors.toList());
            task.setAssigned(assigned);
        }


        board.addTask(task);
        taskRepository.save(task);
        boardRepository.save(board);

        log.debug("Created new task with id {}.", task.getId().toHexString());

        return task;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Task getTask(String boardId, String taskId) {

        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(boardId);

        if (board == null) {
            log.info("Board {} not found", boardId);
            throw new BoardNotFoundException("Board " + boardId + " not found");
        }

        final Set<User> users = board.getUsers();

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !users.contains(user)) {
            log.warn("User {} is not allowed to access the board {} with id {}.", user.getUsername(), board.getName(), board.getId());
            throw new AccessDeniedException("User is not allowed to access this board");
        }

        final Set<Task> tasks = board.getTasks();

        final Optional<Task> first = tasks.stream().filter(task -> task.getId().toHexString().equals(taskId)).findFirst();

        if (!first.isPresent()) {
            log.info("Board {} does not contain task {}.", boardId, taskId);
            throw new TaskNotFoundException("Board " + boardId + " does not contain task " + taskId);
        }

        log.debug("Task with id {} found.", taskId);
        return first.get();
    }

    @Override
    public List<Task> getTasksForUser(String userId) {
        User user = getAuthorizedUser();
        if (!user.hasRole(UserRole.ADMIN) && !user.getUsername().equals(userId)) {
            log.warn("User {} is not allowed to get tasks of user {}.", user.getUsername(), userId);
            throw new AccessDeniedException("User " + user.getUsername() + " is not allowed to get tasks for user " + userId);
        }

        User current;
        if (user.getUsername().equals(userId)) {
            current = user;
        } else {
            current = userService.loadUserByUsername(userId);
        }

        return taskRepository.findByAssigned(current);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> getEndingToday() {
        final List<Task> endingOnDate = taskRepository.findEndingOnDate(new Date());

        return endingOnDate == null ? new ArrayList<>() : endingOnDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Task moveTask(String boardId, String cardId, Task.Category category) {

        User user = getAuthorizedUser();

        final Board board = boardRepository.findOne(boardId);

        if (board == null) {
            log.info("Board {} not found", boardId);
            throw new BoardNotFoundException("Board " + boardId + " not found");
        }

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} is not allowed to modify tasks of board {}.", user.getUsername(), board.getId());
            throw new AccessDeniedException("User " + user.getUsername() + " is not allowed to modify tasks of board " + boardId);
        }

        Task task = getTask(boardId, cardId);
        task.setCategory(category);
        taskRepository.save(task);

        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Task updateTask(String boardId, String cardId, TaskDto dto) {

        User user = getAuthorizedUser();

        final Board board = boardRepository.findOne(boardId);

        if (board == null) {
            log.error("Board {} not found", boardId);
            throw new BoardNotFoundException("Board " + boardId + " not found");
        }

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} is not allowed to delete tasks of board {}.", user.getUsername(), board.getId());
            throw new AccessDeniedException("User " + user.getUsername() + " is not allowed to modify tasks of board " + boardId);
        }

        final Task task = getTask(boardId, cardId);

        task.setTitle(dto.getName());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDuedate());

        List<User> assigned = dto.getAssigned().stream()
                .map(u -> userService.loadUserByUsername(u.getUsername()))
                .filter(user1 -> board.getOwner().equals(user1) || board.getUsers().contains(user1))
                .collect(Collectors.toList());
        task.setAssigned(assigned);
        task.setCategory(dto.getState());
        taskRepository.save(task);
        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteTask(String boardId, String taskId) {

        User user = getAuthorizedUser();

        final Board board = boardRepository.findOne(boardId);

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("");
            throw new AccessDeniedException("User " + user.getUsername() + " is not allowed to delete tasks of board " + boardId);
        }

        final Set<Task> tasks = board.getTasks();

        final Optional<Task> first = tasks.stream().filter(task -> task.getId().toHexString().equals(taskId)).findFirst();

        if (!first.isPresent()) {
            log.info("Board {} does not contain task {}.", boardId, taskId);
            throw new TaskNotFoundException("Board " + boardId + " does not contain task " + taskId);
        }

        Task task = first.get();

         if (board.removeTask(task)) {
             taskRepository.delete(task);
             boardRepository.save(board);
             return true;
         } else {
             log.info("No task was deleted.");
             return false;
         }
    }
}
