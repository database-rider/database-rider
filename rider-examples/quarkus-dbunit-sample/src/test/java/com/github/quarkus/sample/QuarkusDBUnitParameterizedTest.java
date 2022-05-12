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


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.quarkus.sample.domain.Book;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@DBRider
@DBUnit(schema = "public", caseSensitiveTableNames = true, cacheConnection = false)
@DataSet(value = "books.yml")
public class QuarkusDBUnitParameterizedTest {

    @ParameterizedTest
    @CsvSource({"2001,Douglas Adams", "2002,Frank Herbert"})
    public void shouldFindBookByTitle(String id, String title) {
        Book book = Book.findById(Long.parseLong(id));
        assertThat(book)
                .isNotNull()
                .extracting(Book::getAuthor)
                .isEqualTo(title);
    }

    @ParameterizedTest
    @ValueSource(longs = {4004})
    public void shouldFindBookById(Long id) {
        Book book = Book.findById(id);
        assertThat(book)
                .isNotNull()
                .extracting(Book::getAuthor)
                .isEqualTo("J. R. R. Tolkien");
    }
}
