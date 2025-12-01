package org.eclipse.birt.report.soapengine.endpoint;

public class BirtSoapException extends RuntimeException {
    private final String faultCode;

    public BirtSoapException(String faultCode, String message) {
        super(message);
        this.faultCode = faultCode;
    }

    public String getFaultCode() {
        return faultCode;
    }
}