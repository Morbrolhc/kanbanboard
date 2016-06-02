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

import ch.fhnw.imvs.kanban.dto.*;
import ch.fhnw.imvs.kanban.model.Board;

import java.util.List;

public interface BoardService {

    List<BoardDto> getAllBoards();

    List<BoardDto> getBoardsForUser(String username);

    BoardFullDto getBoardById(String id);

    String createBoard(BoardCreateDto board);

    Board updateBoard(String id, BoardUpdateDto boardUpdateDto);

    void deleteBoard(String id);

    /**
     * Adds a user to a board.
     *
     * @param boardId the id of the board
     * @param userDto the user who should be added
     * @return A list of all assigned members after this action.
     */
    List<UserDto> addUserToBoard(String boardId, UserDto userDto);

    /**
     * Removes a user from a board.
     *
     * @param boardId the id of the board
     * @param username the user who should be removed
     * @return A list of all assigned members after this action.
     */
    List<UserDto> removeUserFromBoard(String boardId, String username);

    Board changeBoardOwner(String boardId, UserDto newOwner);
}
