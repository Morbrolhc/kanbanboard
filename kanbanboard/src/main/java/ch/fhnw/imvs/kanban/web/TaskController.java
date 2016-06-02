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

import ch.fhnw.imvs.kanban.dto.ResultDto;
import ch.fhnw.imvs.kanban.dto.TaskDto;
import ch.fhnw.imvs.kanban.dto.TaskMoveDto;
import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "${api.prefix}/boards",
        produces = "application/json")
public class TaskController {

    private static Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    TaskService taskService;

    @RequestMapping(value = "/{boardId}/cards", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> createCard(@PathVariable("boardId") String boardId,
                                        @RequestBody TaskDto taskDto) {


        Task task = taskService.createNewTask(boardId, taskDto);
        return new ResponseEntity<>(new TaskDto(task), HttpStatus.CREATED);

    }

    @RequestMapping(value = "/{boardId}/cards/{cardId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getCardData(@PathVariable("boardId") String boardId,
                                         @PathVariable("cardId") String cardId) {

        Task task = taskService.getTask(boardId, cardId);
        return new ResponseEntity<>(new TaskDto(task), HttpStatus.OK);

    }

    @RequestMapping(value = "/{boardId}/cards/{cardId}/category", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<?> updateCardCategory(@PathVariable("boardId") String boardId,
                                                @PathVariable("cardId") String cardId,
                                                @RequestBody TaskMoveDto dto) {

        log.debug("Begin moving task {}", cardId);

        try {
            Task.Category category = Task.Category.valueOf(dto.getState());
            Task task = taskService.moveTask(boardId, cardId, category);

            return new ResponseEntity<>(new TaskDto(task), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse state {}", dto.getState());
            return new ResponseEntity<>(new ResultDto("Invalid state"), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{boardId}/cards/{cardId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<TaskDto> updateCardData(@PathVariable("boardId") String boardId,
                                                  @PathVariable("cardId") String cardId,
                                                  @RequestBody TaskDto dto) {


        Task task = taskService.updateTask(boardId, cardId, dto);
        return new ResponseEntity<>(new TaskDto(task), HttpStatus.OK);

    }

    @RequestMapping(value = "/{boardId}/cards/{cardId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteCard(@PathVariable("boardId") String boardId,
                                           @PathVariable("cardId") String cardId) {

        taskService.deleteTask(boardId, cardId);

        return ResponseEntity.noContent().build();

    }
}
