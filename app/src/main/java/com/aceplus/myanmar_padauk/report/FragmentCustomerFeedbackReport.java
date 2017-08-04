package com.aceplus.myanmar_padauk.report;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class FragmentCustomerFeedbackReport extends Fragment {

	ListView customerFeedbackReportsListView;

	ArrayList<JSONObject> customerFeedbackReportsArrayList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		 View view = inflater.inflate(R.layout.fragment_customer_feedback_report, container,false);

		 customerFeedbackReportsListView = (ListView) view.findViewById(R.id.customerFeedbacks);
		 ArrayAdapter<JSONObject> customerFeedbackReportsArrayAdapter = new CustomerFeedbackReportsArrayAdapter(getActivity());
		 customerFeedbackReportsListView.setAdapter(customerFeedbackReportsArrayAdapter);
		 return view;
	}

	private class CustomerFeedbackReportsArrayAdapter extends ArrayAdapter<JSONObject> {

		public final Activity context;

		public CustomerFeedbackReportsArrayAdapter(Activity context) {

			super(context, R.layout.list_row_customer_feedback_report, customerFeedbackReportsArrayList);
			this.context = context;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			JSONObject customerFeedbackReportJsonObject = customerFeedbackReportsArrayList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_customer_feedback_report, null, true);

			TextView customerNameTextView = (TextView) view.findViewById(R.id.customerName);
			TextView descriptionTextView = (TextView) view.findViewById(R.id.description);
			TextView remarkTextView = (TextView) view.findViewById(R.id.remark);

			try {

				Log.e("description>>>", customerFeedbackReportJsonObject.getString("description"));
				customerNameTextView.setText(customerFeedbackReportJsonObject.getString("customerName"));
				descriptionTextView.setText(customerFeedbackReportJsonObject.getString("description"));
				remarkTextView.setText(customerFeedbackReportJsonObject.getString("remark"));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			return view;
		}
	}
}
