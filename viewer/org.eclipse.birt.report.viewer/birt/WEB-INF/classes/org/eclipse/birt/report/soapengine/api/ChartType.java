package org.eclipse.birt.report.soapengine.api;

import java.io.Serializable;

/**
 * Represents supported chart types in BIRT.
 */
public enum ChartType implements Serializable {
	VBAR("VBar"), PIE("Pie"), AREA("Area"), LINE("Line"), SCATTER("Scatter"), METER("Meter"), STOCK("Stock");

	private final String value;

	ChartType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ChartType fromValue(String value) {
		for (ChartType type : values()) {
			if (type.value.equalsIgnoreCase(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown ChartType: " + value);
	}

	@Override
	public String toString() {
		return value;
	}
}
