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

import ch.fhnw.imvs.kanban.model.Board;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BoardFullDto {

    @JsonIgnore
    private Board board;

    public BoardFullDto(Board board) {
        this.board = board;
    }

    @JsonProperty
    public String getId() {return board.getId().toHexString();}

    @JsonProperty
    public String getName(){
        return board.getName();
    }

    @JsonProperty
    public UserDto getOwner() {
        return new UserDto(board.getOwner());
    }

    @JsonProperty
    public Set<UserDto> getAssigned() {
        return board.getUsers().stream().map(UserDto::new).collect(Collectors.toSet());
    }

    @JsonProperty
    public Set<TaskDto> getTasks() {
        return board.getTasks().stream().map(TaskDto::new).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @JsonIgnore
    public Board getDetails() {
        return board;
    }
}
