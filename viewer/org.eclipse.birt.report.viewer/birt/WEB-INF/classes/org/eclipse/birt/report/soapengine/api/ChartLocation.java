package org.eclipse.birt.report.soapengine.api;

import java.io.Serializable;

public enum ChartLocation implements Serializable {
	Above, Blow, Left, Right;

	@Override
	public String toString() {
		return name();
	}

	public static ChartLocation fromValue(String value) {
		try {
			return ChartLocation.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown ChartLocation value: " + value, e);
		}
	}
}
