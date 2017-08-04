package com.aceplus.myanmar_padauk.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.TelephonyManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.aceplus.myanmar_padauk.models.SoldProduct;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class Utils {

	private static SQLiteDatabase database;
	private static DecimalFormat decimalFormatterWithComma = new DecimalFormat("###,##0");
	public static final String PREF_FILE_NAME = "pref-file";

	public static final String forPackageSale = "for-package-sale";
	public static final String forPreOrderSale = "for-pre-order-sale";
	public static final String FOR_DELIVERY = "for-delivery";
	public static final String FOR_OTHERS = "for-others";

	public static final String PRINT_FOR_NORMAL_SALE = "print-for-normal-sale";
	public static final String PRINT_FOR_C = "print-for-c";
	public static final String PRINT_FOR_PRE_ORDER = "print-for-preorder";

	public static final String MODE_CUSTOMER_FEEDBACK = "mode_customer_feedback";
	public static final String MODE_GENERAL_SALE = "mode_general_sale";

	private static Formatter formatter;

	public static String getInvoiceID(Context context, String mode, String salemanID, String locationCode) {
		
		if (database == null) {
			
			database = new Database(context).getReadableDatabase();
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
		
		int idLength = 0;
		String prefix = locationCode + salemanID;
		long currentInvoiceNumber = 0;
		if (mode.equals(MODE_CUSTOMER_FEEDBACK)) {
			
			idLength = 20;
			prefix += new SimpleDateFormat("yyMMdd", Locale.ENGLISH).format(new Date());

//			currentInvoiceNumber += DatabaseUtils.queryNumEntries(database, "DID_CUSTOMER_FEEDBACK");
			currentInvoiceNumber += DatabaseUtils.queryNumEntries(database, "INVOICE");

			Date lastUploadedDate = Preferences.getCustomerFeedbackLastUploadedDate(context);
			if (lastUploadedDate != null) {

				try {
					Date todayDate = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
					if (todayDate.after(lastUploadedDate)) {

						Preferences.resetNumberOfCustomerFeedbackUploaded(context);
					}

					currentInvoiceNumber += Preferences.getCustomerFeedbackUploadedCount(context);
				} catch (ParseException e) {

					e.printStackTrace();
				}
			}
			currentInvoiceNumber++;
		} else if (mode.equals(MODE_GENERAL_SALE)) {
			
			idLength = 20;
			prefix += new SimpleDateFormat("yyMMdd", Locale.ENGLISH).format(new Date());

			currentInvoiceNumber += DatabaseUtils.queryNumEntries(database, "DID_CUSTOMER_FEEDBACK");

			Date lastUploadedDate = Preferences.getSaleLastUploadedDate(context);
			if (lastUploadedDate != null) {

				try {
					Date todayDate = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
					if (todayDate.after(lastUploadedDate)) {

						Preferences.resetNumberOfSaleUploaded(context);
					}

					currentInvoiceNumber += Preferences.getSaleUploadedCount(context);
				} catch (ParseException e) {

					e.printStackTrace();
				}
			}
			currentInvoiceNumber++;
		}

		return prefix + String.format("%0" + (idLength - prefix.length()) + "d", currentInvoiceNumber);
	}
	public static String getInvoiceNo(Context context, String salemanId, String locationCode, String mode) {

		if (database == null) {

			database = (new Database(context)).getDataBase();
		}

//		int idLength = 15;
//		if (mode.equals(Utils.forPreOrderSale)) {
//
//			idLength = 16;
//		}
		int idLength = 20;

		String invoiceNo = new String();
		if (mode.equals(Utils.forPackageSale)) {

			invoiceNo += "P";
		} else if (mode.equals(Utils.forPreOrderSale)) {

			invoiceNo += "SO";
		} else if (mode.equals(Utils.FOR_DELIVERY)) {

			invoiceNo += "OS";
		}
		invoiceNo += locationCode;
		invoiceNo += salemanId;
		invoiceNo += new SimpleDateFormat("yyMMdd").format(new Date());

		int next = 0;
		if (mode.equals(Utils.FOR_OTHERS) || mode.equals(Utils.forPackageSale)) {

			Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM DID_CUSTOMER_FEEDBACK", null);
			if (cursor.moveToNext()) {

				next += cursor.getInt(cursor.getColumnIndex("COUNT"));
			}

			cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM INVOICE", null);
			if (cursor.moveToNext()) {

				next += cursor.getInt(cursor.getColumnIndex("COUNT")) + 1;
			}
		} else if (mode.equals(Utils.forPreOrderSale)) {

			Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM PRE_ORDER", null);
			if (cursor.moveToNext()) {

				next += cursor.getInt(cursor.getColumnIndex("COUNT")) + 1;
			}
		} else if (mode.equals(Utils.FOR_DELIVERY)) {

//			next = 1;
			Cursor cursor = database.rawQuery("SELECT COUNT(*) AS COUNT FROM DELIVERED_DATA", null);
			if (cursor.moveToNext()) {

				next += cursor.getInt(cursor.getColumnIndex("COUNT")) + 1;
			}
		}

		return invoiceNo + String.format("%0" + (idLength - invoiceNo.length()) + "d", next);
	}

	public static String getSeparatorForDate() {

		return "/";
	}

	public static String formatAmount(Double amount) {

		return decimalFormatterWithComma.format(amount);
	}

	public static String getDeviceId(Activity activity) {

		return ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}

	public static String getCurrentDate(boolean withTime) {

		String dateFormat = "dd/MM/yyyy";
		if (withTime) {

			dateFormat += " hh:mm:ss";
		}

		return new SimpleDateFormat(dateFormat).format(new Date());
	}

	public static void print (final Activity activity, final String customerName, final String invoiceNumber
			, final String salePersonName, final double payAmount, final List<SoldProduct> soldProductList
			, final String printFor, final String mode) {

		List<PortInfo> portInfoList = null;

		try {

			portInfoList = StarIOPort.searchPrinter("BT:Star");
		} catch (StarIOPortException e) {

			e.printStackTrace();
		}

		if (portInfoList == null || portInfoList.size() == 0) {

			return;
		}

		List<String> availableBluetoothPrinterNameList = new ArrayList<String>();
		for (PortInfo portInfo : portInfoList) {

			availableBluetoothPrinterNameList.add(portInfo.getPortName());
		}
		final ArrayAdapter<String> arrayAdapter =
				new ArrayAdapter<String>(
						activity
						, android.R.layout.select_dialog_singlechoice
						, availableBluetoothPrinterNameList);
		new AlertDialog.Builder(activity)
			.setTitle("Select Printer")
			.setNegativeButton("Cancel", null)
			.setAdapter(arrayAdapter, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int position) {

					StarIOPort starIOPort = null;
					try {

						starIOPort = StarIOPort.getPort(arrayAdapter.getItem(position), "mini", 10000);
						if (starIOPort.retreiveStatus().offline) {

							if (!starIOPort.retreiveStatus().compulsionSwitch) {

								showToast(activity, "The Drawer is offline\nCash Drawer: Close");
							} else {

								showToast(activity, "The Drawer is offline\nCash Drawer: Open");
							}

							return;
						} else {

							if (starIOPort.retreiveStatus().compulsionSwitch) {

								showToast(activity, "The Drawer is online\nCash Drawer: Open");
							} else {

								byte[] printDataByteArray =
										convertFromListByteArrayTobyteArray(
												getPrintDataByteArrayList(
														activity
														, customerName
														, invoiceNumber
														, salePersonName
														, payAmount
														, soldProductList
														, printFor
														, mode));
								starIOPort.writePort(printDataByteArray, 0, printDataByteArray.length);
							}
						}
					} catch (StarIOPortException e) {

						showToast(activity, "Failed to connect to drawer");
						e.printStackTrace();
					} finally {
						if (starIOPort != null) {

							try {

								StarIOPort.releasePort(starIOPort);
							} catch (StarIOPortException e) {

								e.printStackTrace();
							}
						}
					}
				}
			})
			.show();
	}

	private static void showToast(Activity activity, String message) {

		Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
	}

	private static List<byte[]> getPrintDataByteArrayList(Activity activity, String customerName
			, String invoiceNumber, String salePersonName, double payAmount
			, List<SoldProduct> soldProductList, String printFor, String mode) {

		List<byte[]> printDataByteArrayList = new ArrayList<byte[]>();

		DecimalFormat decimalFormatterWithoutComma = new DecimalFormat("##0");
		DecimalFormat decimalFormatterWithComma = new DecimalFormat("###,##0");

		double totalAmount = 0, totalNetAmount = 0;

		printDataByteArrayList.add("        Myanmar Padauk\n\n".getBytes());
		printDataByteArrayList.add((
				"Customer   : " + customerName + "\n").getBytes());
		printDataByteArrayList.add((
				"Invoice No : " + invoiceNumber + "\n").getBytes());
		printDataByteArrayList.add((
				"Sale Person: " + salePersonName + "\n").getBytes());
		printDataByteArrayList.add((
				"Sale Date  : " + new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.US)
					.format(new Date()) + "\n").getBytes());
		printDataByteArrayList.add("--------------------------------\n".getBytes());

		formatter = new Formatter(new StringBuilder(), Locale.US);
		printDataByteArrayList.add(
				formatter.format(
					"%1$-5s %2$6s %3$5s %4$13s\n"
					, "Item"
					, "Qty"
					, "Price"
					, "Amount").toString().getBytes());
		formatter.close();
		printDataByteArrayList.add("--------------------------------\n".getBytes());

		for (SoldProduct soldProduct : soldProductList) {

			String name = new String();
			int quantity = soldProduct.getQuantity();
			double pricePerUnit = soldProduct.getProduct().getPrice();
			double amount = soldProduct.getTotalAmount();
			double pricePerUnitWithDiscount;
			double netAmount;

			double discount = soldProduct.getDiscount(activity);
			if (printFor.equals(Utils.PRINT_FOR_C)) {

				pricePerUnitWithDiscount =
						(soldProduct.getTotalAmount() - soldProduct.getDiscountAmount(activity))
							/ soldProduct.getQuantity();
				netAmount = pricePerUnitWithDiscount * quantity;

			} else {

				pricePerUnitWithDiscount =
						(soldProduct.getTotalAmount() - soldProduct.getDiscountAmount(activity) - soldProduct.getExtraDiscountAmount())
							/ soldProduct.getQuantity();
				netAmount = soldProduct.getNetAmount(activity);

			}
//			if (printFor.equals(Utils.PRINT_FOR_C)
//					&& discount + soldProduct.getExtraDiscount() > 4) {
//
//				pricePerUnitWithDiscount =
//						(soldProduct.getTotalAmount() - soldProduct.getDiscountAmount(activity))
//							/ soldProduct.getQuantity();
//				netAmount = pricePerUnitWithDiscount * quantity;
//			} else {
//
//				pricePerUnitWithDiscount =
//						(soldProduct.getTotalAmount() - soldProduct.getDiscountAmount(activity) - soldProduct.getExtraDiscountAmount())
//							/ soldProduct.getQuantity();
//				netAmount = soldProduct.getNetAmount(activity);
//			}

			totalAmount += amount;
			totalNetAmount += netAmount;

			// Shorthand the name.
			String[] nameFragments = soldProduct.getProduct().getName().split("\\-|\\(");
			for (int i = 0; i < nameFragments.length; i++) {

				nameFragments[i] = nameFragments[i].replaceAll("\\s*", "").replaceAll("\\)", "");
				if (nameFragments[i].endsWith("MHZ") || nameFragments[i].endsWith("MHZ")) {

					name += nameFragments[i].substring(0, 1);
				} else if (nameFragments[i].endsWith("KS") || nameFragments[i].endsWith("ks")) {

					double price = Double.parseDouble(nameFragments[i]
							.replace("KS", "")
							.trim());
					name += decimalFormatterWithoutComma.format(price / 1000) + "K";
				} else {

					name += nameFragments[i].substring(0, 1);
				}
			}
			formatter = new Formatter(new StringBuilder(), Locale.US);
			printDataByteArrayList.add(
					formatter.format(
						"%1$-5s %2$6s %3$5s %4$13s\n"
						, name
						, quantity
						, decimalFormatterWithoutComma.format(pricePerUnit)
						, decimalFormatterWithComma.format(amount)).toString().getBytes());
			formatter.close();

			if (!printFor.equals(Utils.PRINT_FOR_PRE_ORDER)) {

				formatter = new Formatter(new StringBuilder(), Locale.US);
				printDataByteArrayList.add(
						formatter.format("%1$-5s %2$12s %3$13s\n"
							, "N_Amt"
							, decimalFormatterWithoutComma.format(pricePerUnitWithDiscount)
							, decimalFormatterWithComma.format(netAmount)).toString().getBytes());
				formatter.close();
			}
		}
		printDataByteArrayList.add("--------------------------------\n".getBytes());

		formatter = new Formatter(new StringBuilder(), Locale.US);
		if (printFor.equals(Utils.PRINT_FOR_PRE_ORDER)) {

			printDataByteArrayList.add(
					formatter.format("%1$-13s%2$19s\n%3$-15s%4$17s\n\n\n"
						, "Total Amount:", decimalFormatterWithComma.format(totalAmount)
						, "Prepaid Amount:", decimalFormatterWithComma.format(payAmount)).toString().getBytes());
		} else if (mode.equals(Utils.FOR_DELIVERY)) {

			printDataByteArrayList.add(
					formatter.format("%1$-13s%2$19s\n%3$-13s%4$19s\n%5$-13s%6$19s\n%7$-13s%8$19s\n\n\n"
						, "Total Amount:", decimalFormatterWithComma.format(totalAmount)
						, "Net Amount:", decimalFormatterWithComma.format(totalNetAmount)
						, "Discount:", decimalFormatterWithComma.format(totalAmount - totalNetAmount)
						, "Pay Amount:", decimalFormatterWithComma.format(payAmount)).toString().getBytes());
		} else {

			printDataByteArrayList.add(
				formatter.format("%1$-13s%2$19s\n%3$-13s%4$19s\n%5$-13s%6$19s\n%7$-13s%8$19s\n%9$-13s%10$19s\n\n\n"
					, "Total Amount:", decimalFormatterWithComma.format(totalAmount)
					, "Net Amount:", decimalFormatterWithComma.format(totalNetAmount)
					, "Discount:", decimalFormatterWithComma.format(totalAmount - totalNetAmount)
					, "Pay Amount:", decimalFormatterWithComma.format(payAmount)
					, "Change Due:", decimalFormatterWithComma.format(Math.abs(payAmount - totalNetAmount))).toString().getBytes());
		}

		printDataByteArrayList.add(new byte[] { 0x1b, 0x64, 0x02 }); // Cut
		printDataByteArrayList.add(new byte[] { 0x07 }); // Kick cash drawer

		return printDataByteArrayList;
	}

	private static byte[] convertFromListByteArrayTobyteArray(List<byte[]> ByteArray) {
		int dataLength = 0;
		for (int i = 0; i < ByteArray.size(); i++) {
			dataLength += ByteArray.get(i).length;
		}

		int distPosition = 0;
		byte[] byteArray = new byte[dataLength];
		for (int i = 0; i < ByteArray.size(); i++) {
			System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition, ByteArray.get(i).length);
			distPosition += ByteArray.get(i).length;
		}

		return byteArray;
	}
}
