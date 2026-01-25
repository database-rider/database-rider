package com.github.database.rider.cdi.api;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Named;

public class RiderPUAnnotation extends AnnotationLiteral<RiderPU> implements RiderPU {

    private final String value;

    public RiderPUAnnotation(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
