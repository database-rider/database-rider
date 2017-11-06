package com.github.database.rider.core.api.dataset;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import javax.script.ScriptEngineManager;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ScriptableTableTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    @Mock
    private ITable iTable;
    
    @Mock
    private ScriptEngineManager manager;
    
    @InjectMocks
    private ScriptableTable scriptableTable; // will only use constructor injection strategy, other mocks by setUp()
    
    @Before
    public void setUp() {
        ScriptableTable.log = mock(Logger.class);
        scriptableTable.manager = manager;
    }
    
    @Test
    public void testGetValue() throws DataSetException {
        // prepare
        when(iTable.getValue(0, "category")).thenReturn("POPE:01");
        when(manager.getEngineByName(anyString())).thenReturn(null);
        
        // test
        Object value = scriptableTable.getValue(0, "category");
        
        // assert/verify
        assertEquals("POPE:01", value);
        verify(ScriptableTable.log).warning("Could not find script engine by name 'POPE'");
        verify(manager).getEngineByName("POPE");
        verify(iTable).getValue(0, "category");
        verifyNoMoreInteractions(ScriptableTable.log, manager, iTable);
    }

}
