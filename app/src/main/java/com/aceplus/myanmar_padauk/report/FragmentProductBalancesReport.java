package com.aceplus.myanmar_padauk.report;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.utils.Utils;

@SuppressLint("NewApi")
public class FragmentProductBalancesReport extends Fragment {

	ListView saleInvoiceReportsListView;

	ArrayList<JSONObject> productBalanceReportsArrayList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		 View view = inflater.inflate(R.layout.fragment_product_balance_report, container,false);

		 saleInvoiceReportsListView = (ListView) view.findViewById(R.id.productBalanceReports);
		 ArrayAdapter<JSONObject> productBalanceReportsArrayAdapter = new ProductBalanceReportsArrayAdapter(getActivity());
		 saleInvoiceReportsListView.setAdapter(productBalanceReportsArrayAdapter);
		 return view;
	}

	private class ProductBalanceReportsArrayAdapter extends ArrayAdapter<JSONObject> {

		public final Activity context;

		public ProductBalanceReportsArrayAdapter(Activity context) {

			super(context, R.layout.list_row_product_balance_report, productBalanceReportsArrayList);
			this.context = context;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			JSONObject saleInvoiceReportJsonObject = productBalanceReportsArrayList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_product_balance_report, null, true);

			TextView productNameTextView = (TextView) view.findViewById(R.id.productName);
			TextView totalQuantityTextView = (TextView) view.findViewById(R.id.totalQuantity);
			TextView soldQuantityTextView = (TextView) view.findViewById(R.id.soldQuantity);
			TextView remainingQuantityTextView = (TextView) view.findViewById(R.id.remainingQuantity);

			try {

				productNameTextView.setText(saleInvoiceReportJsonObject.getString("productName"));
				totalQuantityTextView.setText(
						Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("totalQuantity")));
				soldQuantityTextView.setText(
						Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("soldQuantity")));
				remainingQuantityTextView.setText(
						Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("remainingQuantity")));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			return view;
		}
	}
}
