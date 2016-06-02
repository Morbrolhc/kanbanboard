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

package ch.fhnw.imvs.kanban.security;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public final class TokenHandler {

	private static final long ONE_MONTH = 2592000000L;

	private static final String languageClaim = "language";

	@Value("${token.secret}")
	private String secret;

	@Value("${kanbanboard.hostname}")
	private String hostname;

	@Autowired
	private UserService userService;

	public TokenHandler() {
		// Default constructor for Spring
	}

	public User parseUserFromToken(String token) {
		String username = Jwts.parser()
				.setSigningKey(secret)
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
		return userService.loadUserByUsername(username);
	}

	public String parseLanguageFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().get(languageClaim, String.class);
	}

	public String createTokenForUser(User user) {
		Claims claims = Jwts.claims()
				.setIssuer(hostname)
				.setSubject(user.getUsername());
		claims.put("displayname", user.getDisplayname() + "");
		claims.put(languageClaim, user.getLanguage());
		claims.put("email", user.getEmail());
		claims.put("role", user.getAuthorities());

		Date date = new Date(System.currentTimeMillis() + ONE_MONTH);

		return Jwts.builder()
				.setSubject(user.getUsername())
				.setClaims(claims)
				.signWith(SignatureAlgorithm.HS256, secret)
				.setExpiration(date)
				.compact();
	}
}
