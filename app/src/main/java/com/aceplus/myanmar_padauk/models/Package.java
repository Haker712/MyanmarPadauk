package com.aceplus.myanmar_padauk.models;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Package implements Serializable {

	private String invoiceNumber;
	private String grade;
//	private int volume;
//	private int quantity;
//	private double minAmount;
//	private double maxAmount;
	private ArrayList<String> items;

	public Package(String invoiceNumber, String grade, JSONArray items) {

		this.invoiceNumber = invoiceNumber;
		this.grade = grade;
		this.items = new ArrayList<String>();
		for (int i = 0; i < items.length(); i++) {

			try {

				this.items.add(items.getString(i));
			} catch (JSONException e) {

				e.printStackTrace();
			}
		}
	}

	public String getInvoiceNumber() {

		return invoiceNumber;
	}
	public String getGrade() {
		
		return grade;
	}

	public ArrayList<String> getItems() {

		return items;
	}
}
