package com.aceplus.myanmar_padauk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.general_sale.GeneralSaleActivity;
import com.aceplus.myanmar_padauk.item_volume_dis_sale.ItemVolumeDisSaleActivity;
import com.aceplus.myanmar_padauk.models.Customer;
import com.aceplus.myanmar_padauk.models.CustomerFeedback;
import com.aceplus.myanmar_padauk.package_sale.PackageSaleActivity;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Preferences;
import com.aceplus.myanmar_padauk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerActivity extends Activity {

	// For pre order
	public static final String IS_PRE_ORDER = "is-pre-order"; 
	public static final String USER_INFO_KEY = "user-info-key";

	private boolean isPreOrder;
	JSONObject salemanInfo;

	Button cancelButton;

	TextView usernameTextView;
	TextView dateTextView;

	EditText searchCustomersEditText;
	ListView customersListView;

	TextView customerNameTextView;
	TextView phoneTextView;
	TextView addressTextView;
	TextView townshipTextView;
	TextView creditTermsTextView;
	TextView creditLimitTextView;
	TextView creditAmountTextView;
	TextView dueAmountTextView;
	TextView prepaidAmountTextView;
	TextView paymentTypeTextView;

	Button generalSaleButton;
	Button packageSaleButton;
	Button itemOrVolumeDisSaleButton;
	Button reportButton;

	Button preOrderOKButton;

	SQLiteDatabase database;

	ArrayList<Customer> customers;
	ArrayList<Customer> customerListForArrayAdapter;
	CustomerListArrayAdapter customerListArrayAdapter;

	Customer customer;

    List<Customer> customer_array = new ArrayList<Customer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customer);

		// Hide keyboard on startup.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        this.isPreOrder = getIntent().getBooleanExtra(CustomerActivity.IS_PRE_ORDER, false);
        try {

			salemanInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));
		} catch (JSONException e) {

			e.printStackTrace();
		}

        cancelButton = (Button) findViewById(R.id.cancel);

		usernameTextView = (TextView) findViewById(R.id.username);
		dateTextView = (TextView) findViewById(R.id.date);

		searchCustomersEditText = (EditText) findViewById(R.id.search);
		customersListView = (ListView) findViewById(R.id.customers);

		customerNameTextView = (TextView) findViewById(R.id.customerName);
		phoneTextView = (TextView) findViewById(R.id.phone);
		addressTextView = (TextView) findViewById(R.id.address);
		townshipTextView = (TextView) findViewById(R.id.township);
		creditTermsTextView = (TextView) findViewById(R.id.creditTerms);
		creditLimitTextView = (TextView) findViewById(R.id.creditLimit);
		creditAmountTextView = (TextView) findViewById(R.id.creditAmount);
		dueAmountTextView = (TextView) findViewById(R.id.dueAmount);
		prepaidAmountTextView = (TextView) findViewById(R.id.prepaidAmount);
		paymentTypeTextView = (TextView) findViewById(R.id.paymentType);

		if (this.isPreOrder) {

			LinearLayout generalButtonsLinearLayout = (LinearLayout) findViewById(R.id.generalButtonsLinearLayout);
			generalButtonsLinearLayout.setVisibility(View.GONE);

			preOrderOKButton = (Button) findViewById(R.id.preOrderOK);
			preOrderOKButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(CustomerActivity.this, GeneralSaleActivity.class);
					intent.putExtra(GeneralSaleActivity.IS_PRE_ORDER, true);
					intent.putExtra(GeneralSaleActivity.USER_INFO_KEY, salemanInfo.toString());
					intent.putExtra(GeneralSaleActivity.CUSTOMER_INFO_KEY, customer);
					startActivity(intent);
					// We finish this activity because we need to select no customer when visit by back button.
					finish();
				}
			});
		} else {

			LinearLayout preOrderButtonsLinearLayout = (LinearLayout) findViewById(R.id.preOrderButtonsLinearLayout);
			preOrderButtonsLinearLayout.setVisibility(View.GONE);

			generalSaleButton = (Button) findViewById(R.id.generalSale);
			packageSaleButton = (Button) findViewById(R.id.packageSale);
			reportButton = (Button) findViewById(R.id.report);

			itemOrVolumeDisSaleButton = (Button) findViewById(R.id.itemOrVolmeDisSale);

			itemOrVolumeDisSaleButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Preferences.wasUploadedSaleDataToServer(CustomerActivity.this)) {
						int count = 0;
						Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM INVOICE", null);
						if (cursor.moveToNext()) {

							count = cursor.getInt(cursor.getColumnIndex("COUNT"));
						}

						if (count > 0) {

							new AlertDialog.Builder(CustomerActivity.this)
									.setTitle("Item Volume Discount sale")
									.setMessage("Your uploaded sale data is not clear yet.\n"
											+ "Before sale, clear all data.")
									.setPositiveButton("OK", null)
									.show();

							return;
						}
					}

					if (didCustomerSelected()) {

						Intent intent = new Intent(CustomerActivity.this, ItemVolumeDisSaleActivity.class);
						intent.putExtra(ItemVolumeDisSaleActivity.USER_INFO_KEY, salemanInfo.toString());
						intent.putExtra(ItemVolumeDisSaleActivity.CUSTOMER_INFO_KEY, customer);
						startActivity(intent);
						// We finish this activity because we need to select no customer when visit by back button.
						finish();
					}
				}
			});

			generalSaleButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {

					if (Preferences.wasUploadedSaleDataToServer(CustomerActivity.this)) {

						int count = 0;
						Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM INVOICE", null);
						if (cursor.moveToNext()) {

							count = cursor.getInt(cursor.getColumnIndex("COUNT"));
						}

						if (count > 0) {

							new AlertDialog.Builder(CustomerActivity.this)
								.setTitle("General sale")
								.setMessage("Your uploaded sale data is not clear yet.\n"
										+ "Before sale, clear all data.")
								.setPositiveButton("OK", null)
								.show();

							return;
						}
					}

					if (didCustomerSelected()) {

						Intent intent = new Intent(CustomerActivity.this, GeneralSaleActivity.class);
						intent.putExtra(GeneralSaleActivity.USER_INFO_KEY, salemanInfo.toString());
						intent.putExtra(GeneralSaleActivity.CUSTOMER_INFO_KEY, customer);
						startActivity(intent);
						// We finish this activity because we need to select no customer when visit by back button.
						finish();
					}
				}
			});

			packageSaleButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {

					if (Preferences.wasUploadedSaleDataToServer(CustomerActivity.this)) {

						int count = 0;
						Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM INVOICE", null);
						if (cursor.moveToNext()) {

							count = cursor.getInt(cursor.getColumnIndex("COUNT"));
						}

						if (count > 0) {

							new AlertDialog.Builder(CustomerActivity.this)
								.setTitle("Package sale")
								.setMessage("Your uploaded sale data is not clear yet.\n"
										+ "Before sale, clear all data.")
								.setPositiveButton("OK", null)
								.show();

							return;
						}
					}

					if (didCustomerSelected()) {

						Intent intent = new Intent(CustomerActivity.this, PackageSaleActivity.class);
						intent.putExtra(PackageSaleActivity.USER_INFO_KEY, salemanInfo.toString());
						intent.putExtra(PackageSaleActivity.CUSTOMER_INFO_KEY, customer);
						startActivity(intent);
						// We finish this activity because we need to select no customer when visit by back button.
						finish();
					}
				}
			});

			reportButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					if (Preferences.wasUploadedCustomerFeedbacksToServer(CustomerActivity.this)) {

						int count = 0;
						Cursor cursor = database.rawQuery(
								"SELECT COUNT(*) AS COUNT FROM DID_CUSTOMER_FEEDBACK", null);
						if (cursor.moveToNext()) {

							count = cursor.getInt(cursor.getColumnIndex("COUNT"));
						}

						if (count > 0) {

							new AlertDialog.Builder(CustomerActivity.this)
								.setTitle("General sale")
								.setMessage("Your uploaded customer feedbacks is not clear yet.\n"
										+ "Before sale, clear all data.")
								.setPositiveButton("OK", null)
								.show();

							return;
						}
					}

					if (didCustomerSelected()) {

						LayoutInflater layoutInflater = (LayoutInflater) CustomerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						final View view = layoutInflater.inflate(R.layout.dialog_box_customer_feedback, null);

						final Spinner descriptionsSpinner = (Spinner) view.findViewById(R.id.description);
						final EditText remarkEditText = (EditText) view.findViewById(R.id.remark);

						final ArrayList<CustomerFeedback> customerFeedbacks = new ArrayList<CustomerFeedback>();

						Cursor cursor = database.rawQuery("SELECT * FROM CUSTOMER_FEEDBACK", null);
						while (cursor.moveToNext()) {

							customerFeedbacks.add(new CustomerFeedback(
									cursor.getString(cursor.getColumnIndex("INV_NO"))
									, cursor.getString(cursor.getColumnIndex("INV_DATE"))
									, cursor.getString(cursor.getColumnIndex("SERIAL_NO"))
									, cursor.getString(cursor.getColumnIndex("DESCRIPTION"))));
						}

						final AlertDialog alertDialog = new AlertDialog.Builder(CustomerActivity.this)
							.setView(view)
							.setTitle("Feedback")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {

									try {

										String salemanId = salemanInfo.getString("userId");
										String deviceId = ((TelephonyManager) CustomerActivity.this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//										String invoiceNumber = Utils.getInvoiceNo(CustomerActivity.this, salemanInfo.getString("userId"), salemanInfo.getString("locationCode"), Utils.FOR_OTHERS);
										String invoiceNumber = Utils.getInvoiceID(getApplicationContext(), Utils.MODE_CUSTOMER_FEEDBACK, salemanInfo.getString("userId"), salemanInfo.getString("locationCode"));
										String invoiceDate = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date());
										//String invoiceDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
										//Toast.makeText(getApplicationContext(), "Date is : " + invoiceDate, Toast.LENGTH_LONG).show();
										String customerNumber = customer.getCustomerId();
										String locationNumber = salemanInfo.getString("locationCode");
										String feedbackNumber = customerFeedbacks.get(descriptionsSpinner.getSelectedItemPosition()).getInvoiceNumber();
										//Log.e("feedbackNo>>>", feedbackNumber);
										String feedbackDate = customerFeedbacks.get(descriptionsSpinner.getSelectedItemPosition()).getInvoiceDate();
										String serialNumber = customerFeedbacks.get(descriptionsSpinner.getSelectedItemPosition()).getSerialNumber();
										String description = customerFeedbacks.get(descriptionsSpinner.getSelectedItemPosition()).getDescription();
                                        Log.e("desc>>>", description);
										String remark = remarkEditText.getText().toString();

										database.beginTransaction();
										database.execSQL("INSERT INTO DID_CUSTOMER_FEEDBACK VALUES (\""
												+ salemanId + "\","
												+ "\"" + deviceId + "\","
												+ "\"" + invoiceNumber + "\","
												+ "\"" + invoiceDate + "\","
												+ "\"" + customerNumber + "\","
												+ "\"" + locationNumber + "\","
												+ "\"" + feedbackNumber + "\","
												+ "\"" + feedbackDate + "\","
												+ "\"" + serialNumber + "\","
												+ "\"" + description + "\","
												+ "\"" + remark + "\")");
										database.setTransactionSuccessful();
										database.endTransaction();
									} catch (JSONException e) {

										e.printStackTrace();
									}

									Preferences.didUploadedCustomerFeedbacksToServer(CustomerActivity.this, false);
								}
							})
							.setNegativeButton("Cancel", null)
							.create();
						alertDialog.setOnShowListener(new OnShowListener() {
							
							@Override
							public void onShow(DialogInterface arg0) {

								ArrayList<String> descriptions = new ArrayList<String>();
								for (CustomerFeedback customerFeedback : customerFeedbacks) {

									descriptions.add(customerFeedback.getDescription());
								}
								ArrayAdapter<String> descriptionsArrayAdapter = new ArrayAdapter<String>(CustomerActivity.this, android.R.layout.simple_spinner_item, descriptions);
								descriptionsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								descriptionsSpinner.setAdapter(descriptionsArrayAdapter);
							}
						});
						alertDialog.show();
					}
				}
			});
		}

		database = new Database(this).getDataBase();

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				CustomerActivity.this.onBackPressed();
			}
		});

		try {

			usernameTextView.setText(salemanInfo.getString("userName"));
		} catch (JSONException e) {

			e.printStackTrace();
		}
		dateTextView.setText(Utils.getCurrentDate(false));

		// Initial setup customers list view
		customers = new ArrayList<Customer>();
		customerListForArrayAdapter = new ArrayList<Customer>();
		Cursor cursor = database.rawQuery("SELECT * FROM CUSTOMER ORDER BY CUSTOMER_NAME ASC", null);
		while (cursor.moveToNext()) {

			/*Customer customer = new Customer(
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
					, cursor.getString(cursor.getColumnIndex("IS_IN_ROUTE")));*/

            String customer_name = cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME"));
            String address = cursor.getString(cursor.getColumnIndex("ADDRESS"));
            String customer_id = cursor.getString(cursor.getColumnIndex("CUSTOMER_ID"));
            String customer_type_id = cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_ID"));
            String customer_type_name = cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_NAME"));
            String ph = cursor.getString(cursor.getColumnIndex("PH"));
            String township = cursor.getString(cursor.getColumnIndex("TOWNSHIP"));
            String credit_term = cursor.getString(cursor.getColumnIndex("CREDIT_TERM"));
            double credit_limit = cursor.getDouble(cursor.getColumnIndex("CREDIT_LIMIT"));
            double credit_amt = cursor.getDouble(cursor.getColumnIndex("CREDIT_AMT"));
            double due_amt = cursor.getDouble(cursor.getColumnIndex("DUE_AMT"));
            double prepaid_amt = cursor.getDouble(cursor.getColumnIndex("PREPAID_AMT"));
            String payment_type = cursor.getString(cursor.getColumnIndex("PAYMENT_TYPE"));
            String is_in_route = cursor.getString(cursor.getColumnIndex("IS_IN_ROUTE"));

            customer = new Customer(customer_id, customer_name,customer_type_id, customer_type_name, address, ph, township, credit_term, credit_limit,
                    credit_amt, due_amt, prepaid_amt, payment_type, is_in_route);

            Customer customer_ = new Customer(customer_name, address);
            customer_array.add(customer_);

			//customers.add(customer);
			customerListForArrayAdapter.add(customer_);
		}

		customerListArrayAdapter = new CustomerListArrayAdapter(this);
		customersListView.setAdapter(customerListArrayAdapter);

		customersListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {

				/*customer = customers.get(position);

				customerNameTextView.setText(customer.getCustomerName());
				phoneTextView.setText(customer.getPhone());
				addressTextView.setText(customer.getAddress());
				townshipTextView.setText(customer.getTownship());
				creditTermsTextView.setText(customer.getCreditTerms());
				creditLimitTextView.setText(customer.getCreditLimit() + "");
				creditAmountTextView.setText(customer.getCreditAmt() + "");
				dueAmountTextView.setText(customer.getDueAmt() + "");
				prepaidAmountTextView.setText(customer.getPrepaidAmt() + "");
				paymentTypeTextView.setText(customer.getPaymentType());*/

                String selected_cus = customer_array.get(position).getCustomerName();
                Log.e("Cus Name>>>", selected_cus);

                Cursor cursor = database.rawQuery("SELECT * FROM CUSTOMER WHERE CUSTOMER_NAME = '" + selected_cus + "'", null);
                while (cursor.moveToNext()){

                    String customer_name = cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME"));
                    String address = cursor.getString(cursor.getColumnIndex("ADDRESS"));
                    String customer_id = cursor.getString(cursor.getColumnIndex("CUSTOMER_ID"));
                    String customer_type_id = cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_ID"));
                    String customer_type_name = cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_NAME"));
                    String ph = cursor.getString(cursor.getColumnIndex("PH"));
                    String township = cursor.getString(cursor.getColumnIndex("TOWNSHIP"));
                    String credit_term = cursor.getString(cursor.getColumnIndex("CREDIT_TERM"));
                    double credit_limit = cursor.getDouble(cursor.getColumnIndex("CREDIT_LIMIT"));
                    double credit_amt = cursor.getDouble(cursor.getColumnIndex("CREDIT_AMT"));
                    double due_amt = cursor.getDouble(cursor.getColumnIndex("DUE_AMT"));
                    double prepaid_amt = cursor.getDouble(cursor.getColumnIndex("PREPAID_AMT"));
                    String payment_type = cursor.getString(cursor.getColumnIndex("PAYMENT_TYPE"));
                    String is_in_route = cursor.getString(cursor.getColumnIndex("IS_IN_ROUTE"));

                    customer = new Customer(customer_id, customer_name,customer_type_id, customer_type_name, address, ph, township, credit_term, credit_limit,
                            credit_amt, due_amt, prepaid_amt, payment_type, is_in_route);

                    customerNameTextView.setText(cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME")));
                    phoneTextView.setText(cursor.getString(cursor.getColumnIndex("PH")));
                    addressTextView.setText(cursor.getString(cursor.getColumnIndex("ADDRESS")));
                    townshipTextView.setText(cursor.getString(cursor.getColumnIndex("TOWNSHIP")));
                    creditTermsTextView.setText(cursor.getString(cursor.getColumnIndex("CREDIT_TERM")));
                    creditLimitTextView.setText(cursor.getString(cursor.getColumnIndex("CREDIT_LIMIT")));
                    creditAmountTextView.setText(cursor.getString(cursor.getColumnIndex("CREDIT_AMT")));
                    dueAmountTextView.setText(cursor.getString(cursor.getColumnIndex("DUE_AMT")));
                    prepaidAmountTextView.setText(cursor.getString(cursor.getColumnIndex("PREPAID_AMT")));
                    paymentTypeTextView.setText(cursor.getString(cursor.getColumnIndex("PAYMENT_TYPE")));
                }
			}
		});

		searchCustomersEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence characterSequence, int arg1, int arg2, int arg3) {

				customerListForArrayAdapter.clear();
                customer_array.clear();

				/*for (Customer customer : customers) {

					if (customer.getCustomerName().toLowerCase()
						.contains(characterSequence.toString().toLowerCase())) {

						customerListForArrayAdapter.add(customer);
						customerListArrayAdapter.notifyDataSetChanged();
					}
				}*/

				Cursor cursor = database.rawQuery("SELECT * FROM CUSTOMER WHERE CUSTOMER_NAME LIKE '"+ searchCustomersEditText.getText().toString() +"%'", null);
				while(cursor.moveToNext()){

                    String customer_name = cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME"));
                    String address = cursor.getString(cursor.getColumnIndex("ADDRESS"));
                    String customer_id = cursor.getString(cursor.getColumnIndex("CUSTOMER_ID"));
                    String customer_type_id = cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_ID"));
                    String customer_type_name = cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_NAME"));
                    String ph = cursor.getString(cursor.getColumnIndex("PH"));
                    String township = cursor.getString(cursor.getColumnIndex("TOWNSHIP"));
                    String credit_term = cursor.getString(cursor.getColumnIndex("CREDIT_TERM"));
                    double credit_limit = cursor.getDouble(cursor.getColumnIndex("CREDIT_LIMIT"));
                    double credit_amt = cursor.getDouble(cursor.getColumnIndex("CREDIT_AMT"));
                    double due_amt = cursor.getDouble(cursor.getColumnIndex("DUE_AMT"));
                    double prepaid_amt = cursor.getDouble(cursor.getColumnIndex("PREPAID_AMT"));
                    String payment_type = cursor.getString(cursor.getColumnIndex("PAYMENT_TYPE"));
                    String is_in_route = cursor.getString(cursor.getColumnIndex("IS_IN_ROUTE"));

                    customer = new Customer(customer_id, customer_name,customer_type_id, customer_type_name, address, ph, township, credit_term, credit_limit,
                            credit_amt, due_amt, prepaid_amt, payment_type, is_in_route);

					Customer customer_ = new Customer(customer_name, address);
                    customer_array.add(customer_);

					customerListForArrayAdapter.add(customer_);
					customerListArrayAdapter.notifyDataSetChanged();

                    customersListView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String selected_cus = customer_array.get(position).getCustomerName();
                            Log.e("Cus Name>>>search>>>", selected_cus);

                            Cursor cursor = database.rawQuery("SELECT * FROM CUSTOMER WHERE CUSTOMER_NAME = '" + selected_cus + "'", null);
                            while (cursor.moveToNext()){

                                String customer_name = cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME"));
                                String address = cursor.getString(cursor.getColumnIndex("ADDRESS"));
                                String customer_id = cursor.getString(cursor.getColumnIndex("CUSTOMER_ID"));
                                String customer_type_id = cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_ID"));
                                String customer_type_name = cursor.getString(cursor.getColumnIndex("CUSTOMER_TYPE_NAME"));
                                String ph = cursor.getString(cursor.getColumnIndex("PH"));
                                String township = cursor.getString(cursor.getColumnIndex("TOWNSHIP"));
                                String credit_term = cursor.getString(cursor.getColumnIndex("CREDIT_TERM"));
                                double credit_limit = cursor.getDouble(cursor.getColumnIndex("CREDIT_LIMIT"));
                                double credit_amt = cursor.getDouble(cursor.getColumnIndex("CREDIT_AMT"));
                                double due_amt = cursor.getDouble(cursor.getColumnIndex("DUE_AMT"));
                                double prepaid_amt = cursor.getDouble(cursor.getColumnIndex("PREPAID_AMT"));
                                String payment_type = cursor.getString(cursor.getColumnIndex("PAYMENT_TYPE"));
                                String is_in_route = cursor.getString(cursor.getColumnIndex("IS_IN_ROUTE"));

                                customer = new Customer(customer_id, customer_name,customer_type_id, customer_type_name, address, ph, township, credit_term, credit_limit,
                                        credit_amt, due_amt, prepaid_amt, payment_type, is_in_route);

                                customerNameTextView.setText(cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME")));
                                phoneTextView.setText(cursor.getString(cursor.getColumnIndex("PH")));
                                addressTextView.setText(cursor.getString(cursor.getColumnIndex("ADDRESS")));
                                townshipTextView.setText(cursor.getString(cursor.getColumnIndex("TOWNSHIP")));
                                creditTermsTextView.setText(cursor.getString(cursor.getColumnIndex("CREDIT_TERM")));
                                creditLimitTextView.setText(cursor.getString(cursor.getColumnIndex("CREDIT_LIMIT")));
                                creditAmountTextView.setText(cursor.getString(cursor.getColumnIndex("CREDIT_AMT")));
                                dueAmountTextView.setText(cursor.getString(cursor.getColumnIndex("DUE_AMT")));
                                prepaidAmountTextView.setText(cursor.getString(cursor.getColumnIndex("PREPAID_AMT")));
                                paymentTypeTextView.setText(cursor.getString(cursor.getColumnIndex("PAYMENT_TYPE")));
                            }
                        }
                    });
				}

			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {}
			
			@Override
			public void afterTextChanged(Editable arg0) {}
		});
	}

	private boolean didCustomerSelected() {

		if (customerNameTextView.getText().length() == 0) {

			new AlertDialog.Builder(this)
				.setTitle("Customer is required")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("You need to select customer.")
				.setPositiveButton("OK", null)
				.show();

			return false;
		}

		return true;
	}

	private class CustomerListArrayAdapter extends ArrayAdapter<Customer> {
		
		public final Activity context;

		public CustomerListArrayAdapter(Activity context) {

			super(context, R.layout.custom_simple_list_item_1, customerListForArrayAdapter);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final Customer customer = customerListForArrayAdapter.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.custom_simple_list_item_1, null, true);

			String address = customer.getAddress();
			if (customer.getAddress().length() >= 10) {

				address = customer.getAddress().substring(0, 10);
			}
			if (customer.getAddress().length() > 10) {

				address += "...";
			}

			TextView textView = (TextView) view.findViewById(android.R.id.text1);
			textView.setText(customer.getCustomerName() + "(" + address + ")");

			return view;
		}
	}
}
