package com.aceplus.myanmar_padauk.package_sale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.R.color;
import com.aceplus.myanmar_padauk.R.id;
import com.aceplus.myanmar_padauk.R.layout;
import com.aceplus.myanmar_padauk.general_sale.GeneralSaleActivity;
import com.aceplus.myanmar_padauk.general_sale.GeneralSaleCheckoutActivity;
import com.aceplus.myanmar_padauk.models.Customer;
import com.aceplus.myanmar_padauk.models.Package;
import com.aceplus.myanmar_padauk.models.SoldProduct;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Preferences;
import com.aceplus.myanmar_padauk.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PackageSaleCheckoutActivity extends Activity {

	public static final String USER_INFO_KEY = "user-info-key";
	public static final String CUSTOMER_INFO_KEY = "customer-info-key";
	public static final String SOLD_PROUDCT_LIST_KEY = "sold-product-list-key";
	public static final String SOLD_PACKAGE_KEY = "sold-package-key";
	public static final String USER_PROVIDED_AMOUNT_KEY = "user-provided-amount-key";

	JSONObject salemanInfo;
	Customer customer;
	ArrayList<SoldProduct> soldProductList;
	Package soldPackage;
	double userProvidedAmount;

	Button cancelButton;

	ListView soldProductsListView;
	SoldProductListArrayAdapter soldProductListArrayAdapter;

	TextView saleDateTextView;

	TextView invoiceIdTextView;
	TextView totalAmountTextView;
	TextView extraDiscountAmountTextView;
	TextView netAmountTextView;
	EditText payAmountEditText;
	TextView refundTextView;
	EditText receiptPersonEditText;

	Button confirmAndPrint;

	SQLiteDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_package_sale_checkout);

		try {

			salemanInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));
		} catch (JSONException e) {

			e.printStackTrace();
		}
		customer = (Customer) getIntent().getSerializableExtra(CUSTOMER_INFO_KEY);
		soldProductList = (ArrayList<SoldProduct>) getIntent().getSerializableExtra(SOLD_PROUDCT_LIST_KEY);
		soldPackage = (Package) getIntent().getSerializableExtra(SOLD_PACKAGE_KEY);
		userProvidedAmount = getIntent().getDoubleExtra(USER_PROVIDED_AMOUNT_KEY, 0);

		cancelButton = (Button) findViewById(R.id.cancel);

		soldProductsListView = (ListView) findViewById(R.id.soldProductList);

		saleDateTextView = (TextView) findViewById(R.id.saleDate);

		invoiceIdTextView = (TextView) findViewById(R.id.invoiceId);
		try {

			invoiceIdTextView.setText(Utils.getInvoiceNo(this, salemanInfo.getString("userId"), salemanInfo.getString("locationCode"), Utils.forPackageSale));
		} catch (JSONException e) {

			e.printStackTrace();
		}
		totalAmountTextView = (TextView) findViewById(R.id.totalAmount);
		extraDiscountAmountTextView = (TextView) findViewById(R.id.extraDiscountAmount);
		netAmountTextView = (TextView) findViewById(R.id.netAmount);
		payAmountEditText = (EditText) findViewById(R.id.payAmount);
		refundTextView = (TextView) findViewById(R.id.refund);
		receiptPersonEditText = (EditText) findViewById(R.id.receiptPerson);

		confirmAndPrint = (Button) findViewById(R.id.confirmAndPrint);

		database = new Database(this).getDataBase();

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				PackageSaleCheckoutActivity.this.onBackPressed();
			}
		});
		// Setup products list view
		soldProductListArrayAdapter = new SoldProductListArrayAdapter(this);
		soldProductListArrayAdapter.notifyDataSetChanged();
		soldProductsListView.setAdapter(soldProductListArrayAdapter);

		saleDateTextView.setText(Utils.getCurrentDate(false));

		Double totalAmount = 0.0;
		Double totalDiscountAmount = 0.0;
		for (SoldProduct soldProduct : soldProductList) {

			totalAmount += soldProduct.getTotalAmount();
//			totalAmount += soldProduct.getNetAmount(this);
			totalDiscountAmount += soldProduct.getDiscountAmount(this) + soldProduct.getExtraDiscountAmount();
//			Double amount = soldProduct.getProduct().getPrice() * soldProduct.getQuantity();
//			totalAmount += amount - (amount * soldProduct.getDiscount(this) / 100);
		}
		totalAmountTextView.setText(Utils.formatAmount(totalAmount));
		extraDiscountAmountTextView.setText(Utils.formatAmount(totalDiscountAmount));
		netAmountTextView.setText(Utils.formatAmount(totalAmount - totalDiscountAmount));
