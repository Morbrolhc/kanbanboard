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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Document(collection = "users")
public class User implements UserDetails {

	@Transient
	private static Logger log = LoggerFactory.getLogger(User.class);

	@Id
	private ObjectId id;

	@NotNull
	private String displayname;

	@NotNull
	@Indexed(unique = true)
	@Size(min = 4, max = 30)
	private String username;

	@NotNull
	@Indexed(unique = true)
	@Size(min = 7, max = 50)
	private String email;

	@NotNull
	private String password;

	@Transient
	private long expires;

	@NotNull
	private boolean accountExpired;

	@NotNull
	private boolean accountLocked;

	@NotNull
	private boolean credentialsExpired;

	@NotNull
	private boolean accountEnabled;

	@NotNull
	private String language;

	private Token activationToken;

	private Token passwordResetToken;

	private Set<UserAuthority> authorities;

	@DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
	private Date creationDate;

	protected User() {
		// Default Constructor for Spring
	}

	public User(String username, String displayname, String email) {
		this(username, displayname, email, Date.from(LocalDate.now().plusMonths(120).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
	}

	public User(String username, String displayname, String email, Date expires) {
		Assert.notNull(username);
		Assert.notNull(displayname);
		Assert.notNull(email);
		Assert.notNull(expires);

		this.username = username;
		this.displayname = displayname;
		this.email = email;
		this.expires = expires.getTime();
		this.accountExpired = false;
		this.accountLocked = false;
		this.language = "DE";

	}


	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	@JsonIgnore
	public String getPassword() {
		return password;
	}

	@JsonProperty
	public void setPassword(String password) {
		this.password = password;
	}

	@JsonProperty
	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	@Override
	@JsonIgnore
	public Set<UserAuthority> getAuthorities() {
		return authorities;
	}

	// Use Roles as external API
	public Set<UserRole> getRoles() {
		Set<UserRole> roles = EnumSet.noneOf(UserRole.class);
		if (authorities != null) {
			for (UserAuthority authority : authorities) {
				roles.add(UserRole.valueOf(authority));
			}
		}
		return roles;
	}

	public void setRoles(Set<UserRole> roles) {
		for (UserRole role : roles) {
			grantRole(role);
		}
	}

	public void grantRole(UserRole role) {
		if (authorities == null) {
			authorities = new HashSet<>();
		}
		authorities.add(role.asAuthorityFor(this));
	}

	public void revokeRole(UserRole role) {
		if (authorities != null) {
			authorities.remove(role.asAuthorityFor(this));
		}
	}

	public boolean hasRole(UserRole role) {
		return authorities.contains(role.asAuthorityFor(this));
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return !accountExpired;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return !credentialsExpired;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return accountEnabled;
	}

	@JsonIgnore
	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + getUsername();
	}

	@JsonIgnore
	public Token getPasswordResetToken() {
		return passwordResetToken;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setPasswordResetToken(Token token) {
		if (this.passwordResetToken != null) {
			log.debug("Replacing token");
		}
		this.passwordResetToken = token;
	}

	public void lockAccount() {
		this.accountLocked = true;
	}

	public void unlockAccount() {
		this.accountLocked = false;
	}

	public boolean isAccountEnabled() {
		return accountEnabled;
	}

	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	public Token getActivationToken() {
		return activationToken;
	}

	public void setActivationToken(Token activationToken) {
		this.activationToken = activationToken;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			User other = (User) obj;

			return this.id.equals(other.id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] {
				id,
				displayname,
				username,
				password
		});
	}

	public Date getCreationDate() {
		return new Date(creationDate.getTime());
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = new Date(creationDate.getTime());
	}
}
