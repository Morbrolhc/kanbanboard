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

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.LinkedList;
import java.util.List;

/**
 * preparation for advanced mapping
 */
@Configuration
@PropertySource("classpath:application.properties")
public class SpringMongoConfig extends AbstractMongoConfiguration {

    private static Logger log = LoggerFactory.getLogger(SpringSecurityConfig.class);

    @Value("${spring.data.mongodb.host}")
    private String hostname;

    @Value("${spring.data.mongodb.database}")
    private String dbName;

    @Value("${spring.data.mongodb.username}")
    private String dbUserName;

    @Value("${spring.data.mongodb.password}")
    private String dbPasswd;

    @Value("${spring.data.mongodb.authentication-database}")
    private String dbAuthDB;

    @Value("${spring.data.mongodb.port}")
    private int dbPort;

    @Value("${mongodb.authentication.enabled}")
    private boolean authenticate;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    protected String getDatabaseName() {
        return dbName;
    }

    @Override
    public Mongo mongo() throws Exception {
        if(!authenticate) {
            //Allows testing with a database without users and authentication configured.
            //should not be used in production.
            log.warn("MongoDB is accessed without any authentication. " +
                    "This is strongly discouraged and should only be used in a testing environment.");
            return new MongoClient(hostname);
        } else {
            List<MongoCredential> cred = new LinkedList<>();
            cred.add(MongoCredential.createCredential(dbUserName, dbAuthDB, dbPasswd.toCharArray()));
            ServerAddress addr = new ServerAddress(hostname, dbPort);
            return new MongoClient(addr, cred);
        }
    }

    @Override
    public String getMappingBasePackage() {
        return "ch.fhnw.imvs.kanban.model";
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
    }

}
