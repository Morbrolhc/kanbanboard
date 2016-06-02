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

package ch.fhnw.imvs.kanban.service.impl;

import ch.fhnw.imvs.kanban.dto.UserAuthentication;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.security.TokenHandler;
import ch.fhnw.imvs.kanban.service.TokenAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

	private static final String AUTH_HEADER_NAME = "Cookie";

	private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationServiceImpl.class);

	@Autowired
	private TokenHandler tokenHandler;

	protected TokenAuthenticationServiceImpl() {
		// default constructor for Spring
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAuthentication(HttpServletResponse response, UserAuthentication authentication) {
		final User user = authentication.getDetails();
		response.addHeader(AUTH_HEADER_NAME, "token=" + tokenHandler.createTokenForUser(user));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Authentication getAuthentication(HttpServletRequest request) {
		final String token = request.getHeader(AUTH_HEADER_NAME);

		if (token == null || !token.contains("token=")) {
			//throw new JwtTokenMissingException("No JWT token found in request headers");
			log.info("No token found in message");
		} else {
			log.debug("Cookie: {}", token);
			String authToken = token.substring(token.indexOf("token=")+6).split(";")[0];
			log.debug("Extracted token: {}", authToken);


			log.trace("Parsing token: {}", token);
			final User user = tokenHandler.parseUserFromToken(authToken);
			if (user != null) {
				log.trace("User found for token: {}", user.toString());
				return new UserAuthentication(user);
			} else {
				log.warn("No user found for token {}", token);
			}
		}
		return null;
	}

	@Override
	public String getLanguage(HttpServletRequest request) {
		final String token = request.getHeader(AUTH_HEADER_NAME);

		if (token == null || !token.startsWith("token=")) {
			return "DE";
		} else {
			return tokenHandler.parseLanguageFromToken(token);
		}
	}
}
