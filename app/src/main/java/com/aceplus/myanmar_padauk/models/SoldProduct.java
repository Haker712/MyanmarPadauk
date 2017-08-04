package com.aceplus.myanmar_padauk.models;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aceplus.myanmar_padauk.utils.Database;

public class SoldProduct implements Serializable {

	private static final long serialVersionUID = 1L;

	private Product product;
	private int quantity;
	private double discount;

	private boolean isForPackage;
	private double extraDiscount;

	private ArrayList<String> serialList;

	private int orderedQuantity;

    private double itemDiscountAmt;
    private double itemDiscountPercent;

    public double getItemDiscountAmt() {
        return itemDiscountAmt;
    }

    public void setItemDiscountAmt(double itemDiscountAmt) {
        this.itemDiscountAmt = itemDiscountAmt;
    }

    public double getItemDiscountPercent() {
        return itemDiscountPercent;
    }

    public void setItemDiscountPercent(double itemDiscountPercent) {
        this.itemDiscountPercent = itemDiscountPercent;
    }

    public SoldProduct(Product product, Boolean isForPackage) {
		
		this.product = product;
		quantity = 0;
		discount = 0;

		this.isForPackage = isForPackage;
		extraDiscount = 0;

		orderedQuantity = 0;
	}
	
	public Product getProduct() {
		
		return product;
	}
	
	public int getQuantity() {
		
		return quantity;
	}
	
	public boolean setQuantity(int quantity) {

		if (quantity > 0) {

			product.setSoldQty(-this.quantity);
		}

		product.setSoldQty(quantity);
//		if (!product.setSoldQty(quantity)) {
//
//			return false;
//		}

		this.quantity = quantity;

		return true;
	}

	public void setDiscount(double discount) {
		
		if (discount >= 0 && discount <= 100) {

			this.discount = discount;
		}
	}
	
	public double getDiscount(Activity context) {

		if (discount != 0) {

			return discount;
		}

		SQLiteDatabase db = new Database(context).getDataBase();
		
		String sql = null;

		if (product.getDiscountType().equalsIgnoreCase("i")) {
			
			sql = "SELECT DISCOUNT_PERCENT FROM ITEM_DISCOUNT WHERE STOCK_NO = '" + product.getId() + "' AND START_DISCOUNT_QTY <= " + quantity + " AND END_DISCOUNT_QTY >= " + quantity;
		}
        //sql = "SELECT DISCOUNT_PERCENT FROM ITEM_DISCOUNT WHERE STOCK_NO = '" + product.getId() + "' AND START_DISCOUNT_QTY <= " + quantity + " AND END_DISCOUNT_QTY >= " + quantity;

		if (sql != null) {
			
			Cursor cursor = db.rawQuery(sql, null);
			System.out.println(cursor.getCount());
			if (cursor.getCount() == 1) {
				
				cursor.moveToNext();
				return cursor.getDouble(cursor.getColumnIndex("DISCOUNT_PERCENT"));
			}
		}
		
		return 0;
	}

	public boolean isForPackage() {
		
		return isForPackage;
	}

	public void setForPackage(boolean isForPackage) {

		this.isForPackage = isForPackage;
	}

	public void setSerialList(ArrayList<String> serialList) {
		
		this.serialList = serialList;
	}

	public ArrayList<String> getSerialList() {

		if (serialList == null) {

			serialList = new ArrayList<String>();
		}

		return serialList;
	}

	public void setExtraDiscount(double extraDiscount) {

		if (extraDiscount >= 0) {

			this.extraDiscount = extraDiscount;
		}
	}

	public double getExtraDiscount() {
		
		return extraDiscount;
	}

	public double getTotalAmount() {

		return product.getPrice() * quantity;
	}

	public double getDiscountAmount(Activity context) {

		return getTotalAmount() * getDiscount(context) / 100;
	}

	public double getExtraDiscountAmount() {

		return getTotalAmount() * extraDiscount / 100;
	}

	public double getNetAmount(Activity context) {

		return getTotalAmount() - getDiscountAmount(context) - getExtraDiscountAmount();
	}

	public void setOrderedQuantity(int orderedQuantity) {

		this.orderedQuantity = orderedQuantity;
	}

	public int getOrderedQuantity() {

		return this.orderedQuantity;
	}
}
