package com.github.database.rider.core.util;

import java.util.List;

public class DummyContainsErrorMessage implements ContainsErrorMessage {

    static final ContainsErrorMessage INSTANCE = new DummyContainsErrorMessage();

    @Override
    public void initWithValues(List<Object> values) {

    }

    @Override
    public void addTableHeader() {

    }

    @Override
    public void addRow(int row) {

    }

    @Override
    public void addFail(int column, int row) {

    }

    @Override
    public void setMatch() {

    }

    @Override
    public void nextLine() {

    }

    @Override
    public void print() {

    }

}
