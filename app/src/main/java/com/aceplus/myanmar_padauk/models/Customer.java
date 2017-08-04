package com.aceplus.myanmar_padauk.models;

import java.io.Serializable;

public class Customer implements Serializable {

	private String customerId;
	private String customerName;
	private String customerTypeId;
	private String customerTypeName;
	private String address;
	private String phone;
	private String township;
	private String creditTerms;
	private double creditLimit;
	private double creditAmt;
	private double dueAmt;
	private double prepaidAmt;
	private String paymentType;
	private boolean isInRoute;

	public Customer(String customerId, String customerName, String customerTypeId, String customerTypeName, String address
			, String phone, String township, String creditTerms, double creditLimit, double creditAmt, double dueAmt
			, double prepaidAmt, String paymentType, String isInRoute) {
		
		this.customerId = customerId;
		this.customerName = customerName;
		this.customerTypeId = customerTypeId;
		this.customerTypeName = customerTypeName;
		this.address = address;
		this.phone = phone;
		this.township = township;
		this.creditTerms = creditTerms;
		this.creditLimit = creditLimit;
		this.creditAmt = creditAmt;
		this.dueAmt = dueAmt;
		this.prepaidAmt = prepaidAmt;
		this.paymentType = paymentType;
		this.isInRoute = isInRoute.equalsIgnoreCase("true");
	}

	public Customer(String customerName, String address){
		this.customerName = customerName;
		this.address = address;
	}

	public String getCustomerId() {
		return customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getCustomerTypeId() {
		return customerTypeId;
	}

	public String getCustomerTypeName() {
		return customerTypeName;
	}

	public String getAddress() {
		return address;
	}

	public String getPhone() {
		return phone;
	}

	public String getTownship() {
		return township;
	}

	public String getCreditTerms() {
		return creditTerms;
	}

	public double getCreditLimit() {
		return creditLimit;
	}

	public double getCreditAmt() {
		return creditAmt;
	}

	public double getDueAmt() {
		return dueAmt;
	}

	public double getPrepaidAmt() {
		return prepaidAmt;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public boolean isInRoute() {
		return isInRoute;
	}
}
