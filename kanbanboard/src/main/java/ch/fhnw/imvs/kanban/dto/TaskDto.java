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

package ch.fhnw.imvs.kanban.dto;


import ch.fhnw.imvs.kanban.model.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskDto {

    private String id;
    private String name;
    private Task.Category state;
    private String description;
    private Date duedate;
    private Set<UserDto> assigned;
    private UserDto createdby;
    private String boardId;

    public TaskDto(Task task) {
        this.id = task.getId().toHexString();
        this.name = task.getTitle();
        this.description = task.getDescription();
        this.state = task.getCategory();
        this.duedate = task.getDueDate();
        this.boardId = task.getContainingBoardId().toHexString();

        this.createdby = new UserDto(task.getCreator());

        if (task.getAssigned() != null) {
            this.assigned = task.getAssigned().stream().map(UserDto::new).collect(Collectors.toSet());
        }
    }

    public TaskDto() {

    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    @JsonProperty
    public Task.Category getState() {
        return state;
    }

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    public Date getDuedate() {
        return new Date(duedate.getTime());
    }

    @JsonProperty
    public Set<UserDto> getAssigned() {
        return assigned == null? Collections.unmodifiableSet(new HashSet<>()) : Collections.unmodifiableSet(assigned);
    }

    @JsonProperty
    public UserDto getCreatedby() {
        return createdby;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setState(Task.Category state) {
        this.state = state;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuedate(Date duedate) {
        this.duedate = new Date(duedate.getTime());
    }

    public void setAssigned(Set<UserDto> assigned) {
        this.assigned = assigned;
    }

    public void setCreatedBy(UserDto createdBy) {
        this.createdby = createdBy;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }
}
