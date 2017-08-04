package com.aceplus.myanmar_padauk.general_sale;

import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aceplus.myanmar_padauk.CustomerActivity;
import com.aceplus.myanmar_padauk.R;
import com.aceplus.myanmar_padauk.models.Category;
import com.aceplus.myanmar_padauk.models.Customer;
import com.aceplus.myanmar_padauk.models.Product;
import com.aceplus.myanmar_padauk.models.SoldProduct;
import com.aceplus.myanmar_padauk.utils.Database;
import com.aceplus.myanmar_padauk.utils.Utils;

public class GeneralSaleActivity extends Activity {

	// For pre order
	public static final String IS_PRE_ORDER = "is-pre-order";
	// For delivery
	public static final String IS_DELIVERY = "is-delivery";
	public static final String REMAINING_AMOUNT_KEY = "remaining-amount-key";

	public static final String USER_INFO_KEY = "user-info-key";
	public static final String CUSTOMER_INFO_KEY = "customer-info-key";
	public static final String SOLD_PROUDCT_LIST_KEY = "sold-product-list-key";
	public static final String ORDERED_INVOICE_KEY = "ordered_invoice_key";

	private boolean isPreOrder;

	private boolean isDelivery;
	private double remainingAmount;

	private JSONObject salemanInfo;
	private Customer customer;
	private ArrayList<SoldProduct> soldProductList = new ArrayList<SoldProduct>(20);
	private JSONObject orderedInvoice;

	private TextView titleTextView;
	private Button cancelButton, checkoutButton;

	private AutoCompleteTextView searchProductTextView;
	private Button previousCategoryButton, nextCategoryButton;
	private TextView categoryTextView;
	private ListView productsInGivenCategoryListView;
	
	private TextView saleDateTextView;
	
	private ListView soldProductListView;
	
	private Category[] categories;
	private int currentCategoryIndex;

	private SoldProductListRowAdapter soldProductListRowAdapter;

	String[] productNames = null;

    SQLiteDatabase database;

