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

import ch.fhnw.imvs.kanban.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.util.Arrays;

public class UserDto {

    private String username;
    private String displayname;
    private String email;

    public UserDto(User user) {
        Assert.notNull(user.getUsername());
        Assert.notNull(user.getDisplayname());
        Assert.notNull(user.getEmail());

        this.username = user.getUsername();
        this.displayname = user.getDisplayname();
        this.email = user.getEmail();
    }

    public UserDto(String username, String displayname, String email) {
        Assert.notNull(username);
        Assert.notNull(displayname);
        Assert.notNull(email);

        this.username = username;
        this.displayname = displayname;
        this.email = email;
    }

    protected UserDto() {
        // Default constructor for Spring
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public String getDisplayname() {
        return displayname;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserDto) {
            UserDto other = (UserDto) obj;
            return this.username.equals(other.username) && this.email.equals(other.email) && this.displayname.equals(other.displayname);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{
                username,
                displayname,
                email
        });
    }

    @Override
    public String toString() {
        return "UserDto: username: " + username +
                " displayname: " + displayname +
                " email: " + email;
    }
}
