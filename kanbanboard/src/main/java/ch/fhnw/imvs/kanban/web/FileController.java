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

package ch.fhnw.imvs.kanban.web;

import ch.fhnw.imvs.kanban.dto.FileDto;
import ch.fhnw.imvs.kanban.exception.FileNotFoundException;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.service.FileService;
import ch.fhnw.imvs.kanban.service.TokenAuthenticationService;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping(value = "${api.prefix}/boards",
        produces = "application/json")
public class FileController {

    private static Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private TokenAuthenticationService authenticationService;

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/{boardId}/cards/{cardId}/files", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Set<FileDto>> getFileList(HttpServletRequest request,
                                         @PathVariable("boardId") String boardId,
                                         @PathVariable("cardId") String cardId) {

        final Authentication authentication = authenticationService.getAuthentication(request);

        if (authentication == null) {
            throw new AccessDeniedException("Invalid token");
        }
        User user = (User) authentication.getDetails();
        try {
            Set<FileDto> dtos = fileService.getAllFiles(boardId, cardId);
            return new ResponseEntity<>(dtos, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @RequestMapping(value = "/{boardId}/cards/{cardId}/files", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<FileDto> uploadFile(HttpServletRequest request,
                                        @PathVariable("boardId") String boardId,
                                        @PathVariable("cardId") String cardId,
                                        @RequestParam("file") MultipartFile file) {

        final Authentication authentication = authenticationService.getAuthentication(request);

        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = (User) authentication.getDetails();
        try {
            FileDto dto = fileService.storeFile(boardId, cardId, file);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

    }

    @RequestMapping(value = "/{boardId}/cards/{cardId}/files/{fileId}", method = RequestMethod.GET)
    @ResponseBody
    public void getFile(HttpServletRequest request, HttpServletResponse response,
                        @PathVariable("boardId") String boardId,
                        @PathVariable("cardId") String cardId,
                        @PathVariable("fileId") String fileId) {

        final Authentication authentication = authenticationService.getAuthentication(request);

        if (authentication == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User user = (User) authentication.getDetails();

        try {
            GridFSDBFile dbFile = fileService.getFile(boardId, fileId);
            response.setContentType(dbFile.getContentType());
            response.setStatus(HttpServletResponse.SC_OK);
            FileCopyUtils.copy(dbFile.getInputStream(), response.getOutputStream());
            response.flushBuffer();
        } catch (FileNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{boardId}/cards/{cardId}/files/{fileId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<?> deleteFile(HttpServletRequest request,
                                        @PathVariable("boardId") String boardId,
                                        @PathVariable("cardId") String cardId,
                                        @PathVariable("fileId") String fileId) {

        final Authentication authentication = authenticationService.getAuthentication(request);

        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = (User) authentication.getDetails();
        fileService.deleteFile(boardId, cardId, fileId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
