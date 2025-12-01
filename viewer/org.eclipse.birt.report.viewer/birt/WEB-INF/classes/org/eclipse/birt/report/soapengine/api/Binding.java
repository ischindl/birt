package org.eclipse.birt.report.soapengine.api;

public class Binding {
	private long rptElementId;
	private long dataSetId;

	public Binding() {
	}

	public Binding(long rptElementId, long dataSetId) {
		this.rptElementId = rptElementId;
		this.dataSetId = dataSetId;
	}

	public long getRptElementId() {
		return rptElementId;
	}

	public void setRptElementId(long rptElementId) {
		this.rptElementId = rptElementId;
	}

	public long getDataSetId() {
		return dataSetId;
	}

	public void setDataSetId(long dataSetId) {
		this.dataSetId = dataSetId;
	}
}
