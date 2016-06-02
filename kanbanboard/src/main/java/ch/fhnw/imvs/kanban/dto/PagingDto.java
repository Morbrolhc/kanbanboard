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


import java.util.Collections;
import java.util.List;

public class PagingDto {

    private int page;
    private int pagesize;
    private int pagecount;

    private List<TaskDto> content;

    private int[] tasks = new int[3];

    protected PagingDto() {
        // Default for Spring
    }

    public PagingDto(int page, int pagesize, int pagecount, List<TaskDto> content) {
        this.page = page;
        this.pagesize = pagesize;
        this.pagecount = pagecount;
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public int getPagesize() {
        return pagesize;
    }

    public int getPagecount() {
        return pagecount;
    }

    public List<TaskDto> getContent() {
        return Collections.unmodifiableList(content);
    }

    public int[] getTasks() {
        tasks = new int[3];
        content.forEach( t -> {
            switch (t.getState()) {
                case TODO:
                    tasks[0]++;
                    break;
                case DOING:
                    tasks[1]++;
                    break;
                case DONE:
                    tasks[2]++;
                    break;
                default:
                    throw new IllegalStateException("Illegal task category.");
            }
        });
        return tasks;
    }
}
