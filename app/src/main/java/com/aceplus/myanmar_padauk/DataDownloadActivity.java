package com.aceplus.myanmar_padauk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.TLAJsonStringMaker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DataDownloadActivity extends Activity {

    public static final String USER_INFO_KEY = "user-info-key";

//	public static final String URL = "http://192.168.1.177:4040/mmpd/";
//	public final static String URL = "http://10.0.3.2:1337/";
//	public static final String URL = "http://192.168.137.1:4040/mmpd/";

    // Myanmar Padauk HO's branch server IP.
//	public static final String URL = "http://192.168.1.1:4040/mmpd/";


    //public static final String URL = "http://192.168.1.87:4040/mmpd/";//for Naypyidaw branch
    //public static final String URL = "http://192.168.2.87:4040/mmpd/";//for Monywa branch
    public static final String URL = "http://192.168.11.62:4040/mmpd/";

    private JSONObject userInfo;

    Button cancelButton, downloadAgainButton;

    ProgressBar dataDownloadProgressBar;
    TextView statusTextView;

    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_download);

        try {

            userInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));

        } catch (JSONException e) {

            e.printStackTrace();
        }

        cancelButton = (Button) findViewById(R.id.cancel);
        downloadAgainButton = (Button) findViewById(R.id.downloadAgain);
        dataDownloadProgressBar = (ProgressBar) findViewById(R.id.datadownloadProgress);
        statusTextView = (TextView) findViewById(R.id.status);

        database = new Database(this).getDataBase();

        // When cancel button is clicked, finish current activity and go to login activity.
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(DataDownloadActivity.this)
                        .setTitle("Cancel Download")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                startActivity(new Intent(DataDownloadActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setCancelable(false)
                        .setMessage("Are you sure you want to cancel.")
                        .show();
            }
        });

        // When download again button is clicked, reset current downloads and download again.
        downloadAgainButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dataDownloadProgressBar.setProgress(0);
                statusTextView.setText("");

                downloadDataFromServer();
            }
        });

        // Set the range of data download progress bar.
        dataDownloadProgressBar.setMax(110);

        downloadDataFromServer();
    }

    private void clearDownloadedDataFromDatabase() {

        String[] tableNames = {"CUSTOMER", "CUSTOMER_FEEDBACK", "ITEM_DISCOUNT", "PACKAGE", "PRODUCT", "VOLUME_DISCOUNT", "ZONE", "TOWNSHIP"};
        for (String tableName : tableNames) {

            database.beginTransaction();
            database.execSQL("DELETE FROM " + tableName);
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    private void downloadDataFromServer() {

        clearDownloadedDataFromDatabase();

        new GetCustomers().execute();
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
            values.add("");    // TODO
        } catch (JSONException e) {

            e.printStackTrace();
        }

        return TLAJsonStringMaker.jsonStringMaker(keys, values);
    }

    private class GetCustomers extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for customers.");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "cus/getcuslistinroute");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(10000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't connect to server when downloading data for customers.";
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

            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO CUSTOMER VALUES (\""
                            + jsonObject.getString("customerID") + "\", \""
                            + jsonObject.getString("customerName") + "\", \""
                            + jsonObject.getString("customerTypeID") + "\", \""
                            + jsonObject.getString("customerTypeName") + "\", \""
                            + jsonObject.getString("address") + "\", \""
                            + jsonObject.getString("ph") + "\", \""
                            + jsonObject.getString("township") + "\", \""
                            + jsonObject.getString("creditTerm") + "\", \""
                            + jsonObject.getString("creditLimit") + "\", \""
                            + jsonObject.getString("creditAmt") + "\", \""
                            + jsonObject.getString("dueAmt") + "\", \""
                            + jsonObject.getString("prepaidAmt") + "\", \""
                            + jsonObject.getString("paymentType") + "\", \""
                            + jsonObject.getString("isInRoute") + "\""
                            + ")";
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                    /*Cursor cursor = database.rawQuery("SELECT CUSTOMER_ID FROM CUSTOMER", null);
					if (cursor.moveToNext()) {

						System.out.println(cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")));
					}*/
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);
                new GetProducts().execute();
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }

    private class GetProducts extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for products.");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "pro/getproductlist");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(5000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't connect to server when downloading data for products.";
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

            // TODO
            // DELETE
            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO PRODUCT VALUES (\""
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
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);
                new GetVolumeDiscounts().execute();
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }

    private class GetVolumeDiscounts extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for volume discounts.");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "pro/getvolumediscountlist");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(3000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't connect to server when downloading data for volume discounts.";
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


            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO VOLUME_DISCOUNT VALUES ("
                            + jsonObject.getDouble("fromAmount") + ", "
                            + jsonObject.getDouble("toAmount") + ", "
                            + jsonObject.getDouble("discountPercent") + ", "
//							+ jsonObject.getString("discountAmount") + "\""		// TODO
                            + 0 + ""
                            + ")";
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);
                new GetItemDiscounts().execute();
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }

    private class GetItemDiscounts extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for item discounts");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "pro/getdiscountlist");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(3000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't connect to server when downloading data for item discounts.";
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

            if (result == null) {

                result = "[]";
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO ITEM_DISCOUNT VALUES (\""
                            + jsonObject.getString("stockNo") + "\", \""
                            + jsonObject.getString("discountPercent") + "\", "
//							+ jsonObject.getString("discountAmount") + "\", \"" // TODO
                            + 0 + ", "
                            + jsonObject.getDouble("startDiscountQty") + ", "
                            + jsonObject.getDouble("endDiscountQty") + ""
                            + ")";
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);
                new GetZones().execute();
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }

    private class GetZones extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for zones");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "cus/getzonelist");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(3000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't connect to server when downloading data for zones.";
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

            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO ZONE VALUES (\""
                            + jsonObject.getString("zoneNo") + "\", \""
                            + jsonObject.getString("zoneName") + "\""
                            + ")";
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);

                new GetCustomerCategories().execute();
