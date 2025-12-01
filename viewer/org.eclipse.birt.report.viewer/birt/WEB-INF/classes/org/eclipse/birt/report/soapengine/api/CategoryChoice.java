package org.eclipse.birt.report.soapengine.api;

import java.io.Serializable;
import java.util.Objects;

public class CategoryChoice implements Serializable {
	private static final long serialVersionUID = 1L;

	private String category;
	private String pattern;

	public CategoryChoice() {
	}

	public CategoryChoice(String category, String pattern) {
		this.category = category;
		this.pattern = pattern;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CategoryChoice))
			return false;
		CategoryChoice that = (CategoryChoice) o;
		return Objects.equals(category, that.category) && Objects.equals(pattern, that.pattern);
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, pattern);
	}

	@Override
	public String toString() {
		return "CategoryChoice{" + "category='" + category + '\'' + ", pattern='" + pattern + '\'' + '}';
	}
}
