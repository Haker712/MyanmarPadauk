package com.aceplus.myanmar_padauk.models;

public class CustomerFeedback {

	private String invoiceNumber;
	private String invoiceDate;
	private String serialNumber;
	private String description;

	public CustomerFeedback(String invoiceNumber, String invoiceDate, String serialNumber, String description) {
		
		this.invoiceNumber = invoiceNumber;
		this.invoiceDate = invoiceDate;
		this.serialNumber = serialNumber;
		this.description = description;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getDescription() {
		return description;
	}
}
