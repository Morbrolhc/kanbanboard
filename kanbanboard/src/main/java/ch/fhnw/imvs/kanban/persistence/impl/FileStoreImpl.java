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

package ch.fhnw.imvs.kanban.persistence.impl;

import ch.fhnw.imvs.kanban.config.SpringMongoConfig;
import ch.fhnw.imvs.kanban.model.FSFile;
import ch.fhnw.imvs.kanban.persistence.FileStore;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Created by joel on 20.03.16.
 */
@Service
public class FileStoreImpl implements FileStore {

    /**
     * Method to store a file within MongoDB's GridFS.
     * @param inputStream containing the file to store.
     * @param fileName the file name under which the file is to be saved.
     * @param contentType a mime type describing the contents of the file.
     * @return a GridFSDBFile object on which an InputStream can be opened.
     */
    public FSFile store(InputStream inputStream, String fileName, String contentType) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class)) {
            GridFsOperations gridOperations =
                    (GridFsOperations) ctx.getBean("gridFsTemplate");
            GridFSFile file = gridOperations.store(inputStream, fileName, contentType);
            return new FSFile((ObjectId) file.getId(), fileName, contentType, file.getLength());
        }
    }

    public GridFSDBFile read(FSFile fsFile) {
        try (AnnotationConfigApplicationContext ctx =  new AnnotationConfigApplicationContext(SpringMongoConfig.class)) {
            GridFsOperations gridOperations =
                    (GridFsOperations) ctx.getBean("gridFsTemplate");
            return gridOperations.findOne(Query.query(Criteria.where("_id").is(fsFile.getId())));
        }

    }

    public void delete(FSFile fsFile) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class)) {
            GridFsOperations gridOperations =
                    (GridFsOperations) ctx.getBean("gridFsTemplate");
            gridOperations.delete(Query.query(Criteria.where("_id").is(fsFile.getId())));
        }
    }

}