    @SuppressWarnings("unchecked")
	@Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_sale);

        this.isPreOrder = getIntent().getBooleanExtra(GeneralSaleActivity.IS_PRE_ORDER, false);

        this.isDelivery = getIntent().getBooleanExtra(GeneralSaleActivity.IS_DELIVERY, false);
        this.remainingAmount = getIntent().getDoubleExtra(GeneralSaleActivity.REMAINING_AMOUNT_KEY, 0);

        try {

			salemanInfo = new JSONObject(getIntent().getStringExtra(USER_INFO_KEY));
			if (getIntent().getStringExtra(GeneralSaleActivity.ORDERED_INVOICE_KEY) != null) {

				orderedInvoice = new JSONObject(
						getIntent().getStringExtra(GeneralSaleActivity.ORDERED_INVOICE_KEY));
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}
        customer = (Customer) getIntent().getSerializableExtra(CUSTOMER_INFO_KEY);
        if ((ArrayList<SoldProduct>) getIntent().getSerializableExtra(SOLD_PROUDCT_LIST_KEY) != null) {

        	soldProductList = (ArrayList<SoldProduct>) getIntent().getSerializableExtra(SOLD_PROUDCT_LIST_KEY);
        }

        // Hide keyboard on startup.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        database = new Database(this).getDataBase();

        database.beginTransaction();
        try{

            database.execSQL("CREATE TABLE IF NOT EXISTS TEMP_SOLD_PRODUCT_LIST("
                    + "PRODUCT_ID text,"
                    + "PRODUCT_NAME text);");
            database.setTransactionSuccessful();
            Log.e("Create table Temp Sold Product ", "is ok");

        }catch(SQLiteException e1)
        {
            e1.printStackTrace();
        }finally{
            database.endTransaction();
        }

        titleTextView = (TextView) findViewById(R.id.title);
        cancelButton = (Button) findViewById(R.id.cancel);
        checkoutButton = (Button) findViewById(R.id.checkout);

        searchProductTextView = (AutoCompleteTextView) findViewById(R.id.searchAutoCompleteTextView);
        previousCategoryButton = (Button) findViewById(R.id.previusCategoryButton);
        categoryTextView = (TextView) findViewById(R.id.categoryTextView);
        nextCategoryButton = (Button) findViewById(R.id.nextCategoryButton);
        productsInGivenCategoryListView = (ListView) findViewById(R.id.productsListView);
        saleDateTextView = (TextView) findViewById(R.id.saleDateTextView);
        soldProductListView = (ListView) findViewById(R.id.soldProductList);

        findViewById(R.id.searchAndSelectProductsLayout).setVisibility(
        		this.isDelivery ? View.GONE : View.VISIBLE);
        findViewById(R.id.tableHeaderOrderedQty).setVisibility(
        		this.isDelivery ? View.VISIBLE : View.GONE);
        findViewById(R.id.tableHeaderDiscount).setVisibility(
        		this.isPreOrder ? View.GONE : View.VISIBLE);
        if (this.isPreOrder) {

        	titleTextView.setText("Pre-Order");
        } else if (this.isDelivery) {

        	titleTextView.setText("Delivery");
        } else {

        	titleTextView.setText("General Sale");
        }
        searchProductTextView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {

				sellProduct(null, parent.getItemAtPosition(position).toString());
				searchProductTextView.setText("");
			}
		});

        previousCategoryButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {

				if (categories.length > 0) {

					if (currentCategoryIndex == 0) {
						
						currentCategoryIndex = categories.length - 1;
					} else {
						
						currentCategoryIndex--;
					}
					
					categoryTextView.setText(categories[currentCategoryIndex].getName());
					setProductListView(categories[currentCategoryIndex].getName());
				}
			}
		});

        nextCategoryButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {

				if (categories.length > 0) {

					if (currentCategoryIndex == categories.length - 1) {
						
						currentCategoryIndex = 0;
					} else {
						
						currentCategoryIndex++;
					}
					
					categoryTextView.setText(categories[currentCategoryIndex].getName());
					setProductListView(categories[currentCategoryIndex].getName());
				}
			}
		});

        productsInGivenCategoryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {

				sellProduct(categoryTextView.getText().toString(), parent.getItemAtPosition(position).toString());
			}
		});

        saleDateTextView.setText(Utils.getCurrentDate(false));

        if (this.isPreOrder) {

        	soldProductListRowAdapter = new SoldProductListRowAdapter(this, R.layout.list_row_sold_product);
        } else {

        	soldProductListRowAdapter = new SoldProductListRowAdapter(this, R.layout.list_row_sold_product_with_custom_discount);
        }

        soldProductListView.setAdapter(soldProductListRowAdapter);
        soldProductListRowAdapter.notifyDataSetChanged();
        soldProductListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {

				new AlertDialog.Builder(GeneralSaleActivity.this)
					.setTitle("Delete sold product")
					.setMessage("Are you sure you want to delete "
							+ soldProductList.get(position).getProduct().getName() + "?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

                            database.beginTransaction();
                            database.execSQL("DELETE FROM TEMP_SOLD_PRODUCT_LIST WHERE PRODUCT_ID='"+soldProductList.get(position).getProduct().getId()+"'");
                            database.setTransactionSuccessful();
                            database.endTransaction();

							soldProductList.remove(position);
							soldProductListRowAdapter.notifyDataSetChanged();

						}
					})
					.setNegativeButton("No", null)
					.show();

				return true;
			}
		});

        cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				GeneralSaleActivity.this.onBackPressed();
			}
		});

        checkoutButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {

				if (soldProductList.size() == 0) {
					
					new AlertDialog.Builder(GeneralSaleActivity.this)
						.setTitle("Alert")
						.setMessage("You must specify at least one product.")		
						.setPositiveButton("OK", null)
						.show();

					return;
				}
				
				for (SoldProduct soldProduct : soldProductList) {
					
					if (soldProduct.getQuantity() == 0) {
						
						new AlertDialog.Builder(GeneralSaleActivity.this)
							.setTitle("Alert")
							.setMessage("Quantity must not be zero.")		
							.setPositiveButton("OK", null)
							.show();

						return;
					}
				}

				Intent intent = new Intent(GeneralSaleActivity.this
						, GeneralSaleCheckoutActivity.class);
				intent.putExtra(GeneralSaleCheckoutActivity.IS_PRE_ORDER
						, GeneralSaleActivity.this.isPreOrder);
				intent.putExtra(GeneralSaleCheckoutActivity.IS_DELIVERY
						, GeneralSaleActivity.this.isDelivery);
				intent.putExtra(GeneralSaleCheckoutActivity.REMAINING_AMOUNT_KEY
						, GeneralSaleActivity.this.remainingAmount);
				intent.putExtra(GeneralSaleCheckoutActivity.USER_INFO_KEY
						, GeneralSaleActivity.this.salemanInfo.toString());
				intent.putExtra(GeneralSaleCheckoutActivity.CUSTOMER_INFO_KEY
						, GeneralSaleActivity.this.customer);
				intent.putExtra(GeneralSaleCheckoutActivity.SOLD_PROUDCT_LIST_KEY
						, GeneralSaleActivity.this.soldProductList);
				if (GeneralSaleActivity.this.orderedInvoice != null) {

					intent.putExtra(GeneralSaleCheckoutActivity.ORDERED_INVOICE_KEY
							, GeneralSaleActivity.this.orderedInvoice.toString());
				}
				startActivity(intent);
				finish();
			}
		});
        
        initCategories();
        
        if (categories.length > 0) {
        	
        	categoryTextView.setText(categories[0].getName());
        	currentCategoryIndex = 0;

        	setProductListView(categories[0].getName());
        	
        	ArrayList<String> products = new ArrayList<String>();
            for (Category category : categories) {
            	
            	for (Product product : category.getProducts()) {
            		
            		products.add(product.getName());
            	}
            }
            searchProductTextView.setAdapter(new ArrayAdapter<String>(
            		this, android.R.layout.simple_list_item_1, products));
            searchProductTextView.setThreshold(1);
        } else {

        	categoryTextView.setText("No product");
        }
    }

    @Override
    public void onBackPressed() {

    	if (this.isDelivery) {

    		super.onBackPressed();
    		return;
    	}

        deleteTempTable();

    	Intent intent = new Intent(this, CustomerActivity.class);
    	intent.putExtra(CustomerActivity.USER_INFO_KEY, salemanInfo.toString());
    	startActivity(intent);
    	finish();
    }

    private void deleteTempTable() {
        database.beginTransaction();
        //database.rawQuery("DELETE FROM TEMP_SOLD_PRODUCT_LIST", null);
        database.execSQL("DROP TABLE IF EXISTS TEMP_SOLD_PRODUCT_LIST");
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    private void initCategories() {
    	
    	if (categories == null) {

    		SQLiteDatabase db = (new Database(this)).getDataBase();
    		
    		Cursor cursor = db.rawQuery(
    				"SELECT CATEGORY_ID, CATEGORY_NAME"
    				+ " FROM PRODUCT"
    				+ " GROUP BY CATEGORY_ID, CATEGORY_NAME", null);
    		
    		categories = new Category[cursor.getCount()];
    		while (cursor.moveToNext()) {
    			
    			categories[cursor.getPosition()] = new Category(cursor.getString(cursor.getColumnIndex("CATEGORY_ID")), cursor.getString(cursor.getColumnIndex("CATEGORY_NAME")));
    			categories[cursor.getPosition()].setProducts(getProducts(categories[cursor.getPosition()].getId()));
    		}
    	}
    }
    
    private Product[] getProducts(String categoryId) {

    	Product[] products;
    	
    	SQLiteDatabase db = (new Database(this)).getDataBase();
		
		Cursor cursor = db.rawQuery(
				"SELECT PRODUCT_ID, PRODUCT_NAME, SELLING_PRICE"
				+ ", PURCHASE_PRICE, DISCOUNT_TYPE, REMAINING_QTY"
				+ " FROM PRODUCT WHERE CATEGORY_ID = '" + categoryId + "'", null);
		
		products = new Product[cursor.getCount()];
		while (cursor.moveToNext()) {
			
			Product tempProduct = new Product(
					cursor.getString(cursor.getColumnIndex("PRODUCT_ID"))
					, cursor.getString(cursor.getColumnIndex("PRODUCT_NAME"))
					, cursor.getDouble(cursor.getColumnIndex("SELLING_PRICE"))
					, cursor.getDouble(cursor.getColumnIndex("PURCHASE_PRICE"))
					, cursor.getString(cursor.getColumnIndex("DISCOUNT_TYPE"))
					, cursor.getInt(cursor.getColumnIndex("REMAINING_QTY")));

			products[cursor.getPosition()] = tempProduct;
		}
    	
    	return products;
    }

    private void setProductListView(String categoryName) {
    	
    	if (categories.length > 0) {
    		
    		Category tempCategory = null;

    		for (Category category : categories) {
    			
    			if (category.getName().equals(categoryName)) {
    				
    				tempCategory = category;
    				break;
    			}
    		}
    		
    		//String[] productNames = null;
    		if (tempCategory != null) {
    			
    			productNames = new String[tempCategory.getProducts().length];
    			for (int i = 0; i < productNames.length; i++) {
    				
    				productNames[i] = tempCategory.getProducts()[i].getName();
    			}
    		}
    		
    		if (productNames != null) {
    			
    			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.custom_simple_list_item_1, android.R.id.text1, productNames);
    			productsInGivenCategoryListView.setAdapter(arrayAdapter);
    		}
    	}
    }

    private void sellProduct(String categoryName, String productName) {
    	
    	if (categories != null) {

        	Product tempProduct = null;

    		if (categoryName != null && categoryName.length() > 0) {
        		
        		for (Category category : categories) {
        			
        			if (category.getName().equals(categoryName)) {

        				for (Product product : category.getProducts()) {
        					
        					if (product.getName().equals(productName)) {
        						
        						tempProduct = product;
        					}
        				}
        			}
        		}
        	} else if (productName != null && productName.length() > 0) {
        		
        		for (Category category : categories) {
        			
        			for (Product product : category.getProducts()) {
        				
        				if (product.getName().equals(productName)) {
        					
        					tempProduct = product;
        				}
        			}
        		}
        	}
    		
    		/*if (tempProduct != null) {

    			soldProductList.add(new SoldProduct(tempProduct, false));
    			soldProductListRowAdapter.notifyDataSetChanged();
    		}*/

            String pr_Id = null;
            if (tempProduct != null) {
                if (soldProductList.size() == 0) {
                    soldProductList.add(new SoldProduct(tempProduct, false));
                    soldProductListRowAdapter.notifyDataSetChanged();

                    database.beginTransaction();
                    database.execSQL("INSERT INTO TEMP_SOLD_PRODUCT_LIST VALUES(\""
                            + tempProduct.getId() + "\", \""
                            + tempProduct.getName() + "\""
                            + ")");
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    Cursor cursor = database.rawQuery("SELECT * FROM TEMP_SOLD_PRODUCT_LIST", null);
                    Log.e("Count in temp>>>", cursor.getCount() + "");
                }
                else {
                    Cursor cursor = database.rawQuery("SELECT * FROM TEMP_SOLD_PRODUCT_LIST WHERE PRODUCT_ID='"+tempProduct.getId()+"'", null);
                    if (cursor.getCount() == 1) {
                        //Toast.makeText(getApplicationContext(), "Duplicate Prodcut!!!", Toast.LENGTH_SHORT).show();
						new AlertDialog.Builder(GeneralSaleActivity.this)
								.setTitle("Alert")
								.setMessage("Product can't be duplicate.")
								.setPositiveButton("OK", null)
								.show();
                    }
                    else {
                        soldProductList.add(new SoldProduct(tempProduct, false));
                        soldProductListRowAdapter.notifyDataSetChanged();

                        database.beginTransaction();
                        database.execSQL("INSERT INTO TEMP_SOLD_PRODUCT_LIST VALUES(\""
                                + tempProduct.getId() + "\", \""
                                + tempProduct.getName() + "\""
                                + ")");
                        database.setTransactionSuccessful();
                        database.endTransaction();

                        Cursor cursor1 = database.rawQuery("SELECT * FROM TEMP_SOLD_PRODUCT_LIST", null);
                        Log.e("Count in temp>>>", cursor1.getCount() + "");
                    }
                }

            }
    	}
    }

    private class SoldProductListRowAdapter extends ArrayAdapter<SoldProduct> {

    	public final Activity context;
    	public final int resource;

		public SoldProductListRowAdapter(Activity context, int resource) {

			super(context, resource, soldProductList);
			this.context = context;
			this.resource = resource;
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final SoldProduct soldProduct = soldProductList.get(position);

			LayoutInflater layoutInflater = context.getLayoutInflater();
			View view= layoutInflater.inflate(this.resource, null, true);

			final TextView nameTextView = (TextView) view.findViewById(R.id.name);
			final Button qtyButton = (Button) view.findViewById(R.id.qty);

			qtyButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					final View view = layoutInflater.inflate(R.layout.dialog_box_sale_quantity, null);

					final TextView remainingQtyTextView = (TextView) view.findViewById(R.id.availableQuantity);
					final EditText quantityEditText = (EditText) view.findViewById(R.id.quantity);
					final TextView messageTextView = (TextView) view.findViewById(R.id.message);
					
					final AlertDialog alertDialog = new AlertDialog.Builder(context)
						.setView(view)
						.setTitle("Sale Quantity")
						.setPositiveButton("Confirm", null)
						.setNegativeButton("Cancel", null)
						.create();
					alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
						
						@Override
						public void onShow(DialogInterface arg0) {

							if (GeneralSaleActivity.this.isPreOrder) {
								
								view.findViewById(R.id.availableQuantityLayout).setVisibility(View.GONE);
							} else {
								
								remainingQtyTextView.setText(soldProduct.getProduct().getRemainingQty() + "");
							}
							
							Button confirmButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
							confirmButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View arg0) {

									if (quantityEditText.getText().toString().length() == 0) {
										
										messageTextView.setText("You must specify quantity.");
										return;
									}

									int quantity = Integer.parseInt(quantityEditText.getText().toString());
//									if (quantity > soldProduct.getProduct().getRemainingQty() + soldProduct.getQuantity()) {
//										
//										((TextView) view.findViewById(R.id.saleQuantityDialogueBox_message))
//											.setText("Your requested quantity is more than remaining.");
//										return;
//									}

									if (GeneralSaleActivity.this.isDelivery
											&& quantity > soldProduct.getOrderedQuantity()) {

										messageTextView.setText("Quantity must be no more than ordered quantity.");
										quantityEditText.selectAll();
										return;
									}

									soldProduct.setQuantity(quantity);
									soldProductListRowAdapter.notifyDataSetChanged();

									alertDialog.dismiss();
								}
							});
