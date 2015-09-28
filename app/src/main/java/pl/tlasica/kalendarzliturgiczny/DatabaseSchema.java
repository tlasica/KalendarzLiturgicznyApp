package pl.tlasica.kalendarzliturgiczny;

import android.provider.BaseColumns;

/**
 * Contract class for application database
 * @author tomek
 *
 */
public class DatabaseSchema {

	public static abstract class Occasion implements BaseColumns {
		
		public static final String TABLENAME = "occasions";
	    public static final String DAY = "daynum";
	    public static final String OCC = "occ";
	    public static final String LINK = "link";
	    public static final String EXTRA = "extra";
	    
	    public static final String sqlTableCreate() {
			return "CREATE TABLE " + TABLENAME + " (" +
			_ID + " INTEGER PRIMARY KEY," +
			DAY + INT_TYPE + NOT_NULL + COMMA_SEP +
			OCC + TEXT_TYPE + NOT_NULL + COMMA_SEP +
			LINK + TEXT_TYPE + COMMA_SEP +
			EXTRA + TEXT_TYPE +
			" )";	    	
	    }
	    
	    public static final String sqlTableDrop() {
	    	return "DROP TABLE IF EXISTS " + Occasion.TABLENAME;
	    }
	}

	public static abstract class Update implements BaseColumns {		
		public static final String TABLENAME = "updates";
	    public static final String MONTH = "month";
	    public static final String MD5 = "md5";
	    
	    public static final String sqlTableCreate() {
		    return "CREATE TABLE " + TABLENAME + " (" +
		    _ID + " INTEGER PRIMARY KEY," +
		    MONTH + INT_TYPE + NOT_NULL + COMMA_SEP +
		    MD5 + TEXT_TYPE +
		    " )";
	    }
	    
	    public static final String sqlTableDrop() {
	    	return "DROP TABLE IF EXISTS " + TABLENAME;
	    }
	    
	}
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";	// for sqlite
	private static final String COMMA_SEP = ",";
	private static final String NOT_NULL = " NOT NULL";
	
	private DatabaseSchema() {}
	
}
