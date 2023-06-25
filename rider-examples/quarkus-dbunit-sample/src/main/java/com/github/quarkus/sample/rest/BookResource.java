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
package com.github.quarkus.sample.rest;

import com.github.quarkus.sample.domain.Book;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

import static java.util.Optional.ofNullable;
import static jakarta.transaction.Transactional.TxType.REQUIRED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.noContent;
import static jakarta.ws.rs.core.Response.ok;
import static jakarta.ws.rs.core.Response.status;

@ApplicationScoped
@Path("books")
public class BookResource {

    private final Logger log = LoggerFactory.getLogger(BookResource.class);

    @ConfigProperty(name = "isbn.prefix")
    String isbnPrefix;
    @ConfigProperty(name = "isbn.suffix")
    String isbnSuffix;

    /**
     * curl -X GET http://localhost:8080/api/books/1234 -v
     */
    @GET
    @Path("/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") final Long id) {
        log.debug("Getting the book " + id);
        return ofNullable(Book.findById(id))
                .map(Response::ok)
                .orElse(status(NOT_FOUND))
                .build();
    }

    /**
     * curl -X GET http://localhost:8080/api/books -v
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAll() {
        log.debug("Getting all the books");
        return ok(Book.findAll().list()).build();
    }

    /**
     * curl -X POST http://localhost:8080/api/books -H "Content-Type:
     * application/json" -d '{"author":"Douglas Adams", "title":"H2G2",
     * "year":"1979", "genre":"sci-fi"}' -v
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(REQUIRED)
    public Response create(Book book, @Context UriInfo uriInfo) {
        log.debug("Creating the book " + book);
        if (book.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        String isbn = isbnPrefix + "-" + (int) (Math.random() * 1000) + "-" + isbnSuffix;
        book.setIsbn(isbn);

        Book.persist(book);

        URI createdURI = uriInfo.getAbsolutePathBuilder().path(String.valueOf(book.getId())).build();
        log.info("Created book URI " + createdURI);
        return Response.created(createdURI).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response update(Book book) {
        log.debug("Updating the book " + book);
        book.persist();
        return ok(book).build();
    }

    @DELETE
    @Path("/{id : \\d+}")
    public Response delete(@PathParam("id") final Long id) {
        log.debug("Deleting the book " + id);
        Book.deleteById(id);
        return noContent().build();
    }
}