//							
//							Button cancelButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//							cancelButton.setOnClickListener(new OnClickListener()
//							{
//								
//								@Override
//								public void onClick(View arg0) 
//								{
//									alertDialog.dismiss();
//								}
//							});
						}
					});
					
					alertDialog.show();
				}
			});

			final TextView priceTextView = (TextView) view.findViewById(R.id.price);
			final TextView discountButtonOrTextView = (TextView) view.findViewById(R.id.discount);
			final TextView totalAmountTextView = (TextView) view.findViewById(R.id.amount);

			nameTextView.setText(soldProduct.getProduct().getName());

			if (this.resource == R.layout.list_row_sold_product_with_custom_discount) {

				TextView orderedQuantityTextView = (TextView) view.findViewById(R.id.orderedQuantity);
				orderedQuantityTextView.setVisibility(
						GeneralSaleActivity.this.isDelivery ? View.VISIBLE : View.GONE);
				if (GeneralSaleActivity.this.isDelivery) {

					orderedQuantityTextView.setText(soldProduct.getOrderedQuantity() + "");
				}
			}

			qtyButton.setText(soldProduct.getQuantity() + "");
			priceTextView.setText(Utils.formatAmount(soldProduct.getProduct().getPrice()));

			Double totalAmount = soldProduct.getProduct().getPrice() * soldProduct.getQuantity();
			if (GeneralSaleActivity.this.isPreOrder) {

				discountButtonOrTextView.setVisibility(View.GONE);
				totalAmountTextView.setText(Utils.formatAmount(totalAmount));
			} else {

				double discountPercent = soldProduct.getDiscount(context) + soldProduct.getExtraDiscount();
				Log.e("Dis%>>>>>", String.valueOf(discountPercent));
				discountButtonOrTextView.setText(discountPercent + "%");
				discountButtonOrTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						final View view = layoutInflater.inflate(R.layout.dialog_box_custom_discount, null);

						final TextView discountTextView = (TextView) view.findViewById(R.id.discount);
						final TextView extradiscountTextView = (TextView) view.findViewById(R.id.extraDiscount);
						new AlertDialog.Builder(context)
							.setView(view)
							.setTitle("Discount")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {

									double discount = 0;

                                    //tested by HAK(start)
									double discountAmt = 0.0;

									if (discountTextView.getText().length() > 0) {

										discount = Double.parseDouble(discountTextView.getText().toString());
									}

									double extraDiscount = 0;
									if (extradiscountTextView.getText().length() > 0) {

										extraDiscount = Double.parseDouble(extradiscountTextView.getText().toString());
									}

									soldProduct.setDiscount(discount);
									soldProduct.setExtraDiscount(extraDiscount);

                                    discountAmt = Math.floor((soldProduct.getProduct().getPrice() * (soldProduct.getDiscount(GeneralSaleActivity.this) + soldProduct.getExtraDiscount()) / 100));
                                    Log.i("DiscountAmt is>>>>>", discountAmt + "");

									soldProductListRowAdapter.notifyDataSetChanged();
								}
							})
							.setNegativeButton("Cancel", null)
							.create()
							.show();
					}
				});

//				Double discount = totalAmount * discountPercent / 100;
				totalAmountTextView.setText(
						Utils.formatAmount(
								totalAmount
									- (soldProduct.getDiscountAmount(this.context)
											+ soldProduct.getExtraDiscountAmount())));
			}

			return view;
		}
    
		@Override
		public void notifyDataSetChanged() {

			super.notifyDataSetChanged();
			
			Double netAmount = 0.0;
			for (SoldProduct soldProduct : soldProductList) {
				netAmount += soldProduct.getNetAmount(GeneralSaleActivity.this);
//				Double totalAmount = soldProduct.getProduct().getPrice() * soldProduct.getQuantity();
//				Double discount = totalAmount * soldProduct.getDiscount(context) / 100;
//
//				netAmount += totalAmount - discount;
			}
			
			((TextView) context.findViewById(R.id.netAmountTextView)).setText(Utils.formatAmount(netAmount));
		}
    }
}
