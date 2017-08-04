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
public class FragmentSaleInvoiceReport extends Fragment {

	ListView saleInvoiceReportsListView;

	ArrayList<JSONObject> saleInvoiceReportsArrayList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		 View view = inflater.inflate(R.layout.fragment_sale_invoice_report, container,false);

		 saleInvoiceReportsListView = (ListView) view.findViewById(R.id.saleInvoceReports);
		 ArrayAdapter<JSONObject> saleInvoiceReportsArrayAdapter = new SaleInvoiceReportsArrayAdapter(getActivity());
		 saleInvoiceReportsListView.setAdapter(saleInvoiceReportsArrayAdapter);
		 return view;
	}

	private class SaleInvoiceReportsArrayAdapter extends ArrayAdapter<JSONObject> {

		public final Activity context;

		public SaleInvoiceReportsArrayAdapter(Activity context) {

			super(context, R.layout.list_row_sale_invoice_report, saleInvoiceReportsArrayList);
			this.context = context;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			JSONObject saleInvoiceReportJsonObject = saleInvoiceReportsArrayList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_sale_invoice_report, null, true);

			TextView invoiceIdTextView = (TextView) view.findViewById(R.id.invoiceId);
			TextView customerNameTextView = (TextView) view.findViewById(R.id.customerName);
			TextView addressTextView = (TextView) view.findViewById(R.id.address);
			TextView totalAmountTextView = (TextView) view.findViewById(R.id.totalAmount);
			TextView discountTextView = (TextView) view.findViewById(R.id.discount);
			TextView netAmountTextView = (TextView) view.findViewById(R.id.netAmount);

			try {

				invoiceIdTextView.setText(saleInvoiceReportJsonObject.getString("invoiceId"));
				customerNameTextView.setText(saleInvoiceReportJsonObject.getString("customerName"));
				addressTextView.setText(saleInvoiceReportJsonObject.getString("address"));
				totalAmountTextView.setText(Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("totalAmount")));
				discountTextView.setText(Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("discount")));
				netAmountTextView.setText(Utils.formatAmount(saleInvoiceReportJsonObject.getDouble("netAmount")));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			return view;
		}
	}
}
