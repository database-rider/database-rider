package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseLifecycleHooks {


    @BeforeEach
    @DataSet(value = "tweetBeforeEach.yml", disableConstraints = true)
    public void loadDataSetBeforeEach() {

    }

    @AfterEach
    @DataSet("tweetAfterEach.yml")
    public void loadDataSetAfterEach() {

    }

}
