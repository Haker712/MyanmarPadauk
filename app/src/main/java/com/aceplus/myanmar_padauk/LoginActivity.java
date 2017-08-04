package com.aceplus.myanmar_padauk;

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
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Utils;

public class LoginActivity extends Activity {

	EditText userNameEditText;
	EditText passwordEditText;
	Button loginButton;

	SQLiteDatabase database;

	JSONObject salemanInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide keyboard on startup.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        userNameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login);

        database = new Database(this).getDataBase();

		findViewById(R.id.imgView).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backupDB();
			}
		});

        loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this)
					.setTitle("Login")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("OK", null);
				if (userNameEditText.getText().length() == 0) {
					
					alertDialog.setMessage("Username is required.");
					alertDialog.show();
					userNameEditText.requestFocus();

					return;
				} else if (passwordEditText.getText().length() == 0) {
					
					alertDialog.setMessage("Password is required.");
					alertDialog.show();
					passwordEditText.requestFocus();

					return;
				}

				if (hasSaleManDataInDb()) {
					
					salemanInfo = getSaleManFromDb();
					if (salemanInfo != null) {
						
						Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
						intent.putExtra(HomeActivity.USER_INFO_KEY, salemanInfo.toString());
						startActivity(intent);
						finish();
					} else {

						new AlertDialog.Builder(LoginActivity.this)
							.setTitle("Login")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setMessage("Username or password is incorrect.")
							.setPositiveButton("OK", null)
							.show();
						passwordEditText.selectAll();
					}
				} else {
					
					new Login().execute();
				}
			}
		});
    }

    private boolean hasSaleManDataInDb() {

    	return database.rawQuery("SELECT * FROM SALE_MAN", null).getCount() == 1;
    }

    private JSONObject getSaleManFromDb() {

    	Cursor cursor = database.rawQuery(
    			"SELECT * FROM SALE_MAN"
    			+ " WHERE USER_ID = \"" + userNameEditText.getText().toString() + "\""
    			+ " AND PASSWORD = \"" + passwordEditText.getText().toString() + "\"", null);
    	if (cursor.moveToNext()) {

    		JSONObject salemanJsonObject = new JSONObject();
    		try {

    			salemanJsonObject.put("userId", cursor.getString(cursor.getColumnIndex("USER_ID")));
    			salemanJsonObject.put("pwd", cursor.getString(cursor.getColumnIndex("PASSWORD")));
    			salemanJsonObject.put("userName", cursor.getString(cursor.getColumnIndex("USER_NAME")));
    			salemanJsonObject.put("locationCode", cursor.getString(cursor.getColumnIndex("LOCATION_CODE")));
			} catch (JSONException e) {

				e.printStackTrace();
			}

    		return salemanJsonObject;
    	}

    	return null;
    }

    private class Login extends AsyncTask<Void, Void, String> {

    	ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Login ...");
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {

			String result = null;

			try {

				URL url = new URL(DataDownloadActivity.URL + "usr/checkuserlogin/");
				HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setReadTimeout(3000);
				httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type", "application/json");

				OutputStream outputStream = httpUrlConnection.getOutputStream();
				try {

					JSONObject postJsonObject = new JSONObject();
					postJsonObject.put("userId", userNameEditText.getText().toString());
					postJsonObject.put("pwd", passwordEditText.getText().toString());
					postJsonObject.put("devID", Utils.getDeviceId(LoginActivity.this));
					postJsonObject.put("userGroup", "SaleMan");

					outputStream.write(postJsonObject.toString().getBytes());
					outputStream.flush();
				} catch (JSONException e) {

					e.printStackTrace();
				}

				if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

					return null;
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

				new AlertDialog.Builder(LoginActivity.this)
					.setTitle("Login")
					.setMessage("Can't connect to server.")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("OK", null)
					.show();

				return;
			}

			try {

				salemanInfo = new JSONObject(result);

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this)
					.setTitle("Login")
					.setPositiveButton("OK", null)
					.setIcon(android.R.drawable.ic_dialog_alert);
				if (salemanInfo.getString("status").equalsIgnoreCase("fail")) {

					alertDialog.setMessage("Login fail.")
						.show();
					passwordEditText.selectAll();

					return;
				} else if (!salemanInfo.getString("status").equalsIgnoreCase("success")) {
					
					alertDialog.setMessage(salemanInfo.getString("status"))
						.show();
					passwordEditText.selectAll();
	
					return;
				} else if (salemanInfo.getString("status").equalsIgnoreCase("lock")) {
					
					alertDialog.setMessage("User is locked.")
						.show();
	
					return;
				}

				database.beginTransaction();
				database.execSQL("INSERT INTO SALE_MAN VALUES (\""
						+ salemanInfo.getString("userId") + "\", \""
						+ salemanInfo.getString("pwd") + "\", \""
						+ salemanInfo.getString("userName") + "\", \""
						+ salemanInfo.getString("locationCode") + "\""
						+ ")");
				database.setTransactionSuccessful();	
				database.endTransaction();

				Intent intent = new Intent(LoginActivity.this, DataDownloadActivity.class);
				intent.putExtra(DataDownloadActivity.USER_INFO_KEY, salemanInfo.toString());
				startActivity(intent);
				finish();
			} catch (JSONException e) {

				e.printStackTrace();
			}
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
