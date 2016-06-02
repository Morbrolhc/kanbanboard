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

import ch.fhnw.imvs.kanban.dto.*;
import ch.fhnw.imvs.kanban.model.Board;
import ch.fhnw.imvs.kanban.service.BoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "${api.prefix}/boards", produces = "application/json")
public class BoardController {

    private static Logger log = LoggerFactory.getLogger(BoardController.class);

    @Autowired
    private BoardService boardService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BoardDto>> findAll() {

        List<BoardDto> boards = boardService.getAllBoards();
        log.debug("Found " + boards.size() + " boards");
        return new ResponseEntity<>(boards, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> createBoard(@RequestBody BoardCreateDto boardDto) {


        try {
            String board = boardService.createBoard(boardDto);
            return new ResponseEntity<>("{ \"id\": \"" + board + "\"}", HttpStatus.OK);

        } catch (Exception e) {
            log.error("Could not create board. The error was " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<BoardFullDto> findOne(@PathVariable("id") String id) {


        BoardFullDto board = boardService.getBoardById(id);
        return new ResponseEntity<>(board, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = "application/json")
    public ResponseEntity<BoardDto> updateBoard(@PathVariable("id") String id,
                                                @RequestBody BoardUpdateDto boardUpdateDto) {

        Board board = boardService.updateBoard(id, boardUpdateDto);
        log.debug("Updated board with id " + board.getId().toHexString());
        return new ResponseEntity<>(new BoardDto(board), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("id") String id) {

        boardService.deleteBoard(id);
        log.debug("Deleted board with id " + id);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}/members/{username}",
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<UserDto>> removeUsersFromBoard(@PathVariable("id") String id,
                                                              @PathVariable("username") String username) {


        log.debug("Remove user from board with id " + id);
        final List<UserDto> currentMembers = boardService.removeUserFromBoard(id, username);
        return new ResponseEntity<>(currentMembers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}/members",
            consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<UserDto>> addUsersToBoard(@PathVariable("id") String id,
                                                         @RequestBody UserDto userDto) {


        List<UserDto> currentMembers = boardService.addUserToBoard(id, userDto);
        log.debug("Added new users to board with id " + id);
        return new ResponseEntity<>(currentMembers, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}/owner", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<BoardDto> changeBoardOwner(@PathVariable("id") String id,
                                                     @RequestBody UserDto userDto) {

        Board board = boardService.changeBoardOwner(id, userDto);
        log.debug("Changed owner of board with id {}", id);
        return new ResponseEntity<>(new BoardDto(board), HttpStatus.OK);
    }

}
