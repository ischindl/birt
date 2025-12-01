package org.eclipse.birt.report.soapengine.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BoundDataColumnList implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<BoundDataColumn> boundDataColumns = new ArrayList<>();

	public BoundDataColumnList() {
	}

	public BoundDataColumnList(List<BoundDataColumn> boundDataColumns) {
		this.boundDataColumns = boundDataColumns;
	}

	public List<BoundDataColumn> getBoundDataColumns() {
		return boundDataColumns;
	}

	public void setBoundDataColumns(List<BoundDataColumn> boundDataColumns) {
		this.boundDataColumns = boundDataColumns;
	}

	public void addBoundDataColumn(BoundDataColumn column) {
		this.boundDataColumns.add(column);
	}

	public BoundDataColumn get(int index) {
		return this.boundDataColumns.get(index);
	}

	public void set(int index, BoundDataColumn column) {
		this.boundDataColumns.set(index, column);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof BoundDataColumnList))
			return false;
		BoundDataColumnList that = (BoundDataColumnList) o;
		return Objects.equals(boundDataColumns, that.boundDataColumns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(boundDataColumns);
	}

	@Override
	public String toString() {
		return "BoundDataColumnList{" + "boundDataColumns=" + boundDataColumns + '}';
	}
}
