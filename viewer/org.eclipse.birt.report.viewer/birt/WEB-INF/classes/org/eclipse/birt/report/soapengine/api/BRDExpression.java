package org.eclipse.birt.report.soapengine.api;

public class BRDExpression {
	private String expression;
	private Boolean isValid;
	private String parserError;

	public BRDExpression() {
	}

	public BRDExpression(String expression, Boolean isValid, String parserError) {
		this.expression = expression;
		this.isValid = isValid;
		this.parserError = parserError;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	public String getParserError() {
		return parserError;
	}

	public void setParserError(String parserError) {
		this.parserError = parserError;
	}
}
