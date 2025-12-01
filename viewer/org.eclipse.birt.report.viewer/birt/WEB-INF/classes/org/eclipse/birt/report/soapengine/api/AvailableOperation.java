package org.eclipse.birt.report.soapengine.api;

/**
 * AvailableOperation - simplified DTO, SOAP-free.
 */
public class AvailableOperation {

	private Boolean saveView;
	private Boolean applyView;
	private Boolean print;
	private Boolean export;
	private Boolean toc;
	private Boolean undo;
	private Boolean redo;
	private Boolean sortAsc;
	private Boolean sortDsc;
	private Boolean advancedSort;
	private Boolean addGroup;
	private Boolean deleteGroup;
	private Boolean hideColumn;
	private Boolean showColumns;
	private Boolean reorderColumns;
	private Boolean filter;
	private Boolean calculation;
	private Boolean aggregation;
	private Boolean changeFont;
	private Boolean format;
	private Boolean text;
	private Boolean alignLeft;
	private Boolean alignCenter;
	private Boolean alignRight;

	// Konštruktory
	public AvailableOperation() {
	}

	// Get/Set metódy
	public Boolean getSaveView() {
		return saveView;
	}

	public void setSaveView(Boolean saveView) {
		this.saveView = saveView;
	}

	public Boolean getApplyView() {
		return applyView;
	}

	public void setApplyView(Boolean applyView) {
		this.applyView = applyView;
	}

	public Boolean getPrint() {
		return print;
	}

	public void setPrint(Boolean print) {
		this.print = print;
	}

	public Boolean getExport() {
		return export;
	}

	public void setExport(Boolean export) {
		this.export = export;
	}

	public Boolean getToc() {
		return toc;
	}

	public void setToc(Boolean toc) {
		this.toc = toc;
	}

	public Boolean getUndo() {
		return undo;
	}

	public void setUndo(Boolean undo) {
		this.undo = undo;
	}

	public Boolean getRedo() {
		return redo;
	}

	public void setRedo(Boolean redo) {
		this.redo = redo;
	}

	public Boolean getSortAsc() {
		return sortAsc;
	}

	public void setSortAsc(Boolean sortAsc) {
		this.sortAsc = sortAsc;
	}

	public Boolean getSortDsc() {
		return sortDsc;
	}

	public void setSortDsc(Boolean sortDsc) {
		this.sortDsc = sortDsc;
	}

	public Boolean getAdvancedSort() {
		return advancedSort;
	}

	public void setAdvancedSort(Boolean advancedSort) {
		this.advancedSort = advancedSort;
	}

	public Boolean getAddGroup() {
		return addGroup;
	}

	public void setAddGroup(Boolean addGroup) {
		this.addGroup = addGroup;
	}

	public Boolean getDeleteGroup() {
		return deleteGroup;
	}

	public void setDeleteGroup(Boolean deleteGroup) {
		this.deleteGroup = deleteGroup;
	}

	public Boolean getHideColumn() {
		return hideColumn;
	}

	public void setHideColumn(Boolean hideColumn) {
		this.hideColumn = hideColumn;
	}

	public Boolean getShowColumns() {
		return showColumns;
	}

	public void setShowColumns(Boolean showColumns) {
		this.showColumns = showColumns;
	}

	public Boolean getReorderColumns() {
		return reorderColumns;
	}

	public void setReorderColumns(Boolean reorderColumns) {
		this.reorderColumns = reorderColumns;
	}

	public Boolean getFilter() {
		return filter;
	}

	public void setFilter(Boolean filter) {
		this.filter = filter;
	}

	public Boolean getCalculation() {
		return calculation;
	}

	public void setCalculation(Boolean calculation) {
		this.calculation = calculation;
	}

	public Boolean getAggregation() {
		return aggregation;
	}

	public void setAggregation(Boolean aggregation) {
		this.aggregation = aggregation;
	}

	public Boolean getChangeFont() {
		return changeFont;
	}

	public void setChangeFont(Boolean changeFont) {
		this.changeFont = changeFont;
	}

	public Boolean getFormat() {
		return format;
	}

	public void setFormat(Boolean format) {
		this.format = format;
	}

	public Boolean getText() {
		return text;
	}

	public void setText(Boolean text) {
		this.text = text;
	}

	public Boolean getAlignLeft() {
		return alignLeft;
	}

	public void setAlignLeft(Boolean alignLeft) {
		this.alignLeft = alignLeft;
	}

	public Boolean getAlignCenter() {
		return alignCenter;
	}

	public void setAlignCenter(Boolean alignCenter) {
		this.alignCenter = alignCenter;
	}

	public Boolean getAlignRight() {
		return alignRight;
	}

	public void setAlignRight(Boolean alignRight) {
		this.alignRight = alignRight;
	}
}
