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

import ch.fhnw.imvs.kanban.dto.TaskDto;
import ch.fhnw.imvs.kanban.model.Task;

import java.util.List;

public interface TaskService {

    /**
     * creates a new task for an existing board. All fields in the dto can be used to create the task. The creating
     * user has to be a member of this board.
     *
     * @param boardId the board of the new task
     * @param taskDto the data for the new task
     * @return the created task
     */
    Task createNewTask(String boardId, TaskDto taskDto);

    /**
     * returns an existing task of the given board.
     *
     * @param boardId the board of the task
     * @param taskId the id of the task
     * @return the task with the given id
     */
    Task getTask(String boardId, String taskId);

    List<Task> getTasksForUser(String userId);

    /**
     * Updates a task with new data.
     *
     * @param boardId the id of the board which contains the task.
     * @param cardId the id of the task to be changed.
     * @param dto the new date.
     * @return the changed task.
     */
    Task updateTask(String boardId, String cardId, TaskDto dto);

    /**
     * Deletes a task.
     *
     * @param boardId the id of the board which contains the task.
     * @param cardId the id of the task to be deleted.
     * @return true if a task was deleted, false otherwise.
     */
    boolean deleteTask(String boardId, String cardId);

    /**
     * Moves a task to the new category.
     *
     * @param boardId the id of the board which contains the task.
     * @param cardId the id of the task to be moved.
     * @param category the new category.
     * @return the moved task.
     */
    Task moveTask(String boardId, String cardId, Task.Category category);

    /**
     * Gets a list of all task which duedate is today.
     *
     * @return a list of task with duedate today.
     */
    List<Task> getEndingToday();

}
