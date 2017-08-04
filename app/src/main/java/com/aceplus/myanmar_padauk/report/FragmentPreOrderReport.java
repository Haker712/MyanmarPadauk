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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.utils.Utils;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

@SuppressLint("NewApi")
public class FragmentPreOrderReport extends Fragment {

	ListView preOrderReportsListView;

	ArrayList<JSONObject> preOrderReportsArrayList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		 View view = inflater.inflate(R.layout.fragment_pre_order_report, container,false);

		 preOrderReportsListView = (ListView) view.findViewById(R.id.preOrderReports);
		 preOrderReportsListView.setAdapter(new PreOrderReportsArrayAdapter(getActivity()));
		 preOrderReportsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				View dialogBoxView = getActivity().getLayoutInflater().inflate(R.layout.dialog_box_pre_order_products, null);
				ListView preOrderProductsListView = (ListView) dialogBoxView.findViewById(R.id.preOrderProducts);
				try {

//					if (preOrderProductsList.size() == 0) {

						preOrderProductsList.clear();
						JSONArray preOrderProductsJSONArray = FragmentPreOrderReport.this.preOrderReportsArrayList.get(position).getJSONArray("productList");
						for (int i = 0; i < preOrderProductsJSONArray.length(); i++) {

							preOrderProductsList.add(preOrderProductsJSONArray.getJSONObject(i));
						}
//					}
				} catch (JSONException e) {

					e.printStackTrace();
				}
				preOrderProductsListView.setAdapter(new PreOrderProductsArrayAdapter(getActivity()));
				new AlertDialog.Builder(getActivity())
					.setView(dialogBoxView)
					.setTitle("Pre-Order Products")
					.setPositiveButton("OK", null)
					.show();
			}
		});

		 return view;
	}

	private class PreOrderReportsArrayAdapter extends ArrayAdapter<JSONObject> {

		public final Activity context;

		public PreOrderReportsArrayAdapter(Activity context) {

			super(context, R.layout.list_row_pre_order_report, preOrderReportsArrayList);
			this.context = context;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			JSONObject saleInvoiceReportJsonObject = preOrderReportsArrayList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_pre_order_report, null, true);

			TextView customerNameTextView = (TextView) view.findViewById(R.id.customerName);
			TextView prepaidAmountTextView = (TextView) view.findViewById(R.id.prepaidAmount);
			TextView totalAmountTextView = (TextView) view.findViewById(R.id.totalAmount);

			try {

				customerNameTextView.setText(saleInvoiceReportJsonObject.getString("customerName"));
				prepaidAmountTextView.setText(Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("prepaidAmount")));
				totalAmountTextView.setText(Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("totalAmount")));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			return view;
		}
	}

	List<JSONObject> preOrderProductsList = new ArrayList<JSONObject>();
	private class PreOrderProductsArrayAdapter extends ArrayAdapter<JSONObject> {

		public final Activity context;

		public PreOrderProductsArrayAdapter(Activity context) {

			super(context, R.layout.list_row_pre_order_product, preOrderProductsList);
			this.context = context;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			JSONObject saleInvoiceReportJsonObject = preOrderProductsList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_pre_order_product, null, true);

			TextView productNameTextView = (TextView) view.findViewById(R.id.productName);
			TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
			TextView totalAmountTextView = (TextView) view.findViewById(R.id.totalAmount);

			try {

				productNameTextView.setText(saleInvoiceReportJsonObject.getString("productName"));
				quantityTextView.setText(saleInvoiceReportJsonObject.getString("orderQty"));
				totalAmountTextView.setText(saleInvoiceReportJsonObject.getString("totalAmt"));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			return view;
		}
	}
}
