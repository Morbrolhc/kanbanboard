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

import ch.fhnw.imvs.kanban.dto.FileDto;
import ch.fhnw.imvs.kanban.exception.FileNotFoundException;
import ch.fhnw.imvs.kanban.model.*;
import ch.fhnw.imvs.kanban.persistence.BoardRepository;
import ch.fhnw.imvs.kanban.persistence.FileStore;
import ch.fhnw.imvs.kanban.persistence.FilesRepository;
import ch.fhnw.imvs.kanban.persistence.TaskRepository;
import ch.fhnw.imvs.kanban.service.FileService;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by joel on 14.05.16.
 */
@Service
public class FileServiceImpl extends AuthenticatedService implements FileService {

    private Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    FilesRepository filesRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    FileStore fileStore;

    @Override
    public Set<FileDto> getAllFiles(String boardId, String taskId) throws FileNotFoundException {
        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(boardId);

        if(!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} has no rights on board {}", user.getUsername(), boardId);
            throw new AccessDeniedException("User does not have rights to access this board");
        }

        Task task = taskRepository.findOne(taskId);
        if(task != null) {
            Set<FSFile> files = task.getFiles();
            if(files != null) {
                return task.getFiles().stream().map(FileDto::new).collect(Collectors.toSet());
            } else {
                log.debug("Task {} has no files.", taskId);
            }
        } else {
            log.debug("No task with given id {} found.", taskId);
            throw new FileNotFoundException();
        }
        return null;
    }

    @Override
    public FileDto storeFile(String boardId, String taskId, MultipartFile file) throws IOException {
        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(boardId);

        if(!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} has no rights on board {}", user.getUsername(), boardId);
            throw new AccessDeniedException("User does not have rights to access this board");
        }

        Task task = taskRepository.findOne(taskId);
        if (task != null) {
            FSFile fsFile = fileStore.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
            task.addFile(fsFile);
            taskRepository.save(task);
            return new FileDto(fsFile);
        } else {
            log.debug("No Task with given ID " + taskId + " found.");
        }
        return null;
    }

    @Override
    public GridFSDBFile getFile(String boardId, String fileId) throws FileNotFoundException {
        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(boardId);

        if(!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} has no rights on board {}", user.getUsername(), boardId);
            throw new AccessDeniedException("User does not have rights to access this board");
        }

        FSFile file = filesRepository.findOne(fileId);
        if ( file == null ) {
            log.debug("No File with given ID " + fileId + " found.");
            throw new FileNotFoundException();
        }
        return fileStore.read(file);
    }

    @Override
    public void deleteFile(String boardId, String taskId, String fileId) {
        User user = getAuthorizedUser();

        Board board = boardRepository.findOne(boardId);

        if(!user.hasRole(UserRole.ADMIN) && !board.getOwner().equals(user) && !board.getUsers().contains(user)) {
            log.warn("User {} has no rights on board {}", user.getUsername(), boardId);
            throw new AccessDeniedException("User does not have rights to access this board");
        }

        Task task = taskRepository.findOne(taskId);
        FSFile file = filesRepository.findOne(fileId);
        task.removeFile(file);
        taskRepository.save(task);
        fileStore.delete(file);
    }
}
