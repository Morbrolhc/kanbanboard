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

package ch.fhnw.imvs.kanban.web;

import ch.fhnw.imvs.kanban.dto.ResultDto;
import ch.fhnw.imvs.kanban.exception.BoardNotFoundException;
import ch.fhnw.imvs.kanban.exception.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionControllerAdvice {

    private static Logger log = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({TaskNotFoundException.class, BoardNotFoundException.class, UsernameNotFoundException.class})
    @ResponseBody
    ResultDto handleNotFound(HttpServletRequest req, Exception ex) {
        return new ResultDto(ex.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({AccessDeniedException.class, LockedException.class,
            DisabledException.class, CredentialsExpiredException.class})
    @ResponseBody
    ResultDto handleUserException(HttpServletRequest req, Exception ex) {
        return new ResultDto(ex.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({IllegalStateException.class})
    @ResponseBody
    ResultDto handleConflict(HttpServletRequest req, Exception ex) {
        return new ResultDto(ex.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseBody
    ResultDto handleBadRequest(HttpServletRequest req, Exception ex) {
        return new ResultDto(ex.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({NullPointerException.class})
    @ResponseBody
    ResultDto handleNullPointer(HttpServletRequest req, Exception ex) {
        log.error("Null Pointer, this should never happen! Check implementations. The stacktrace of this error: ", ex);
        return new ResultDto("Bad request");
    }


}
