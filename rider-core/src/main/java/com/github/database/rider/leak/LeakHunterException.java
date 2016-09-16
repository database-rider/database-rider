package com.github.database.rider.leak;

/**
 * Created by pestano on 07/09/16.
 */
public class LeakHunterException extends RuntimeException {


    public LeakHunterException(String methodName, int numberOfConnections) {
        super(String.format("Execution of method %s left %d open connection(s).",methodName,numberOfConnections));
    }
}
