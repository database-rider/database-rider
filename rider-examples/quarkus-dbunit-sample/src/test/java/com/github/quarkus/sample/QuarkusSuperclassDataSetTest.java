/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.quarkus.sample;


import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import com.github.quarkus.sample.domain.Book;
import io.quarkus.test.junit.QuarkusTest;
import org.dbunit.dataset.IDataSet;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@QuarkusTest
@DBRider
public class QuarkusSuperclassDataSetTest extends BaseQuarkusTest {

    @Inject
    BookRepository repository;

    @Test
    public void shouldFindAllBooks() {
        List<Book> books = repository.findAll();
        assertThat(books)
                .isNotNull()
                .hasSize(4)
                .extracting("title")
                .contains("H2G2","Dune", "Nineteen Eighty-Four", "The Silmarillion");
    }

    @Test
    public void shouldFindAllBooksViaRestApi() {
        given()
             .when().get("/api/books")
             .then()
             .statusCode(OK.getStatusCode())
             .body("", hasSize(4))
             .body("title", hasItem("The Silmarillion"));
    }

    @Test
    @DataSet("book-empty.yml") //this overrides superclass dataset
    public void shouldCreateBook() {
        final Book book = new Book("Joshua Bloch", "Effective Java (2nd Edition)", 2001, "Tech", "978-0-3213-5668-0");

        Book bookeCreated = repository.create(book);
        assertThat(bookeCreated.getId())
                .isNotNull();
        assertThat(bookeCreated)
                .extracting("isbn","title")//isbn is changed with prefix only on rest api
                .contains("978-0-3213-5668-0","Effective Java (2nd Edition)");
    }

    @Test
    @DataSet(provider = QuarkusDBUnitTest.BookDataSetProvider.class)
    public void shouldFindBookById() {
        Book book = repository.findById(1L);
        assertThat(book)
                .isNotNull()
                .extracting("title")
                .isEqualTo("DBrider loves Quarkus!");
    }

    @Test
    @DataSet("book-empty.yml")
    @ExpectedDataSet("book-expected.yml")
    public void shouldCreateBookViaRestApi() {
        final Book book = new Book("Joshua Bloch", "Effective Java (2nd Edition)", 2001, "Tech", " 978-0-3213-5668-0");

        given()
                .body(book)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("/api/books")
                .then()
                .statusCode(CREATED.getStatusCode());
    }


    public static class BookDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("BOOK")
                    .row()
                    .column("ID", 1)
                    .column("AUTHOR", "DBrider")
                    .column("GENRE", "Tech")
                    .column("TITLE", "DBrider loves Quarkus!")
                    .column("YEAR", 2019);

            return builder.build();
        }
    }
}
