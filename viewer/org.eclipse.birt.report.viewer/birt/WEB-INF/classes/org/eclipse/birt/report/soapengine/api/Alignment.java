package org.eclipse.birt.report.soapengine.api;

/**
 * Simplified alignment enum (SOAP-free version).
 */
public enum Alignment {
	LEFT("left"), RIGHT("right"), CENTER("center");

	private final String value;

	Alignment(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Alignment fromValue(String value) {
		for (Alignment a : values()) {
			if (a.value.equalsIgnoreCase(value)) {
				return a;
			}
		}
		throw new IllegalArgumentException("Unknown alignment: " + value);
	}

	@Override
	public String toString() {
		return value;
	}
}
