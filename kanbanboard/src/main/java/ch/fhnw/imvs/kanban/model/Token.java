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

import org.springframework.util.Assert;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

public class Token implements Serializable {

    private String token;

    private Date expiryDate;

    /**
     * Creates a random token with validity 24 hours.
     */
    public Token() {
        final SecureRandom random = new SecureRandom();
        this.token = new BigInteger(130, random).toString(32);

        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        this.expiryDate = cal.getTime();
    }

    /**
     * Creates a token with validity 24 hours and user supplied content.
     *
     * @param token the plain token
     */
    public Token(final String token) {
        Assert.notNull(token);

        this.token = token;
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        expiryDate = cal.getTime();
    }

    /**
     * Creates a random token.
     *
     * @param expiryDate the expiry date of the token.
     */
    public Token(final Date expiryDate) {
        Assert.notNull(expiryDate);

        final SecureRandom random = new SecureRandom();
        this.token = new BigInteger(130, random).toString(32);

        this.expiryDate = new Date(expiryDate.getTime());
    }

    /**
     * Creates a token.
     *
     * @param token the plain token
     * @param expiryDate the expiry date of the token
     */
    public Token(String token, Date expiryDate) {
        Assert.notNull(token);
        Assert.notNull(expiryDate);
        this.token = token;
        this.expiryDate = new Date(expiryDate.getTime());
    }

    public String getToken() {
        return token;
    }

    public Date getExpiryDate() {
        return new Date(expiryDate.getTime());
    }
}
