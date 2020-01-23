package com.github.quarkus.sample;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.BeforeEach;

@DBRider
public class BaseQuarkusTest {

    @BeforeEach
    @DataSet(value = "books.yml")
    public void loadBooks() {

    }
}
