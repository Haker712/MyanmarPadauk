package com.aceplus.myanmar_padauk.report;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.CustomerActivity;
import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.delivery.DeliveryActivity;
import com.aceplus.myanmar_padauk.general_sale.GeneralSaleActivity;
import com.aceplus.myanmar_padauk.models.Customer;
import com.aceplus.myanmar_padauk.models.Product;
import com.aceplus.myanmar_padauk.models.SoldProduct;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Utils;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

@SuppressLint("NewApi")
public class FragmentDeliveryReport extends Fragment {

	ListView deliveryReportsListView;

	public ArrayList<JSONObject> deliveryReportsArrayList;
	public boolean isDelivery;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		 View view = inflater.inflate(R.layout.fragment_delivery_report, container,false);

		 deliveryReportsListView = (ListView) view.findViewById(R.id.deliveryReports);
		 deliveryReportsListView.setAdapter(new DeliveryReportsArrayAdapter(getActivity()));
		 deliveryReportsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (FragmentDeliveryReport.this.isDelivery) {

					try {

						JSONObject userInfo = null;
						Customer customer = null;
						ArrayList<SoldProduct> soldProductList = FragmentDeliveryReport.this.getProductList(
								FragmentDeliveryReport.this.deliveryReportsArrayList.get(position)
									.getJSONArray("saleOrderItems"));
						ArrayList<SoldProduct> copySoldProductList = new ArrayList<SoldProduct>(soldProductList);
						for (SoldProduct soldProduct : soldProductList) {

							if (soldProduct.getOrderedQuantity() == 0) {

								copySoldProductList.remove(soldProduct);
							}
						}
						soldProductList = copySoldProductList;

						if (getActivity() instanceof DeliveryActivity) {

							userInfo = ((DeliveryActivity) getActivity()).userInfo;
						}

						SQLiteDatabase database = new Database(getActivity()).getDataBase();
						Cursor cursor = database.rawQuery("SELECT * FROM CUSTOMER"
								+ " WHERE CUSTOMER_ID = '" + FragmentDeliveryReport.this.deliveryReportsArrayList.get(position).getString("customerID") + "'", null);
						if (cursor.moveToNext()) {

							customer = new Customer(
									cursor.getString(cursor.getColumnIndex("CUSTOMER_ID"))
									, cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME"))
									, cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_ID"))
									, cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_NAME"))
									, cursor.getString(cursor.getColumnIndex("ADDRESS"))
									, cursor.getString(cursor.getColumnIndex("PH"))
									, cursor.getString(cursor.getColumnIndex("TOWNSHIP"))
									, cursor.getString(cursor.getColumnIndex("CREDIT_TERM"))
									, cursor.getDouble(cursor.getColumnIndex("CREDIT_LIMIT"))
									, cursor.getDouble(cursor.getColumnIndex("CREDIT_AMT"))
									, cursor.getDouble(cursor.getColumnIndex("DUE_AMT"))
									, cursor.getDouble(cursor.getColumnIndex("PREPAID_AMT"))
									, cursor.getString(cursor.getColumnIndex("PAYMENT_TYPE"))
									, cursor.getString(cursor.getColumnIndex("IS_IN_ROUTE")));

						}

						if (soldProductList.size() == 0) {

							new AlertDialog.Builder(getActivity())
								.setTitle("Delivery")
								.setMessage("No products to deliver for this invoice.")
								.setPositiveButton("OK", null)
								.show();

							return;
						} else if (userInfo != null && customer != null && soldProductList.size() != 0) {

							double remainingAmount = deliveryReportsArrayList.get(position).getDouble("remainingAmount");
							remainingAmount = remainingAmount < 0 ? 0 : remainingAmount;
							Intent intent = new Intent(getActivity(), GeneralSaleActivity.class);
							intent.putExtra(GeneralSaleActivity.IS_DELIVERY, true);
							intent.putExtra(GeneralSaleActivity.REMAINING_AMOUNT_KEY, remainingAmount);
							intent.putExtra(GeneralSaleActivity.USER_INFO_KEY, userInfo.toString());
							intent.putExtra(GeneralSaleActivity.CUSTOMER_INFO_KEY, customer);
							intent.putExtra(GeneralSaleActivity.SOLD_PROUDCT_LIST_KEY, soldProductList);
							intent.putExtra(GeneralSaleActivity.ORDERED_INVOICE_KEY
									, deliveryReportsArrayList.get(position).toString());
							startActivity(intent);
							getActivity().finish();
						}
					} catch (JSONException e) {

						e.printStackTrace();
					}
				} else {

					View dialogBoxView = getActivity().getLayoutInflater().inflate(R.layout.dialog_box_delivery_products, null);
					ListView preOrderProductsListView = (ListView) dialogBoxView.findViewById(R.id.deliveryProducts);
					try {

//						if (saleOrderItemsList.size() == 0) {

							saleOrderItemsList.clear();
							JSONArray preOrderProductsJSONArray = FragmentDeliveryReport.this.deliveryReportsArrayList.get(position).getJSONArray("saleOrderItems");
							for (int i = 0; i < preOrderProductsJSONArray.length(); i++) {

								saleOrderItemsList.add(preOrderProductsJSONArray.getJSONObject(i));
							}
//						}
					} catch (JSONException e) {

						e.printStackTrace();
					}
					preOrderProductsListView.setAdapter(new DeliveryProductsArrayAdapter(getActivity()));
					new AlertDialog.Builder(getActivity())
						.setView(dialogBoxView)
						.setTitle("Delivery Products")
						.setPositiveButton("OK", null)
						.show();
				}
			}
		 });

		 return view;
	}

	private ArrayList<SoldProduct> getProductList(JSONArray saleOrderItemsJSONArray) {

		ArrayList<SoldProduct> soldProductList = new ArrayList<SoldProduct>();

		SQLiteDatabase database = new Database(getActivity()).getDataBase();
		Cursor cursor;
		for (int i = 0; i < saleOrderItemsJSONArray.length(); i++) {

			try {

				JSONObject saleOrderItemJSONObject = saleOrderItemsJSONArray.getJSONObject(i);
				cursor = database.rawQuery("SELECT * FROM PRODUCT", null);
				while (cursor.moveToNext()) {

					System.out.println(cursor.getString(cursor.getColumnIndex("PRODUCT_ID")));
				}
				cursor = database.rawQuery(
						"SELECT * FROM PRODUCT"
								+ " WHERE PRODUCT_ID = '" + saleOrderItemJSONObject.getString("productId") + "'", null);
				if (cursor.moveToNext()) {

					SoldProduct soldProduct = new SoldProduct(new Product(
							cursor.getString(cursor.getColumnIndex("PRODUCT_ID"))
							, cursor.getString(cursor.getColumnIndex("PRODUCT_NAME"))
							, cursor.getDouble(cursor.getColumnIndex("SELLING_PRICE"))
							, cursor.getDouble(cursor.getColumnIndex("PURCHASE_PRICE"))
							, cursor.getString(cursor.getColumnIndex("DISCOUNT_TYPE"))
							, cursor.getInt(cursor.getColumnIndex("REMAINING_QTY"))), false);
					soldProduct.setOrderedQuantity(saleOrderItemJSONObject.getInt("orderQuantity")
								- saleOrderItemJSONObject.getInt("deliveredQuantity"));
					soldProduct.setQuantity(soldProduct.getOrderedQuantity());
					soldProductList.add(soldProduct);
				}
			} catch (JSONException e) {

				e.printStackTrace();
			}
		}
		return soldProductList;
	}

	private class DeliveryReportsArrayAdapter extends ArrayAdapter<JSONObject> {

		public final Activity context;

		public DeliveryReportsArrayAdapter(Activity context) {

			super(context, R.layout.list_row_delivery_report, deliveryReportsArrayList);
			this.context = context;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			JSONObject saleInvoiceReportJsonObject = deliveryReportsArrayList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_delivery_report, null, true);

			TextView customerNameTextView = (TextView) view.findViewById(R.id.customerName);
			TextView remainingAmountTextView = (TextView) view.findViewById(R.id.remainingAmount);
			TextView totalAmountTextView = (TextView) view.findViewById(R.id.totalAmount);

			try {

				customerNameTextView.setText(
						saleInvoiceReportJsonObject.getString("customerName"));
				double remainingAmount = saleInvoiceReportJsonObject.getDouble("remainingAmount");
				remainingAmount = remainingAmount < 0 ? 0 : remainingAmount;
				remainingAmountTextView.setText(
						Utils.formatAmount(remainingAmount));
				totalAmountTextView.setText(
						Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("amount")));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			return view;
		}
	}

	List<JSONObject> saleOrderItemsList = new ArrayList<JSONObject>();
	private class DeliveryProductsArrayAdapter extends ArrayAdapter<JSONObject> {

		public final Activity context;

		public DeliveryProductsArrayAdapter(Activity context) {

			super(context, R.layout.list_row_delivery_product, saleOrderItemsList);
			this.context = context;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			JSONObject saleInvoiceReportJsonObject = saleOrderItemsList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_delivery_product, null, true);

			TextView productNameTextView = (TextView) view.findViewById(R.id.productName);
			TextView remainingQuantityTextView = (TextView) view.findViewById(R.id.remainingQuantity);

			try {

				productNameTextView.setText(saleInvoiceReportJsonObject.getString("productName"));
				remainingQuantityTextView.setText(
						Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("orderQuantity")
								- saleInvoiceReportJsonObject.getDouble("deliveredQuantity")) + "");
			} catch (JSONException e) {

				e.printStackTrace();
			}
			return view;
		}
	}
}
