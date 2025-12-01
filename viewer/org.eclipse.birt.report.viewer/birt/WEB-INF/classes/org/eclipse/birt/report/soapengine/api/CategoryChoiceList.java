package org.eclipse.birt.report.soapengine.api;

import java.io.Serializable;
import java.util.Arrays;

public class CategoryChoiceList implements Serializable {
	private static final long serialVersionUID = 1L;

	private CategoryChoice[] categoryChoice;

	public CategoryChoiceList() {
	}

	public CategoryChoiceList(CategoryChoice[] categoryChoice) {
		this.categoryChoice = categoryChoice;
	}

	public CategoryChoice[] getCategoryChoice() {
		return categoryChoice;
	}

	public void setCategoryChoice(CategoryChoice[] categoryChoice) {
		this.categoryChoice = categoryChoice;
	}

	public CategoryChoice getCategoryChoice(int i) {
		return categoryChoice[i];
	}

	public void setCategoryChoice(int i, CategoryChoice value) {
		categoryChoice[i] = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CategoryChoiceList))
			return false;
		CategoryChoiceList that = (CategoryChoiceList) o;
		return Arrays.equals(categoryChoice, that.categoryChoice);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(categoryChoice);
	}

	@Override
	public String toString() {
		return "CategoryChoiceList{" + "categoryChoice=" + Arrays.toString(categoryChoice) + '}';
	}
}
