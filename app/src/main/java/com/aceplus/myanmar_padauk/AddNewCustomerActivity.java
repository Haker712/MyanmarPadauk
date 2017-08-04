package com.aceplus.myanmar_padauk;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddNewCustomerActivity extends Activity {

	public static final String USER_INFO_KEY = "user-info-key";

	private JSONObject userInfo;

	Button cancelButton;

	EditText customerNameEditText;
	EditText contactPersonEditText;
	EditText phoneNumberEditText;
	EditText addressEditText;
	Spinner townshipSpinner;
	Spinner customerCategorySpinner;
	Spinner zoneSpinner;

	RadioButton radioCash, radioCredit;
	String payment_type = "P";

	Button addCustomerButton, resetButton;

	SQLiteDatabase database;

	ArrayList<JSONObject> townshipList;
	ArrayList<JSONObject> customerCategoryList;
	ArrayList<JSONObject> zoneList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_customer);

		// Hide keyboard on startup.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        try {

			userInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));
		} catch (JSONException e) {

			e.printStackTrace();
		}

        cancelButton = (Button) findViewById(R.id.cancel);

        customerNameEditText = (EditText) findViewById(R.id.customerName);
        contactPersonEditText = (EditText) findViewById(R.id.contactPerson);
        phoneNumberEditText = (EditText) findViewById(R.id.phoneNumber);
        addressEditText = (EditText) findViewById(R.id.address);
        townshipSpinner = (Spinner) findViewById(R.id.township);
        customerCategorySpinner = (Spinner) findViewById(R.id.customerCategoryList);
        zoneSpinner = (Spinner) findViewById(R.id.zoneList);

		radioCash = (RadioButton) findViewById(R.id.radioCash);
		radioCash.setChecked(true);
		radioCredit = (RadioButton) findViewById(R.id.radioCredit);

		radioCash.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				radioCredit.setChecked(false);
				payment_type = "P";
			}
		});

		radioCredit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				radioCredit.setChecked(true);
				radioCash.setChecked(false);
				payment_type = "R";
			}
		});

        addCustomerButton = (Button) findViewById(R.id.addCustomer);
        resetButton = (Button) findViewById(R.id.reset);

        database = new Database(this).getDataBase();

        cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AddNewCustomerActivity.this.onBackPressed();
			}
		});

        townshipList = getTownshipList();
        String[] townshipNames = new String[townshipList.size()];
        for (int i = 0; i < townshipNames.length; i++) {

        	try {

        		townshipNames[i] = townshipList.get(i).getString("townshipName");
			} catch (JSONException e) {

				e.printStackTrace();
			}
        }
        ArrayAdapter<String> townshipNamesArrayAdapter =
        		new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, townshipNames);
        townshipNamesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        townshipSpinner.setAdapter(townshipNamesArrayAdapter);

        customerCategoryList = getCustomerCategoryList();
        String[] customerCategoryNames = new String[customerCategoryList.size()];
        for (int i = 0; i < customerCategoryNames.length; i++) {

        	try {

				customerCategoryNames[i] = customerCategoryList.get(i).getString("name");
			} catch (JSONException e) {

				e.printStackTrace();
			}
        }
        ArrayAdapter<String> customerCategoryNamesArrayAdapter =
        		new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, customerCategoryNames);
        customerCategoryNamesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerCategorySpinner.setAdapter(customerCategoryNamesArrayAdapter);

        zoneList = getZoneList();
        String[] zones = new String[zoneList.size()];
        for (int i = 0; i < zones.length; i++) {

        	try {

				zones[i] = zoneList.get(i).getString("zoneName");
			} catch (JSONException e) {

				e.printStackTrace();
			}
        }
        ArrayAdapter<String> zonesArrayAdapter = 
        		new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, zones);
        zonesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoneSpinner.setAdapter(zonesArrayAdapter);

        addCustomerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				boolean isErrorFlag = false;
				if (customerNameEditText.getText().length() == 0) {

					customerNameEditText.setError("Customer name is required.");
					isErrorFlag = true;
				}

				if (contactPersonEditText.getText().length() == 0) {

					contactPersonEditText.setError("Contact person is required.");
					isErrorFlag = true;
				}

				if (phoneNumberEditText.getText().length() == 0) {

					phoneNumberEditText.setError("Phone number is required.");
					isErrorFlag = true;
				}

				if (addressEditText.getText().length() == 0) {

					addressEditText.setError("Address is required.");
					isErrorFlag = true;
				}

				if (isErrorFlag) {

					return;
				}

				String userId = "";
				String zoneCode = "";
				String townshipNumber = "";
				String townshipName = "";
				String customerCategoryId = "";
				String customerCategoryName = "";
				try {

					userId = userInfo.getString("userId");
					zoneCode = zoneList.get(zoneSpinner.getSelectedItemPosition()).getString("zoneCode");
					townshipNumber = townshipList.get(townshipSpinner.getSelectedItemPosition()).getString("townshipNumber");
					townshipName = townshipList.get(townshipSpinner.getSelectedItemPosition()).getString("townshipName");
					customerCategoryId = customerCategoryList.get(customerCategorySpinner.getSelectedItemPosition()).getString("id");
					customerCategoryName = customerCategoryList.get(customerCategorySpinner.getSelectedItemPosition()).getString("name");
				} catch (JSONException e) {

					e.printStackTrace();
				}
				String customerId = Preferences.getNextNewCustomerId(AddNewCustomerActivity.this, userId);
				Log.e("TempCusId>>>", customerId);
				/*String sql = "INSERT INTO NEW_CUSTOMER VALUES("
						+ "\"" + customerId + "\","
						+ "\"" + customerNameEditText.getText().toString() + "\","
						+ "\"" + phoneNumberEditText.getText().toString() + "\","
						+ "\"" + addressEditText.getText().toString() + "\","
						+ "\"" + userId + "\","
						+ "\"" + contactPersonEditText.getText().toString() + "\","
						+ "\"" + zoneCode + "\","
						+ "\"" + customerCategoryId + "\","
						+ "\"" + townshipNumber + "\""
						+ ")";*/
                String sql = "INSERT INTO NEW_CUSTOMER VALUES("
                        + "\"" + payment_type + "\","
                        + "\"" + customerId + "\","
                        + "\"" + customerNameEditText.getText().toString() + "\","
                        + "\"" + phoneNumberEditText.getText().toString() + "\","
                        + "\"" + addressEditText.getText().toString() + "\","
                        + "\"" + userId + "\","
                        + "\"" + contactPersonEditText.getText().toString() + "\","
                        + "\"" + zoneCode + "\","
                        + "\"" + customerCategoryId + "\","
                        + "\"" + townshipNumber + "\""
                        + ")";
				database.beginTransaction();
				database.execSQL(sql);
				database.setTransactionSuccessful();	
				database.endTransaction();
				
				/*sql = "INSERT INTO CUSTOMER VALUES ("
						+ "\"" + customerId + "\","
						+ "\"" + customerNameEditText.getText().toString() + "\","
						+ "\"" + customerCategoryId + "\","
						+ "\"" + customerCategoryName + "\","
						+ "\"" + addressEditText.getText().toString() + "\","
						+ "\"" + phoneNumberEditText.getText().toString() + "\","
						+ "\"" + townshipName + "\","
						+ "" + 0 + ","
						+ "" + 0 + ","
						+ "" + 0 + ","
						+ "" + 0 + ","
						+ "" + 0 + ","
						+ "\"" + "R" + "\","
						+ "\"" + "true" + "\""
						+ ")";*/


				sql = "INSERT INTO CUSTOMER VALUES ("
						+ "\"" + customerId + "\","
						+ "\"" + customerNameEditText.getText().toString() + "\","
						+ "\"" + customerCategoryId + "\","
						+ "\"" + customerCategoryName + "\","
						+ "\"" + addressEditText.getText().toString() + "\","
						+ "\"" + phoneNumberEditText.getText().toString() + "\","
						+ "\"" + townshipName + "\","
						+ "" + 0 + ","
						+ "" + 0 + ","
						+ "" + 0 + ","
						+ "" + 0 + ","
						+ "" + 0 + ","
						+ "\"" + payment_type + "\","
						+ "\"" + "true" + "\""
						+ ")";
				database.beginTransaction();
				database.execSQL(sql);
				database.setTransactionSuccessful();	
				database.endTransaction();

				Cursor cursor = database.rawQuery("SELECT * FROM CUSTOMER WHERE CUSTOMER_ID = '"+customerId+"'", null);
				Cursor cursor1 = database.rawQuery("SELECT * FROM NEW_CUSTOMER WHERE CUSTOMER_ID = '"+customerId+"'", null);

                if(cursor.getCount() == cursor1.getCount()){
                    Toast.makeText(getApplicationContext(), "Add New Customer is successful.", Toast.LENGTH_SHORT).show();
                }

				Preferences.didUploadedNewCustomersToServer(AddNewCustomerActivity.this, false);
				reset();
			}
		});

        resetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				reset();
			}
		});
	}

	private ArrayList<JSONObject> getTownshipList() {

		ArrayList<JSONObject> townshipList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery("SELECT * FROM TOWNSHIP", null);
		while (cursor.moveToNext()) {

			JSONObject townshipJsonObject = new JSONObject();
			try {

				townshipJsonObject.put("townshipNumber"
						, cursor.getString(cursor.getColumnIndex("TOWNSHIP_NUMBER")));
				townshipJsonObject.put("townshipName"
						, cursor.getString(cursor.getColumnIndex("TOWNSHIP_NAME")));
				townshipJsonObject.put("areaNumber"
						, cursor.getString(cursor.getColumnIndex("AREA_NUMBER")));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			townshipList.add(townshipJsonObject);
		}

		return townshipList;
	}

	private ArrayList<JSONObject> getCustomerCategoryList() {

		ArrayList<JSONObject> customerCategoryList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery("SELECT * FROM CUSTOMER_CATEGORY", null);
		while (cursor.moveToNext()) {

			JSONObject customerCategoryJsonObject = new JSONObject();
			try {

				customerCategoryJsonObject.put("id"
						, cursor.getString(cursor.getColumnIndex("CUSTOMER_CATEGORY_ID")));
				customerCategoryJsonObject.put("name"
						, cursor.getString(cursor.getColumnIndex("CUSTOMER_CATEGORY_NAME")));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			customerCategoryList.add(customerCategoryJsonObject);
		}

		return customerCategoryList;
	}

	private ArrayList<JSONObject> getZoneList() {

		ArrayList<JSONObject> zoneList = new ArrayList<JSONObject>();

		Cursor cursor = database.rawQuery("SELECT * FROM ZONE", null);
		while (cursor.moveToNext()) {

			JSONObject zoneJsonObject = new JSONObject();
			try {

				zoneJsonObject.put("zoneCode"
						, cursor.getString(cursor.getColumnIndex("ZONE_CODE")));
				zoneJsonObject.put("zoneName"
						, cursor.getString(cursor.getColumnIndex("ZONE_NAME")));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			zoneList.add(zoneJsonObject);
		}

		return zoneList;
	}

	private void reset() {

		customerNameEditText.setText("");
		customerNameEditText.setError(null);
		contactPersonEditText.setText("");
		contactPersonEditText.setError(null);
		phoneNumberEditText.setText("");
		phoneNumberEditText.setError(null);
		addressEditText.setText("");
		addressEditText.setError(null);
		if (customerCategoryList.size() > 0) {

			customerCategorySpinner.setSelection(0);
		}
		if (zoneList.size() > 0) {

			zoneSpinner.setSelection(0);
		}

		customerNameEditText.requestFocus();
	}
}
