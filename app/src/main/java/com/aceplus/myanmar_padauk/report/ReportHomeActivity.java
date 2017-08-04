package com.aceplus.myanmar_padauk.report;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.utils.Database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReportHomeActivity extends Activity {

	public static final String USER_INFO_KEY = "user-info-key";

	JSONObject userInfo;

	Button cancelButton;
	Spinner reportsSpinner;

	SQLiteDatabase database;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_home);

		try {

			userInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));
		} catch (JSONException e) {

			e.printStackTrace();
		}

		cancelButton = (Button) findViewById(R.id.cancel);
		reportsSpinner = (Spinner) findViewById(R.id.reports);

		database = new Database(this).getDataBase();

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				ReportHomeActivity.this.onBackPressed();
			}
		});

		final ArrayList<JSONObject> saleInvoiceReportsArrayList = new ArrayList<JSONObject>();
		final ArrayList<JSONObject> productBalanceReportsArrayList = new ArrayList<JSONObject>();
		final ArrayList<JSONObject> customerFeedbackReportsArrayList = new ArrayList<JSONObject>();
		final ArrayList<JSONObject> preOrderReportsArrayList = new ArrayList<JSONObject>();
		final ArrayList<JSONObject> deliveryReportsArrayList = new ArrayList<JSONObject>();
		final ArrayList<JSONObject> deliveredInvoiceReportsArrayList = new ArrayList<JSONObject>();

		final String[] reportNames = {
				"Product Balance Report"
				, "Sale Invoice Report"
				, "Customer Feedback Report"
				, "Pre-Order Report"
				, "Delivery Report"
				, "Delivered Invoice Report"};
		ArrayAdapter<String> reportsSpinnerAdapter
			= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reportNames);
		reportsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportsSpinner.setAdapter(reportsSpinnerAdapter);
        reportsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				if (position == 0) {


					if (productBalanceReportsArrayList.size() == 0) {
						
						for (JSONObject productBalanceReportJsonObject : getProductBalanceReports()) {

							productBalanceReportsArrayList.add(productBalanceReportJsonObject);
						}
					}

					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					FragmentProductBalancesReport productBalancesReportFragment = new FragmentProductBalancesReport();
					productBalancesReportFragment.productBalanceReportsArrayList = productBalanceReportsArrayList;
					fragmentTransaction.replace(R.id.fragment_report, productBalancesReportFragment);
					fragmentTransaction.commit();
				} else if (position == 1) {

					if (saleInvoiceReportsArrayList.size() == 0) {
						
						for (JSONObject saleInvoiceReportJsonObject : getSaleInvoiceReports()) {

							saleInvoiceReportsArrayList.add(saleInvoiceReportJsonObject);
						}
					}

					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					FragmentSaleInvoiceReport saleInvoiceFragment = new FragmentSaleInvoiceReport();
					saleInvoiceFragment.saleInvoiceReportsArrayList = saleInvoiceReportsArrayList;
					fragmentTransaction.replace(R.id.fragment_report, saleInvoiceFragment);
					fragmentTransaction.commit();
				} else if (position == 2) {

					if (customerFeedbackReportsArrayList.size() == 0) {
						
						for (JSONObject customerFeedbackReportJsonObject : getCustomerFeedbackReports()) {

							customerFeedbackReportsArrayList.add(customerFeedbackReportJsonObject);
						}
					}

					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					FragmentCustomerFeedbackReport customerFeedbackReportFragment = new FragmentCustomerFeedbackReport();
					customerFeedbackReportFragment.customerFeedbackReportsArrayList = customerFeedbackReportsArrayList;
					fragmentTransaction.replace(R.id.fragment_report, customerFeedbackReportFragment);
					fragmentTransaction.commit();
				} else if (position == 3) {

					// Used lazy loading
					if (preOrderReportsArrayList.size() == 0) {

						// Need to add implicitly because preOrderReportsArrayList is final
						for (JSONObject preOrderReportJsonObject : getPreOrderReports()) {

							preOrderReportsArrayList.add(preOrderReportJsonObject);
						}
					}

					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					FragmentPreOrderReport preOrderReportFragment = new FragmentPreOrderReport();
					preOrderReportFragment.preOrderReportsArrayList = preOrderReportsArrayList;
					fragmentTransaction.replace(R.id.fragment_report, preOrderReportFragment);
					fragmentTransaction.commit();
				} else if (position == 4) {

					// Used lazy loading
					if (deliveryReportsArrayList.size() == 0) {

						// Need to add implicitly because deliveryReportsArrayList is final
						for (JSONObject deliveryReportJsonObject : getDeliveryReports()) {

							deliveryReportsArrayList.add(deliveryReportJsonObject);
						}
					}

					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					FragmentDeliveryReport deliveryReportFragment = new FragmentDeliveryReport();
					deliveryReportFragment.deliveryReportsArrayList = deliveryReportsArrayList;
					fragmentTransaction.replace(R.id.fragment_report, deliveryReportFragment);
					fragmentTransaction.commit();
				} else if (position == 5) {

					// Used lazy loading
					if (deliveredInvoiceReportsArrayList.size() == 0) {

						// Need to add implicitly because saleInvoiceReportsArrayList is final
						for (JSONObject deliveredInvoiceReportJsonObject : getDeliveredInvoiceReports()) {

							deliveredInvoiceReportsArrayList.add(deliveredInvoiceReportJsonObject);
						}
					}

					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					FragmentSaleInvoiceReport saleInvoiceFragment = new FragmentSaleInvoiceReport();
					saleInvoiceFragment.saleInvoiceReportsArrayList = deliveredInvoiceReportsArrayList;
					fragmentTransaction.replace(R.id.fragment_report, saleInvoiceFragment);
					fragmentTransaction.commit();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	private ArrayList<JSONObject> getSaleInvoiceReports() {

		ArrayList<JSONObject> saleInvoiceReportsArrayList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery("SELECT * FROM INVOICE", null);
		while(cursor.moveToNext()) {

			JSONObject saleInvoiceReportJsonObject = new JSONObject();
			try {

				saleInvoiceReportJsonObject.put("invoiceId", cursor.getString(cursor.getColumnIndex("INVOICE_ID")));

				Cursor cursorForCustomer = database.rawQuery(
						"SELECT CUSTOMER_NAME, ADDRESS FROM CUSTOMER"
						+ " WHERE CUSTOMER_ID = \"" + cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")) + "\""
						, null);
				if (cursorForCustomer.moveToNext()) {

					saleInvoiceReportJsonObject.put("customerName"
							, cursorForCustomer.getString(cursorForCustomer.getColumnIndex("CUSTOMER_NAME")));
					saleInvoiceReportJsonObject.put("address"
							, cursorForCustomer.getString(cursorForCustomer.getColumnIndex("ADDRESS")));

					double totalAmount = cursor.getDouble(cursor.getColumnIndex("TOTAL_AMOUNT"));
					double discount = cursor.getDouble(cursor.getColumnIndex("TOTAL_DISCOUNT_AMOUNT"));
					saleInvoiceReportJsonObject.put("totalAmount", totalAmount);
					saleInvoiceReportJsonObject.put("discount", discount);
					saleInvoiceReportJsonObject.put("netAmount", totalAmount - discount);
				}
			} catch (JSONException e) {

				e.printStackTrace();
			}

			saleInvoiceReportsArrayList.add(saleInvoiceReportJsonObject);
		}

		return saleInvoiceReportsArrayList;
	}

	private ArrayList<JSONObject> getProductBalanceReports() {

		ArrayList<JSONObject> productBalanceReportsArrayList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery("SELECT * FROM PRODUCT", null);
		while(cursor.moveToNext()) {

			JSONObject productBalanceReportJsonObject = new JSONObject();
			try {

				double totalQuantity = cursor.getDouble(cursor.getColumnIndex("TOTAL_QTY"));
				double remainingQuanity = cursor.getDouble(cursor.getColumnIndex("REMAINING_QTY"));

				productBalanceReportJsonObject.put("productName"
						, cursor.getString(cursor.getColumnIndex("PRODUCT_NAME")));
				productBalanceReportJsonObject.put("totalQuantity", totalQuantity);
				productBalanceReportJsonObject.put("soldQuantity", totalQuantity - remainingQuanity);
				productBalanceReportJsonObject.put("remainingQuantity", remainingQuanity);
			} catch (JSONException e) {

				e.printStackTrace();
			}

			productBalanceReportsArrayList.add(productBalanceReportJsonObject);
		}

		return productBalanceReportsArrayList;
	}

	private ArrayList<JSONObject> getCustomerFeedbackReports() {

		ArrayList<JSONObject> customerFeedbackReportsArrayList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery("SELECT * FROM DID_CUSTOMER_FEEDBACK ", null);
		while(cursor.moveToNext()) {

			JSONObject customerFeedbackReportJsonObject = new JSONObject();
			try {

				Cursor cursorForCustomer = database.rawQuery(
						"SELECT * FROM CUSTOMER"
						+ " WHERE CUSTOMER_ID = \"" + cursor.getString(cursor.getColumnIndex("CUSTOMER_NO")) + "\""
						, null);
				if (cursorForCustomer.moveToNext()) {

					customerFeedbackReportJsonObject.put("customerName"
							, cursorForCustomer.getString(cursorForCustomer.getColumnIndex("CUSTOMER_NAME")));
				}

				Cursor cursorForCustomerFeedback = database.rawQuery(
						"SELECT * FROM CUSTOMER_FEEDBACK"
						+ " WHERE SERIAL_NO = \"" + cursor.getString(cursor.getColumnIndex("SERIAL_NO")) + "\"", null);
				if (cursorForCustomerFeedback.moveToNext()) {

					customerFeedbackReportJsonObject.put("description"
							, cursorForCustomerFeedback.getString(cursorForCustomerFeedback.getColumnIndex("DESCRIPTION")));
				}

				customerFeedbackReportJsonObject.put("remark", cursor.getString(cursor.getColumnIndex("REMARK")));
			} catch (JSONException e) {

				e.printStackTrace();
			}

			customerFeedbackReportsArrayList.add(customerFeedbackReportJsonObject);
		}

		return customerFeedbackReportsArrayList;
	}

	private ArrayList<JSONObject> getPreOrderReports() {

		ArrayList<JSONObject> preOrderReportsArrayList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery(
				"SELECT CUSTOMER.CUSTOMER_NAME, ADVANCE_PAYMENT_AMOUNT, NET_AMOUNT, PRODUCT_LIST"
				+ " FROM PRE_ORDER"
				+ " INNER JOIN CUSTOMER"
				+ " ON CUSTOMER.CUSTOMER_ID = PRE_ORDER.CUSTOMER_ID"
				, null);
		while(cursor.moveToNext()) {

			JSONObject preOrderReportJSONObject = new JSONObject();
			try {

				preOrderReportJSONObject.put("customerName", cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME")));
				preOrderReportJSONObject.put("prepaidAmount", cursor.getDouble(cursor.getColumnIndex("ADVANCE_PAYMENT_AMOUNT")));
				preOrderReportJSONObject.put("totalAmount", cursor.getDouble(cursor.getColumnIndex("NET_AMOUNT")));

				JSONArray productListJSONArray = new JSONArray(cursor.getString(cursor.getColumnIndex("PRODUCT_LIST")));
				for (int i = 0; i < productListJSONArray.length(); i++) {

					JSONObject productJSONObject = productListJSONArray.getJSONObject(i);
					System.out.println("SELECT PRODUCT_NAME FROM PRODUCT"
							+ " WHERE PRODUCT_ID = '" + productJSONObject.getString("productId") + "'");
					Cursor cursorForProduct = database.rawQuery(
							"SELECT PRODUCT_NAME FROM PRODUCT"
							+ " WHERE PRODUCT_ID = '" + productJSONObject.getString("productId") + "'", null);
					if (cursorForProduct.moveToNext()) {

						productJSONObject.put("productName", cursorForProduct.getString(cursorForProduct.getColumnIndex("PRODUCT_NAME")));
					}
				}
				preOrderReportJSONObject.put("productList", productListJSONArray);
			} catch (JSONException e) {

				e.printStackTrace();
			}

			preOrderReportsArrayList.add(preOrderReportJSONObject);
		}

		return preOrderReportsArrayList;
	}

	private ArrayList<JSONObject> getDeliveryReports() {

		ArrayList<JSONObject> deliveryReportsArrayList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery(
				"SELECT CUSTOMER.CUSTOMER_NAME, DELIVERY.AMOUNT, DELIVERY.FIRST_PAID_AMOUNT, DELIVERY.REMAINING_AMOUNT, DELIVERY.SALE_ORDER_ITEMS"
				+ " FROM DELIVERY"
				+ " INNER JOIN CUSTOMER"
				+ " ON CUSTOMER.CUSTOMER_ID = DELIVERY.CUSTOMER_ID"
				, null);
		while(cursor.moveToNext()) {

			JSONObject preOrderReportJSONObject = new JSONObject();
			try {

				JSONArray productListJSONArray = new JSONArray(
						cursor.getString(cursor
								.getColumnIndex("SALE_ORDER_ITEMS")));

				// We should care incomplete order vouchers.
				if (productListJSONArray.length() == 0) {

					continue;
				}

				preOrderReportJSONObject.put("customerName", cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME")));
				preOrderReportJSONObject.put("amount", cursor.getDouble(cursor.getColumnIndex("AMOUNT")));
				preOrderReportJSONObject.put("firstPaidAmount", cursor.getDouble(cursor.getColumnIndex("FIRST_PAID_AMOUNT")));
				preOrderReportJSONObject.put("remainingAmount", cursor.getDouble(cursor.getColumnIndex("REMAINING_AMOUNT")));

				for (int i = 0; i < productListJSONArray.length(); i++) {

					JSONObject productJSONObject = productListJSONArray.getJSONObject(i);
					Cursor cursorForProduct = database.rawQuery(
							"SELECT PRODUCT_NAME FROM PRODUCT"
							+ " WHERE PRODUCT_ID = '" + productJSONObject.getString("productId") + "'", null);
					if (cursorForProduct.moveToNext()) {

						productJSONObject.put("productName", cursorForProduct.getString(cursorForProduct.getColumnIndex("PRODUCT_NAME")));
					}
				}
				preOrderReportJSONObject.put("saleOrderItems", productListJSONArray);
			} catch (JSONException e) {

				e.printStackTrace();
			}

			deliveryReportsArrayList.add(preOrderReportJSONObject);
		}

		return deliveryReportsArrayList;
	}

	private ArrayList<JSONObject> getDeliveredInvoiceReports() {

		ArrayList<JSONObject> deliveredInvoiceReportsArrayList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery("SELECT * FROM DELIVERED_DATA", null);
		while(cursor.moveToNext()) {

			JSONObject deliveredInvoiceReportJSONObject = new JSONObject();
			try {

				deliveredInvoiceReportJSONObject.put("invoiceId"
						, cursor.getString(cursor.getColumnIndex("DELIVERY_INVOICE_ID")));

				Cursor cursorForCustomer = database.rawQuery(
						"SELECT CUSTOMER_NAME, ADDRESS FROM CUSTOMER"
						+ " WHERE CUSTOMER_ID = \"" + cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")) + "\""
						, null);
				if (cursorForCustomer.moveToNext()) {

					deliveredInvoiceReportJSONObject.put("customerName"
							, cursorForCustomer.getString(cursorForCustomer.getColumnIndex("CUSTOMER_NAME")));
					deliveredInvoiceReportJSONObject.put("address"
							, cursorForCustomer.getString(cursorForCustomer.getColumnIndex("ADDRESS")));
				}

				double totalAmount = cursor.getDouble(cursor.getColumnIndex("TOTAL_AMOUNT"));
				double discount = cursor.getDouble(cursor.getColumnIndex("TOTAL_DISCOUNT_AMOUNT"));
				deliveredInvoiceReportJSONObject.put("totalAmount", totalAmount);
				deliveredInvoiceReportJSONObject.put("discount", discount);
				deliveredInvoiceReportJSONObject.put("netAmount", totalAmount - discount);
			} catch (JSONException e) {

				e.printStackTrace();
			}

			deliveredInvoiceReportsArrayList.add(deliveredInvoiceReportJSONObject);
		}

		return deliveredInvoiceReportsArrayList;
	}
}
