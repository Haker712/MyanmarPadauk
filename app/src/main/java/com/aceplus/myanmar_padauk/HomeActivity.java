package com.aceplus.myanmar_padauk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aceplus.myanmar_padauk.delivery.DeliveryActivity;
import com.aceplus.myanmar_padauk.report.ReportHomeActivity;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Preferences;
import com.aceplus.myanmar_padauk.utils.TLAJsonStringMaker;
import com.aceplus.myanmar_padauk.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends Activity {

	public static final String USER_INFO_KEY = "user-info-key";

	private JSONObject userInfo;

	Button cancelButton;

	TextView usernameTextView;
	TextView dateTextView;

	Button reissueButton;
	Button uploadToServerButton;

	Button customerButton;
	Button addNewCustomerButton;
	private Button preOrderButton;
	private Button deliveryButton;
	Button reportButton;

	TextView clearAllDataTextView;

	SQLiteDatabase database;

	ProgressDialog progressDialog;

    int readTimeOut = 5000;

    Gson gson;

	public static final String PREF_FILE_NAME = "pref-file";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		cancelButton = (Button) findViewById(R.id.cancel);

		usernameTextView = (TextView) findViewById(R.id.username);
		dateTextView = (TextView) findViewById(R.id.date);

		reissueButton = (Button) findViewById(R.id.reissue);
		uploadToServerButton = (Button) findViewById(R.id.uploadToServer);

		customerButton = (Button) findViewById(R.id.customer);
		addNewCustomerButton = (Button) findViewById(R.id.addNewCustomer);
		this.preOrderButton = (Button) findViewById(R.id.preOrder);
		this.deliveryButton = (Button) findViewById(R.id.delivery);
		reportButton = (Button) findViewById(R.id.report);

		clearAllDataTextView = (TextView) findViewById(R.id.clearAllData);

		database = new Database(this).getDataBase();

		try {

			userInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));

			usernameTextView.setText(userInfo.getString("userName"));
		} catch (JSONException e) {

			e.printStackTrace();
		}


		//if un-uploaded data is existed, backup process can't work. So, manually do backup database
		usernameTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(HomeActivity.this)
						.setTitle("Backup database")
						.setMessage("Are you sure want to do?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								backupDB();
							}
						})
						.setNegativeButton("No", null)
						.show();
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				HomeActivity.this.onBackPressed();
			}
		});

		dateTextView.setText(Utils.getCurrentDate(false));

		reissueButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Reissue")
					.setMessage("Your uploaded sale data is not clear yet.\n"
							+ "Before reissue, clear all data")
					.setPositiveButton("OK", null);

				Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM INVOICE", null);
				if (cursor.moveToNext()
						&& cursor.getInt(cursor.getColumnIndex("COUNT")) > 0) {

					alertDialog.show();
					return;
				}

				new GetProducts().execute();
			}
		});

        gson = new Gson();

        //testing ..... notice to delete after test
		//Preferences.didUploadedNewCustomersToServer(HomeActivity.this, false);
        //Preferences.didUploadedSaleDataToServer(HomeActivity.this, false);

		uploadToServerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Check customer feedbacks are already uploaded to server or not.
				int countCustomerFeedback = 0;
				Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM DID_CUSTOMER_FEEDBACK", null);
				if (cursor.moveToNext()) {

					countCustomerFeedback += cursor.getInt(cursor.getColumnIndex("COUNT"));
				}

                if(countCustomerFeedback >= 20) {
                    readTimeOut += 5000;
                }

				if (countCustomerFeedback == 0) {

					Toast.makeText(HomeActivity.this, "No customer feedbacks to upload."
							, Toast.LENGTH_LONG).show();
					checkSaleDataExistsOrNot();
				} else if (Preferences.wasUploadedCustomerFeedbacksToServer(HomeActivity.this)) {
					
					Toast.makeText(HomeActivity.this, "Customer feedbacks are already uploaded to server."
							, Toast.LENGTH_LONG).show();
					checkSaleDataExistsOrNot();
				} else {

					new UploadCustomerFeedbacks().execute();
				}

				//Toast.makeText(HomeActivity.this, "Successfully uploaded to server.", Toast.LENGTH_SHORT).show();

				/*// Check sale data are already uploaded to server or not.
				int countSaleData = 0;
				cursor = database.rawQuery("SELECT COUNT(*) COUNT FROM INVOICE", null);
				if (cursor.moveToNext()) {

					countSaleData += cursor.getInt(cursor.getColumnIndex("COUNT"));
				}

				if (countSaleData == 0) {

					Toast.makeText(HomeActivity.this, "No sale data to upload.", Toast.LENGTH_LONG).show();
				} else if (Preferences.wasUploadedSaleDataToServer(HomeActivity.this)) {

					Toast.makeText(HomeActivity.this, "Sale data are already uploaded to server."
							, Toast.LENGTH_LONG).show();
				} else {

					new UploadSaleData().execute();
				}

				// Check new customers are already uploaded to server or not.
				int countNewCustomers= 0;
				cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM NEW_CUSTOMER", null);
				if (cursor.moveToNext()) {

					countNewCustomers += cursor.getInt(cursor.getColumnIndex("COUNT"));
				}

				if (countNewCustomers == 0) {

					Toast.makeText(HomeActivity.this, "No new customer to upload."
							, Toast.LENGTH_LONG).show();
				} else if (Preferences.wasUploadedNewCustomersToServer(HomeActivity.this)) {
					
					Toast.makeText(HomeActivity.this, "New customers are already uploaded to server."
							, Toast.LENGTH_LONG).show();
				} else {

					new UploadNewCustomers().execute();
				}

				// Check pre-order data are already uploaded to server or not.
				int countPreOrderData = 0;
				cursor = database.rawQuery("SELECT COUNT(*) COUNT FROM PRE_ORDER", null);
				if (cursor.moveToNext()) {

					countPreOrderData += cursor.getInt(cursor.getColumnIndex("COUNT"));
				}

				if (countPreOrderData == 0) {

					Toast.makeText(HomeActivity.this, "No pre-order data to upload.", Toast.LENGTH_LONG).show();
				} else if (Preferences.wasUploadedPreOrderDataToServer(HomeActivity.this)) {

					Toast.makeText(HomeActivity.this, "Pre-Order data are already uploaded to server."
							, Toast.LENGTH_LONG).show();
				} else {

					new UploadPreOrders().execute();
				}

				// Check delivered data are already uploaded to server or not.
				int countDeliveredData = 0;
				cursor = database.rawQuery("SELECT COUNT(*) COUNT FROM DELIVERED_DATA", null);
				if (cursor.moveToNext()) {

					countDeliveredData += cursor.getInt(cursor.getColumnIndex("COUNT"));
				}

				if (countDeliveredData == 0) {

					Toast.makeText(HomeActivity.this, "No delivered data to upload.", Toast.LENGTH_LONG).show();
				} else if (Preferences.wasUploadedDeliveredDataToServer(HomeActivity.this)) {

					Toast.makeText(HomeActivity.this, "Delivered data are already uploaded to server."
							, Toast.LENGTH_LONG).show();
				} else {

					new UploadDeliveredData().execute();
				}*/
			}
		});
		
		customerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(HomeActivity.this, CustomerActivity.class);
				intent.putExtra(CustomerActivity.USER_INFO_KEY, userInfo.toString());
				startActivity(intent);
			}
		});

		addNewCustomerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (Preferences.wasUploadedNewCustomersToServer(HomeActivity.this)) {

					int count = 0;
					Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM NEW_CUSTOMER", null);
					if (cursor.moveToNext()) {

						count = cursor.getInt(cursor.getColumnIndex("COUNT"));
					}

					if (count > 0) {

						new AlertDialog.Builder(HomeActivity.this)
							.setTitle("New Customer")
							.setMessage("Your uploaded new customers data is not clear yet.\n"
									+ "Before sale, clear all data.")
							.setPositiveButton("OK", null)
							.show();

						return;
					}
				}

				Intent intent = new Intent(HomeActivity.this, AddNewCustomerActivity.class);
				intent.putExtra(AddNewCustomerActivity.USER_INFO_KEY, userInfo.toString());
				startActivity(intent);
			}
		});

		this.preOrderButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (Preferences.wasUploadedPreOrderDataToServer(HomeActivity.this)) {

					int count = 0;
					Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM PRE_ORDER", null);
					if (cursor.moveToNext()) {

						count = cursor.getInt(cursor.getColumnIndex("COUNT"));
					}

					if (count > 0) {

						new AlertDialog.Builder(HomeActivity.this)
							.setTitle("Pre-Order")
							.setMessage("Your pre-order data is not clear yet.\n"
									+ "Before pre-order, clear all data.")
							.setPositiveButton("OK", null)
							.show();

						return;
					}
				}

				Intent intent = new Intent(HomeActivity.this, CustomerActivity.class);
				intent.putExtra(CustomerActivity.IS_PRE_ORDER, true);
				intent.putExtra(CustomerActivity.USER_INFO_KEY, userInfo.toString());
				startActivity(intent);
			}
		});

		this.deliveryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (Preferences.wasUploadedDeliveredDataToServer(HomeActivity.this)) {

					int count = 0;
					Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM DELIVERED_DATA", null);
					if (cursor.moveToNext()) {

						count = cursor.getInt(cursor.getColumnIndex("COUNT"));
					}

					if (count > 0) {

						new AlertDialog.Builder(HomeActivity.this)
							.setTitle("Delivery")
							.setMessage("Your uploaded delivered data is not clear yet.\n"
									+ "Before sale, clear all data.")
							.setPositiveButton("OK", null)
							.show();

						return;
					}
				}

				Intent intent = new Intent(HomeActivity.this, DeliveryActivity.class);
				intent.putExtra(DeliveryActivity.USER_INFO_KEY, userInfo.toString());
				startActivity(intent);
			}
		});
		reportButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(HomeActivity.this, ReportHomeActivity.class);
				intent.putExtra(ReportHomeActivity.USER_INFO_KEY, userInfo.toString());
				startActivity(intent);
			}
		});

		clearAllDataTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Clear all data")
					.setPositiveButton("OK", null);

				int countData = 0;
				Cursor cursor = database.rawQuery(
						"SELECT COUNT(*) AS COUNT FROM DID_CUSTOMER_FEEDBACK", null);
				if (cursor.moveToNext()) {

					countData += cursor.getInt(cursor.getColumnIndex("COUNT"));
					if (countData > 0 
							&& !Preferences.wasUploadedCustomerFeedbacksToServer(HomeActivity.this)) {

						alertDialog.setMessage("Customer feedbacks not uploaded to server yet.")
							.show();
						return;
					}
				}

				cursor = database.rawQuery(
						"SELECT COUNT(*) AS COUNT FROM INVOICE", null);
				countData = 0;
				if (cursor.moveToNext()) {

					countData = cursor.getInt(cursor.getColumnIndex("COUNT"));
					if (countData > 0
							&& !Preferences.wasUploadedSaleDataToServer(HomeActivity.this)) {

						alertDialog.setMessage("Sale data not uploaded to server yet.")
							.show();
						return;
					}
				}

				cursor = database.rawQuery(
						"SELECT COUNT(*) AS COUNT FROM PRE_ORDER", null);
				countData = 0;
				if (cursor.moveToNext()) {

					countData = cursor.getInt(cursor.getColumnIndex("COUNT"));
					if (countData > 0
							&& !Preferences.wasUploadedPreOrderDataToServer(HomeActivity.this)) {

						alertDialog.setMessage("Pre-Order data not uploaded to server yet.")
							.show();
						return;
					}
				}

				cursor = database.rawQuery(
						"SELECT COUNT(*) AS COUNT FROM DELIVERED_DATA", null);
				countData = 0;
				if (cursor.moveToNext()) {

					countData = cursor.getInt(cursor.getColumnIndex("COUNT"));
					if (countData > 0
							&& !Preferences.wasUploadedDeliveredDataToServer(HomeActivity.this)) {

						alertDialog.setMessage("Delivered data not uploaded to server yet.")
							.show();
						return;
					}
				}

				new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Clear all data")
					.setMessage("Are you sure you want to clear all data?")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

                            backupDB();

							database.beginTransaction();
							String[] tableNames = {"CUSTOMER", "CUSTOMER_CATEGORY", "CUSTOMER_FEEDBACK", "DID_CUSTOMER_FEEDBACK"
									, "INVOICE", "ITEM_DISCOUNT", "PACKAGE", "PRODUCT", "SALE_MAN", "VOLUME_DISCOUNT", "ZONE", "NEW_CUSTOMER"
									, "PRE_ORDER", "DELIVERY", "DELIVERED_DATA", "duplicate_record"};
							for (String tableName: tableNames) {

								database.execSQL("DELETE FROM " + tableName);
							}
							database.setTransactionSuccessful();	
							database.endTransaction();

							//delete add new customer count
							SharedPreferences sharedPreferences =
									HomeActivity.this.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

							sharedPreferences.edit().putInt("todayAddedCustomerCount", 0).commit();

                            //clear database and cache data by java class
                            //ClearDB_and_Cache.getInstance().clearApplicationData();

							Cursor cursor = database.rawQuery("SELECT * FROM INVOICE", null);
							System.out.println(cursor.getCount());
                            
							new AlertDialog.Builder(HomeActivity.this)
								.setTitle("Clear all data")
								.setMessage("Successfully clear all data.\n"
										+ "You need to connect server and login again.")
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										startActivity(new Intent(HomeActivity.this, LoginActivity.class));
										HomeActivity.this.finish();
									}
								})
								.setCancelable(false)
								.show();
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
			}
		});
	}

	@Override
	public void onBackPressed() {

		new AlertDialog.Builder(this)
			.setTitle("Log out")
			.setMessage("Are you sure you want to exit?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {

					startActivity(new Intent(HomeActivity.this, LoginActivity.class));
					finish();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}

	@SuppressLint("NewApi")
	private class UploadCustomerFeedbacks extends AsyncTask<Void, Void, String> {

    	JSONArray customerFeedbacks;

    	long numberOfUploadedCustomerFeedbacks = 0;

		@Override
		protected void onPreExecute() {

			Toast.makeText(HomeActivity.this, "Uploading customer feedbacks to server.", Toast.LENGTH_LONG).show();

			super.onPreExecute();

			customerFeedbacks = new JSONArray();
			Cursor cursor = database.rawQuery("SELECT * FROM DID_CUSTOMER_FEEDBACK", null);
			while (cursor.moveToNext()) {

				JSONObject jsonObject = new JSONObject();
				try {

					jsonObject.put("saleManId", cursor.getString(cursor.getColumnIndex("SALEMAN_ID")));
					jsonObject.put("devId", cursor.getString(cursor.getColumnIndex("DEV_ID")));
					jsonObject.put("invNo", cursor.getString(cursor.getColumnIndex("INV_NO")));
					jsonObject.put("invDate", cursor.getString(cursor.getColumnIndex("INV_DATE")).replace("/", "-"));
					jsonObject.put("cusNo", cursor.getString(cursor.getColumnIndex("CUSTOMER_NO")));
					jsonObject.put("lNo", cursor.getString(cursor.getColumnIndex("LOCATION_NO")));
					jsonObject.put("feedBackNo", cursor.getString(cursor.getColumnIndex("FEEDBACK_NO")));
					jsonObject.put("feedBackDate", cursor.getString(cursor.getColumnIndex("FEEDBACK_DATE")));
					jsonObject.put("srNo", cursor.getString(cursor.getColumnIndex("SERIAL_NO")));
					jsonObject.put("description", cursor.getString(cursor.getColumnIndex("DESCRIPTION")));
					
					byte[] remarkBytes = cursor.getString(cursor.getColumnIndex("REMARK")).getBytes(Charset.forName("UTF-8"));
					String remark = "[";
					for (int i = 0; i < remarkBytes.length; i++) {

						remark += remarkBytes[i];
						if (i != remarkBytes.length - 1) {

							remark += ",";
						}
					}
					remark += "]";
					jsonObject.put("remark", remark);
				} catch (JSONException e) {

					e.printStackTrace();
				}
				customerFeedbacks.put(jsonObject);
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			String result = null;

			try {

				URL url = new URL(DataDownloadActivity.URL + "cus/savecustomerfeedback");
				HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setReadTimeout(readTimeOut);
				httpUrlConnection.setConnectTimeout(readTimeOut);
				httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type", "application/json");

				String sendingString = customerFeedbacks.toString().replace("\\", "");
				sendingString = sendingString.replace("\"[", "[");
				sendingString = sendingString.replace("]\"", "]");
//				sendingString = sendingString.replace("/", "-");

				Log.e("Feedbacksend>>>>", sendingString);

  				OutputStream outputStream = httpUrlConnection.getOutputStream();
				outputStream.write(sendingString.getBytes());
				outputStream.flush();

                Preferences.setCustomerFeedbackLastUploadedDate(getApplicationContext(), new Date());
                Preferences.addCustomerFeedbackUploadedCount(getApplicationContext(), numberOfUploadedCustomerFeedbacks);

                Preferences.didUploadedCustomerFeedbacksToServer(HomeActivity.this, true);

				if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

					return null;
				} else {

					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
					String output;
					while ((output = bufferedReader.readLine()) != null) {

						result = output;
					}

					numberOfUploadedCustomerFeedbacks = customerFeedbacks.length();
				}

				httpUrlConnection.disconnect();
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

//			progressDialog.dismiss();

			if (result == null) {

				new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Upload data to server")
					.setMessage("Can't connect to server.")
					.setPositiveButton("OK", null)
					.show();

				return;
			}
			else {
				//Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
				try {
					JSONObject resultJson = new JSONObject(result);
                    if (!resultJson.getString("status").equalsIgnoreCase("success")) {

                        if(resultJson.getString("status").equalsIgnoreCase("duplicate record")) {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status") + " in uploading customer feedbacks.Click 'Ok' to continue.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            checkSaleDataExistsOrNot();
                                        }
                                    })
                                    .show();


                            return;
                        }
                        else {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status"))
                                    .show();

                            return;
                        }
                    }else if (resultJson.getString("status").equalsIgnoreCase("success")) {

						Toast.makeText(HomeActivity.this, "Customer feedbacks are successfully uploaded.", Toast.LENGTH_LONG).show();

                        /*String str = resultJson.get("duplicateIdList").toString();

                        String[] mystr = gson.fromJson(str, String[].class);
                        Log.e("mystr len>>>", mystr.length + "");

                        database.beginTransaction();
                        for(int i = 0; i < mystr.length; i++) {
                            //Toast.makeText(getApplicationContext(), "duplicate customer : " + mystr[i], Toast.LENGTH_SHORT).show();
                            database.execSQL("insert into duplicate_record values ('"+mystr[i]+"','CF')");

                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();*/

                        checkSaleDataExistsOrNot();

					}
				} catch (JSONException e) {

					e.printStackTrace();
				}
			}


//			if (haveSaleDataToUploadFlag) {
//
//				new UploadSaleData().execute();
//			}
		}
	}

	private class UploadSaleData extends AsyncTask<Void, Void, String> {

    	JSONArray saleData;

		@Override
		protected void onPreExecute() {

			Toast.makeText(HomeActivity.this, "Uploading sale data to server.", Toast.LENGTH_LONG).show();

			super.onPreExecute();

			saleData = new JSONArray();
			Cursor cursor = database.rawQuery("SELECT * FROM INVOICE", null);
			while (cursor.moveToNext()) {

				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("customerID", cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")));
					jsonObject.put("saleDate", cursor.getString(cursor.getColumnIndex("SALE_DATE")).replace("/", "-"));
					jsonObject.put("invoiceID", cursor.getString(cursor.getColumnIndex("INVOICE_ID")));
					jsonObject.put("totalAmt", cursor.getDouble(cursor.getColumnIndex("TOTAL_AMOUNT")));
					jsonObject.put("totalDiscountAmt", cursor.getDouble(cursor.getColumnIndex("TOTAL_DISCOUNT_AMOUNT")));
					jsonObject.put("payAmt", cursor.getDouble(cursor.getColumnIndex("PAY_AMOUNT")));
					jsonObject.put("refundAmt", cursor.getDouble(cursor.getColumnIndex("REFUND_AMOUNT")));
					jsonObject.put("receitpPersonName", cursor.getString(cursor.getColumnIndex("RECEIPT_PERSON_NAME")));
					jsonObject.put("salePersonID", cursor.getString(cursor.getColumnIndex("SALE_PERSON_ID")));
					jsonObject.put("dueDate", cursor.getString(cursor.getColumnIndex("DUE_DATE")).replace("/", "-"));
					jsonObject.put("cashOrCredit", cursor.getString(cursor.getColumnIndex("CASH_OR_CREDIT")));
					jsonObject.put("locationCode", cursor.getString(cursor.getColumnIndex("LOCATION_CODE")));
					jsonObject.put("devID", cursor.getString(cursor.getColumnIndex("DEVICE_ID")));
					jsonObject.put("invoiceTime", cursor.getString(cursor.getColumnIndex("INVOICE_TIME")).replace("/", "-"));
					jsonObject.put("packageInvno", cursor.getString(cursor.getColumnIndex("PACKAGE_INVOICE_NUMBER")));
					jsonObject.put("packageStatus", cursor.getString(cursor.getColumnIndex("PACKAGE_STATUS")));
					jsonObject.put("volumeAmount", cursor.getDouble(cursor.getColumnIndex("VOLUME_AMOUNT")));
					jsonObject.put("packageGrade", cursor.getString(cursor.getColumnIndex("PACKAGE_GRADE")));
					jsonObject.put("saleProduct", cursor.getString(cursor.getColumnIndex("SALE_PRODUCT")));
				} catch (JSONException e) {

					e.printStackTrace();
				}
				saleData.put(jsonObject);
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			String result = null;

			try {

				URL url = new URL(DataDownloadActivity.URL + "sale/savesaledata");
				HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setReadTimeout(readTimeOut);
				httpUrlConnection.setConnectTimeout(readTimeOut);
				httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type", "application/json");

				String sendingString = saleData.toString();

				Log.e("before>>>>>", "before");

				sendingString = sendingString.replace("\\", "");
				sendingString = sendingString.replace("\"[", "[");
				sendingString = sendingString.replace("]\"", "]");

				Log.e("Sending String is>>>>>>", sendingString);

				//sendingString ="[{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"xxxxxxx\",\"saleProduct\":[{\"saleQty\":100,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":95500,\"serialList\":[{\"toSerialNo\":\"100\",\"fromSerialNo\":\"1\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000}],\"invoiceID\":\"NPTADMIN150820000001\",\"totalDiscountAmt\":4500,\"totalAmt\":100000,\"salePersonID\":\"ADMIN\",\"devID\":\"000000000000000\",\"invoiceTime\":\"2015-08-20 07:35:08\",\"locationCode\":\"NPT\",\"payAmt\":95500,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-20\",\"customerID\":\"NPT1\",\"dueDate\":\"2015-08-20\"}]";

				//sendingString = "[{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Kyi Soe\",\"saleProduct\":[{\"saleQty\":200,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":190400,\"serialList\":[{\"toSerialNo\":\"507200\",\"fromSerialNo\":\"507001\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":60,\"salePrice\":3000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":171360,\"serialList\":[{\"toSerialNo\":\"277200\",\"fromSerialNo\":\"277141\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000},{\"saleQty\":50,\"salePrice\":5000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":238000,\"serialList\":[{\"toSerialNo\":\"318810\",\"fromSerialNo\":\"318761\"}],\"discountAmt\":200,\"productID\":\"TUP003\",\"purchasePrice\":5000},{\"saleQty\":50,\"salePrice\":10000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":476000,\"serialList\":[{\"toSerialNo\":\"022129\",\"fromSerialNo\":\"022080\"}],\"discountAmt\":400,\"productID\":\"TUP004\",\"purchasePrice\":10000}],\"invoiceID\":\"NPTADMIN150801000001\",\"totalDiscountAmt\":54240,\"totalAmt\":1130000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 10:43:52\",\"locationCode\":\"NPT\",\"payAmt\":1075760,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"ADMIN0101\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Than sin\",\"saleProduct\":[{\"saleQty\":350,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":333200,\"serialList\":[{\"toSerialNo\":\"279800\",\"fromSerialNo\":\"279661\"},{\"toSerialNo\":\"507410\",\"fromSerialNo\":\"507201\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":60,\"salePrice\":3000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":171360,\"serialList\":[{\"toSerialNo\":\"277260\",\"fromSerialNo\":\"277201\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000},{\"saleQty\":5,\"salePrice\":5000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":23800,\"serialList\":[{\"toSerialNo\":\"09956\",\"fromSerialNo\":\"09952\"}],\"discountAmt\":200,\"productID\":\"TUP007\",\"purchasePrice\":5000}],\"invoiceID\":\"NPTADMIN150801000002\",\"totalDiscountAmt\":26640,\"totalAmt\":555000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 10:51:30\",\"locationCode\":\"NPT\",\"payAmt\":528360,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06065\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"aung kyaw moe\",\"saleProduct\":[{\"saleQty\":100,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":95200,\"serialList\":[{\"toSerialNo\":\"507500\",\"fromSerialNo\":\"507411\"},{\"toSerialNo\":\"513510\",\"fromSerialNo\":\"513501\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000}],\"invoiceID\":\"NPTADMIN150801000003\",\"totalDiscountAmt\":4800,\"totalAmt\":100000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:18:49\",\"locationCode\":\"NPT\",\"payAmt\":95200,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06006\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Htake Tan\",\"saleProduct\":[{\"saleQty\":400,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":380800,\"serialList\":[{\"toSerialNo\":\"513910\",\"fromSerialNo\":\"513511\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":102,\"salePrice\":3000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":291312,\"serialList\":[{\"toSerialNo\":\"277362\",\"fromSerialNo\":\"277261\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000}],\"invoiceID\":\"NPTADMIN150801000004\",\"totalDiscountAmt\":33888,\"totalAmt\":706000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:35:15\",\"locationCode\":\"NPT\",\"payAmt\":672112,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06016\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Moe\",\"saleProduct\":[{\"saleQty\":12,\"salePrice\":3000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":34272,\"serialList\":[{\"toSerialNo\":\"277374\",\"fromSerialNo\":\"277363\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000},{\"saleQty\":10,\"salePrice\":5000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":47600,\"serialList\":[{\"toSerialNo\":\"318820\",\"fromSerialNo\":\"318811\"}],\"discountAmt\":200,\"productID\":\"TUP003\",\"purchasePrice\":5000}],\"invoiceID\":\"NPTADMIN150801000005\",\"totalDiscountAmt\":4128,\"totalAmt\":86000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:38:24\",\"locationCode\":\"NPT\",\"payAmt\":81872,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06042\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Hnin\",\"saleProduct\":[{\"saleQty\":20,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":19040,\"serialList\":[{\"toSerialNo\":\"513930\",\"fromSerialNo\":\"513911\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":30,\"salePrice\":3000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":85680,\"serialList\":[{\"toSerialNo\":\"277404\",\"fromSerialNo\":\"277375\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000},{\"saleQty\":20,\"salePrice\":5000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":95200,\"serialList\":[{\"toSerialNo\":\"319640\",\"fromSerialNo\":\"319621\"}],\"discountAmt\":200,\"productID\":\"TUP003\",\"purchasePrice\":5000},{\"saleQty\":5,\"salePrice\":10000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":47600,\"serialList\":[{\"toSerialNo\":\"022134\",\"fromSerialNo\":\"022130\"}],\"discountAmt\":400,\"productID\":\"TUP004\",\"purchasePrice\":10000}],\"invoiceID\":\"NPTADMIN150801000006\",\"totalDiscountAmt\":12480,\"totalAmt\":260000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:42:36\",\"locationCode\":\"NPT\",\"payAmt\":247520,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06115\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"War\",\"saleProduct\":[{\"saleQty\":30,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":28560,\"serialList\":[{\"toSerialNo\":\"513960\",\"fromSerialNo\":\"513931\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":3,\"salePrice\":3000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":8568,\"serialList\":[{\"toSerialNo\":\"277407\",\"fromSerialNo\":\"277405\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000}],\"invoiceID\":\"NPTADMIN150801000007\",\"totalDiscountAmt\":1872,\"totalAmt\":39000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:46:09\",\"locationCode\":\"NPT\",\"payAmt\":37128,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"ADMIN0102\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Zaw\",\"saleProduct\":[{\"saleQty\":200,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":190400,\"serialList\":[{\"toSerialNo\":\"504930\",\"fromSerialNo\":\"504731\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":60,\"salePrice\":3000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":171360,\"serialList\":[{\"toSerialNo\":\"277467\",\"fromSerialNo\":\"277408\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000}],\"invoiceID\":\"NPTADMIN150801000008\",\"totalDiscountAmt\":18240,\"totalAmt\":380000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:48:59\",\"locationCode\":\"NPT\",\"payAmt\":361760,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06064\",\"dueDate\":\"2015-08-01\"}]";

				//sendingString = "[{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Kyi Soe\",\"saleProduct\":[{\"saleQty\":200,\"salePrice\":1000,\"extraDiscount\":0.5,\"discountPercent\":4,\"totalAmt\":190400,\"serialList\":[{\"toSerialNo\":\"507200\",\"fromSerialNo\":\"507001\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000}],\"invoiceID\":\"NPTADMIN150801000008\",\"totalDiscountAmt\":18240,\"totalAmt\":380000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:48:59\",\"locationCode\":\"NPT\",\"payAmt\":361760,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06064\",\"dueDate\":\"2015-08-01\"}]";

				//sendingString = "[{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Kyi Soe\",\"saleProduct\":[{\"saleQty\":200,\"salePrice\":1000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":190400,\"serialList\":[{\"toSerialNo\":\"507200\",\"fromSerialNo\":\"507001\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":60,\"salePrice\":3000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":171360,\"serialList\":[{\"toSerialNo\":\"277200\",\"fromSerialNo\":\"277141\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000},{\"saleQty\":50,\"salePrice\":5000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":238000,\"serialList\":[{\"toSerialNo\":\"318810\",\"fromSerialNo\":\"318761\"}],\"discountAmt\":200,\"productID\":\"TUP003\",\"purchasePrice\":5000},{\"saleQty\":50,\"salePrice\":10000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":476000,\"serialList\":[{\"toSerialNo\":\"022129\",\"fromSerialNo\":\"022080\"}],\"discountAmt\":400,\"productID\":\"TUP004\",\"purchasePrice\":10000}],\"invoiceID\":\"NPTADMIN150801000001\",\"totalDiscountAmt\":54240,\"totalAmt\":1130000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 10:43:52\",\"locationCode\":\"NPT\",\"payAmt\":1075760,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"ADMIN0101\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Than sin\",\"saleProduct\":[{\"saleQty\":350,\"salePrice\":1000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":333200,\"serialList\":[{\"toSerialNo\":\"279800\",\"fromSerialNo\":\"279661\"},{\"toSerialNo\":\"507410\",\"fromSerialNo\":\"507201\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":60,\"salePrice\":3000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":171360,\"serialList\":[{\"toSerialNo\":\"277260\",\"fromSerialNo\":\"277201\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000},{\"saleQty\":5,\"salePrice\":5000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":23800,\"serialList\":[{\"toSerialNo\":\"09956\",\"fromSerialNo\":\"09952\"}],\"discountAmt\":200,\"productID\":\"TUP007\",\"purchasePrice\":5000}],\"invoiceID\":\"NPTADMIN150801000002\",\"totalDiscountAmt\":26640,\"totalAmt\":555000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 10:51:30\",\"locationCode\":\"NPT\",\"payAmt\":528360,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06065\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"aung kyaw moe\",\"saleProduct\":[{\"saleQty\":100,\"salePrice\":1000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":95200,\"serialList\":[{\"toSerialNo\":\"507500\",\"fromSerialNo\":\"507411\"},{\"toSerialNo\":\"513510\",\"fromSerialNo\":\"513501\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000}],\"invoiceID\":\"NPTADMIN150801000003\",\"totalDiscountAmt\":4800,\"totalAmt\":100000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:18:49\",\"locationCode\":\"NPT\",\"payAmt\":95200,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06006\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Htake Tan\",\"saleProduct\":[{\"saleQty\":400,\"salePrice\":1000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":380800,\"serialList\":[{\"toSerialNo\":\"513910\",\"fromSerialNo\":\"513511\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":102,\"salePrice\":3000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":291312,\"serialList\":[{\"toSerialNo\":\"277362\",\"fromSerialNo\":\"277261\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000}],\"invoiceID\":\"NPTADMIN150801000004\",\"totalDiscountAmt\":33888,\"totalAmt\":706000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:35:15\",\"locationCode\":\"NPT\",\"payAmt\":672112,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06016\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Moe\",\"saleProduct\":[{\"saleQty\":12,\"salePrice\":3000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":34272,\"serialList\":[{\"toSerialNo\":\"277374\",\"fromSerialNo\":\"277363\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000},{\"saleQty\":10,\"salePrice\":5000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":47600,\"serialList\":[{\"toSerialNo\":\"318820\",\"fromSerialNo\":\"318811\"}],\"discountAmt\":200,\"productID\":\"TUP003\",\"purchasePrice\":5000}],\"invoiceID\":\"NPTADMIN150801000005\",\"totalDiscountAmt\":4128,\"totalAmt\":86000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:38:24\",\"locationCode\":\"NPT\",\"payAmt\":81872,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06042\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Hnin\",\"saleProduct\":[{\"saleQty\":20,\"salePrice\":1000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":19040,\"serialList\":[{\"toSerialNo\":\"513930\",\"fromSerialNo\":\"513911\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":30,\"salePrice\":3000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":85680,\"serialList\":[{\"toSerialNo\":\"277404\",\"fromSerialNo\":\"277375\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000},{\"saleQty\":20,\"salePrice\":5000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":95200,\"serialList\":[{\"toSerialNo\":\"319640\",\"fromSerialNo\":\"319621\"}],\"discountAmt\":200,\"productID\":\"TUP003\",\"purchasePrice\":5000},{\"saleQty\":5,\"salePrice\":10000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":47600,\"serialList\":[{\"toSerialNo\":\"022134\",\"fromSerialNo\":\"022130\"}],\"discountAmt\":400,\"productID\":\"TUP004\",\"purchasePrice\":10000}],\"invoiceID\":\"NPTADMIN150801000006\",\"totalDiscountAmt\":12480,\"totalAmt\":260000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:42:36\",\"locationCode\":\"NPT\",\"payAmt\":247520,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06115\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"War\",\"saleProduct\":[{\"saleQty\":30,\"salePrice\":1000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":28560,\"serialList\":[{\"toSerialNo\":\"513960\",\"fromSerialNo\":\"513931\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":3,\"salePrice\":3000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":8568,\"serialList\":[{\"toSerialNo\":\"277407\",\"fromSerialNo\":\"277405\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000}],\"invoiceID\":\"NPTADMIN150801000007\",\"totalDiscountAmt\":1872,\"totalAmt\":39000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:46:09\",\"locationCode\":\"NPT\",\"payAmt\":37128,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"ADMIN0102\",\"dueDate\":\"2015-08-01\"},{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"Zaw\",\"saleProduct\":[{\"saleQty\":200,\"salePrice\":1000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":190400,\"serialList\":[{\"toSerialNo\":\"504930\",\"fromSerialNo\":\"504731\"}],\"discountAmt\":40,\"productID\":\"TUP001\",\"purchasePrice\":1000},{\"saleQty\":60,\"salePrice\":3000,\"extraDiscount\":0.8,\"discountPercent\":4,\"totalAmt\":171360,\"serialList\":[{\"toSerialNo\":\"277467\",\"fromSerialNo\":\"277408\"}],\"discountAmt\":120,\"productID\":\"TUP002\",\"purchasePrice\":3000}],\"invoiceID\":\"NPTADMIN150801000008\",\"totalDiscountAmt\":18240,\"totalAmt\":380000,\"salePersonID\":\"ADMIN\",\"devID\":\"358113061889332\",\"invoiceTime\":\"2015-08-01 11:48:59\",\"locationCode\":\"NPT\",\"payAmt\":361760,\"volumeAmount\":0,\"refundAmt\":0,\"saleDate\":\"2015-08-01\",\"customerID\":\"NP06064\",\"dueDate\":\"2015-08-01\"}]";


//				sendingString = sendingString.replace("/", "-");



				OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(sendingString.getBytes());
				outputStream.flush();

                Preferences.didUploadedSaleDataToServer(HomeActivity.this, true);

				if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

					return null;
				}

				Log.e("HTTP Response Code>>>", httpUrlConnection.getResponseCode() + "");
				//Toast.makeText(getApplicationContext(), "HTTP Response Code is " +httpUrlConnection.getResponseCode(), Toast.LENGTH_SHORT).show();
				/*if(httpUrlConnection.getResponseCode() == 200){
					Log.e("200", "");
				}
				else{
					Log.e("not 200", "");
				}*/

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
				String output;
				while ((output = bufferedReader.readLine()) != null) {

					result = output;
				}
				//Log.e("Result of sale upload>>", result);
				//Toast.makeText(getApplicationContext(), "Result of sale upload is " +result, Toast.LENGTH_SHORT).show();

				httpUrlConnection.disconnect();
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

			if (result == null) {

				new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Upload data to server")
					.setMessage("Can't connect to server.")
					.setPositiveButton("OK", null)
					.show();

				return;
			}

			else {
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
				try {
					JSONObject resultJson = new JSONObject(result);
					if (!resultJson.getString("status").equalsIgnoreCase("success")) {

                        if(resultJson.getString("status").equalsIgnoreCase("duplicate record")) {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status") + " in uploading sale data.")
                                    .show();

                            checkNewCusExistsOrNot();

                            return;
                        }
                        else {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status"))
                                    .show();

                            return;
                        }
                    } else if (resultJson.getString("status").equalsIgnoreCase("success")) {

						Toast.makeText(HomeActivity.this, "Sale data are successfully uploaded.", Toast.LENGTH_LONG).show();

                        String str = resultJson.get("duplicateIdList").toString();

                        String[] mystr = gson.fromJson(str, String[].class);
                        Log.e("mystr len>>>", mystr.length + "");

                        database.beginTransaction();
                        for(int i = 0; i < mystr.length; i++) {
                            //Toast.makeText(getApplicationContext(), "duplicate sale : " + mystr[i], Toast.LENGTH_SHORT).show();
                            Cursor cursor = database.rawQuery("select * from duplicate_record where duplicate_id='"+mystr[i]+"'", null);
                            if(cursor.getCount() == 1) {

                            }
                            else {
                                database.execSQL("insert into duplicate_record values ('" + mystr[i] + "','S')");
                            }

                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();

                        checkNewCusExistsOrNot();

					}
				} catch (JSONException e) {

					e.printStackTrace();
				}
			}

		}
	}

	private class UploadPreOrders extends AsyncTask<Void, Void, String> {

    	JSONArray preOrdersJSONArray;

		@Override
		protected void onPreExecute() {

			Toast.makeText(HomeActivity.this, "Uploading pre-orders to server.", Toast.LENGTH_LONG).show();

			super.onPreExecute();

			preOrdersJSONArray = new JSONArray();
			Cursor cursor = database.rawQuery("SELECT * FROM PRE_ORDER", null);
			while (cursor.moveToNext()) {

				JSONObject jsonObject = new JSONObject();
				try {

					jsonObject.put("invoiceId", cursor.getString(cursor.getColumnIndex("INVOICE_ID")));
					jsonObject.put("customerId", cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")));
					jsonObject.put("salePersonID", cursor.getString(cursor.getColumnIndex("SALEPERSON_ID")));
					jsonObject.put("devID", cursor.getString(cursor.getColumnIndex("DEV_ID")));
					jsonObject.put("preOrderDate", cursor.getString(cursor.getColumnIndex("PREORDER_DATE")).replace("/", "-"));
					jsonObject.put("expectedDeliveryDate", cursor.getString(cursor.getColumnIndex("EXPECTED_DELIVERY_DATE")).replace("/", "-"));
					jsonObject.put("advancePaymentAmt", cursor.getDouble(cursor.getColumnIndex("ADVANCE_PAYMENT_AMOUNT")));
					jsonObject.put("netAmt", cursor.getDouble(cursor.getColumnIndex("NET_AMOUNT")));
					jsonObject.put("productList", cursor.getString(cursor.getColumnIndex("PRODUCT_LIST")));
				} catch (JSONException e) {

					e.printStackTrace();
				}
				preOrdersJSONArray.put(jsonObject);
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			String result = null;

			try {

				URL url = new URL(DataDownloadActivity.URL + "pre/savepreorderdata");
				HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setReadTimeout(readTimeOut);
				httpUrlConnection.setConnectTimeout(readTimeOut);
				httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type", "application/json");

				String sendingString = preOrdersJSONArray.toString().replace("\\", "");
				sendingString = sendingString.replace("\"[", "[");
				sendingString = sendingString.replace("]\"", "]");

  				OutputStream outputStream = httpUrlConnection.getOutputStream();
				outputStream.write(sendingString.getBytes());
				outputStream.flush();

                Preferences.didUploadedPreOrderDataToServer(HomeActivity.this, true);

				int a =  httpUrlConnection.getResponseCode();
				if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

					return null;
				} else {
					
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
					String output;
					while ((output = bufferedReader.readLine()) != null) {

						result = output;
					}
				}

				httpUrlConnection.disconnect();
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

//			progressDialog.dismiss();

			if (result == null) {

				new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Upload data to server")
					.setMessage("Can't connect to server.")
					.setPositiveButton("OK", null)
					.show();

				return;
			}
			else {

				try {

					JSONObject resultJson = new JSONObject(result);
                    if (!resultJson.getString("status").equalsIgnoreCase("success")) {

                        if(resultJson.getString("status").equalsIgnoreCase("duplicate record")) {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status") + " in uploading pre-order data.")
                                    .show();

                            checkDeliveryDataExistsOrNot();

                            return;
                        }
                        else {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status"))
                                    .show();

                            return;
                        }
                    } else if (resultJson.getString("status").equalsIgnoreCase("success")) {

						Toast.makeText(HomeActivity.this, "Pre-Order data are successfully uploaded.", Toast.LENGTH_LONG).show();

                        /*String str = resultJson.get("duplicateIdList").toString();

                        String[] mystr = gson.fromJson(str, String[].class);
                        Log.e("mystr len>>>", mystr.length + "");

                        database.beginTransaction();
                        for(int i = 0; i < mystr.length; i++) {
                            //Toast.makeText(getApplicationContext(), "duplicate pre-order: " + mystr[i], Toast.LENGTH_SHORT).show();
                            database.execSQL("insert into duplicate_record values ('"+mystr[i]+"','P')");

                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();*/

						checkDeliveryDataExistsOrNot();

					}
				} catch (JSONException e) {

					e.printStackTrace();
				}
			}
		}
	}

	private class UploadDeliveredData extends AsyncTask<Void, Void, String> {

    	JSONArray deliveredDataJSONArray;

		@Override
		protected void onPreExecute() {

			Toast.makeText(HomeActivity.this, "Uploading delivered data to server.", Toast.LENGTH_LONG).show();

			super.onPreExecute();

			deliveredDataJSONArray = new JSONArray();
			Cursor cursor = database.rawQuery("SELECT * FROM DELIVERED_DATA", null);
			while (cursor.moveToNext()) {

				/*JSONObject jsonObject = new JSONObject();
				try {

					jsonObject.put("customerID", cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")));
					jsonObject.put("deliveredDate", cursor.getString(cursor.getColumnIndex("INVOICE_TIME")).replace("/", "-"));
					jsonObject.put("deliverInvoiceID", cursor.getString(cursor.getColumnIndex("DELIVERY_INVOICE_ID")));
					jsonObject.put("saleOrderNo", cursor.getString(cursor.getColumnIndex("SALE_ORDER_NO")));
					jsonObject.put("totalAmt", cursor.getDouble(cursor.getColumnIndex("TOTAL_AMOUNT")));
					jsonObject.put("totalDiscountAmt", cursor.getDouble(cursor.getColumnIndex("TOTAL_DISCOUNT_AMOUNT")));

                    //jsonObject.put("fpAmount", cursor.getDouble(cursor.getColumnIndex("FIRST_PAID_AMOUNT")));

                    jsonObject.put("payAmt", cursor.getDouble(cursor.getColumnIndex("PAY_AMOUNT")));
					jsonObject.put("receitpPersonName", cursor.getString(cursor.getColumnIndex("RECEIPT_PERSON_NAME")));
					jsonObject.put("salePersonID", cursor.getString(cursor.getColumnIndex("SALE_PERSON_ID")));
					jsonObject.put("dueDate", cursor.getString(cursor.getColumnIndex("INVOICE_TIME")).replace("/", "-"));
					jsonObject.put("cashOrCredit", cursor.getString(cursor.getColumnIndex("CASH_OR_CREDIT")));
					jsonObject.put("locationCode", cursor.getString(cursor.getColumnIndex("LOCATION_CODE")));
					jsonObject.put("devID", cursor.getString(cursor.getColumnIndex("DEVICE_ID")));
					jsonObject.put("invoiceTime", cursor.getString(cursor.getColumnIndex("INVOICE_TIME")).replace("/", "-"));
					jsonObject.put("deliveredProduct", cursor.getString(cursor.getColumnIndex("DELIVERED_PRODUCTS")));
				} catch (JSONException e) {

					e.printStackTrace();
				}
				deliveredDataJSONArray.put(jsonObject);*/


                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put("fpAmount", cursor.getDouble(cursor.getColumnIndex("FIRST_PAID_AMOUNT")));

                    jsonObject.put("customerID", cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")));
                    jsonObject.put("deliveredDate", cursor.getString(cursor.getColumnIndex("INVOICE_TIME")).replace("/", "-"));
                    jsonObject.put("deliverInvoiceID", cursor.getString(cursor.getColumnIndex("DELIVERY_INVOICE_ID")));
                    jsonObject.put("saleOrderNo", cursor.getString(cursor.getColumnIndex("SALE_ORDER_NO")));
                    jsonObject.put("totalAmt", cursor.getDouble(cursor.getColumnIndex("TOTAL_AMOUNT")));
                    jsonObject.put("totalDiscountAmt", cursor.getDouble(cursor.getColumnIndex("TOTAL_DISCOUNT_AMOUNT")));
                    jsonObject.put("payAmt", cursor.getDouble(cursor.getColumnIndex("PAY_AMOUNT")));
                    jsonObject.put("receitpPersonName", cursor.getString(cursor.getColumnIndex("RECEIPT_PERSON_NAME")));
                    jsonObject.put("salePersonID", cursor.getString(cursor.getColumnIndex("SALE_PERSON_ID")));
                    jsonObject.put("dueDate", cursor.getString(cursor.getColumnIndex("INVOICE_TIME")).replace("/", "-"));
                    jsonObject.put("cashOrCredit", cursor.getString(cursor.getColumnIndex("CASH_OR_CREDIT")));
                    jsonObject.put("locationCode", cursor.getString(cursor.getColumnIndex("LOCATION_CODE")));
                    jsonObject.put("devID", cursor.getString(cursor.getColumnIndex("DEVICE_ID")));
                    jsonObject.put("invoiceTime", cursor.getString(cursor.getColumnIndex("INVOICE_TIME")).replace("/", "-"));
                    jsonObject.put("deliveredProduct", cursor.getString(cursor.getColumnIndex("DELIVERED_PRODUCTS")));
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                deliveredDataJSONArray.put(jsonObject);
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			String result = null;

			try {

				URL url = new URL(DataDownloadActivity.URL + "dvr/savedelieverdata");
				HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
				httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(readTimeOut);
                httpUrlConnection.setConnectTimeout(readTimeOut);
                httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type", "application/json");

				/*String sendingString = deliveredDataJSONArray.toString().replace("\\", "");
				sendingString = sendingString.replace("\"[", "[");
				sendingString = sendingString.replace("]\"", "]");*/

                //String sendingString = "[{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"ttttt\",\"fpAmount\":1000,\"deliveredProduct\":[{\"orderedQty\":150,\"deliveredQty\":150,\"salePrice\":5000,\"extraDiscount\":0.2,\"discountPercent\":4,\"totalAmt\":718500,\"serialList\":[{\"serialQty\":150,\"toSerialNo\":\"150\",\"fromSerialNo\":\"1\"}],\"discountAmt\":200,\"productID\":\"TUP003\",\"purchasePrice\":5000}],\"totalDiscountAmt\":31500,\"totalAmt\":750000,\"salePersonID\":\"ADMIN\",\"devID\":\"000000000000000\",\"deliverInvoiceID\":\"OSNPTADMIN1511200001\",\"invoiceTime\":\"2015-11-20 07:59:14\",\"locationCode\":\"NPT\",\"payAmt\":718500,\"saleOrderNo\":\"SONPTADMIN1511200001\",\"deliveredDate\":\"2015-11-20 07:59:14\",\"customerID\":\"NPC01040\",\"dueDate\":\"2015-11-20 07:59:14\"}]";

                String sendingString = "[{\"fpAmount\":100,\"customerID\":\"NPC01037\",\"deliveredDate\":\"2015-11-20 13:11:10\",\"deliverInvoiceID\":\"OSNPTADMIN1511200001\",\"saleOrderNo\":\"SONPTADMIN1511200001\",\"totalAmt\":9000,\"totalDiscountAmt\":369,\"payAmt\":8631,\"receitpPersonName\":\"hak\",\"salePersonID\":\"ADMIN\",\"dueDate\":\"2015-11-20 13:11:10\",\"cashOrCredit\":\"C\",\"locationCode\":\"NPT\",\"devID\":\"000000000000000\",\"invoiceTime\":\"2015-11-20 13:11:10\",\"deliveredProduct\":[{\"productID\":\"E-0001\",\"deliveredQty\":10,\"salePrice\":5000.0,\"purchasePrice\":4750.0,\"discountAmt\":200.0,\"totalAmt\":47500.0,\"discountPercent\":10.0,\"extraDiscount\":10.0,\"serialList\":[{\"serialQty\":5,\"fromSerialNo\":\"1\",\"toSerialNo\":\"100\"},{\"serialQty\":5,\"fromSerialNo\":\"200\",\"toSerialNo\":\"300\"}]},{\"productID\":\"E-0002\",\"deliveredQty\":10,\"salePrice\":10000.0,\"purchasePrice\":9750.0,\"discountAmt\":360.0,\"totalAmt\":96400.0,\"discountPercent\":20.0,\"extraDiscount\":20.0,\"serialList\":[{\"serialQty\":5,\"fromSerialNo\":\"1\",\"toSerialNo\":\"100\"},{\"serialQty\":5,\"fromSerialNo\":\"200\",\"toSerialNo\":\"300\"}]},{\"productID\":\"M-0002\",\"deliveredQty\":10,\"salePrice\":10000.0,\"purchasePrice\":9700.0,\"discountAmt\":0.0,\"totalAmt\":100000.0,\"discountPercent\":30.0,\"extraDiscount\":30.0,\"serialList\":[{\"serialQty\":5,\"fromSerialNo\":\"1\",\"toSerialNo\":\"100\"},{\"serialQty\":5,\"fromSerialNo\":\"200\",\"toSerialNo\":\"300\"}]}]}]";

                //String sendingString = "[{\"cashOrCredit\":\"C\",\"receitpPersonName\":\"aaaaa\",\"fpAmount\":1000,\"deliveredProduct\":[{\"orderedQty\":150,\"deliveredQty\":150,\"salePrice\":5000,\"extraDiscount\":0.1,\"discountPercent\":4,\"totalAmt\":719250,\"serialList\":[{\"serialQty\":100,\"fromSerialNo\":\"1\",\"toSerialNo\":\"100\"},{\"serialQty\":50,\"fromSerialNo\":\"1\",\"toSerialNo\":\"50\"}],\"discountAmt\":200,\"productID\":\"TUP003\",\"purchasePrice\":5000}],\"totalDiscountAmt\":30750,\"totalAmt\":750000,\"salePersonID\":\"ADMIN\",\"devID\":\"000000000000000\",\"deliverInvoiceID\":\"OSNPTADMIN1511200001\",\"invoiceTime\":\"2015-11-20 07:10:45\",\"locationCode\":\"NPT\",\"payAmt\":719250,\"saleOrderNo\":\"SONPTADMIN1511200001\",\"deliveredDate\":\"2015-11-20 07:10:45\",\"customerID\":\"NPC01040\",\"dueDate\":\"2015-11-20 07:10:45\"}]";

                //String sendingString = "[{\"customerID\":\"1806/01\",\"deliveredDate\":\"2015-11-17 13:40:43\",\"deliverInvoiceID\":\"OSADMIN15071500\",\"saleOrderNo\":\"SO15070900003\",\"totalAmt\":250000,\"totalDiscountAmt\":6100,\"payAmt\":100000,\"receitpPersonName\":\"Aye\",\"salePersonID\":\"ADMIN\",\"dueDate\":\"2015-11-17 13:40:43\",\"cashOrCredit\":\"C\",\"locationCode\":\"L1\",\"devID\":\"354400055683688\",\"invoiceTime\":\"2015-11-17 13:40:43\",\"deliveredProduct\":[{\"productID\":\"E-0001\",\"deliveredQty\":10,\"salePrice\":5000.0,\"purchasePrice\":4750.0,\"discountAmt\":200.0,\"totalAmt\":47500.0,\"discountPercent\":10.0,\"extraDiscount\":10.0,\"serialList\":[{\"fromSerialNo\":\"1\",\"toSerialNo\":\"100\"},{\"fromSerialNo\":\"200\",\"toSerialNo\":\"300\"}]},{\"productID\":\"E-0002\",\"deliveredQty\":10,\"salePrice\":10000.0,\"purchasePrice\":9750.0,\"discountAmt\":360.0,\"totalAmt\":96400.0,\"discountPercent\":20.0,\"extraDiscount\":20.0,\"serialList\":[{\"fromSerialNo\":\"1\",\"toSerialNo\":\"100\"},{\"fromSerialNo\":\"200\",\"toSerialNo\":\"300\"}]},{\"productID\":\"M-0002\",\"deliveredQty\":10,\"salePrice\":10000.0,\"purchasePrice\":9700.0,\"discountAmt\":0.0,\"totalAmt\":100000.0,\"discountPercent\":30.0,\"extraDiscount\":30.0,\"serialList\":[{\"fromSerialNo\":\"1\",\"toSerialNo\":\"100\"},{\"fromSerialNo\":\"200\",\"toSerialNo\":\"300\"}]}]}]";

				Log.e("Upload Deli Data>>>>", sendingString);

  				OutputStream outputStream = httpUrlConnection.getOutputStream();
				outputStream.write(sendingString.getBytes());
				outputStream.flush();

                Preferences.didUploadedDeliveredDataToServer(HomeActivity.this, true);

				int a = httpUrlConnection.getResponseCode();
                Log.i("Connection Code>>>>", a + "");
				if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

					return null;
				} else {
					
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
					String output;
					while ((output = bufferedReader.readLine()) != null) {

						result = output;
					}
				}

				httpUrlConnection.disconnect();
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

			if (result == null) {

				new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Upload data to server")
					.setMessage("Can't connect to server.")
					.setPositiveButton("OK", null)
					.show();

				return;
			}
			else {

				try {

					JSONObject resultJson = new JSONObject(result);
                    if (!resultJson.getString("status").equalsIgnoreCase("success")) {

                        if(resultJson.getString("status").equalsIgnoreCase("duplicate record")) {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status") + " in uploading delivery data.")
                                    .show();

                            return;
                        }
                        else {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status"))
                                    .show();

                            return;
                        }
                    } else if (resultJson.getString("status").equalsIgnoreCase("success")) {
						Toast.makeText(HomeActivity.this, "Delivery data are successfully uploaded.", Toast.LENGTH_LONG).show();

                        /*String str = resultJson.get("duplicateIdList").toString();

                        String[] mystr = gson.fromJson(str, String[].class);
                        Log.e("mystr len>>>", mystr.length + "");

                        database.beginTransaction();
                        for(int i = 0; i < mystr.length; i++) {
                            //Toast.makeText(getApplicationContext(), "duplicate delivery : " + mystr[i], Toast.LENGTH_SHORT).show();
                            database.execSQL("insert into duplicate_record values ('"+mystr[i]+"','D')");

                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();*/
					}
				} catch (JSONException e) {

					e.printStackTrace();
				}
			}


		}
	}

	private class GetProducts extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(HomeActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Reissue data from server ...");
			progressDialog.show();

			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {

			String result = null;

			try {

				URL url = new URL(DataDownloadActivity.URL + "pro/getproductlist");
				HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setReadTimeout(3000);
				httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type", "application/json");

				OutputStream outputStream = httpUrlConnection.getOutputStream();
				outputStream.write(makeSendingString().getBytes());
				outputStream.flush();

				if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

					return "Can't connect to server when downloading data for packages.";
				}

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
				String output;
				while ((output = bufferedReader.readLine()) != null) {

					result = output;
				}

				httpUrlConnection.disconnect();
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

			progressDialog.dismiss();

			if (result == null) {

				new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Reissue")
					.setMessage("Can't connect to server.")
					.setPositiveButton("OK", null)
					.show();

				return;
			}

			try {

				JSONArray jsonArray = new JSONArray(result);
				for (int i = 0; i < jsonArray.length(); i++) {

					String sql = null;
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					Cursor cursor = database.rawQuery(
							"SELECT * FROM PRODUCT"
							+ " WHERE PRODUCT_ID = \"" + jsonObject.getString("productID") + "\""
							, null);
					if (cursor.moveToNext()) {

						sql = "UPDATE PRODUCT"
								+ " SET TOTAL_QTY = " + (cursor.getInt(cursor.getColumnIndex("TOTAL_QTY")) + jsonObject.getInt("totalQty"))
								+ ", REMAINING_QTY = " + (cursor.getInt(cursor.getColumnIndex("REMAINING_QTY")) + jsonObject.getInt("totalQty"))
								+ " WHERE PRODUCT_ID = \"" + jsonObject.getString("productID") + "\"";
					} else {
						
						sql = "INSERT INTO PRODUCT VALUES (\""
								+ jsonObject.getString("categoryID") + "\", \""
								+ jsonObject.getString("categoryName") + "\", \""
								+ jsonObject.getString("groupID") + "\", \""
								+ jsonObject.getString("groupName") + "\", \""
								+ jsonObject.getString("productID") + "\", \""
								+ jsonObject.getString("productName") + "\", \""
								+ jsonObject.getString("totalQty") + "\", \""
								+ jsonObject.getString("totalQty") + "\", \""
								+ jsonObject.getString("sellingPrice") + "\", \""
								+ jsonObject.getString("purchasePrice") + "\", \""
								+ jsonObject.getString("discountType") + "\""
								+ ")";
					}

					database.beginTransaction();
					database.execSQL(sql);
					database.setTransactionSuccessful();	
					database.endTransaction();

					System.out.println(sql + ";");
				};
			} catch (JSONException e) {

				e.printStackTrace();
			}
		}
	}

	private class UploadNewCustomers extends AsyncTask<Void, Void, String> {

    	JSONArray customerFeedbacks;

		@Override
		protected void onPreExecute() {

			Toast.makeText(HomeActivity.this, "Uploading new customers to server.", Toast.LENGTH_LONG).show();

			super.onPreExecute();

			customerFeedbacks = new JSONArray();
			Cursor cursor = database.rawQuery("SELECT * FROM NEW_CUSTOMER", null);
			while (cursor.moveToNext()) {

				JSONObject jsonObject = new JSONObject();
				try {

					jsonObject.put("customerID", cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")));
					jsonObject.put("customerName", cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME")));
					jsonObject.put("phone", cursor.getString(cursor.getColumnIndex("PHONE")));
					jsonObject.put("address", cursor.getString(cursor.getColumnIndex("ADDRESS")));
					jsonObject.put("userId", cursor.getString(cursor.getColumnIndex("USER_ID")));
					jsonObject.put("contactPerson", cursor.getString(cursor.getColumnIndex("CONTACT_PERSON")));
					jsonObject.put("zone", cursor.getString(cursor.getColumnIndex("ZONE_NO")));
					jsonObject.put("customerCagNo", cursor.getString(cursor.getColumnIndex("CUSTOMER_CATEGORY_NO")));
					jsonObject.put("townshipNo", cursor.getString(cursor.getColumnIndex("TOWNSHIP_NUMBER")));

					jsonObject.put("paymentType", cursor.getString(cursor.getColumnIndex("PAYMENT_TYPE")));//edited by HAK
				} catch (JSONException e) {

					e.printStackTrace();
				}
				customerFeedbacks.put(jsonObject);
			}
		}

		@Override
		protected String doInBackground(Void... params) {

			
			String result = null;

			try {

				System.out.println(customerFeedbacks.toString());
				URL url = new URL(DataDownloadActivity.URL + "cus/savenewcus");
				HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setReadTimeout(readTimeOut);
				httpUrlConnection.setConnectTimeout(readTimeOut);
				httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type", "application/json");

				String sendingString = customerFeedbacks.toString().replace("\\", "");
				sendingString = sendingString.replace("\"[", "[");
				sendingString = sendingString.replace("]\"", "]");
//				sendingString = sendingString.replace("/", "-");

				Log.i("SendNewCustomer>>>>", sendingString);

				OutputStream outputStream = httpUrlConnection.getOutputStream();
				outputStream.write(sendingString.getBytes());
				outputStream.flush();

                Preferences.didUploadedNewCustomersToServer(HomeActivity.this, true);

				System.out.println(httpUrlConnection.getResponseCode());
				if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

					return null;

				} else {
					
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
					String output;
					while ((output = bufferedReader.readLine()) != null) {

						result = output;
					}
				}

				httpUrlConnection.disconnect();
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

//			progressDialog.dismiss();

			if (result == null) {

				new AlertDialog.Builder(HomeActivity.this)
					.setTitle("Upload data to server")
					.setMessage("Can't connect to server.")
					.setPositiveButton("OK", null)
					.show();

				return;
			}
			else {

				try {

					JSONObject resultJson = new JSONObject(result);
                    if (!resultJson.getString("status").equalsIgnoreCase("success")) {

                        if(resultJson.getString("status").equalsIgnoreCase("duplicate record")) {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status") + " in uploading new customers.")
                                    .show();

                            checkPreOrderDataExistsOrNot();

                            return;
                        }
                        else {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle("Upload data to server")
                                    .setMessage(resultJson.getString("status"))
                                    .show();

                            return;
                        }
                    } else if (resultJson.getString("status").equalsIgnoreCase("success")) {

						Toast.makeText(HomeActivity.this, "New Customers are successfully uploaded.", Toast.LENGTH_LONG).show();

                        String str = resultJson.get("duplicateIdList").toString();

                        String[] mystr = gson.fromJson(str, String[].class);
                        Log.e("mystr len>>>", mystr.length + "");

                        database.beginTransaction();
                        for(int i = 0; i < mystr.length; i++) {
                            //Toast.makeText(getApplicationContext(), "duplicate new customer : " + mystr[i], Toast.LENGTH_SHORT).show();
                            Cursor cursor = database.rawQuery("select * from duplicate_record where duplicate_id='"+mystr[i]+"'", null);
                            if(cursor.getCount() == 1) {

                            }
                            else {
                                database.execSQL("insert into duplicate_record values ('" + mystr[i] + "','NC')");
                            }

                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();

						checkPreOrderDataExistsOrNot();

					}
				} catch (JSONException e) {

					e.printStackTrace();
				}
			}
		}
	}

	private String makeSendingString() {
		
		ArrayList<String> keys = new ArrayList<String>();
		keys.add("userId");
		keys.add("pwd");
		keys.add("devID");
		keys.add("locationCode");

		ArrayList<String> values = new ArrayList<String>();
		try {

			values.add(userInfo.getString("userId"));
			values.add(userInfo.getString("pwd"));
			values.add(((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
			values.add("");	// TODO
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return TLAJsonStringMaker.jsonStringMaker(keys, values);
	}

	private void checkSaleDataExistsOrNot() {
		// Check sale data are already uploaded to server or not.
		int countSaleData = 0;
		Cursor cursor = database.rawQuery("SELECT COUNT(*) COUNT FROM INVOICE", null);
		if (cursor.moveToNext()) {

			countSaleData += cursor.getInt(cursor.getColumnIndex("COUNT"));
		}

        if(countSaleData > 10) {
            //readTimeOut += 5000;
			readTimeOut = readTimeOut * ((countSaleData / 10) + 1);
			Log.e("Time for sale>>>", readTimeOut + "");
        }

		if (countSaleData == 0) {

			Toast.makeText(HomeActivity.this, "No sale data to upload.", Toast.LENGTH_LONG).show();
			checkNewCusExistsOrNot();
		} /*else if (Preferences.wasUploadedSaleDataToServer(HomeActivity.this)) {

						Toast.makeText(HomeActivity.this, "Sale data are already uploaded to server."
								, Toast.LENGTH_LONG).show();
						checkNewCusExistsOrNot();
        }*/ else {

			new UploadSaleData().execute();
		}
	}

	private void checkNewCusExistsOrNot() {
		// Check new customers are already uploaded to server or not.
		int countNewCustomers = 0;
		Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM NEW_CUSTOMER", null);
		if (cursor.moveToNext()) {

			countNewCustomers += cursor.getInt(cursor.getColumnIndex("COUNT"));
		}

        if(countNewCustomers >= 20) {
            readTimeOut += 5000;
        }

		if (countNewCustomers == 0) {

			Toast.makeText(HomeActivity.this, "No new customer to upload."
					, Toast.LENGTH_LONG).show();
			checkPreOrderDataExistsOrNot();
		} /*else if (Preferences.wasUploadedNewCustomersToServer(HomeActivity.this)) {

			Toast.makeText(HomeActivity.this, "New customers are already uploaded to server."
					, Toast.LENGTH_LONG).show();
			checkPreOrderDataExistsOrNot();
		}*/ else {

			new UploadNewCustomers().execute();
		}
	}

	private void checkPreOrderDataExistsOrNot() {
		// Check pre-order data are already uploaded to server or not.
		int countPreOrderData = 0;
		Cursor cursor = database.rawQuery("SELECT COUNT(*) COUNT FROM PRE_ORDER", null);
		if (cursor.moveToNext()) {

			countPreOrderData += cursor.getInt(cursor.getColumnIndex("COUNT"));
		}

		Log.e("countPreOrderData>>>", countPreOrderData + "");

        if(countPreOrderData >= 20) {
            readTimeOut += 5000;
        }

		if (countPreOrderData == 0) {

			Toast.makeText(HomeActivity.this, "No pre-order data to upload.", Toast.LENGTH_LONG).show();
			checkDeliveryDataExistsOrNot();
		} /*else if (Preferences.wasUploadedPreOrderDataToServer(HomeActivity.this)) {

			Toast.makeText(HomeActivity.this, "Pre-Order data are already uploaded to server."
					, Toast.LENGTH_LONG).show();
			checkDeliveryDataExistsOrNot();
		}*/ else {

			new UploadPreOrders().execute();
		}
	}

	private void checkDeliveryDataExistsOrNot() {
		// Check delivered data are already uploaded to server or not.
		int countDeliveredData = 0;
		Cursor cursor = database.rawQuery("SELECT COUNT(*) COUNT FROM DELIVERED_DATA", null);
		if (cursor.moveToNext()) {

			countDeliveredData += cursor.getInt(cursor.getColumnIndex("COUNT"));
		}

        if(countDeliveredData >= 20) {
            readTimeOut += 5000;
        }

		if (countDeliveredData == 0) {

			Toast.makeText(HomeActivity.this, "No delivered data to upload.", Toast.LENGTH_LONG).show();
		} /*else if (Preferences.wasUploadedDeliveredDataToServer(HomeActivity.this)) {

			Toast.makeText(HomeActivity.this, "Delivered data are already uploaded to server."
					, Toast.LENGTH_LONG).show();
		}*/ else {

			new UploadDeliveredData().execute();
		}
	}

    @SuppressLint("SdCardPath")
    private void backupDB()
    {
        Calendar now = Calendar.getInstance();
        String today = now.get(Calendar.DATE) + "." + (now.get(Calendar.MONTH) + 1)
                + "." + now.get(Calendar.YEAR);
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite())
            {
                Toast.makeText(getApplicationContext(), "Backup database is starting...",
                        Toast.LENGTH_SHORT).show();
                String currentDBPath = "/data/com.aceplus.myanmar_padauk/databases/myanmar-padauk.sqlite";
				//String currentDBPath = "/data/com.aceplus.myanmar_padauk/databases/myanmar-padauk.db";

                String backupDBPath = "MMPD_DB_Backup"+today+".db";
                File currentDB = new File(data, currentDBPath);

                String folderPath="mnt/sdcard/MMPD_DB_Backup";
                File f= new File(folderPath);
                f.mkdir();
                File backupDB = new File(f, backupDBPath);
                FileChannel source = new FileInputStream(currentDB).getChannel();
                FileChannel destination = new FileOutputStream(backupDB).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(getApplicationContext(), "Backup database Successful!",
                        Toast.LENGTH_SHORT).show();

            }
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "Cannot Backup!",
                    Toast.LENGTH_SHORT).show();
        }

    }
}
