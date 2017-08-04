package com.aceplus.myanmar_padauk.delivery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.report.FragmentDeliveryReport;
import com.aceplus.myanmar_padauk.utils.Database;

public class DeliveryActivity extends Activity {

	public static final String USER_INFO_KEY = "user-info-key";

	public JSONObject userInfo;

	Button cancelButton;
	private TextView titleTextView;

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
		this.titleTextView = (TextView) findViewById(R.id.title);

		findViewById(R.id.reports).setVisibility(View.GONE);

		database = new Database(this).getDataBase();

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				DeliveryActivity.this.onBackPressed();
			}
		});

		this.titleTextView.setText("Delivery");

		ArrayList<JSONObject> deliveryReportsArrayList = new ArrayList<JSONObject>();

		deliveryReportsArrayList = getDeliveryReports();

		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		FragmentDeliveryReport deliveryReportFragment = new FragmentDeliveryReport();
		deliveryReportFragment.deliveryReportsArrayList = deliveryReportsArrayList;
		deliveryReportFragment.isDelivery = true;
		fragmentTransaction.replace(R.id.fragment_report,
				deliveryReportFragment).commit();
	}

	private ArrayList<JSONObject> getDeliveryReports() {

		ArrayList<JSONObject> deliveryReportsArrayList = new ArrayList<JSONObject>();

		Cursor cursor = database
				.rawQuery(
						"SELECT CUSTOMER.CUSTOMER_ID, CUSTOMER.CUSTOMER_NAME, DELIVERY.ORDER_NUMBER, DELIVERY.AMOUNT"
							+ ", DELIVERY.FIRST_PAID_AMOUNT, DELIVERY.REMAINING_AMOUNT, DELIVERY.SALE_ORDER_ITEMS"
							+ " FROM DELIVERY"
							+ " INNER JOIN CUSTOMER"
							+ " ON CUSTOMER.CUSTOMER_ID = DELIVERY.CUSTOMER_ID",
						null);
		while (cursor.moveToNext()) {

			JSONObject preOrderReportJSONObject = new JSONObject();
			try {

				JSONArray productListJSONArray = new JSONArray(
						cursor.getString(cursor
								.getColumnIndex("SALE_ORDER_ITEMS")));

				// We should care incomplete order vouchers.
				if (productListJSONArray.length() == 0) {

					continue;
				}

				preOrderReportJSONObject.put("orderNumber"
						, cursor.getString(cursor.getColumnIndex("ORDER_NUMBER")));
				preOrderReportJSONObject.put("customerID"
						, cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")));
				preOrderReportJSONObject.put("customerName"
						, cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME")));
				preOrderReportJSONObject.put("amount",
						cursor.getDouble(cursor.getColumnIndex("AMOUNT")));
				preOrderReportJSONObject.put("firstPaidAmount"
						, cursor.getDouble(cursor.getColumnIndex("FIRST_PAID_AMOUNT")));
				preOrderReportJSONObject.put("remainingAmount"
						, cursor.getDouble(cursor.getColumnIndex("REMAINING_AMOUNT")));

				for (int i = 0; i < productListJSONArray.length(); i++) {

					JSONObject productJSONObject = productListJSONArray
							.getJSONObject(i);
					Cursor cursorForProduct = database.rawQuery(
							"SELECT PRODUCT_NAME FROM PRODUCT"
									+ " WHERE PRODUCT_ID = '"
									+ productJSONObject.getString("productId")
									+ "'", null);
					if (cursorForProduct.moveToNext()) {

						productJSONObject.put("productName", cursorForProduct
								.getString(cursorForProduct
										.getColumnIndex("PRODUCT_NAME")));
					}
				}
				preOrderReportJSONObject.put("saleOrderItems",
						productListJSONArray);

				deliveryReportsArrayList.add(preOrderReportJSONObject);
			} catch (JSONException e) {

				e.printStackTrace();
			}
		}

		return deliveryReportsArrayList;
	}
}
