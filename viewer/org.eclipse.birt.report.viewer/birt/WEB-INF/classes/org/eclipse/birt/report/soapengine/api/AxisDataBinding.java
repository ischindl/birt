package org.eclipse.birt.report.soapengine.api;

public class AxisDataBinding {
	private ColumnDefinition columnData;
	private SectionDefinition sectionData;

	public AxisDataBinding() {
	}

	public AxisDataBinding(ColumnDefinition columnData, SectionDefinition sectionData) {
		this.columnData = columnData;
		this.sectionData = sectionData;
	}

	public ColumnDefinition getColumnData() {
		return columnData;
	}

	public void setColumnData(ColumnDefinition columnData) {
		this.columnData = columnData;
	}

	public SectionDefinition getSectionData() {
		return sectionData;
	}

	public void setSectionData(SectionDefinition sectionData) {
		this.sectionData = sectionData;
	}
}
