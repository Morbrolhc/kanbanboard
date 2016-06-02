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
import ch.fhnw.imvs.kanban.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class BoardDto {

    @JsonIgnore
    private static Logger log = LoggerFactory.getLogger(BoardDto.class);

    @JsonIgnore
    private Board board;

    public BoardDto(Board board) {
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
    public Set<UserDto> getUsers() {
        Set<User> users = board.getUsers();
        return users.stream().map(UserDto::new).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Board getDetails() {
        return board;
    }

    // Returns an array with the three task types enumerated needed to display a preview.
    @JsonProperty
    public int[] getTasks() {
        int[] types = new int[3];
        board.getTasks().forEach( t -> {
            switch (t.getCategory()) {
                case TODO:
                    types[0]++;
                    break;
                case DOING:
                    types[1]++;
                    break;
                case DONE:
                    types[2]++;
                    break;
                default:
                    log.error("Unknown type: {}", t.getCategory());
                    throw new InternalError("Tasktype " + t.getCategory() + " unknown.");
            }
        });
        return types;
    }

}
