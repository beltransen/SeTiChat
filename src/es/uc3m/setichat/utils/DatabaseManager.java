package es.uc3m.setichat.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper{

	// Table names
	private final String TABLE_CONVERSATIONS = "conversations";
	private final String TABLE_PROFILE = "profile";
	private final String TABLE_CONTACTS = "contacts";
	
	// SQL creation statements
	private final String conversationTable = "CREATE TABLE "+TABLE_CONVERSATIONS;
	private final String profileTable = "CREATE TABLE "+TABLE_PROFILE;
	private final String contactsTable = "CREATE TABLE "+TABLE_CONTACTS;
	
	
	public DatabaseManager(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(profileTable);
		db.execSQL(contactsTable);
		db.execSQL(conversationTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
		
	}

}
