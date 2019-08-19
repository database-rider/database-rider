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

import com.github.quarkus.sample.domain.Book;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
public class BookRepository {


    @Inject
    EntityManager entityManager;

 
    @Transactional(SUPPORTS)
    public Book findById(final Long id) {
        return entityManager.find(Book.class, id);
    }

    @Transactional(SUPPORTS)
    public List<Book> findAll() {
        return entityManager.createQuery("SELECT m FROM Book m", Book.class).getResultList();
    }

    @Transactional
    public Book create(final Book book) {
        entityManager.persist(book);
        return book;
    }

    @Transactional
    public Book update(final Book book) {
        return entityManager.merge(book);
    }

    @Transactional
    public void deleteById(final Long id) {
        Optional.ofNullable(entityManager.getReference(Book.class, id)).ifPresent(entityManager::remove);
    }
}
