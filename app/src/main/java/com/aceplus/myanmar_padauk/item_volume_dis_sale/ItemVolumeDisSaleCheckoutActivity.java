package com.aceplus.myanmar_padauk.item_volume_dis_sale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.general_sale.GeneralSaleActivity;
import com.aceplus.myanmar_padauk.models.Customer;
import com.aceplus.myanmar_padauk.models.SoldProduct;
import com.aceplus.myanmar_padauk.package_sale.PackageSalePrint;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Preferences;
import com.aceplus.myanmar_padauk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by i'm lovin' her on 11/16/15.
 */
public class ItemVolumeDisSaleCheckoutActivity extends Activity {

    public static final String USER_INFO_KEY = "user-info-key";
    public static final String CUSTOMER_INFO_KEY = "customer-info-key";
    public static final String SOLD_PROUDCT_LIST_KEY = "sold-product-list-key";

    JSONObject salemanInfo;
    Customer customer;
    ArrayList<SoldProduct> soldProductList;

    Button cancelButton;
    private TextView titleTextView;

    ListView soldProductsListView;

    TextView invoiceIdTextView;
    TextView saleDateTextView;
    TextView totalAmountTextView;

    TextView advancedPaidAmountTextView;

    TextView discountTextView;
    TextView netAmountTextView;
    EditText payAmountEditText;
    TextView refundTextView;
    EditText receiptPersonEditText;

    private EditText prepaidAmountEditText;

