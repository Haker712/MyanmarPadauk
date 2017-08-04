package com.aceplus.myanmar_padauk.general_sale;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.models.Customer;
import com.aceplus.myanmar_padauk.models.SoldProduct;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Utils;

public class GeneralSalePrintActivity extends Activity {

	public static final String USER_INFO_KEY = "user-info-key";
	public static final String CUSTOMER_INFO_KEY = "customer-info-key";
	public static final String SOLD_PRODUCT_LIST_KEY = "sold-product-list";
	public static final String INVOICE_ID_KEY = "invoice-id-key";
//	public static final String PAY_AMOUNT_KEY = "pay-amount";
	public static final String PRE_PAID_AMOUNT_KEY = "pre-paid-amount-key";

	JSONObject userInfo;
	Customer customer;
	ArrayList<SoldProduct> soldProductList;
	String invoiceId;
	private double prepaidAmount;

	Button cancelButton;
	Button printButton;
	TextView saleDateTextView;
	TextView invoiceIdTextView;
	TextView saleManTextView;
	TextView branchTextView;
	ListView soldProductsListView;
	TextView totalAmountTextView;
//	TextView discountTextView;
//	TextView netAmountTextView;
//	TextView payAmountTextView;
//	TextView refundTextView;
	private TextView prepaidAmountTextView;

	SoldProudctListArrayAdapter soldProudctListArrayAdapter;

	SQLiteDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_general_sale_print);

		try {

			userInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));
		} catch (JSONException e) {

			e.printStackTrace();
		}
		customer = (Customer) getIntent().getSerializableExtra(CUSTOMER_INFO_KEY);
		soldProductList = (ArrayList<SoldProduct>) getIntent().getSerializableExtra(SOLD_PRODUCT_LIST_KEY);
		invoiceId = getIntent().getStringExtra(INVOICE_ID_KEY);
//		payAmount = getIntent().getDoubleExtra(PAY_AMOUNT_KEY, 0.0);
		this.prepaidAmount = getIntent().getDoubleExtra(GeneralSalePrintActivity.PRE_PAID_AMOUNT_KEY, 0);

		cancelButton = (Button) findViewById(R.id.cancel);
		printButton = (Button) findViewById(R.id.print);

		saleDateTextView = (TextView) findViewById(R.id.saleDate);
		invoiceIdTextView = (TextView) findViewById(R.id.invoiceId);
		saleManTextView = (TextView) findViewById(R.id.saleMan);
		branchTextView = (TextView) findViewById(R.id.branch);
		soldProductsListView = (ListView) findViewById(R.id.soldProductList);
		totalAmountTextView = (TextView) findViewById(R.id.totalAmount);
