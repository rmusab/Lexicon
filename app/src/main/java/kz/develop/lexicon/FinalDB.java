package kz.develop.lexicon;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Random;

/**
 * Created by Rav_4 on 27.06.2014.
 */
public class FinalDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "database.sqlite";
    private static String DB_PATH;
    private Context context;
    public static SQLiteDatabase sqdb;

    public static final String TABLE_WORD = "Word";
    public static final String TABLE_TERM = "Term";
    public static final String TABLE_WORDS = "words";
    public static final String TABLE_WORDS_COLLECTIONS = "words_collections";
    public static final int COLLECTION_BASE = 1;

    //For word & term tables
    public static final String COLUMN_WORD = "word";
    public static final String COLUMN_TRANSCRIPTION = "transcription";
    public static final String COLUMN_PART_OF_SPEECH = "partOfSpeech";
    public static final String COLUMN_WORD_PAIR_ID = "wordPairId";
    public static final String COLUMN_WORD_ORDER = "wordOrder";
    public static final String PART_VERB = "verb";
    public static final String COLUMN_COLLECTION = "collection";
    public static final String COLUMN_WORDS_COUNT = "words_count";
    public static final String COLUMN_DESC = "desc";
    public static final String COLUMN_TRANSLATE = "translate";

    public FinalDB(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
        DB_PATH = context.getDatabasePath(DB_NAME).getAbsolutePath();
    }

    private static boolean catchForEqual(String word, String[] words) {
        boolean caught = false;
        for (int i = 0; i < words.length; i++)
            if (word.equals(words[i])) {
                caught = true;
                break;
            }
        return caught;
    }

    public static String[] getVariantsByID(int id, int collection_id) {
        Cursor c;
        String[] result = new String[4];
        String partOfSpeech = getPartOfSpeechByID(id, collection_id);
        Random random = new Random();
        int i;
        switch (collection_id) {
            case COLLECTION_BASE:
                c = sqdb.query(TABLE_TERM, new String[] {COLUMN_WORD, COLUMN_WORD_PAIR_ID},
                        COLUMN_PART_OF_SPEECH + " = ?", new String[] {partOfSpeech}, null, null, null);
                i = 0;
                while (i <= 3) {
                    c.moveToPosition(random.nextInt(c.getCount()));
                    String word = c.getString(c.getColumnIndex(COLUMN_WORD));
                    int wordPairId = c.getInt(c.getColumnIndex(COLUMN_WORD_PAIR_ID));
                    if (catchForEqual(word, result) || wordPairId == id) continue;
                    else {
                        result[i] = word;
                        i++;
                    }
                }
                break;
            default:
                c = sqdb.query(TABLE_WORDS, new String[] {COLUMN_TRANSLATE, DB.COLUMN_ID},
                        DB.COLUMN_COLLECTION_ID + " = ?", new String[] {String.valueOf(collection_id)}, null, null, null);
                i = 0;
                while (i <= 3) {
                    c.moveToPosition(random.nextInt(c.getCount()));
                    String word = c.getString(c.getColumnIndex(COLUMN_TRANSLATE));
                    int checkID = c.getInt(c.getColumnIndex(DB.COLUMN_ID));
                    if (catchForEqual(word, result) || checkID == id) continue;
                    else {
                        result[i] = word;
                        i++;
                    }
                }
                break;
        }
        c.close();
        return result;
    }

    public static String getPartOfSpeechByID(int id, int collection_id) {
        String result;
        switch (collection_id) {
            case COLLECTION_BASE:
                Cursor c = sqdb.query(TABLE_WORD, new String[] {COLUMN_PART_OF_SPEECH},
                        DB.COLUMN_ID + " = ?", new String[] {String.valueOf(id)}, null, null, null);
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(COLUMN_PART_OF_SPEECH));
                c.close();
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    public static int getSumOfCount() {
        Cursor c = sqdb.rawQuery("select sum(" + COLUMN_WORDS_COUNT + ") from " + TABLE_WORDS_COLLECTIONS, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    public static String getWordByID(int id, int collection_id) {
        Cursor c;
        String result = "";
        switch (collection_id) {
            case COLLECTION_BASE:
                c = sqdb.query(TABLE_WORD, new String[] {COLUMN_WORD},
                        DB.COLUMN_ID + " = ?", new String[] {String.valueOf(id)}, null, null, null);
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(COLUMN_WORD));
                break;
            default:
                c = sqdb.query(TABLE_WORDS, new String[] {COLUMN_WORD},
                        DB.COLUMN_ID + " = ?", new String[] {String.valueOf(id)}, null, null, null);
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(COLUMN_WORD));
                break;
        }
        c.close();
        return result;
    }

    public static String getFirstWordTranslationByID(int id, int collection_id) {
        Cursor c;
        String result = "";
        switch (collection_id) {
            case COLLECTION_BASE:
                c = sqdb.query(TABLE_TERM, new String[] {COLUMN_WORD},
                        COLUMN_WORD_PAIR_ID + " = ?", new String[] {String.valueOf(id)}, null, null, null);
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(COLUMN_WORD));
                break;
            default:
                c = sqdb.query(TABLE_WORDS, new String[] {COLUMN_TRANSLATE},
                        DB.COLUMN_ID + " = ?", new String[] {String.valueOf(id)}, null, null, null);
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(COLUMN_TRANSLATE));
                break;
        }
        c.close();
        return result;
    }

    public static Cursor getCollectionsCursor() {
        return sqdb.rawQuery("select "+COLUMN_COLLECTION+" from "+TABLE_WORDS_COLLECTIONS, null);
    }

    public static String getTableNameByID(int collection_id) {
        String table;
        switch (collection_id) {
            case COLLECTION_BASE:
                table = TABLE_WORD;
                break;
            default:
                table = TABLE_WORDS;
                break;
        }
        return table;
    }

    public static Cursor getWordColumnsByID(int wordID, int collection_id) {
        String table = getTableNameByID(collection_id);
        return sqdb.rawQuery("select * from "+table+" where "+DB.COLUMN_ID+" = "+String.valueOf(wordID),null);
    }

    public static String getTranslation(int wordID, int collection_id) {
        String result = "";
        Cursor c;
        switch (collection_id) {
            case COLLECTION_BASE:
                c = sqdb.rawQuery("select * from "+TABLE_TERM+" where "+COLUMN_WORD_PAIR_ID+" = "+String.valueOf(wordID),null);
                if (c.moveToFirst()) {
                    do {
                      int i = c.getInt(c.getColumnIndex(COLUMN_WORD_ORDER));
                      if (i==0) {
                          if (!(c.getPosition() == 0)) result = result + "\n\n";
                          result = result + c.getString(c.getColumnIndex(COLUMN_PART_OF_SPEECH)) + ": ";
                          result = result + c.getString(c.getColumnIndex(COLUMN_WORD));
                      } else result = result + ", " + c.getString(c.getColumnIndex(COLUMN_WORD));
                    } while (c.moveToNext());
                }
                break;
            default:
                c = sqdb.rawQuery("select * from "+TABLE_WORDS+" where "+DB.COLUMN_ID+" = "+String.valueOf(wordID)+" and "+
                                  DB.COLUMN_COLLECTION_ID+" = "+String.valueOf(collection_id), null);
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(COLUMN_TRANSLATE));
                break;
        }
        return result;
    }

    public static String getWordTranscription(Cursor c, int collection_id) {
        String result = "";
        c.moveToFirst();
        switch (collection_id) {
            case COLLECTION_BASE:
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(FinalDB.COLUMN_TRANSCRIPTION));
                break;
            default:
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(FinalDB.COLUMN_DESC));
                break;
        }
        return result;
    }

    public static String getPartOfSpeech(Cursor c, int collection_id) {
        String result = "";
        c.moveToFirst();
        switch (collection_id) {
            case COLLECTION_BASE:
                c.moveToFirst();
                result = c.getString(c.getColumnIndex(FinalDB.COLUMN_PART_OF_SPEECH));
                break;
            default:
                break;
        }
        return result;
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if(dbExist){
            //Nothing to do. Base already exists
        }else{
            //Creating empty database
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDataBase(){
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch(SQLiteException e) {
            //Base do not exist
        }
        if(checkDB != null){
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    private void copyDataBase() throws IOException{
        InputStream myInput = context.getAssets().open(DB_NAME);
        String outFileName = DB_PATH;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH;
        sqdb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if(sqdb != null)
            sqdb.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
