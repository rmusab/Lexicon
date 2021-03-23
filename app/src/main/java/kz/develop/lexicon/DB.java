package kz.develop.lexicon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rav_4 on 24.06.2014.
 */
public class DB extends SQLiteOpenHelper {

    public static final String DB_NAME = "user.sqlite";
    public static final int DB_VERSION = 1;

    //For main table
    public static final String TABLE_MAIN = "main";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_DAY_OF_MONTH = "day";
    public static final String COLUMN_IS_COMPLETE = "is_complete";
    public static final String COLUMN_COUNT = "count";

    //For list table
    public static final String TABLE_LIST = "list";
    public static final String COLUMN_ID_LIST = "id";
    public static final String COLUMN_COLLECTION_ID = "collection_id";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SET = "_set";

    //For sqlite_sequence table
    public static final String TABLE_SEQUENCE = "sqlite_sequence";

    public DB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creating main table
        String sql = "CREATE TABLE "+TABLE_MAIN+" ("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                    +COLUMN_YEAR+" INTEGER, "
                                                    +COLUMN_MONTH+" INTEGER, "
                                                    +COLUMN_DAY_OF_MONTH+" INTEGER, "
                                                    +COLUMN_COLLECTION_ID+" INTEGER, "
                                                    +COLUMN_IS_COMPLETE+" INTEGER, "
                                                    +COLUMN_COUNT+" INTEGER);";
        db.execSQL(sql);
        //Creating list table
        sql = "CREATE TABLE "+TABLE_LIST+" ("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                                             +COLUMN_SET+" INTEGER, "
                                             +COLUMN_COLLECTION_ID+" INTEGER, "
                                             +COLUMN_ID_LIST+" INTEGER);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