//		discountTextView = (TextView) findViewById(R.id.discount);
//		netAmountTextView = (TextView) findViewById(R.id.netAmount);
//		payAmountTextView = (TextView) findViewById(R.id.payAmount);
//		refundTextView = (TextView) findViewById(R.id.refund);
		prepaidAmountTextView = (TextView) findViewById(R.id.prepaidAmount);

		findViewById(R.id.tableHeaderDiscount).setVisibility(View.GONE);

		database = new Database(this).getDataBase();

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				GeneralSalePrintActivity.this.onBackPressed();
			}
		});

		printButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				try {

					Utils.print(GeneralSalePrintActivity.this, customer.getCustomerName(), invoiceId
							, userInfo.getString("userName"), prepaidAmount, soldProductList, Utils.PRINT_FOR_PRE_ORDER
							, Utils.FOR_OTHERS);
				} catch (JSONException e) {

					e.printStackTrace();
				}
			}
		});

		saleDateTextView.setText(Utils.getCurrentDate(false));
		try {

			saleManTextView.setText(userInfo.getString("userName"));
		} catch (JSONException e) {

			e.printStackTrace();
		}
		invoiceIdTextView.setText(invoiceId);
		try {

			Cursor cursor = database.rawQuery("SELECT ZONE_NAME FROM ZONE WHERE ZONE_CODE = \""
					+ userInfo.getString("locationCode") + "\"", null);
			if (cursor.getCount() == 1) {

				cursor.moveToNext();
				branchTextView.setText(cursor.getString(cursor.getColumnIndex("ZONE_NAME")));
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}

		soldProudctListArrayAdapter = new SoldProudctListArrayAdapter(this);
		soldProductsListView.setAdapter(soldProudctListArrayAdapter);

		Double totalAmount = 0.0;
//		Double totalAmountForVolumeDiscount = 0.0;
//		Double totalItemDiscountAmount = 0.0;
		for (SoldProduct soldProduct : soldProductList) {

			totalAmount += soldProduct.getTotalAmount();
//			totalAmount += soldProduct.getNetAmount(this);
//			if (soldProduct.getProduct().getDiscountType().equalsIgnoreCase("v")) {
//
//				totalAmountForVolumeDiscount += soldProduct.getTotalAmount();
//			}
//			totalItemDiscountAmount += soldProduct.getDiscountAmount(this);
		}
//		Cursor cursor = database.rawQuery("SELECT DISCOUNT_PERCENT, DISCOUNT_AMOUNT FROM VOLUME_DISCOUNT WHERE"
//				+ " FROM_AMOUNT <= " + totalAmountForVolumeDiscount + " AND TO_AMOUNT >= " + totalAmountForVolumeDiscount, null);
//		Double totalVolumeDiscount = 0.0;
//		if (cursor.getCount() == 1) {
//
//			cursor.moveToNext();
//			if (cursor.getDouble(cursor.getColumnIndex("DISCOUNT_AMOUNT")) != 0) {
//
//				totalVolumeDiscount = cursor.getDouble(cursor.getColumnIndex("DISCOUNT_AMOUNT"));
//			} else {
//
//				totalVolumeDiscount = totalAmountForVolumeDiscount * cursor.getDouble(cursor.getColumnIndex("DISCOUNT_PERCENT")) / 100;
//			}
//		}
		totalAmountTextView.setText(Utils.formatAmount(totalAmount));
//		discountTextView.setText(Utils.formatAmount(totalItemDiscountAmount + totalVolumeDiscount));
//		netAmountTextView.setText(Utils.formatAmount(totalAmount - totalItemDiscountAmount - totalVolumeDiscount));
//		payAmountTextView.setText(Utils.formatAmount(payAmount));
//		refundTextView.setText(Utils.formatAmount(payAmount - (totalAmount - totalItemDiscountAmount - totalVolumeDiscount)));
		prepaidAmountTextView.setText(Utils.formatAmount(this.prepaidAmount));
	}

	@Override
	public void onBackPressed() {

		new AlertDialog.Builder(this)
			.setTitle("Comfirmation")
			.setMessage("Are you complete printing?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
	
					finish();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}

	private class SoldProudctListArrayAdapter extends ArrayAdapter<SoldProduct> {

		Activity context;

		public SoldProudctListArrayAdapter(Activity context) {

			super(context, R.layout.list_row_sold_product_for_print, soldProductList);
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final SoldProduct soldProduct = soldProductList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_sold_product_for_print, null, true);

			((TextView) view.findViewById(R.id.name)).setText(soldProduct.getProduct().getName());
			((TextView) view.findViewById(R.id.qty)).setText(soldProduct.getQuantity() + "");
			((TextView) view.findViewById(R.id.price)).setText(Utils.formatAmount(soldProduct.getProduct().getPrice()));
//			((TextView) view.findViewById(R.id.discount)).setText(soldProduct.getDiscount(context) + "%");
			((TextView) view.findViewById(R.id.discount)).setVisibility(View.GONE);

//			Double amount = soldProduct.getProduct().getPrice() * soldProduct.getQuantity();
//			Double discount = amount * soldProduct.getDiscount(context) / 100;
			((TextView) view.findViewById(R.id.amount)).setText(Utils.formatAmount(soldProduct.getNetAmount(GeneralSalePrintActivity.this)));

			return view;
		}
	}
}
