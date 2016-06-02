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

import ch.fhnw.imvs.kanban.dto.*;
import ch.fhnw.imvs.kanban.exception.BoardNotFoundException;
import ch.fhnw.imvs.kanban.model.*;
import ch.fhnw.imvs.kanban.persistence.BoardRepository;
import ch.fhnw.imvs.kanban.persistence.FileStore;
import ch.fhnw.imvs.kanban.persistence.TaskRepository;
import ch.fhnw.imvs.kanban.service.BoardService;
import ch.fhnw.imvs.kanban.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl extends AuthenticatedService implements BoardService {

    private static Logger log = LoggerFactory.getLogger(BoardServiceImpl.class);

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStore fileStore;

    @Override
    public List<BoardDto> getAllBoards() {
        User user = getAuthorizedUser();


        if (!user.hasRole(UserRole.ADMIN)) {
            log.warn("User {} has no admin rights. Can't access all boards.", user.getUsername());
            throw new AccessDeniedException("User does not have rights to access all boards");
        }

        List<Board> boards = boardRepository.findAll();
        if (boards != null) {
            return boards.stream().map(BoardDto::new).collect(Collectors.toList());
        } else {
            log.info("No boards in database.");
            return new ArrayList<>();
        }
    }

    @Override
    public List<BoardDto> getBoardsForUser(String username) {
        User user = getAuthorizedUser();

        if (!user.hasRole(UserRole.ADMIN) && !user.getUsername().equals(username)) {
            throw new AccessDeniedException("Not allowed to get boards of user " + username);
        }

        List<Board> boards = boardRepository.findByOwnerOrUsers(user, user);
        if (boards == null) {
            log.debug("User {} has no associated boards.", user.getUsername());
            return new ArrayList<>();
        } else {
            return boards.stream().map(BoardDto::new).collect(Collectors.toList());
        }
    }

    @Override
    public BoardFullDto getBoardById(String id) {
        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(id);

        if (board == null) {
            log.info("No board with id {} found.", id);
            throw new BoardNotFoundException("Board with id " + id + " not found.");
        }

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} does not have rights to access board {}", user.getUsername(), id);
            throw new AccessDeniedException("User does not have rights to access this board");
        }

        return new BoardFullDto(board);
    }


    @Override
    public String createBoard(BoardCreateDto board) {
        User user = getAuthorizedUser();

        Board newBoard = new Board(board.getName());
        newBoard.setOwner(user);
        newBoard.addUser(user);
        boardRepository.save(newBoard);
        return newBoard.getId().toHexString();
    }

    @Override
    public Board updateBoard(String id, BoardUpdateDto boardUpdateDto) {
        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(id);

        if (board != null) {

            if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
                log.error("User {} does not have rights to alter board {}.", user.getUsername(), id);
                throw new AccessDeniedException("User is not allowed to alter board.");
            }
            board.setName(boardUpdateDto.getName());
            board.setOwner(userService.loadUserByUsername(boardUpdateDto.getOwner().getUsername()));
            if (boardUpdateDto.getMembers() != null) {
                Set<User> members = new LinkedHashSet<>();
                boardUpdateDto.getMembers().forEach(d -> {
                    try {
                        members.add(userService.loadUserByUsername(d.getUsername()));
                    } catch (AccessDeniedException | LockedException | DisabledException | CredentialsExpiredException e) {
                        log.warn(e.getMessage());
                    }
                });
                board.setUsers(members);
            }
            boardRepository.save(board);
        } else {
            log.info("No board with given id {} found.", id);
            throw new BoardNotFoundException("Board with id " + id + " not found.");
        }
        return board;
    }

    @Override
    public void deleteBoard(String id) {
        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(id);

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user)) {
            log.warn("User {} does not have rights to alter board {}. Only board owner allowed.", user.getUsername(), id);
            throw new AccessDeniedException("User is not allowed to alter board.");
        }

        if (board != null) {
            Set<Task> tasks = board.getTasks();
            if (tasks != null) {
                for (Task t : tasks) {
                    Set<FSFile> files = t.getFiles();
                    if (files != null) {
                        for (FSFile f : files) {
                            fileStore.delete(f);
                        }
                    }
                }
                taskRepository.delete(tasks);
            }
            boardRepository.delete(id);
        } else {
            log.debug("No board with given id " + id + " found.");
            throw new BoardNotFoundException("Board with id " + id + " not found.");
        }
    }

    @Override
    public List<UserDto> addUserToBoard(String boardId, UserDto userDto) {
        final User user = getAuthorizedUser();
        final Board board = boardRepository.findOne(boardId);

        if (board == null) {
            throw new BoardNotFoundException("Unknown board");
        }

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} does not have rights to modify users of board {}. Only board members are allowed.", user.getUsername(), boardId);
            throw new AccessDeniedException("User is not allowed to modify users.");
        }


        User tmp = userService.loadUserByUsername(userDto.getUsername());
        if (tmp == null) {
            throw new UsernameNotFoundException("User with name " + userDto.getUsername() + " not found.");
        }
        log.debug("User: {}", tmp.toString());
        if (!tmp.equals(board.getOwner())) {
            board.addUser(tmp);
            log.debug("Added user {} to board {}", tmp.getUsername(), board.getId());
        } else {
            log.info("Owner {} can't be added as user to the board.", board.getOwner().toString());
        }
        boardRepository.save(board);

        return board.getUsers().stream().map(UserDto::new).collect(Collectors.toList());
    }

    @Override
    public List<UserDto> removeUserFromBoard(String boardId, String username) {

        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(boardId);
        if (board == null) {
            throw new BoardNotFoundException("Unknown board");
        }

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} does not have rights to modify users of board {}. Only board members are allowed.", user.getUsername(), boardId);
            throw new AccessDeniedException("User is not allowed to modify users.");
        }

        User tmp = userService.loadUserByUsername(username);
        if (tmp != null) {
            board.removeUser(tmp);
            log.debug("User {} removed from board {}", tmp.getUsername(), board.getId());
        } else {
            log.info("User with username {} not found.", username);
            throw new UsernameNotFoundException("User with username " + username + " not found");
        }

        boardRepository.save(board);

        return board.getUsers().stream().map(UserDto::new).collect(Collectors.toList());
    }

    @Override
    public Board changeBoardOwner(String boardId, UserDto newOwnerDto) {

        User user = getAuthorizedUser();
        Board board = boardRepository.findOne(boardId);

        if (!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user)) {
            log.warn("User {} does not have rights to modify owner of board {}.", user.getUsername(), boardId);
            throw new AccessDeniedException("User " + user.getUsername() + " is not allowed to modify owner.");
        }

        final User oldOwner = board.getOwner();
        final User newOwner = userService.loadUserByUsername(newOwnerDto.getUsername());

        if (!board.getUsers().contains(newOwner)) {
            throw new AccessDeniedException("New owner " + newOwner.getUsername() +
                    " must be a member of the board first.");
        }

        board.setOwner(newOwner);
        board.getUsers().remove(newOwner);
        board.getUsers().add(oldOwner);

        return boardRepository.save(board);

    }


}
