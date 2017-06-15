package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Created by salashnik on 6/15/17.
 */
@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
public class WithoutDataSetIt {

    @Test
    public void shouldNotFall() {
    }

}