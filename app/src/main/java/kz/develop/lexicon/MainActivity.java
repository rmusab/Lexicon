package kz.develop.lexicon;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private DB myDB;
    private FinalDB finaldb;
    public static SQLiteDatabase db;

    /*public static final String APP_PREFERENCES = "app_settings";
    public static final String APP_PREFERENCES_LAST_SET_ID = "last_set_id";
    public static SharedPreferences myPref;
    public static SharedPreferences.Editor editor;*/

    public static SimpleCursorAdapter adapter;
    DialogFill dlgfill;

    public static final String EXTRA_POSITION = "position";

    private final Handler mHandler = new Handler();
    private ColorAnimationDrawable myColorAnimationDrawable;

    public static SpeechHelper speechHelper;
    public static ListView myListView;
    private static Cursor collectionsCursor;
    private ProgressBar pbMain;
    private int totalCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setMyCustomActionBar();

        myDB = new DB(this);
        db = myDB.getWritableDatabase();
        finaldb = new FinalDB(this);
        try {
            finaldb.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            finaldb.openDataBase();
        } catch (SQLException sqle) {
            throw new Error("Unable to open database");
        }

        //myPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        myListView = getListView();
        myListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        final View header = getLayoutInflater().inflate(R.layout.header, null);
        ProgressBar pbMain = (ProgressBar) header.findViewById(R.id.pbMain);
        //pbMain.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        TextView tvWholeNum = (TextView) header.findViewById(R.id.tvWholeNum);
        totalCount = FinalDB.getSumOfCount();
        tvWholeNum.setText(getResources().getString(R.string.tvWholeNum_text) + " " +
                                String.valueOf(totalCount));

        myListView.addHeaderView(header, null, false);

        collectionsCursor = FinalDB.getCollectionsCursor();
        speechHelper = new SpeechHelper(this);
        setAdapter();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, WordActivity.class);
                intent.putExtra(EXTRA_POSITION, i - 1);
                startActivity(intent);
            }
        });
        registerForContextMenu(getListView());
    }

    private void setMyCustomActionBar() {

        Drawable.Callback myDrawableCallback = new Drawable.Callback() {
            @Override
            public void invalidateDrawable(Drawable drawable) {
                getActionBar().setBackgroundDrawable(drawable);
            }

            @Override
            public void scheduleDrawable(Drawable drawable, Runnable runnable, long l) {
                mHandler.postAtTime(runnable, l);
            }

            @Override
            public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
                mHandler.removeCallbacks(runnable);
            }
        };

        myColorAnimationDrawable = new ColorAnimationDrawable();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            myColorAnimationDrawable.setCallback(myDrawableCallback);
        } else getActionBar().setBackgroundDrawable(myColorAnimationDrawable);
        myColorAnimationDrawable.setColor(getResources().getColor(R.color.theme));
        getActionBar().setTitle("");
        getActionBar().setIcon(R.drawable.ic_white);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        final long id = ((AdapterView.AdapterContextMenuInfo) menuInfo).id - 1;
        DialogContext dialog = new DialogContext();
        dialog.setCancelable(true);
        Bundle arg = new Bundle();
        arg.putLong(DialogContext.KEY_ID, id);
        dialog.setArguments(arg);
        dialog.show(getFragmentManager(), "dlg_context");
    }

    public static int getLastSetID() {
        Cursor c = db.rawQuery("select * from "+DB.TABLE_MAIN, null);
        return c.getCount();
    }

    public static void deleteAll() {
        db.delete(DB.TABLE_MAIN, null, null);
        db.delete(DB.TABLE_LIST, null, null);
        db.execSQL("update "+DB.TABLE_SEQUENCE+" set seq = 0");
        scrollMyListViewToBottom();
    }

    public static void deleteSet(long menu_id) {
        db.delete(DB.TABLE_MAIN, DB.COLUMN_ID +" = ?", new String[] {String.valueOf(menu_id)});
        db.delete(DB.TABLE_LIST, DB.COLUMN_SET + " = ?", new String[]{String.valueOf(menu_id)});
        Cursor c = db.rawQuery("select * from "+DB.TABLE_MAIN+" where "+DB.COLUMN_ID+" > "+String.valueOf(menu_id), null);
        if (c.moveToFirst()) {
            do {
                int i = c.getInt(c.getColumnIndex(DB.COLUMN_ID));
                db.execSQL("update "+DB.TABLE_MAIN+" set "+DB.COLUMN_ID+" = "+String.valueOf(i-1)+" where "
                        +DB.COLUMN_ID+" = "+String.valueOf(i));
                db.execSQL("update "+DB.TABLE_LIST+" set "+DB.COLUMN_SET+" = "+String.valueOf(i-1)+" where "
                        +DB.COLUMN_SET+" = "+String.valueOf(i));
            } while (c.moveToNext());
        }
        db.execSQL("update "+DB.TABLE_SEQUENCE+" set seq = 0");
        scrollMyListViewToBottom();
    }

    private static void scrollMyListViewToBottom() {
        myListView.post(new Runnable() {
            @Override
            public void run() {
                myListView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    private void setAdapter() {
        String[] from = {DB.COLUMN_IS_COMPLETE, DB.COLUMN_ID, DB.COLUMN_ID, DB.COLUMN_ID};
        int[] to = {R.id.imgItem, R.id.tvItem, R.id.tvCollection, R.id.tvCount};

        getLoaderManager().initLoader(0, null, this);

        adapter = new SimpleCursorAdapter(this, R.layout.main_item, null, from, to, 0);
        adapter.setViewBinder(new myViewBinder());
        getListView().setAdapter(adapter);
        scrollMyListViewToBottom();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new myCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    static class myCursorLoader extends CursorLoader {
        SQLiteDatabase db;

        public myCursorLoader(Context context, SQLiteDatabase db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            return db.rawQuery("select * from "+DB.TABLE_MAIN,null);
        }
    }

    class myViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int i) {
            int setID, year, month, day;
            switch (view.getId()) {
                case R.id.tvItem:
                    setID = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID));
                    year = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_YEAR));
                    month = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_MONTH));
                    day = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_DAY_OF_MONTH));
                    ((TextView) view).setText("Set "+String.valueOf(setID)+" ("
                                              +String.valueOf(day)+"."
                                              +String.valueOf(month)+"."
                                              +String.valueOf(year)+")");
                    return true;
                case R.id.tvCount:
                    int count = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_COUNT));
                    /*setID = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID));
                    c = db.query(DB.TABLE_LIST, new String[] {DB.COLUMN_SET, DB.COLUMN_ID_LIST, DB.COLUMN_COLLECTION_ID}
                            , DB.COLUMN_SET + " = ?"
                            , new String[] {String.valueOf(setID)}
                            ,null,null,null);
                    String text = String.valueOf(count) + " words: ";
                    int j = 0;
                    if (c.moveToFirst()) {
                        do {
                            j++;
                            if (!(j==1)) text += ", ";
                            int wordID = c.getInt(c.getColumnIndex(DB.COLUMN_ID_LIST));
                            int col_id = c.getInt(c.getColumnIndex(DB.COLUMN_COLLECTION_ID));
                            Cursor c2 = FinalDB.getWordColumnsByID(wordID,col_id);
                            c2.moveToFirst();
                            String word = c2.getString(c2.getColumnIndex(FinalDB.COLUMN_WORD));
                            text += word;
                        } while (c.moveToNext());
                        ((TextView) view).setText(text);
                    }*/
                    ((TextView) view).setText(String.valueOf(count) + " " + getResources().getString(R.string.tvCount_text));
                    return true;
                case R.id.tvCollection:
                    int collection_id = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_COLLECTION_ID));
                    int j = 0;
                    if (collectionsCursor.moveToFirst()) {
                        do {
                            j++;
                            if (j==collection_id) {
                                ((TextView) view).setText(collectionsCursor.
                                        getString(collectionsCursor.getColumnIndex(FinalDB.COLUMN_COLLECTION)));
                                break;
                            }
                        } while (collectionsCursor.moveToNext());
                    }
                    return true;
                case R.id.imgItem:
                    boolean is_completed = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_IS_COMPLETE))==1 ? true : false;
                    if (is_completed) ((ImageView) view).setImageResource(R.drawable.checked);
                    else ((ImageView) view).setImageResource(R.drawable.unchecked);
                    return true;
            }
            return false;
        }
    }

    public void fillNewSet(int collection_id, int n, int set_number) {
        String table = FinalDB.getTableNameByID(collection_id);

        Cursor c = FinalDB.sqdb.rawQuery("select * from "+table+" where "+DB.COLUMN_COLLECTION_ID+" = "+
                String.valueOf(collection_id), null);
        int count = c.getCount();
        c.close();

        Param p = new Param(FinalDB.sqdb, collection_id, n, set_number, count, table);
        MyTask mt = new MyTask();
        mt.execute(p);
    }

    public class Param {
        SQLiteDatabase sqdb;
        int collection_id, n, set_number, count;
        String table;

        Param(SQLiteDatabase sqdb, int collection_id, int n, int set_number, int count, String table){
            this.sqdb = sqdb;
            this.collection_id = collection_id;
            this.n = n;
            this.set_number = set_number;
            this.count = count;
            this.table = table;
        }
    }

    private void restoreOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public class MyTask extends AsyncTask<Param, Integer, Void> {
        SQLiteDatabase sqdb;
        int collection_id, n, set_number, count;
        String table;
        boolean isBroken = false;
        int checkCount = 0, i;

        @Override
        protected Void doInBackground(Param... p) {
            sqdb = p[0].sqdb;
            collection_id = p[0].collection_id;
            n = p[0].n;
            set_number = p[0].set_number;
            count = p[0].count;
            table = p[0].table;
            for (i = 1; i <= n; i++) {
                final Random random = new Random();
                boolean b = true;
                int id = 0;
                do {
                    Cursor check = db.query(DB.TABLE_LIST, new String[]{DB.COLUMN_ID_LIST, DB.COLUMN_COLLECTION_ID},
                                            DB.COLUMN_COLLECTION_ID + " = ?", new String[]{String.valueOf(collection_id)}, null, null, null);
                    checkCount = check.getCount();
                    if ((count - checkCount) == 0) {
                        isBroken = true;
                        checkCount = i - 1;
                        break;
                    }
                    Cursor c = sqdb.query(table, new String[]{DB.COLUMN_ID, DB.COLUMN_COLLECTION_ID},
                            DB.COLUMN_COLLECTION_ID + " = ?",
                            new String[]{String.valueOf(collection_id)}, null, null, null);
                    c.moveToPosition(random.nextInt(count));
                    id = c.getInt(c.getColumnIndex(DB.COLUMN_ID));
                    if (id != 0) {
                        Cursor c2 = db.query(DB.TABLE_LIST, new String[]{DB.COLUMN_ID_LIST, DB.COLUMN_COLLECTION_ID},
                                DB.COLUMN_ID_LIST + " = ? and " + DB.COLUMN_COLLECTION_ID + " = ?",
                                new String[]{String.valueOf(id), String.valueOf(collection_id)}, null, null, null);
                        b = !(c2.getCount() == 0);
                    }
                } while (b == true);
                if (isBroken) break;
                ContentValues cv = new ContentValues();
                cv.put(DB.COLUMN_SET, set_number);
                cv.put(DB.COLUMN_COLLECTION_ID, collection_id);
                cv.put(DB.COLUMN_ID_LIST, id);
                db.insert(DB.TABLE_LIST, null, cv);
                publishProgress(i);
            }
            if ((isBroken) && (i==1)) {
                publishProgress(n);
                return null;
            } else if ((isBroken) && (i>1)) publishProgress(n);
            //Adding new set into main table
            ContentValues cv = new ContentValues();
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            cv.put(DB.COLUMN_YEAR, year);
            cv.put(DB.COLUMN_MONTH, month);
            cv.put(DB.COLUMN_DAY_OF_MONTH, day);
            cv.put(DB.COLUMN_COLLECTION_ID, collection_id);
            cv.put(DB.COLUMN_IS_COMPLETE, 0);
            cv.put(DB.COLUMN_COUNT, i - 1);
            db.insert(DB.TABLE_MAIN, null, cv);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            double val = 1d;
            val = Double.valueOf(values[0]) / Double.valueOf(n);
            val *= 100d;
            int progress = (int) val;
            dlgfill.updateProgress(progress);
            if (isBroken) Toast.makeText(getBaseContext(), getResources().getString(R.string.toastYouHave_text) + " " +
                                            String.valueOf(checkCount), Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dlgfill.dismiss();
            if ((isBroken) && (i==1)) {
                restoreOrientation();
                return;
            }
            getLoaderManager().getLoader(0).forceLoad();
            scrollMyListViewToBottom();
            restoreOrientation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static interface FillNewSetCallback {

        void fill(int collection, int count);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Intent intent = new Intent(this, WordAddActivity.class);
            intent.putExtra(WordAddActivity.EXTRA_SET_ID, -1);
            intent.putExtra(WordAddActivity.EXTRA_READ_ONLY, true);
            startActivity(intent);
        }
        if (id == R.id.action_add) {
              DialogAdd dlgAdd = new DialogAdd();
              dlgAdd.setCallback(new FillNewSetCallback() {
                  @Override
                  public void fill(int collection, int count) {
                      dlgfill = new DialogFill();
                      dlgfill.show(getFragmentManager(), "dlgfill");
                      dlgfill.setCancelable(false);
                      getFragmentManager().executePendingTransactions();
                      fillNewSet(collection, count, getLastSetID() + 1);
                  }
              });
              dlgAdd.setCancelable(false);
              dlgAdd.show(getFragmentManager(), "dlgadd");
              return true;
        }
        if (id == R.id.action_exit) {
            closeAllDatabases();
            speechHelper.Destroy();
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().getLoader(0).forceLoad();
    }

    private void closeAllDatabases() {
        if (db != null) db.close();
        if (myDB != null) myDB.close();
        if (finaldb != null) finaldb.close();
        if (collectionsCursor != null) collectionsCursor.close();
    }

    @Override
    protected void onDestroy() {
        speechHelper.Destroy();
        super.onDestroy();
    }
}
