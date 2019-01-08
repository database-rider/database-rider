package com.github.database.rider.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.database.rider.core.api.configuration.DBUnit;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class AnnotationUtilsTest {
	
	
	@Test
	public void shouldFindDBUnitAnnotationOnSuperClass() {
		DBUnit dbUnit = AnnotationUtils.findAnnotation(MyTest.class, DBUnit.class);
		assertThat(dbUnit).isNotNull();
		assertThat(dbUnit.caseSensitiveTableNames()).isTrue();
	}

	@DBUnit(caseSensitiveTableNames = true)
	public abstract class MyBase {
	}
	
	public class MyTest extends MyBase {
	    public void myTest() {
	    }
	}
}