//				(new GetCustomerFeedbacks()).execute();
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }

    private class GetCustomerCategories extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for customer categories.");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "cus/getcustomercategorylist");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(3000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't connect to server when downloading data for customer categories.";
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

            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO CUSTOMER_CATEGORY VALUES (\""
                            + jsonObject.getString("customerCagNo") + "\", \""
                            + jsonObject.getString("customerCagName") + "\""
                            + ")";
                    database.execSQL(sql);

                    dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);

                    System.out.println(sql + ";");
                }

                new GetCustomerFeedbacks().execute();
            } catch (JSONException e) {

                statusTextView.setText(result);

                e.printStackTrace();
            }
        }
    }

    private class GetCustomerFeedbacks extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for customer feedbacks");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "cus/getcustomerfeedback");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(3000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't download data for customer feedbacks.";
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

            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO CUSTOMER_FEEDBACK VALUES (\""
                            + jsonObject.getString("invNo") + "\", \""
                            + jsonObject.getString("invDate") + "\", \""
                            + jsonObject.getString("srNo") + "\", \""
                            + jsonObject.getString("description") + "\""
                            + ")";
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);

                new GetPackages().execute();
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }

    private class GetPackages extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for packages.");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "pack/getpackagelist");
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

            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO PACKAGE VALUES (\""
                            + jsonObject.getString("invno") + "\", \""
                            + jsonObject.getString("lno") + "\", \""
                            + jsonObject.getString("grade") + "\", \""
                            + jsonObject.getString("volumnStatus") + "\", \""
                            + jsonObject.getString("quantityStatus") + "\", "
                            + jsonObject.getDouble("maxAmount") + ", "
                            + jsonObject.getDouble("minAmount") + ", \'"
                            + jsonObject.getString("packageItems") + "\'"
                            + ")";
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);
                new GetTownships().execute();
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }

    private class GetTownships extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for townships.");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "/cus/gettownshiplist");
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

            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String sql = "INSERT INTO TOWNSHIP VALUES (\""
                            + jsonObject.getString("townshipNo") + "\", \""
                            + jsonObject.getString("townshipName") + "\", \""
                            + jsonObject.getString("areaNo") + "\""
                            + ")";
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);
//				new ConfirmGet().execute();
//				Intent intent = new Intent(DataDownloadActivity.this, HomeActivity.class);
//				intent.putExtra(HomeActivity.USER_INFO_KEY, userInfo.toString());
//				startActivity(intent);
//				finish();
                new GetPreOrders().execute();
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }

    private class GetPreOrders extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Downloading data for pre-orders.");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "pre/getpreorderlist");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(3000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't connect to server when downloading data for pre-orders.";
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

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            if (result == null) {

                return;
            }

            try {

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONArray preOrderItemListJSONArray = jsonObject.getJSONArray("preOrderItemList");
                    for (int j = 0; j < preOrderItemListJSONArray.length(); j++) {

                        for (int k = 0; k < preOrderItemListJSONArray.length(); ) {

                            if (j != k
                                    && preOrderItemListJSONArray.getJSONObject(j).getString("productId")
                                    .equals(preOrderItemListJSONArray.getJSONObject(k).getString("productId"))) {

                                int orderQuantity = preOrderItemListJSONArray.getJSONObject(j).getInt("orderQuantity")
                                        + preOrderItemListJSONArray.getJSONObject(k).getInt("orderQuantity");
                                int deliveredQuantity = preOrderItemListJSONArray.getJSONObject(j).getInt("deliveredQuantity")
                                        + preOrderItemListJSONArray.getJSONObject(k).getInt("deliveredQuantity");
                                preOrderItemListJSONArray.getJSONObject(j).put("orderQuantity", orderQuantity);
                                preOrderItemListJSONArray.getJSONObject(j).put("deliveredQuantity", deliveredQuantity);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                                    preOrderItemListJSONArray.remove(k);
                                } else {

                                    JSONArray copyPreOrderItemListJSONArray = new JSONArray();
                                    for (int l = 0; l < preOrderItemListJSONArray.length(); l++) {

                                        if (l != k) {

                                            copyPreOrderItemListJSONArray.put(preOrderItemListJSONArray.getJSONObject(l));
                                        }
                                    }
                                    preOrderItemListJSONArray = copyPreOrderItemListJSONArray;
                                }
                            } else {

                                k++;
                            }
                        }
                    }

                    double remainingAmt = 0;
                    remainingAmt = jsonObject.getDouble("amount") - jsonObject.getDouble("firstPaidAmount");

                    String sql = "INSERT INTO DELIVERY VALUES (\""
                            + jsonObject.getString("orderNo") + "\", \""
                            + jsonObject.getString("customerNo") + "\", "
                            + jsonObject.getDouble("amount") + ", "
                            + jsonObject.getDouble("firstPaidAmount") + ", "
                            //+ jsonObject.getDouble("remainAmount") + ", '"
                            + remainingAmt + ", '"
                            + preOrderItemListJSONArray.toString() + "'"
                            + ")";
                    database.beginTransaction();
                    database.execSQL(sql);
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    System.out.println(sql + ";");
                }

                dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);
                Intent intent = new Intent(DataDownloadActivity.this, HomeActivity.class);
                intent.putExtra(HomeActivity.USER_INFO_KEY, userInfo.toString());
                startActivity(intent);
                finish();
            } catch (JSONException e) {

                statusTextView.setText("Error in download preorder data.");
                e.printStackTrace();
            }
        }
    }

    private class ConfirmGet extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            statusTextView.setText("Confirm to server.");
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {

                URL url = new URL(URL + "usr/confirmget");
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setReadTimeout(3000);
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(makeSendingString().getBytes());
                outputStream.flush();

                if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    return "Can't connect to server when confirmation.";
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

            if (result == null) {

                return;
            }

            try {

                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getString("status").equalsIgnoreCase("success")) {

                    dataDownloadProgressBar.setProgress(dataDownloadProgressBar.getProgress() + 10);
                    statusTextView.setText("Data download complete.");

                    Intent intent = new Intent(DataDownloadActivity.this, HomeActivity.class);
                    intent.putExtra(HomeActivity.USER_INFO_KEY, userInfo.toString());
                    startActivity(intent);
                    finish();
                } else {

                    statusTextView.setText(jsonObject.getString("status"));
                }
            } catch (JSONException e) {

                statusTextView.setText(result);
                e.printStackTrace();
            }
        }
    }
}