    private Button confirmAndPrintButton;

    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_sale_checkout);

        try {

            salemanInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));

        } catch (JSONException e) {

            e.printStackTrace();
        }
        customer = (Customer) getIntent().getSerializableExtra(CUSTOMER_INFO_KEY);
        soldProductList = (ArrayList<SoldProduct>) getIntent().getSerializableExtra(SOLD_PROUDCT_LIST_KEY);

        this.titleTextView = (TextView) findViewById(R.id.title);
        cancelButton = (Button) findViewById(R.id.cancel);
        soldProductsListView = (ListView) findViewById(R.id.soldProductList);
        saleDateTextView = (TextView) findViewById(R.id.saleDateTextView);
        invoiceIdTextView = (TextView) findViewById(R.id.invoiceId);
        totalAmountTextView = (TextView) findViewById(R.id.totalAmount);

        advancedPaidAmountTextView = (TextView) findViewById(R.id.advancedPaidAmount);

        discountTextView = (TextView) findViewById(R.id.discount);
        netAmountTextView = (TextView) findViewById(R.id.netAmount);
        payAmountEditText = (EditText) findViewById(R.id.payAmount);
        refundTextView = (TextView) findViewById(R.id.refund);
        receiptPersonEditText = (EditText) findViewById(R.id.receiptPerson);

        this.prepaidAmountEditText = (EditText) findViewById(R.id.prepaidAmount);
        this.prepaidAmountEditText.setVisibility(View.GONE);

        this.confirmAndPrintButton = (Button) findViewById(R.id.confirmAndPrint);

        this.titleTextView.setText("Item Volume Discount Sale - Checkout");

        double totalAmount = 0.0;
        for (SoldProduct soldProduct : soldProductList) {

            totalAmount += soldProduct.getTotalAmount();
        }
        totalAmountTextView.setText(Utils.formatAmount(totalAmount));

        LinearLayout totalInfoForGeneralSaleLinearLayout = (LinearLayout) findViewById(R.id.totalInfoForGeneralSale);
        LinearLayout totalInfoForPreOrderLinearLayout = (LinearLayout) findViewById(R.id.totalInfoForPreOrder);
        totalInfoForPreOrderLinearLayout.setVisibility(View.GONE);

        findViewById(R.id.advancedPaidAmountLayout).setVisibility(View.GONE);

        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                ItemVolumeDisSaleCheckoutActivity.this.onBackPressed();
            }
        });
        soldProductsListView.setAdapter(new SoldProductListRowAdapter(this));
        saleDateTextView.setText(Utils.getCurrentDate(false));

        try {
            invoiceIdTextView.setText(Utils.getInvoiceNo(this, salemanInfo.getString("userId"), salemanInfo.getString("locationCode"), Utils.FOR_OTHERS));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        database = new Database(this).getDataBase();

        double totalAmountForVolumeDiscount = 0.0;
        double totalItemDiscountAmount = 0.0;
        for (SoldProduct soldProduct : soldProductList) {

//			totalAmount += soldProduct.getTotalAmount();
            if (soldProduct.getProduct().getDiscountType().equalsIgnoreCase("v")) {

                totalAmountForVolumeDiscount += soldProduct.getTotalAmount();
            }
            //totalItemDiscountAmount += soldProduct.getDiscountAmount(this) + soldProduct.getExtraDiscountAmount();
            totalItemDiscountAmount += soldProduct.getDiscountAmount(this);
        }
        totalAmountTextView.setText(Utils.formatAmount(totalAmount));
        Double totalVolumeDiscount = 0.0;
        Cursor cursor = database.rawQuery(
                "SELECT DISCOUNT_PERCENT, DISCOUNT_AMOUNT"
                        + " FROM VOLUME_DISCOUNT WHERE"
                        + " FROM_AMOUNT <= " + totalAmountForVolumeDiscount
                        + " AND TO_AMOUNT >= " + totalAmountForVolumeDiscount, null);
        if (cursor.getCount() == 1) {

            cursor.moveToNext();
            if (cursor.getDouble(cursor.getColumnIndex("DISCOUNT_AMOUNT")) != 0) {

                totalVolumeDiscount = cursor.getDouble(cursor.getColumnIndex("DISCOUNT_AMOUNT"));
            } else {

                totalVolumeDiscount = totalAmountForVolumeDiscount
                        * cursor.getDouble(cursor.getColumnIndex("DISCOUNT_PERCENT")) / 100;
            }
        }

        Log.e("Total Item Dis>>>", totalItemDiscountAmount + "");
        Log.e("Total Vol Dis>>>", totalVolumeDiscount + "");

        discountTextView.setText(Utils.formatAmount(totalItemDiscountAmount + totalVolumeDiscount));

        netAmountTextView.setText(Utils.formatAmount(totalAmount - totalItemDiscountAmount - totalVolumeDiscount));

        payAmountEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

                if (charSequence.toString().length() > 0) {

                    String convertedString = charSequence.toString();
                    convertedString = Utils.formatAmount(Double.parseDouble(charSequence.toString().replace(",", "")));
                    if (!payAmountEditText.getText().toString().equals(convertedString)
                            && convertedString.length() > 0) {

                        payAmountEditText.setText(convertedString);
                        payAmountEditText.setSelection(payAmountEditText.getText().length());
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {}

            @Override
            public void afterTextChanged(Editable editable) {

                String tempPayAmount = editable.toString().replace(",", "");
                String tempNetAmount = netAmountTextView.getText().toString().replace(",", "");

                if (tempPayAmount.length() > 0 && tempNetAmount.length() > 0) {

                    if (Double.parseDouble(tempPayAmount) >= Double.parseDouble(tempNetAmount)) {

                        refundTextView.setText(Utils.formatAmount(Double.parseDouble(tempPayAmount) - Double.parseDouble(tempNetAmount)));
                    } else {

                        refundTextView.setText("0");
                    }
                }
            }
        });

        this.confirmAndPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (payAmountEditText.getText().length() == 0) {

                    new AlertDialog.Builder(ItemVolumeDisSaleCheckoutActivity.this)
                            .setTitle("Item Volume Discount Sale")
                            .setMessage("You must provide 'Pay Amount'.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                    payAmountEditText.requestFocus();
                                }
                            })
                            .show();

                    return;
                } else if (Double.parseDouble(payAmountEditText.getText().toString().replace(",", ""))
                        < Double.parseDouble(netAmountTextView.getText().toString().replace(",", ""))) {

                    if(!customer.getPaymentType().equals("R")){
                        new AlertDialog.Builder(ItemVolumeDisSaleCheckoutActivity.this)
                                .setTitle("Item Volume Discount Sale")
                                .setMessage("You aren't Credit Customer.Insuficient pay amount.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {

                                        payAmountEditText.selectAll();
                                    }
                                })
                                .show();

                        return;
                    }
                    /*else{
                        if((Double.parseDouble(netAmountTextView.getText().toString().replace(",", "")) - Double.parseDouble(payAmountEditText.getText().toString().replace(",", ""))) > customer.getCreditLimit()){
                            new AlertDialog.Builder(GeneralSaleCheckoutActivity.this)
                                    .setTitle("General Sale")
                                    .setMessage("Insuficient credit limit.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {

                                            payAmountEditText.selectAll();
                                        }
                                    })
                                    .show();

                            return;
                        }
                    }*/

                } else if (receiptPersonEditText.getText().toString().length() == 0) {

                    new AlertDialog.Builder(ItemVolumeDisSaleCheckoutActivity.this)
                            .setTitle("Alert")
                            .setMessage("Your must provide 'Receipt Person'.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                    receiptPersonEditText.requestFocus();
                                }
                            })
                            .show();

                    return;
                } else if (!isSerialsMatchWithQuantity()) {

					new AlertDialog.Builder(ItemVolumeDisSaleCheckoutActivity.this)
						.setTitle("Serial setup required.")
						.setMessage("Serial setup is not match with sale quantity.")
						.setPositiveButton("OK", null)
						.show();

					return;
				}

                insertIntoDB();
                updateRemainingQuantityForProducts();

                Preferences.didUploadedSaleDataToServer(ItemVolumeDisSaleCheckoutActivity.this, false);

                Intent intent = new Intent(ItemVolumeDisSaleCheckoutActivity.this, PackageSalePrint.class);
                intent.putExtra(PackageSalePrint.USER_INFO_KEY, salemanInfo.toString());
                intent.putExtra(PackageSalePrint.CUSTOMER_INFO_KEY, customer);
                intent.putExtra(PackageSalePrint.SOLD_PROUDCT_LIST_KEY, soldProductList);
                intent.putExtra(PackageSalePrint.INVOICE_ID, invoiceIdTextView.getText().toString());
                intent.putExtra(PackageSalePrint.PAY_AMOUNT_KEY, Double.parseDouble(payAmountEditText.getText().toString().replace(",", "")));

                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(ItemVolumeDisSaleCheckoutActivity.this
                , ItemVolumeDisSaleActivity.class);
        intent.putExtra(ItemVolumeDisSaleActivity.USER_INFO_KEY, this.salemanInfo.toString());
        intent.putExtra(ItemVolumeDisSaleActivity.CUSTOMER_INFO_KEY, this.customer);
        intent.putExtra(ItemVolumeDisSaleActivity.SOLD_PROUDCT_LIST_KEY, this.soldProductList);

        startActivity(intent);
        super.onBackPressed();
    }

    private boolean isSerialsMatchWithQuantity() {

        for (SoldProduct soldProduct : soldProductList) {

            int count = 0;
            for (String serial : soldProduct.getSerialList()) {

                try {

                    JSONObject jsonObject = new JSONObject(serial);
                    count += jsonObject.getInt("serialQty");
                    //count += jsonObject.getInt("toSerialNo") - jsonObject.getInt("fromSerialNo") + 1;
					/*if((jsonObject.getInt("toSerialNo") - jsonObject.getInt("fromSerialNo") + 1)%3 == 0){
						count += 1;
					}
					else if((jsonObject.getInt("toSerialNo") - jsonObject.getInt("fromSerialNo") + 1)%5 == 0){
						count += 1;
					}
					else{
						count += jsonObject.getInt("toSerialNo") - jsonObject.getInt("fromSerialNo") + 1;
					}*/

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }

            if (count != soldProduct.getQuantity()) {

                return false;
            }
        }
        return true;
    }

    private void insertIntoDB() {

        String customerId = customer.getCustomerId();
        String saleDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String invoiceId = invoiceIdTextView.getText().toString();
        double totalDiscountAmount = Double.parseDouble(discountTextView.getText().toString().replace(",", ""));
        double totalAmount = Double.parseDouble(netAmountTextView.getText().toString().replace(",", ""))
                + totalDiscountAmount;
        double payAmount = Double.parseDouble(payAmountEditText.getText().toString().replace(",", ""));
        double refundAmount = Double.parseDouble(refundTextView.getText().toString().replace(",", ""));
        String receiptPersonName = receiptPersonEditText.getText().toString();
        String salePersonId = null;
        try {

            salePersonId = salemanInfo.getString("userId");
        } catch (JSONException e) {

            e.printStackTrace();
        }
        String dueDate = saleDate;
        //String cashOrCredit = "C";
        String cashOrCredit = customer.getPaymentType();//edited by HAK
        String locationCode = null;
        try {

            locationCode = salemanInfo.getString("locationCode");
        } catch (JSONException e) {

            e.printStackTrace();
        }
        String deviceId = Utils.getDeviceId(this);
        String invoiceTime = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date());

        JSONArray saleProducts = new JSONArray();
        for (SoldProduct soldProduct : soldProductList) {

            JSONObject jsonObject = new JSONObject();
            try {

                jsonObject.put("productID", soldProduct.getProduct().getId());
                jsonObject.put("saleQty", soldProduct.getQuantity());
                jsonObject.put("salePrice", soldProduct.getProduct().getPrice());
                jsonObject.put("purchasePrice", soldProduct.getProduct().getPurchasePrice());
                //jsonObject.put("discountAmt", soldProduct.getProduct().getPrice()
                //* soldProduct.getDiscount(this) / 100);
                //jsonObject.put("discountAmt", Math.floor((soldProduct.getProduct().getPrice() * (soldProduct.getDiscount(this) + soldProduct.getExtraDiscount()) / 100)));//update dis problem by HAK
                jsonObject.put("discountAmt", soldProduct.getItemDiscountAmt());
                jsonObject.put("totalAmt", soldProduct.getNetAmount(this));
                //jsonObject.put("discountPercent", soldProduct.getDiscount(this));
                //jsonObject.put("extraDiscount", soldProduct.getExtraDiscount());
                jsonObject.put("discountPercent", soldProduct.getItemDiscountPercent());

                JSONArray serials = new JSONArray();
                for (String serial : soldProduct.getSerialList()) {

                    serials.put(new JSONObject(serial));
                }
                jsonObject.put("serialList", serials.toString());
            } catch (JSONException e) {

                e.printStackTrace();
            }
            saleProducts.put(jsonObject);
        }
        String saleProduct = saleProducts.toString();

        database.beginTransaction();
        database.execSQL("INSERT INTO INVOICE VALUES (\""
                + customerId + "\", \""
                + saleDate + "\", \""
                + invoiceId + "\", \""
                + totalAmount + "\", \""
                + totalDiscountAmount + "\", \""
                + payAmount + "\", \""
                + refundAmount + "\", \""
                + receiptPersonName + "\", \""
                + salePersonId + "\", \""
                + dueDate + "\", \""
                + cashOrCredit + "\", \""
                + locationCode + "\", \""
                + deviceId + "\", \""
                + invoiceTime + "\", "
                + null + ", "
                + null + ", "
                + null + ", "
                + null + ", \'"
                + saleProduct.toString() + "\'"
                + ")");
        database.setTransactionSuccessful();
        database.endTransaction();

        System.out.println("INSERT INTO INVOICE VALUES (\""
                + customerId + "\", \""
                + saleDate + "\", \""
                + invoiceId + "\", \""
                + totalAmount + "\", \""
                + totalDiscountAmount + "\", \""
                + payAmount + "\", \""
                + refundAmount + "\", \""
                + receiptPersonName + "\", \""
                + salePersonId + "\", \""
                + dueDate + "\", \""
                + cashOrCredit + "\", \""
                + locationCode + "\", \""
                + deviceId + "\", \""
                + invoiceTime + "\", "
                + null + ", "
                + null + ", "
                + null + ", "
                + null + ", \'"
                + saleProduct.toString() + "\'"
                + ")");
        Cursor cursor = database.rawQuery("SELECT CUSTOMER_ID FROM INVOICE", null);
        if (cursor.moveToNext()) {

            System.out.println(cursor.getString(cursor.getColumnIndex("CUSTOMER_ID")));
        }
    }

    private void updateRemainingQuantityForProducts() {

        Cursor cursor;
        for (SoldProduct soldProduct : soldProductList) {

            cursor = database.rawQuery("SELECT REMAINING_QTY FROM PRODUCT WHERE PRODUCT_ID = \""
                    + soldProduct.getProduct().getId() + "\"", null);
            // Sure cursor has record.
            cursor.moveToNext();
            int totalQuantity = cursor.getInt(cursor.getColumnIndex("REMAINING_QTY")) - soldProduct.getQuantity();
            database.beginTransaction();
            database.execSQL("UPDATE PRODUCT SET REMAINING_QTY = " + totalQuantity + " WHERE PRODUCT_ID = \""
                    + soldProduct.getProduct().getId() + "\"");
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    private class SoldProductListRowAdapter extends ArrayAdapter<SoldProduct> {

        final Activity context;

        public SoldProductListRowAdapter(Activity context) {

            super(context, R.layout.list_row_sold_product_with_serial_setup, soldProductList);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final SoldProduct soldProduct = soldProductList.get(position);

            LayoutInflater layoutInflater = context.getLayoutInflater();
            View view= layoutInflater.inflate(R.layout.list_row_sold_product_with_serial_setup, null, true);

            final TextView nameTextView = (TextView) view.findViewById(R.id.name);
            final TextView qtyTextView = (TextView) view.findViewById(R.id.qty);
            final TextView priceTextView = (TextView) view.findViewById(R.id.price);
            final TextView discountTextView = (TextView) view.findViewById(R.id.discount);
            final TextView totalAmountTextView = (TextView) view.findViewById(R.id.amount);

            Button serialSetupButton = (Button) view.findViewById(R.id.serial);

                serialSetupButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View view = layoutInflater.inflate(R.layout.dialog_box_serial_setup, null);

                        ((Button) view.findViewById(R.id.add)).setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {

                                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                final View view = layoutInflater.inflate(R.layout.dialog_box_serial_add, null);

                                final AlertDialog alertDialog = new AlertDialog.Builder(context)
                                        .setView(view)
                                        .setTitle("Add Serial")
                                        .setPositiveButton("OK", null)
                                        .setNegativeButton("Cancel", null)
                                        .create();
                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                    @Override
                                    public void onShow(DialogInterface arg0) {

                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View arg0) {

                                                JSONObject jsonObject = new JSONObject();

                                                EditText quantityEditText = (EditText) view.findViewById(R.id.quantity);
                                                EditText fromEditText = (EditText) view.findViewById(R.id.from);
                                                EditText toEditText = (EditText) view.findViewById(R.id.to);

                                                TextView message = (TextView) view.findViewById(R.id.message);
                                                if (quantityEditText.length() == 0) {

                                                    message.setText("You must provide 'Quantity'.");
                                                    quantityEditText.requestFocus();

                                                    return;
                                                } else if (fromEditText.length() == 0) {

                                                    message.setText("You must provide 'From'.");
                                                    fromEditText.requestFocus();

                                                    return;
                                                } else if (toEditText.length() == 0) {

                                                    message.setText("You must provide 'To'.");
                                                    toEditText.requestFocus();

                                                    return;
                                                } /*else if (Double.parseDouble(quantityEditText.getText().toString())
														!= Double.parseDouble(toEditText.getText().toString())
															- Double.parseDouble(fromEditText.getText().toString()) + 1) {

													message.setText("Quantity and serial number range is not match.");
													fromEditText.selectAll();

													return;
												}*/

                                                try {
                                                    jsonObject.put("serialQty", quantityEditText.getText().toString());
                                                    jsonObject.put("toSerialNo", toEditText.getText().toString());
                                                    jsonObject.put("fromSerialNo", fromEditText.getText().toString());
                                                } catch (JSONException e) {

                                                    e.printStackTrace();
                                                }

                                                soldProduct.getSerialList().add(jsonObject.toString());
                                                serialList.clear();
                                                for (String serial : soldProduct.getSerialList()) {

                                                    try {

                                                        serialList.add(new JSONObject(serial));
                                                    } catch (JSONException e) {

                                                        e.printStackTrace();
                                                    }
                                                }
                                                serialListRowAdapter.notifyDataSetChanged();

                                                alertDialog.dismiss();
                                            }
                                        });
                                    }
                                });
                                alertDialog.show();
                            }
                        });

                        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setView(view)
                                .setTitle("Serial Setup")
                                .setPositiveButton("OK", null)
                                .create();
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                            @Override
                            public void onShow(DialogInterface arg0) {

                                serialListRowAdapter = new SerialListRowAdapter(context);
                                serialListRowAdapter.soldProduct = soldProduct;
                                serialList.clear();
                                for (String serial : soldProduct.getSerialList()) {

                                    try {

                                        serialList.add(new JSONObject(serial));
                                    } catch (JSONException e) {

                                        e.printStackTrace();
                                    }
                                }
                                ((ListView) view.findViewById(R.id.serialList)).setAdapter(serialListRowAdapter);
                                ((ListView) view.findViewById(R.id.serialList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(AdapterView<?> parent,
                                                            View view, final int position, long id) {

                                        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                        final View editView = layoutInflater.inflate(R.layout.dialog_box_serial_add, null);

                                        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                                                .setView(editView)
                                                .setTitle("Edit Serial")
                                                .setPositiveButton("OK", null)
                                                .setNegativeButton("Cancel", null)
                                                .create();
                                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                            @Override
                                            public void onShow(DialogInterface arg0) {

                                                final EditText quantityEditText = (EditText) editView.findViewById(R.id.quantity);
                                                final EditText fromEditText = (EditText) editView.findViewById(R.id.from);
                                                final EditText toEditText = (EditText) editView.findViewById(R.id.to);
                                                final TextView message = (TextView) editView.findViewById(R.id.message);

                                                try {

                                                    JSONObject serialJsonObject = new JSONObject(soldProduct.getSerialList().get(position));
                                                    /*int fromSerialNumber = serialJsonObject.getInt("fromSerialNo");
                                                    int toSerialNumber = serialJsonObject.getInt("toSerialNo");*/

                                                    String fromSerialNumber = serialJsonObject.getString("fromSerialNo");
                                                    String toSerialNumber = serialJsonObject.getString("toSerialNo");

                                                    //quantityEditText.setText(toSerialNumber - fromSerialNumber + 1 + "");
                                                    quantityEditText.setText(serialJsonObject.getString("serialQty"));
                                                    fromEditText.setText(fromSerialNumber + "");
                                                    toEditText.setText(toSerialNumber + "");
                                                } catch (JSONException e1) {

                                                    e1.printStackTrace();
                                                }

                                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                                                    @Override
                                                    public void onClick(View arg0) {

                                                        if (quantityEditText.length() == 0) {

                                                            message.setText("You must provide 'Quantity'.");
                                                            quantityEditText.requestFocus();

                                                            return;
                                                        } else if (fromEditText.length() == 0) {

                                                            message.setText("You must provide 'From'.");
                                                            fromEditText.requestFocus();

                                                            return;
                                                        } else if (toEditText.length() == 0) {

                                                            message.setText("You must provide 'To'.");
                                                            toEditText.requestFocus();

                                                            return;
                                                        } /*else if (Double.parseDouble(quantityEditText.getText().toString())
																!= Double.parseDouble(toEditText.getText().toString())
																	- Double.parseDouble(fromEditText.getText().toString()) + 1) {

															message.setText("Quantity and serial number range is not match.");
															fromEditText.selectAll();

															return;
														}*/

                                                        JSONObject updatedSerialJsonObject = new JSONObject();
                                                        try {
                                                            updatedSerialJsonObject.put("serialQty", quantityEditText.getText().toString());
                                                            updatedSerialJsonObject.put("toSerialNo", toEditText.getText().toString());
                                                            updatedSerialJsonObject.put("fromSerialNo", fromEditText.getText().toString());
                                                        } catch (JSONException e) {

                                                            e.printStackTrace();
                                                        }

                                                        soldProduct.getSerialList().set(position, updatedSerialJsonObject.toString());
                                                        serialList.clear();
                                                        for (String serial : soldProduct.getSerialList()) {

                                                            try {

                                                                serialList.add(new JSONObject(serial));
                                                            } catch (JSONException e) {

                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        serialListRowAdapter.notifyDataSetChanged();

                                                        alertDialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                        alertDialog.show();
                                    }
                                });
                            }
                        });

                        alertDialog.show();
                    }
                });

            nameTextView.setText(soldProduct.getProduct().getName());
            qtyTextView.setText(soldProduct.getQuantity() + "");
            priceTextView.setText(Utils.formatAmount(soldProduct.getProduct().getPrice()));

            //double discountPercent = soldProduct.getDiscount(context) + soldProduct.getExtraDiscount();
            double discountPercent = soldProduct.getItemDiscountPercent();
            discountTextView.setText(discountPercent + "%");

            Double totalAmount = soldProduct.getProduct().getPrice() * soldProduct.getQuantity();
            Double discount = totalAmount * discountPercent / 100;
            totalAmountTextView.setText(Utils.formatAmount(totalAmount - discount));

            return view;
        }
    }

    final ArrayList<JSONObject> serialList = new ArrayList<JSONObject>();
    SerialListRowAdapter serialListRowAdapter;
    private class SerialListRowAdapter extends ArrayAdapter<JSONObject> {

        final Activity context;
        SoldProduct soldProduct;

        public SerialListRowAdapter(Activity context) {

            super(context, R.layout.list_row_serial, serialList);
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater layoutInflater = context.getLayoutInflater();
            View view= layoutInflater.inflate(R.layout.list_row_serial, null, true);

            try {
                String serialQty = serialList.get(position).getString("serialQty");
                String from = serialList.get(position).getString("fromSerialNo");
                String to = serialList.get(position).getString("toSerialNo");
                //int quantity = Integer.parseInt(to) - Integer.parseInt(from) + 1;

                //((TextView) view.findViewById(R.id.quantity)).setText(quantity + "");
                ((TextView) view.findViewById(R.id.quantity)).setText(serialQty);
                ((TextView) view.findViewById(R.id.from)).setText(from);
                ((TextView) view.findViewById(R.id.to)).setText(to);
            } catch (JSONException e) {

                e.printStackTrace();
            }

            ((Button) view.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    soldProduct.getSerialList().remove(position);
                    serialList.clear();
                    for (String serial : soldProduct.getSerialList()) {

                        try {

                            serialList.add(new JSONObject(serial));
                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }
                    serialListRowAdapter.notifyDataSetChanged();
                }
            });

            return view;
        }
    }
}
