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

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;

import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import com.github.quarkus.sample.domain.Book;

import org.dbunit.dataset.IDataSet;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QuarkusSuperclassDataSetTest extends BaseQuarkusTest {

    @Test
    public void shouldFindAllBooks() {
        List<Book> books = Book.findAll().list();
        assertThat(books)
                .isNotNull()
                .hasSize(4)
                .extracting("title")
                .contains("H2G2", "Dune", "Nineteen Eighty-Four", "The Silmarillion");
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
    @DataSet("book-empty.yml") // this overrides dataset declared in superclass beforeEach because CLEAN_INSERT
                               // on the same table
    @Transactional
    public void shouldCreateBook() {
        final Book bookeCreated = new Book("Joshua Bloch", "Effective Java (2nd Edition)", 2001, "Tech",
                "978-0-3213-5668-0");
        bookeCreated.persist();
        assertThat(bookeCreated.getId())
                .isNotNull();
        assertThat(bookeCreated)
                .extracting("isbn", "title")// isbn is changed with prefix only on rest api
                .contains("978-0-3213-5668-0", "Effective Java (2nd Edition)");
    }

    @Test
    @DataSet(provider = QuarkusDBUnitTest.BookDataSetProvider.class)
    public void shouldFindBookById() {
        Book book = Book.findById(1L);
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
            DBUnitConfig config = new DBUnitConfig();
            config.cacheConnection(false);
            config.setCaseInsensitiveStrategy(Orthography.LOWERCASE);
            DataSetBuilder builder = new DataSetBuilder(config);
            builder.table("book")
                    .row()
                    .column("id", 1)
                    .column("author", "DBrider")
                    .column("genre", "Tech")
                    .column("title", "DBrider loves Quarkus!")
                    .column("year", 2019);

            return builder.build();
        }
    }
}
