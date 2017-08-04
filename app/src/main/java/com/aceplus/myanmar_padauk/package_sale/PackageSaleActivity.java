package com.aceplus.myanmar_padauk.package_sale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.aceplus.myanmar_padauk.CustomerActivity;
import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.general_sale.GeneralSaleActivity;
import com.aceplus.myanmar_padauk.models.Customer;
import com.aceplus.myanmar_padauk.models.Package;
import com.aceplus.myanmar_padauk.models.Product;
import com.aceplus.myanmar_padauk.models.SoldProduct;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PackageSaleActivity extends Activity {

	public static final String USER_INFO_KEY = "user-info-key";
	public static final String CUSTOMER_INFO_KEY = "customer-info-key";

	JSONObject salemanInfo;
	Customer customer;

	Button checkoutButton, cancelButton;

	Spinner packagesSpinner;
	EditText amountEditText;
	TextView saleDateTextView;
	ListView soldProuductListView;
	TextView netAmountTextView;

	SQLiteDatabase database;

	double userProvidedAmount;

	ArrayList<Package> packageList;
	ArrayAdapter<String> packagesAdapter;

	private ArrayList<SoldProduct> soldProductList = new ArrayList<SoldProduct>();

	SoldProductListRowAdapter soldProductListRowAdapter;
	
	int currentCategoryIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_sale);

        // Hide keyboard at startup
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        checkoutButton = (Button) findViewById(R.id.checkout);
        cancelButton = (Button) findViewById(R.id.cancel);

        packagesSpinner = (Spinner) findViewById(R.id.packages);
        amountEditText = (EditText) findViewById(R.id.amount);
        saleDateTextView = (TextView) findViewById(R.id.saleDate);
        saleDateTextView.setText(Utils.getCurrentDate(false));
        soldProuductListView = (ListView) findViewById(R.id.soldProductList);
        netAmountTextView = (TextView) findViewById(R.id.netAmount);

        soldProductListRowAdapter = new SoldProductListRowAdapter(this);

        try {

			salemanInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));
		} catch (JSONException e) {

			e.printStackTrace();
		}
        customer = (Customer) getIntent().getSerializableExtra(CUSTOMER_INFO_KEY);

        database = new Database(this).getDataBase();

        cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				PackageSaleActivity.this.onBackPressed();
			}
		});

        checkoutButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(PackageSaleActivity.this)
					.setTitle("No sold product.")
					.setPositiveButton("OK", null)
					.setIcon(android.R.drawable.ic_dialog_alert);

				if (soldProductList.size() == 0) {

					alertDialog.setMessage("At least one sold product is required.");
					alertDialog.show();
				} else {

					Intent intent = new Intent(PackageSaleActivity.this, PackageSaleCheckoutActivity.class);
					intent.putExtra(PackageSaleCheckoutActivity.USER_INFO_KEY, salemanInfo.toString());
					intent.putExtra(PackageSaleCheckoutActivity.CUSTOMER_INFO_KEY, customer);
					intent.putExtra(PackageSaleCheckoutActivity.SOLD_PROUDCT_LIST_KEY, soldProductList);
					intent.putExtra(PackageSaleCheckoutActivity.SOLD_PACKAGE_KEY
							, packageList.get(packagesSpinner.getSelectedItemPosition()));
					intent.putExtra(PackageSaleCheckoutActivity.USER_PROVIDED_AMOUNT_KEY, userProvidedAmount);
					startActivity(intent);
					finish();
				}
			}
		});

        packageList = new ArrayList<Package>();

        final ArrayList<String> grades = new ArrayList<String>();
        packagesAdapter = new ArrayAdapter<String>(PackageSaleActivity.this, android.R.layout.simple_spinner_item, grades);
        packagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        packagesSpinner.setAdapter(packagesAdapter);

        amountEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

				if (charSequence.toString().length() > 0) {

					String convertedString = charSequence.toString();
					convertedString = Utils.formatAmount(Double.parseDouble(charSequence.toString().replace(",", "")));
					if (!amountEditText.getText().toString().equals(convertedString)
							&& convertedString.length() > 0) {

						amountEditText.setText(convertedString);
						amountEditText.setSelection(amountEditText.getText().length());
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {}
			
			@Override
			public void afterTextChanged(Editable arg0) {}
		});

        ((Button) findViewById(R.id.findGrade)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (amountEditText.getText().length() > 0) {

					Double amount = Double.parseDouble(amountEditText.getText().toString().replace(",", ""));
					userProvidedAmount = amount;
					String sql = "SELECT INV_NO, GRADE, ITEMS FROM PACKAGE"
							+ " WHERE MIN_AMOUNT <= " + amount + " AND MAX_AMOUNT >= " + amount
							+ " ORDER BY INV_NO";
					Cursor cursor = database.rawQuery(sql, null);
					if (cursor.getCount() > 0) {

						packageList.clear();
					}
					while (cursor.moveToNext()) {

						try {

							for (int i = 0; i < packageList.size(); i++) {

								if (cursor.getString(cursor.getColumnIndex("GRADE"))
										.equalsIgnoreCase(packageList.get(i).getGrade())) {

									packageList.remove(i);
									break;
								}
							}
	
							packageList.add(new Package(cursor.getString(cursor.getColumnIndex("INV_NO"))
										, cursor.getString(cursor.getColumnIndex("GRADE"))
										, new JSONArray(cursor.getString(cursor.getColumnIndex("ITEMS")))));
						} catch (JSONException e) {

							e.printStackTrace();
						}
					}

					// Setup package spinner;
					if (packageList.size() > 0) {

						packagesSpinner.setVisibility(View.VISIBLE);
					} else {

						packagesSpinner.setVisibility(View.INVISIBLE);
					}
					grades.clear();
			        for (Package pkg : packageList) {
			        	
			        	grades.add("Grade " + pkg.getGrade());
			        }
			        packagesAdapter.notifyDataSetChanged();

			        if (grades.size() > 0) {

			        	changeSoldProductListView(0);
			        }
				}
			}
		});

		// Setup adapter for soldProductListView
		soldProuductListView.setAdapter(soldProductListRowAdapter);

		// Setup onItemSelectedListener on packagesSpinner 
		packagesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				if (position < packageList.size()) {

					changeSoldProductListView(position);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
    }

    @Override
    public void onBackPressed() {

    	Intent intent = new Intent(this, CustomerActivity.class);
    	intent.putExtra(CustomerActivity.USER_INFO_KEY, salemanInfo.toString());
    	startActivity(intent);
    	finish();
    }

    private void changeSoldProductListView(int position) {

    	soldProductList.clear();
		for (int i = 0; i < packageList.get(position).getItems().size(); i++) {
			
			try {

				JSONObject item = new JSONObject(packageList.get(position).getItems().get(i));
				Cursor cursor = database.rawQuery("SELECT * from PRODUCT where PRODUCT_ID = \"" + item.getString("sNo") + "\"", null);
				if (cursor.getCount() == 0) {

					soldProductList.clear();
					soldProductListRowAdapter.notifyDataSetChanged();

					new AlertDialog.Builder(this)
						.setTitle("Insufficient product")
						.setMessage("Product is insufficient")
						.setPositiveButton("OK", null)
						.show();

					return;
				}

				if (cursor.getCount() == 1) {
					
					cursor.moveToNext();

					Cursor cursorForRemainingQuantity = database.rawQuery("SELECT PRODUCT_NAME, REMAINING_QTY FROM PRODUCT WHERE PRODUCT_ID = \""
							+ cursor.getString(cursor.getColumnIndex("PRODUCT_ID")) + "\""
							, null);
					if (cursorForRemainingQuantity.moveToNext()) {

						int quantity = (int) Math.ceil((Double.parseDouble(amountEditText.getText().toString().replace(",", ""))
								* item.getInt("percentage") / 100) / item.getDouble("sPrice"));
//						if (quantity > cursorForRemainingQuantity.getInt(cursorForRemainingQuantity.getColumnIndex("REMAINING_QTY"))) {
//
//							soldProductList.clear();
//							soldProductListRowAdapter.notifyDataSetChanged();
//
//							packagesSpinner.setVisibility(View.INVISIBLE);
//
//							new AlertDialog.Builder(this)
//								.setTitle("Insufficient quanity")
//								.setMessage("Quantity for "
//										+ cursorForRemainingQuantity.getString(cursorForRemainingQuantity.getColumnIndex("PRODUCT_NAME"))
//										+ " is insufficient.")
//								.setPositiveButton("OK", null)
//								.show();
//
//							return;
//						}
					} else {

						soldProductList.clear();
						new AlertDialog.Builder(this)
							.setTitle("Product unavailable")
							.setMessage("Product "
									+ cursorForRemainingQuantity.getString(cursorForRemainingQuantity.getColumnIndex("PRODUCT_NAME"))
									+ " is unavailable.")
							.setPositiveButton("OK", null)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();

						return;
					}

					Product product = new Product(cursor.getString(cursor.getColumnIndex("PRODUCT_ID"))
							, cursor.getString(cursor.getColumnIndex("PRODUCT_NAME"))
							, cursor.getDouble(cursor.getColumnIndex("SELLING_PRICE"))
							, cursor.getDouble(cursor.getColumnIndex("PURCHASE_PRICE"))
							, cursor.getString(cursor.getColumnIndex("DISCOUNT_TYPE"))
							, cursor.getInt(cursor.getColumnIndex("REMAINING_QTY")));
					SoldProduct soldProduct = null;
					if (item.getString("extra").equalsIgnoreCase("n")) {
						
						soldProduct = new SoldProduct(product, true);
					} else {
						
						soldProduct = new SoldProduct(product, false);
					}

					if (item.getInt("quantity") > 0) {

						soldProduct.setQuantity(item.getInt("quantity"));
					} else {
						
						soldProduct.setQuantity(
								(int) Math.ceil((Double.parseDouble(
										amountEditText.getText().toString().replace(",", ""))
											* item.getInt("percentage") / 100) / item.getDouble("sPrice")));
					}

					soldProduct.setDiscount(item.getDouble("discountPercent"));
					soldProduct.setExtraDiscount(item.getDouble("extraPercent"));
					soldProductList.add(soldProduct);
					
				}
				soldProductListRowAdapter.notifyDataSetChanged();
			} catch (JSONException e) {

				e.printStackTrace();
			}
		}
    }

    private class SoldProductListRowAdapter extends ArrayAdapter<SoldProduct> {

    	public final Activity context;

		public SoldProductListRowAdapter(Activity context) {

			super(context, R.layout.list_row_sold_product, soldProductList);
			this.context = context;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final SoldProduct soldProduct = soldProductList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(R.layout.list_row_sold_product, null, true);

			final TextView nameTextView = (TextView) view.findViewById(R.id.name);
			final Button qtyButton = (Button) view.findViewById(R.id.qty);
			final TextView priceTextView = (TextView) view.findViewById(R.id.price);
			final TextView discountTextView = (TextView) view.findViewById(R.id.discount);
			final TextView totalAmountTextView = (TextView) view.findViewById(R.id.amount);

			nameTextView.setText(soldProduct.getProduct().getName());
			qtyButton.setText(soldProduct.getQuantity() + "");
			qtyButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					new AlertDialog.Builder(PackageSaleActivity.this)
						.setTitle("Switch to general sale.")
						.setMessage("If you want to change quantity, package sale will switch to general sale.\n"
								+ "Are you sure you want to switch to general sale")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

//								ArrayList<SoldProduct> soldProductList = new ArrayList<SoldProduct>();
								for (SoldProduct soldProduct : PackageSaleActivity.this.soldProductList) {

									soldProduct.setDiscount(0);
									soldProduct.setExtraDiscount(0);
									soldProduct.setForPackage(false);
//									SoldProduct newSoldProduct = new SoldProduct(soldProduct.getProduct(), false);
//									
//									newSoldProduct.setQuantity(soldProduct.getQuantity());
//									soldProductList.add(newSoldProduct);
								}

								Intent intent = new Intent(PackageSaleActivity.this, GeneralSaleActivity.class);
								intent.putExtra(GeneralSaleActivity.USER_INFO_KEY, salemanInfo.toString());
								intent.putExtra(GeneralSaleActivity.CUSTOMER_INFO_KEY, customer);
								intent.putExtra(GeneralSaleActivity.SOLD_PROUDCT_LIST_KEY, soldProductList);
								startActivity(intent);
								finish();
							}
						})
						.setNegativeButton("Cancel", null)
						.show();
				}
			});
			priceTextView.setText(Utils.formatAmount(soldProduct.getProduct().getPrice()));

			discountTextView.setText(soldProduct.getDiscount(context) + soldProduct.getExtraDiscount() + "%");

			totalAmountTextView.setText(Utils.formatAmount(soldProduct.getNetAmount(context)));

			if (!soldProduct.isForPackage()) {
				
				nameTextView.setTextColor(getResources().getColor(R.color.blue));
			}

			return view;
		}

		@Override
		public void notifyDataSetChanged() {

			super.notifyDataSetChanged();

			Double netAmount = 0.0;
			for (SoldProduct soldProduct : soldProductList) {

				netAmount += soldProduct.getNetAmount(PackageSaleActivity.this);
			}

			netAmountTextView.setText(Utils.formatAmount(netAmount));
		}
    }
}
