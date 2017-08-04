package com.aceplus.myanmar_padauk.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Database extends SQLiteOpenHelper {

	public static String DB_PATH;
	public static final String DB_NAME = "myanmar-padauk.sqlite";

	//public static final String DB_NAME = "akt_npt_26.9.2016.sqlite";//after testing, notice to comment

	private SQLiteDatabase myanmarPadaukDatabase;
	
	private Context context;
	
	public Database(Context context) {

		super(context, DB_NAME, null, 1);
		
		if(android.os.Build.VERSION.SDK_INT >= 17) {

	       DB_PATH = context.getApplicationInfo().dataDir + "/databases/";         
	    } else {

	       DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
	    }

	    this.context = context;
	}
	
	private void createDatabase() throws IOException {
		
		if (!(new File(DB_PATH + DB_NAME)).exists()) {
			
			this.getReadableDatabase();
			this.close();
			try {
				
				// Copy database
				InputStream inputStream = context.getAssets().open(DB_NAME);
		        String outFileName = DB_PATH + DB_NAME;
		        OutputStream outputStream = new FileOutputStream(outFileName);
		        byte[] mBuffer = new byte[1024];
		        int mLength;
		        while ((mLength = inputStream.read(mBuffer))>0)
		        {
		        	outputStream.write(mBuffer, 0, mLength);
		        }
		        outputStream.flush();
		        outputStream.close();
		        inputStream.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	public SQLiteDatabase getDataBase() {

		try {

			createDatabase();
			
			if (myanmarPadaukDatabase == null) {
				
				myanmarPadaukDatabase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		return myanmarPadaukDatabase;
    }

    @Override
    public synchronized void close() {

        if(myanmarPadaukDatabase != null) {
        	
        	myanmarPadaukDatabase.close();
        }

        super.close();
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
