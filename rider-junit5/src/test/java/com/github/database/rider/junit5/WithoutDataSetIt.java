package com.github.database.rider.junit5;

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