//		extraDiscountButton.setText(extraDiscount + "%");
//		extraDiscountButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//
//				LayoutInflater layoutInflater = (LayoutInflater) PackageSaleCheckoutActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				final View view = layoutInflater.inflate(R.layout.request_activation_code_dialog_box, null);
//
//				final EditText activationCodeEditText = (EditText) view.findViewById(R.id.activationCode);
//				
//				final AlertDialog alertDialog = new AlertDialog.Builder(PackageSaleCheckoutActivity.this)
//					.setView(view)
//					.setTitle("Enter Activation Code")
//					.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface arg0, int arg1) {
//
//							if (activationCodeEditText.getText().toString().equalsIgnoreCase("01234")) {
//								
//								final AlertDialog alertDialog = new AlertDialog.Builder(PackageSaleCheckoutActivity.this)
//									.setTitle("Alert")
//									.setMessage("Your activation code is incorrect.")		
//									.setPositiveButton("OK", null)
//									.create();
//	
//								alertDialog.show();
//								return;
//							} else {
//
//								LayoutInflater layoutInflater = (LayoutInflater) PackageSaleCheckoutActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//								final View view = layoutInflater.inflate(R.layout.request_extra_discount_dialog_box, null);
//
//								final EditText extraDiscountEditteEditText = (EditText) view.findViewById(R.id.extraDiscount);
//								AlertDialog alertdialog = new AlertDialog.Builder(PackageSaleCheckoutActivity.this)
//									.setView(view)
//									.setTitle("Enter Extra Discount")
//									.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//										
//										@Override
//										public void onClick(DialogInterface arg0, int arg1) {
//
//											double extraDiscount = 0.0;
//											for (SoldProduct soldProduct : soldProductList) {
//												
//												soldProduct.setCurrentExtraDiscount(Double.parseDouble(extraDiscountEditteEditText.getText().toString()));
//												extraDiscountButton.setText(extraDiscountEditteEditText.getText().toString() + "%");
//
//												double totalAmount = soldProduct.getProduct().getPrice() * soldProduct.getQuantity();
//												double sample = totalAmount * Double.parseDouble(extraDiscountEditteEditText.getText().toString()) / 100;
//												extraDiscount += totalAmount * Double.parseDouble(extraDiscountEditteEditText.getText().toString()) / 100;
//											}
//
//											extraDiscountAmount.setText(extraDiscount + "");
//											netAmountTextView.setText(Double.parseDouble(totalAmountTextView.getText().toString()) - extraDiscount + "");
//											if (payAmountEditText.getText().length() > 0) {
//												
//												refundTextView.setText(Double.parseDouble(payAmountEditText.getText().toString()) - Double.parseDouble(netAmountTextView.getText().toString()) + "");
//											}
//										}
//									})
//									.setCancelable(false)
//									.create();
//								alertdialog.show();
//							}
//						}
//					})
//					.setNegativeButton("Cancel", null)
//					.setCancelable(false)
//					.create();
//				alertDialog.show();
//			}
//		});

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
			public void afterTextChanged(Editable arg0) {

				String tempPayAmount = payAmountEditText.getText().toString().replace(",", "");
				String tempNetAmount = netAmountTextView.getText().toString().replace(",", "");
				
				if (tempPayAmount.length() > 0 && tempNetAmount.length() > 0) {
					
					if (Double.parseDouble(tempPayAmount) >= Double.parseDouble(tempNetAmount)) {
						
						refundTextView.setText(Utils.formatAmount(Double.parseDouble(tempPayAmount) - Double.parseDouble(tempNetAmount)));
					}
				} else {

					refundTextView.setText("0");
				}
			}
		});

		confirmAndPrint.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(PackageSaleCheckoutActivity.this)
					.setTitle("Incomplete form.")
					.setPositiveButton("OK", null)
					.setIcon(android.R.drawable.ic_dialog_alert);

				if (payAmountEditText.getText().length() == 0) {

					alertDialog.setMessage("Pay amount is required.");
					alertDialog.show();
					payAmountEditText.requestFocus();
				} else if (Double.parseDouble(payAmountEditText.getText().toString().replace(",", ""))
						< Double.parseDouble(netAmountTextView.getText().toString().replace(",", ""))) {

					alertDialog.setMessage("Insufficient pay amount.");
					alertDialog.show();
					payAmountEditText.selectAll();
				} else if (receiptPersonEditText.getText().length() == 0) {

					alertDialog.setMessage("Receipt person is required.");
					alertDialog.show();
					receiptPersonEditText.requestFocus();
				} else if (!isSerialsMatchWithQuantity()) {

					alertDialog.setMessage("Serial setup is not match with sale quantity.");
					alertDialog.show();
				} else {

					insertIntoDB();
					updateRemainingQuantityForProducts();

					Preferences.didUploadedSaleDataToServer(PackageSaleCheckoutActivity.this, false);

					Intent intent = new Intent(PackageSaleCheckoutActivity.this, PackageSalePrint.class);
					intent.putExtra(PackageSalePrint.USER_INFO_KEY, salemanInfo.toString());
					intent.putExtra(PackageSalePrint.CUSTOMER_INFO_KEY, customer);
					intent.putExtra(PackageSalePrint.SOLD_PROUDCT_LIST_KEY, soldProductList);
					intent.putExtra(PackageSalePrint.INVOICE_ID, invoiceIdTextView.getText().toString());
					intent.putExtra(PackageSalePrint.PAY_AMOUNT_KEY, Double.parseDouble(payAmountEditText.getText().toString().replace(",", "")));
//					intent.putExtra(PackageSalePrint.CURRENT_DISCOUNT_KEY, Double.parseDouble(extraDiscountAmountTextView.getText().toString().replace(",", "")));
					startActivity(intent);
					finish();
				}
			}
		});
	}

	private void insertIntoDB() {

		String customerId = customer.getCustomerId();
		String saleDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
		String invoiceId = invoiceIdTextView.getText().toString();
		double totalDiscountAmount = Double.parseDouble(extraDiscountAmountTextView.getText().toString().replace(",", ""));
		double totalAmount = Double.parseDouble(netAmountTextView.getText().toString().replace(",", ""))
				+ totalDiscountAmount;
		double payAmount = Double.parseDouble(payAmountEditText.getText().toString().replace(",", ""));
		double refundAmount = Double.parseDouble(refundTextView.getText().toString().replace(",", ""));
		String receiptPersonName = receiptPersonEditText.getText().toString();
		String salePersonId = null;
		try {

			salePersonId = salemanInfo.getString("userId");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String dueDate = saleDate;
		String cashOrCredit = "C";
		String locationCode = null;
		try {

			locationCode = salemanInfo.getString("locationCode");
		} catch (JSONException e) {

			e.printStackTrace();
		}
		String deviceId = Utils.getDeviceId(this);
		String invoiceTime = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date());
		String packageInvoiceNumber = soldPackage.getInvoiceNumber();
		String packageStatus = new String();
		Cursor cursor = database.rawQuery(
				"SELECT VOLUME, QUANTITY FROM PACKAGE"
				+ " WHERE INV_NO = \"" + packageInvoiceNumber + "\"", null);
		if (cursor.moveToNext()) {

			if (cursor.getString(cursor.getColumnIndex("VOLUME")).equalsIgnoreCase("Y")) {

				packageStatus = "V";
			} else if (cursor.getString(cursor.getColumnIndex("QUANTITY")).equalsIgnoreCase("Y")) {

				packageStatus = "Q";
			}
		}
		double volumeAmount = userProvidedAmount;
		String packageGrade = soldPackage.getGrade();
		JSONArray saleProducts = new JSONArray();
		for (SoldProduct soldProduct : soldProductList) {

			JSONObject jsonObject = new JSONObject();
			try {

				jsonObject.put("productID", soldProduct.getProduct().getId());
				jsonObject.put("saleQty", soldProduct.getQuantity());
				jsonObject.put("salePrice", soldProduct.getProduct().getPrice());
				jsonObject.put("purchasePrice", soldProduct.getProduct().getPurchasePrice());
				//jsonObject.put("discountAmt", soldProduct.getProduct().getPrice()
						//* (soldProduct.getDiscount(this) + soldProduct.getExtraDiscount()) / 100);
				jsonObject.put("discountAmt", Math.floor((soldProduct.getProduct().getPrice() * (soldProduct.getDiscount(this) + soldProduct.getExtraDiscount()) / 100)));//update dis problem by HAK
				jsonObject.put("totalAmt", soldProduct.getNetAmount(this));
				jsonObject.put("discountPercent", soldProduct.getDiscount(this));
				// TODO
				jsonObject.put("extraDiscount", soldProduct.getExtraDiscount());

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
				+ invoiceTime + "\", \""
				+ packageInvoiceNumber + "\", \""
				+ packageStatus + "\", \""
				+ volumeAmount + "\", \""
				+ packageGrade + "\", \'"
				+ saleProduct.toString() + "\'"
				+ ")");
		database.setTransactionSuccessful();	
		database.endTransaction();
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

	private boolean isSerialsMatchWithQuantity() {

		for (SoldProduct soldProduct : soldProductList) {

			int count = 0;
			for (String serial : soldProduct.getSerialList()) {

				try {

					JSONObject jsonObject = new JSONObject(serial);
					count += jsonObject.getInt("serialQty");
					//count += jsonObject.getInt("toSerialNo") - jsonObject.getInt("fromSerialNo") + 1;
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

	private class SoldProductListArrayAdapter extends ArrayAdapter<SoldProduct> {
		
		final Activity context;

		public SoldProductListArrayAdapter(Activity context) {

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
			serialSetupButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

					LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					final View view = layoutInflater.inflate(R.layout.dialog_box_serial_setup, null);

					((Button) view.findViewById(R.id.add)).setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {

							LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							final View view = layoutInflater.inflate(R.layout.dialog_box_serial_add, null);

							final AlertDialog alertDialog = new AlertDialog.Builder(context)
								.setView(view)
								.setTitle("Add Serial")
								.setPositiveButton("OK", null)
								.setNegativeButton("Cancel", null)
								.setCancelable(false)
								.create();
							alertDialog.setOnShowListener(new OnShowListener() {
								
								@Override
								public void onShow(DialogInterface arg0) {

									alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
										
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
											} else if (fromEditText == null || fromEditText.length() == 0) {
												
												message.setText("You must provide 'From'");
												fromEditText.requestFocus();

												return;
											} else if (toEditText == null || toEditText.length() == 0) {
												
												message.setText("You must provide 'To'");
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
												jsonObject.put("fromSerialNo", fromEditText.getText().toString());
												jsonObject.put("toSerialNo", toEditText.getText().toString());
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
					alertDialog.setOnShowListener(new OnShowListener() {
						
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
									alertDialog.setOnShowListener(new OnShowListener() {
										
										@Override
										public void onShow(DialogInterface arg0) {

											final EditText quantityEditText = (EditText) editView.findViewById(R.id.quantity);
											final EditText fromEditText = (EditText) editView.findViewById(R.id.from);
											final EditText toEditText = (EditText) editView.findViewById(R.id.to);
											final TextView message = (TextView) editView.findViewById(R.id.message);

											try {

												JSONObject serialJsonObject = new JSONObject(soldProduct.getSerialList().get(position));
                                                String fromSerialNumber = serialJsonObject.getString("fromSerialNo");
                                                String toSerialNumber = serialJsonObject.getString("toSerialNo");

                                                //quantityEditText.setText(toSerialNumber - fromSerialNumber + 1 + "");
                                                //quantityEditText.setText(Integer.parseInt(toSerialNumber) - Integer.parseInt(fromSerialNumber) + 1);
                                                quantityEditText.setText(serialJsonObject.getString("serialQty"));
												fromEditText.setText(fromSerialNumber + "");
												toEditText.setText(toSerialNumber + "");
											} catch (JSONException e1) {

												e1.printStackTrace();
											}

											alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
												
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
			
			double discountPercent = soldProduct.getDiscount(context);
			discountTextView.setText(soldProduct.getDiscount(context) + soldProduct.getExtraDiscount() + "%");
			
//			Double totalAmount = soldProduct.getProduct().getPrice() * soldProduct.getQuantity();
//			Double discount = totalAmount * discountPercent / 100;
			totalAmountTextView.setText(Utils.formatAmount(soldProduct.getNetAmount(context)));

			if (!soldProduct.isForPackage()) {
				
				nameTextView.setTextColor(getResources().getColor(R.color.blue));
			}

			return view;
		}

		@Override
		public void notifyDataSetChanged() {

			super.notifyDataSetChanged();

//			Double totalAmount = 0.0;
//			for (SoldProduct soldProduct : soldProductList) {
//
//				Double amount = soldProduct.getProduct().getPrice() * soldProduct.getQuantity();
//				totalAmount += amount - (amount * soldProduct.getDiscount(context) / 100);
//			}
//
//			totalAmountTextView.setText(totalAmount + "");
//
//			netAmountTextView.setText(totalAmount - (totalAmount * extraDiscount / 100) + "");
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

			((Button) view.findViewById(R.id.delete)).setOnClickListener(new OnClickListener() {
				
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
