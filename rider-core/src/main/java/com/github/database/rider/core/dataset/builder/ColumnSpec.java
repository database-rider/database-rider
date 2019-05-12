package com.github.database.rider.core.dataset.builder;

public class ColumnSpec {

	private final String name;

	public static <T> ColumnSpec of(String name) {
		return new ColumnSpec(name);
	}

	protected ColumnSpec(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

}
