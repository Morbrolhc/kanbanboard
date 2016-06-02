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

package ch.fhnw.imvs.kanban.config;

import ch.fhnw.imvs.kanban.filter.StatelessAuthenticationFilter;
import ch.fhnw.imvs.kanban.security.EntryPointUnauthorizedHandler;
import ch.fhnw.imvs.kanban.service.TokenAuthenticationService;
import ch.fhnw.imvs.kanban.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * This class defines the security used for incoming request with pattern matching and sets the security headers.
 */
@EnableWebSecurity
@Configuration
@Order(1)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    private static Logger log = LoggerFactory.getLogger(SpringSecurityConfig.class);
    @Value("${api.prefix}")
    private String apiPrefix;
    @Autowired
    private EntryPointUnauthorizedHandler unauthorizedHandler;
    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;
    @Autowired
    private UserService userService;

    public SpringSecurityConfig() {
        // Disable default
        super(true);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        Assert.notNull(apiPrefix);
        Assert.notNull(tokenAuthenticationService);
        log.debug("Api prefix: {}", apiPrefix);
        http
                .httpBasic().and()

                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .anonymous().and()
                .servletApi().and()

                .authorizeRequests()
                // allow anonymous resource requests
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/**/*.html").permitAll()
                .antMatchers("/**/*.css").permitAll()
                .antMatchers("/**/*.js").permitAll()
                .antMatchers("/**/*.json").permitAll()

                //allow anonymous resource requests
                .antMatchers("/").permitAll()

                //allow anonymous POSTs to login
                .antMatchers(HttpMethod.POST, apiPrefix + "/login").permitAll()

                //allow anonymous POSTs to register
                .antMatchers(HttpMethod.POST, apiPrefix + "/users").permitAll()
                //allow anonymous POSTs to reset password
                .antMatchers(HttpMethod.POST, apiPrefix + "/users/resetPassword").permitAll()
                .antMatchers(HttpMethod.POST, apiPrefix + "/users/**/resetPassword").permitAll()
                .antMatchers(HttpMethod.GET, apiPrefix + "/users/**/resetPassword").permitAll()
                .antMatchers(HttpMethod.POST, apiPrefix + "/users/**/activate").permitAll()

                .and()
//                .addFilterBefore(new StatelessLoginFilter(apiPrefix + "/login2", tokenAuthenticationService, userService, authenticationManager()), UsernamePasswordAuthenticationFilter.class)

                // Custom Token based authentication
                .addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return userService;
    }

    /**
     * Creates a secure random instance to use. It is singleton because it is expensive to initialize, but thread
     * safe. For a more secure setup to get even less predictable numbers it is recommended to periodically reseed
     * the random generator.
     *
     * @return the SecureRandom object.
     */
    @Bean
    @Scope("singleton")
    public SecureRandom initializeSecureRandom() {

        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            log.error("RNG provider not found. This should never happen", e);
            throw new InternalError("RNG provider not found. This should never happen");
        }
    }
}
