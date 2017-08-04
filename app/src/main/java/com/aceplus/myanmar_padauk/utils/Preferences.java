package com.aceplus.myanmar_padauk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Preferences {

	public static final String PREF_FILE_NAME = "pref-file";
	
	public static final String KEY_CUSTOMER_FEEDBACK_LAST_UPLOADED_DATE = "key_customer_feedback_last_uploaded_date";
	public static final String KEY_CUSTOMER_FEEDBACK_UPLOADED_COUNT = "key_customer_feedback_uploaded_count";

	public static final String KEY_SALE_LAST_UPLOADED_DATE = "key_sale_last_uploaded_date";
	public static final String KEY_SALE_UPLOADED_COUNT = "key_sale_uploaded_count";

	/*
	 * For Customer Feedback ID 
	 */
	public static final Date getCustomerFeedbackLastUploadedDate(Context context) {

		Date lastUploadedDate = null;

		String lastUploadedDateString = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.getString(KEY_CUSTOMER_FEEDBACK_LAST_UPLOADED_DATE, null);
		
		try {

			lastUploadedDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(lastUploadedDateString);
		} catch (NullPointerException e) {

			e.printStackTrace();
		} catch (ParseException e) {

			e.printStackTrace();
		}
		
		return lastUploadedDate;
	}

	public static final void setCustomerFeedbackLastUploadedDate(Context context, Date customerFeedbackLastUploadedDate) {

		context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().putString(KEY_CUSTOMER_FEEDBACK_LAST_UPLOADED_DATE
					, new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(customerFeedbackLastUploadedDate)).commit();
	}
	
	public static final long getCustomerFeedbackUploadedCount(Context context) {

		return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.getLong(KEY_CUSTOMER_FEEDBACK_UPLOADED_COUNT, 0);
	}

	public static final void addCustomerFeedbackUploadedCount(Context context, long numberOfUploadedCustomerFeedbacks) {

		if (numberOfUploadedCustomerFeedbacks > 0) {

			context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.edit().putLong(KEY_CUSTOMER_FEEDBACK_UPLOADED_COUNT
						, getCustomerFeedbackUploadedCount(context) + numberOfUploadedCustomerFeedbacks).commit();
		}
	}

	public static final void resetNumberOfCustomerFeedbackUploaded(Context context) {

		context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().remove(KEY_CUSTOMER_FEEDBACK_UPLOADED_COUNT).commit();
	}

	/*
	 * End for customer feedback.
	 */

	/*
	 * For sale ID.
	 */
	public static final Date getSaleLastUploadedDate(Context context) {

		Date lastUploadedDate = null;

		String lastUploadedDateString = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.getString(KEY_SALE_LAST_UPLOADED_DATE, null);
		
		try {

			lastUploadedDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(lastUploadedDateString);
		} catch (NullPointerException e) {

			e.printStackTrace();
		} catch (ParseException e) {

			e.printStackTrace();
		}
		
		return lastUploadedDate;
	}

	public static final void setSaleLastUploadedDate(Context context, Date saleLastUploadedDate) {

		context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().putString(KEY_SALE_LAST_UPLOADED_DATE
					, new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(saleLastUploadedDate)).commit();
	}
	
	public static final long getSaleUploadedCount(Context context) {

		return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.getLong(KEY_SALE_UPLOADED_COUNT, 0);
	}

	public static final void addSaleUploadedCount(Context context, long numberOfUploadedSale) {

		if (numberOfUploadedSale > 0) {

			context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.edit().putLong(KEY_SALE_UPLOADED_COUNT
						, getCustomerFeedbackUploadedCount(context) + numberOfUploadedSale).commit();
		}
	}

	public static final void resetNumberOfSaleUploaded(Context context) {

		context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().remove(KEY_SALE_UPLOADED_COUNT).commit();
	}

	/*
	 * End for sale ID.
	 */
	public static boolean wasUploadedCustomerFeedbacksToServer(Activity activity) {

		return activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getBoolean("wasUploadedCustomerFeedbacksToServer", false);
	}

	public static boolean wasUploadedSaleDataToServer(Activity activity) {

		return activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getBoolean("wasUploadedSaleDataToServer", false);
	}

	public static void didUploadedCustomerFeedbacksToServer(Activity activity, boolean value) {

		activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().putBoolean("wasUploadedCustomerFeedbacksToServer", value).commit();
	}

	public static void didUploadedSaleDataToServer(Activity activity, boolean value) {

		activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().putBoolean("wasUploadedSaleDataToServer", value).commit();
	}

	public static void didUploadedNewCustomersToServer(Activity activity, boolean value) {

		activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().putBoolean("wasUploadedNewCustomersToServer", value).commit();
	}

	public static boolean wasUploadedNewCustomersToServer(Activity activity) {

		return activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getBoolean("wasUploadedNewCustomersToServer", false);
	}

	public static boolean wasUploadedPreOrderDataToServer(Activity activity) {

		return activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getBoolean("wasUploadedPreOrderDataToServer", false);
	}

	public static void didUploadedPreOrderDataToServer(Activity activity, boolean value) {

		activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().putBoolean("wasUploadedPreOrderDataToServer", value).commit();
	}

	public static boolean wasUploadedDeliveredDataToServer(Activity activity) {

		return activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getBoolean("wasUploadedDeliveredDataToServer", false);
	}

	public static void didUploadedDeliveredDataToServer(Activity activity, boolean value) {

		activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit().putBoolean("wasUploadedDeliveredDataToServer", value).commit();
	}

	public static String getNextNewCustomerId(Activity activity, String salemanId) {

		SharedPreferences sharedPreferences = 
				activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		String todayDate = new SimpleDateFormat("yyyy/mm/dd").format(new Date());

		String lastNewCustomerAddedDate = 
				sharedPreferences.getString("lastNewCustomerAddedDate", null);
		if (lastNewCustomerAddedDate == null) {

			lastNewCustomerAddedDate = todayDate;
			sharedPreferences.edit().putString("lastNewCustomerAddedDate", lastNewCustomerAddedDate);
		}

		int todayAddedCustomerCount = 0;
		if (lastNewCustomerAddedDate.equalsIgnoreCase(todayDate)) {

			todayAddedCustomerCount = sharedPreferences.getInt("todayAddedCustomerCount", 0);
		} else {

			sharedPreferences.edit().remove(lastNewCustomerAddedDate).commit();
			sharedPreferences.edit().putString("lastNewCustomerAddedDate", todayDate);
		}
		sharedPreferences.edit().putInt("todayAddedCustomerCount", todayAddedCustomerCount + 1)
			.commit();

		//change date format from (dd) to (yyMMdd)- HAK
		return salemanId + new SimpleDateFormat("yyMMdd").format(
				new Date()) + new DecimalFormat("00").format(todayAddedCustomerCount + 1.0);
	}
}
