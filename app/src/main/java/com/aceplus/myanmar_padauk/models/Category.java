package com.aceplus.myanmar_padauk.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Category implements Serializable {

	private String id, name;
	private Product[] products;
	
	public Category(String id, String name) {

		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		
		return id;
	}
	
	public String getName() {
		
		return name;
	}
	
	public Product[] getProducts() {
		
		return products;
	}
	
	public void setProducts(Product[] products) {
		
		this.products = products;
	}
}
