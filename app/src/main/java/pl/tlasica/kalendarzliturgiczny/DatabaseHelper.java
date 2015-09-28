package pl.tlasica.kalendarzliturgiczny;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
    public static final int 		DATABASE_VERSION = 1;
    public static final String 	DATABASE_NAME = "Okazje.db";

    public static DatabaseHelper	singleton;
        
    public static synchronized DatabaseHelper getInstance(Context context) {
    	if (singleton == null) {
    		singleton = new DatabaseHelper( context ); 
    	}
    	return singleton;
    }
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseSchema.Occasion.sqlTableCreate());
        db.execSQL(DatabaseSchema.Update.sqlTableCreate());
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	// TODO: Nothing so far
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public int cleanDay(int daynum) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	String whereClause = "daynum=?";
    	String[] whereArgs = new String[] {String.valueOf(daynum)};
    	return db.delete(DatabaseSchema.Occasion.TABLENAME, whereClause, whereArgs);    	    	    	
    }
    
    public void addOccasion(OneOccasion in) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(DatabaseSchema.Occasion.DAY, in.dayNum);
    	values.put(DatabaseSchema.Occasion.OCC, in.text);
    	if (in.link != null) values.put(DatabaseSchema.Occasion.LINK, in.link);
    	if (in.extra != null) values.put(DatabaseSchema.Occasion.EXTRA, in.extra);
    	
    	db.insert(DatabaseSchema.Occasion.TABLENAME, "null", values);    	
    }

    public void addOccasions(List<OneOccasion> inList) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	for(OneOccasion in: inList) {    	
    		ContentValues values = new ContentValues();
    		values.put(DatabaseSchema.Occasion.DAY, in.dayNum);
    		values.put(DatabaseSchema.Occasion.OCC, in.text);
    		if (in.link != null) values.put(DatabaseSchema.Occasion.LINK, in.link);
    		if (in.extra != null) values.put(DatabaseSchema.Occasion.EXTRA, in.extra);    	
    		long id = db.insert(DatabaseSchema.Occasion.TABLENAME, "null", values);
    	}
    }
    
    public List<OneOccasion> fetchDay(int daynum) {
    	SQLiteDatabase db = getReadableDatabase();
    	
    	Cursor cur = db.rawQuery("select occ, link, extra from occasions where daynum=?", new String[]{String.valueOf(daynum)}); 

    	List<OneOccasion> res = new LinkedList<OneOccasion>();
    	if (cur.moveToFirst() ) {
	    	do {
	    		OneOccasion occ = new OneOccasion();
	    		occ.dayNum = daynum;
	    		occ.text = cur.getString(0);
	    		occ.link = cur.getString(1);
	    		occ.extra = cur.getString(2);
	    		res.add( occ );
	    	} while( cur.moveToNext() );
    		cur.close();
    	}    	    	
    	return res;
    	
    }

    //FIXME: nie działa wczytywanie tego update
    public boolean isUpdateNeeded(int month, String md5) {
    	SQLiteDatabase db = getReadableDatabase();
    	String[] whereArgs = new String[] {String.valueOf(month), md5};
    	Cursor cur = db.rawQuery("select count(1) from updates where month=? and md5=?", whereArgs);
    	if (cur.moveToFirst()) {
    		int res = cur.getInt(0);
    		cur.close();
    		return (0==res);
    	}
    	else return true;	// now checksum stored	
    }
    
    public void storeMD5(int month, String md5) {
    	SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DatabaseSchema.Update.MD5, md5);
		values.put(DatabaseSchema.Update.MONTH, month);
		long id = (int) db.insertWithOnConflict("updates", null, values, SQLiteDatabase.CONFLICT_REPLACE);		
    }
    
}
