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

package ch.fhnw.imvs.kanban.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

@Document(collection = "tasks")
public class Task implements Serializable {

    public enum Category {
        TODO,
        DOING,
        DONE
    }

    @Id
    private ObjectId id;
    @NotNull
    private String title;
    private String description;
    @NotNull
    private Category category;
    @DBRef
    private User creator;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dueDate;
    @DBRef
    private List<User> assigned;
    @DBRef
    private Set<FSFile> files;
    private ObjectId containingBoard;

    protected Task() {
        // Default constructor for spring
    }

    public Task(String title, Category category) {
        Assert.notNull(title);
        Assert.notNull(category);

        this.title = title;
        this.category = category;
        this.assigned = new ArrayList<>();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public Task setDescription(String description) {
        this.description = description;
        return this;
    }

    public Category getCategory() {
        return category;
    }

    public Task setCategory(Category category) {
        this.category = category;
        return this;
    }

    public Set<FSFile> getFiles() {
        return files;
    }

    public Date getCreationDate() {
        return new Date(creationDate.getTime());
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = new Date(creationDate.getTime());
    }

    public Date getDueDate() {
        return new Date(dueDate.getTime());
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = new Date(dueDate.getTime());
    }

    public List<User> getAssigned() {
        return assigned;
    }

    public void setAssigned(List<User> assigned) {
        this.assigned = new ArrayList<>(assigned);
    }

    public void assign(User user) {
        if (!this.assigned.contains(user)) {
            this.assigned.add(user);
        }
    }

    public void remove(User user) {
        this.assigned.remove(user);
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public ObjectId getContainingBoardId() {
        return containingBoard;
    }

    public void setContainingBoardId(ObjectId containingBoard) {
        this.containingBoard = containingBoard;
    }

    public void addFile(FSFile file) {
        if (files == null) {
            files = new HashSet<>();
        }
        files.add(file);
    }

    public void removeFile(FSFile file) {
        files.remove(file);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task) {
            Task other = (Task) obj;

            return this.id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{
                id,
                title,
                description,
                category
        });
    }
}
