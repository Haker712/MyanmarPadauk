package com.aceplus.myanmar_padauk.package_sale;

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
import com.aceplus.myanmar_padauk.general_sale.GeneralSaleActivity;
import com.aceplus.myanmar_padauk.general_sale.GeneralSalePrintActivity;
import com.aceplus.myanmar_padauk.models.Customer;
import com.aceplus.myanmar_padauk.models.SoldProduct;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Utils;

public class PackageSalePrint extends Activity {

	public static final String FOR_DELIVERY_KEY = "for-delivery-key";

	public static final String USER_INFO_KEY = "user-info-key";
	public static final String CUSTOMER_INFO_KEY = "customer-info-key";
	public static final String SOLD_PROUDCT_LIST_KEY = "sold-product-list-key";
	public static final String INVOICE_ID = "invoice-id";
	public static final String PAY_AMOUNT_KEY = "pay-amount-key";
	public static final String RECEIPT_PERSON_NAME_KEY = "receipt-person-name-key";

	boolean forDelivery;

	JSONObject userInfo;
	Customer customer;
	ArrayList<SoldProduct> soldProductList;
	String invoiceId;
	double payAmount;
	String receiptPersonNameKey;

	TextView saleDate;
	TextView invoiceIdTextView;
	TextView saleMan;
	TextView branchTextView;

	ListView soldProductsListView;
	
	TextView totalAmount;
	TextView discount;
	TextView netAmount;
	TextView payAmountTextView;
	TextView refund;

	Button printButton;

	/*
	 * for invoice - c
	 */

	TextView saleDateTextViewC;
	TextView invoiceIdTextViewC;
	TextView saleManTextViewC;
	TextView branchTextViewC;

	ListView soldProductsListViewC;
	
	TextView totalAmountTextViewC;
	TextView discountTextViewC;
	TextView netAmountTextViewC;
	TextView payAmountTextViewC;
	TextView refundTextViewC;

	Button printCButton;

	SQLiteDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_package_sale_print);

		this.forDelivery = getIntent().getBooleanExtra(PackageSalePrint.FOR_DELIVERY_KEY, false);

		try {

			userInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));
		} catch (JSONException e) {

			e.printStackTrace();
		}
		customer = (Customer) getIntent().getSerializableExtra(CUSTOMER_INFO_KEY);
		soldProductList = (ArrayList<SoldProduct>) getIntent().getSerializableExtra(SOLD_PROUDCT_LIST_KEY);
		invoiceId = getIntent().getStringExtra(INVOICE_ID);
		payAmount = getIntent().getDoubleExtra(PAY_AMOUNT_KEY, 0.0);
		receiptPersonNameKey = getIntent().getStringExtra(RECEIPT_PERSON_NAME_KEY);

		saleDate = (TextView) findViewById(R.id.saleDate);
		invoiceIdTextView = (TextView) findViewById(R.id.invoiceId);
		saleMan = (TextView) findViewById(R.id.saleMan);
		branchTextView = (TextView) findViewById(R.id.branch);

		soldProductsListView = (ListView) findViewById(R.id.soldProductList);

		totalAmount = (TextView) findViewById(R.id.totalAmount);
		discount = (TextView) findViewById(R.id.discount);
		netAmount = (TextView) findViewById(R.id.netAmount);
		payAmountTextView = (TextView) findViewById(R.id.payAmount);
		refund = (TextView) findViewById(R.id.refund);

		printButton = (Button) findViewById(R.id.print);

		/*
		 * For Invoice - C
		 */
		saleDateTextViewC = (TextView) findViewById(R.id.saleDate_c);
		invoiceIdTextViewC = (TextView) findViewById(R.id.invoiceId_c);
		saleManTextViewC = (TextView) findViewById(R.id.saleMan_c);
		branchTextViewC = (TextView) findViewById(R.id.branch_c);

		ListView soldProductsListViewC = (ListView) findViewById(R.id.soldProductList_c);
		
		totalAmountTextViewC = (TextView) findViewById(R.id.totalAmount_c);
		discountTextViewC = (TextView) findViewById(R.id.discount_c);
		netAmountTextViewC = (TextView) findViewById(R.id.netAmount_c);
		payAmountTextViewC = (TextView) findViewById(R.id.payAmount_c);
		refundTextViewC = (TextView) findViewById(R.id.refund_c);

		printCButton = (Button) findViewById(R.id.printC);

		database = new Database(this).getDataBase();

		saleDate.setText(Utils.getCurrentDate(false));
		saleDateTextViewC.setText(Utils.getCurrentDate(false));
		try {

			saleMan.setText(userInfo.getString("userName"));
			saleManTextViewC.setText(userInfo.getString("userName"));
		} catch (JSONException e) {

			e.printStackTrace();
		}

		invoiceIdTextView.setText(invoiceId);
		invoiceIdTextViewC.setText(invoiceId);

		try {

			Cursor cursor = database.rawQuery("SELECT ZONE_NAME FROM ZONE WHERE ZONE_CODE = \""
					+ userInfo.getString("locationCode") + "\"", null);
			if (cursor.getCount() == 1) {

				cursor.moveToNext();
				branchTextView.setText(cursor.getString(cursor.getColumnIndex("ZONE_NAME")));
				branchTextViewC.setText(cursor.getString(cursor.getColumnIndex("ZONE_NAME")));
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}

		ArrayAdapter<SoldProduct> soldProductListArrayAdapter = new SoldProductListAdapter(this);
		soldProductsListView.setAdapter(soldProductListArrayAdapter);

		ArrayAdapter<SoldProduct> soldProductListArrayAdapterForC = new SoldProductListAdapterForC(this);
		soldProductsListViewC.setAdapter(soldProductListArrayAdapterForC);

		double totalAmount = 0.0;
		double totalDiscountAmount = 0.0;
		double totalDiscountAmountForC = 0.0;
		double totalNetAmount = 0.0;
		for (SoldProduct soldProduct : soldProductList) {

			totalAmount += soldProduct.getTotalAmount();
			totalDiscountAmount += soldProduct.getDiscountAmount(this) + soldProduct.getExtraDiscountAmount();
			totalDiscountAmountForC += soldProduct.getDiscountAmount(this);
			totalNetAmount += soldProduct.getNetAmount(this);
		}

		this.totalAmount.setText(Utils.formatAmount(totalAmount));
		totalAmountTextViewC.setText(Utils.formatAmount(totalAmount));
		discount.setText(Utils.formatAmount(totalDiscountAmount));
		discountTextViewC.setText(Utils.formatAmount(totalDiscountAmountForC));
		this.netAmount.setText(Utils.formatAmount(totalNetAmount));
		netAmountTextViewC.setText(Utils.formatAmount(totalAmount - totalDiscountAmountForC));
		payAmountTextView.setText(Utils.formatAmount(payAmount));
		payAmountTextViewC.setText(Utils.formatAmount(payAmount));
		if (this.forDelivery) {

			findViewById(R.id.changeDueLayout).setVisibility(View.GONE);
			findViewById(R.id.changeDueLayout_c).setVisibility(View.GONE);
		} else {

			//refund.setText(Utils.formatAmount(payAmount - totalNetAmount));
			refund.setText(Utils.formatAmount(Math.abs(payAmount - totalNetAmount)));
			refundTextViewC.setText(Utils.formatAmount(
					Math.abs(payAmount - (totalAmount - totalDiscountAmountForC))));
		}
		
		printButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				try {

					String printMode;
					if (PackageSalePrint.this.forDelivery) {

						printMode = Utils.FOR_DELIVERY;
					} else {

						printMode = Utils.FOR_OTHERS;
					}

					Utils.print(PackageSalePrint.this, customer.getCustomerName(), invoiceId
							, userInfo.getString("userName"), payAmount, soldProductList, Utils.PRINT_FOR_NORMAL_SALE
							, printMode);
				} catch (JSONException e) {

					e.printStackTrace();
				}
			}
		});

		printCButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				try {

					String printMode;
					if (PackageSalePrint.this.forDelivery) {

						printMode = Utils.FOR_DELIVERY;
					} else {

						printMode = Utils.FOR_OTHERS;
					}

					Utils.print(PackageSalePrint.this, customer.getCustomerName(), invoiceId
							, userInfo.getString("userName"), payAmount, soldProductList, Utils.PRINT_FOR_C
							, printMode);
				} catch (JSONException e) {

					e.printStackTrace();
				}
			}
		});
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

	private class SoldProductListAdapter extends ArrayAdapter<SoldProduct> {
		
		final Activity context;

		public SoldProductListAdapter(Activity context) {

			super(context, R.layout.list_row_sold_product_for_print, soldProductList);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_sold_product_for_print, null, true);

			SoldProduct soldProduct = soldProductList.get(position);

			final TextView nameTextView = (TextView) view.findViewById(R.id.name);
			final TextView qtyTextView = (TextView) view.findViewById(R.id.qty);
			final TextView priceTextView = (TextView) view.findViewById(R.id.price);
			final TextView discountTextView = (TextView) view.findViewById(R.id.discount);
			final TextView totalAmountTextView = (TextView) view.findViewById(R.id.amount);

			nameTextView.setText(soldProduct.getProduct().getName());
			qtyTextView.setText(soldProduct.getQuantity() + "");
			priceTextView.setText(Utils.formatAmount(soldProduct.getProduct().getPrice()));

			discountTextView.setText(soldProduct.getDiscount(context) + soldProduct.getExtraDiscount() + "%");

			totalAmountTextView.setText(Utils.formatAmount(soldProduct.getNetAmount(context)));

//			if (!soldProduct.isForPackage()) {
//				
//				nameTextView.setTextColor(getResources().getColor(R.color.blue));
//			}
			return view;
		}
	}

	private class SoldProductListAdapterForC extends ArrayAdapter<SoldProduct> {
		
		final Activity context;

		public SoldProductListAdapterForC(Activity context) {

			super(context, R.layout.list_row_sold_product_for_print, soldProductList);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_sold_product_for_print, null, true);

			SoldProduct soldProduct = soldProductList.get(position);

			final TextView nameTextView = (TextView) view.findViewById(R.id.name);
			final TextView qtyTextView = (TextView) view.findViewById(R.id.qty);
			final TextView priceTextView = (TextView) view.findViewById(R.id.price);
			final TextView discountTextView = (TextView) view.findViewById(R.id.discount);
			final TextView totalAmountTextView = (TextView) view.findViewById(R.id.amount);

			nameTextView.setText(soldProduct.getProduct().getName());
			qtyTextView.setText(soldProduct.getQuantity() + "");
			priceTextView.setText(Utils.formatAmount(soldProduct.getProduct().getPrice()));

			discountTextView.setText(soldProduct.getDiscount(context) + "%");

			totalAmountTextView.setText(Utils.formatAmount(soldProduct.getTotalAmount() - soldProduct.getDiscountAmount(context)));

//			if (!soldProduct.isForPackage()) {
//				
//				nameTextView.setTextColor(getResources().getColor(R.color.blue));
//			}
			return view;
		}
	}
}
