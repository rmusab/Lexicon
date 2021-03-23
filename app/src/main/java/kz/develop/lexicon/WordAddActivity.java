package kz.develop.lexicon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steve Fox on 12.08.2014.
 */
public class WordAddActivity extends Activity {

    private ListView lvSearch;
    private Spinner spDicSpinner;
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_WORD = "word";
    public static final String ATTRIBUTE_TRANSL = "transl";
    public static final String ATTRIBUTE_ADDED = "added";
    public static final String ATTRIBUTE_COLLECTION_ID = "collection_id";

    public static final String EXTRA_SET_ID = "set_id";
    public static final String EXTRA_READ_ONLY = "read_only";

    private static ArrayList<Map<String, Object>> data;
    private static SimpleAdapter adapter;
    private int collection = -1;

    private final String BUNDLE_DATA = "data";
    private final String BUNDLE_COLLECTION = "collection";

    private final Handler mHandler = new Handler();
    private ColorAnimationDrawable myColorAnimationDrawable;

    private int getCollectionID() {
        int setID = getIntent().getIntExtra(EXTRA_SET_ID, -1);
        if (setID == -1) return -1;
        Cursor c = MainActivity.db.rawQuery("select "+DB.COLUMN_COLLECTION_ID+" from "+DB.TABLE_MAIN+
                " where "+DB.COLUMN_ID+" = "+String.valueOf(setID), null);
        c.moveToFirst();
        int result = c.getInt(c.getColumnIndex(DB.COLUMN_COLLECTION_ID));
        c.close();
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_add);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        collection = getCollectionID();

        final EditText etWord = (EditText) findViewById(R.id.etWord);
        lvSearch = (ListView) findViewById(R.id.lvSearch);
        spDicSpinner = (Spinner) findViewById(R.id.spDicSpinner);
        if (savedInstanceState != null) {
            data = (ArrayList<Map<String, Object>>) savedInstanceState.getSerializable(BUNDLE_DATA);
            collection = savedInstanceState.getInt(BUNDLE_COLLECTION, -1);
        }
        if (getLastNonConfigurationInstance() != null) {
            adapter = (SimpleAdapter) getLastNonConfigurationInstance();
            lvSearch.setAdapter(adapter);
        }

        Cursor c = FinalDB.sqdb.rawQuery("select * from "+FinalDB.TABLE_WORDS_COLLECTIONS, null);
        ArrayList<String> listItems = new ArrayList<String>();
        if (c.moveToFirst()) {
            do listItems.add(c.getString(c.getColumnIndex(FinalDB.COLUMN_COLLECTION)));
            while (c.moveToNext());
        }
        String[] items = {};
        items = listItems.toArray(new String[listItems.size()]);
        int[] images = new int[listItems.size()];
        Arrays.fill(images, R.drawable.book);
        ImageListAdapter imageAdapter = new ImageListAdapter(this, getLayoutInflater(),
                items, images);
        spDicSpinner.setAdapter(imageAdapter);
        spDicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                collection = (int) l + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (collection != -1) spDicSpinner.setSelection(collection - 1);
            else {
            spDicSpinner.setSelection(0);
            collection = 1;
        }

        Button btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etWord.getText().toString().trim().equals("")) {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.toastEmpty_text), Toast.LENGTH_SHORT).show();
                    return;
                }
                final String s = etWord.getText().toString().trim();
                Cursor c = FinalDB.sqdb.rawQuery("select "+DB.COLUMN_ID+", "+FinalDB.COLUMN_WORD
                        +" from "+FinalDB.getTableNameByID(collection)+" where "+FinalDB.COLUMN_WORD+" like "+"'"+s+"%'"
                        +" and "+DB.COLUMN_COLLECTION_ID+" = "+String.valueOf(collection), null);
                data = new ArrayList<Map<String, Object>>();
                Map<String, Object> m;
                if (c.moveToFirst()) {
                    do {
                        final int id = c.getInt(c.getColumnIndex(DB.COLUMN_ID));
                        m = new HashMap<String, Object>();
                        m.put(ATTRIBUTE_WORD, c.getString(c.getColumnIndex(FinalDB.COLUMN_WORD)));
                        m.put(ATTRIBUTE_TRANSL, FinalDB.getFirstWordTranslationByID(id, collection));
                        Cursor c2 = MainActivity.db.query(DB.TABLE_LIST, new String[]{DB.COLUMN_ID_LIST, DB.COLUMN_COLLECTION_ID},
                                DB.COLUMN_ID_LIST + " = ? and " + DB.COLUMN_COLLECTION_ID + " = ?",
                                new String[]{String.valueOf(id), String.valueOf(collection)}, null, null, null);
                        Boolean b = (c2.getCount() != 0);
                        c2.close();
                        m.put(ATTRIBUTE_ADDED, b);
                        m.put(ATTRIBUTE_ID, id);
                        m.put(ATTRIBUTE_COLLECTION_ID, collection);
                        data.add(m);
                    } while (c.moveToNext() && c.getPosition() <= 40);
                }
                c.close();
                String[] from = {ATTRIBUTE_WORD, ATTRIBUTE_TRANSL, ATTRIBUTE_ADDED};
                int[] to = {R.id.tvItem, R.id.tvItemWords, R.id.imgItem};
                adapter = new SimpleAdapter(getBaseContext(), data, R.layout.item_add, from, to);
                adapter.setViewBinder(new MyViewBinder());
                lvSearch.setAdapter(adapter);
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etWord.getWindowToken(), 0);
            }
        });
        lvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                DialogWordInfo dlgInfo = new DialogWordInfo();
                Bundle arg = new Bundle();
                Intent intent = getIntent();
                Integer wordID = (Integer) data.get(pos).get(ATTRIBUTE_ID);
                Integer colID = (Integer) data.get(pos).get(ATTRIBUTE_COLLECTION_ID);
                arg.putInt(DialogWordInfo.KEY_SET_ID, intent.getIntExtra(EXTRA_SET_ID, 1));
                arg.putInt(DialogWordInfo.KEY_ID, wordID);
                arg.putInt(DialogWordInfo.KEY_COLLECTION_ID, colID);
                arg.putString(DialogWordInfo.KEY_TITLE, (String) data.get(pos).get(ATTRIBUTE_WORD));
                arg.putString(DialogWordInfo.KEY_MESSAGE, FinalDB.getTranslation(wordID, colID));
                arg.putString(DialogWordInfo.KEY_TRANSCRIPTION,
                        FinalDB.getWordTranscription(FinalDB.getWordColumnsByID(wordID, colID), colID));
                Boolean added = (Boolean) data.get(pos).get(ATTRIBUTE_ADDED);
                arg.putBoolean(DialogWordInfo.KEY_ADDED, added);
                arg.putBoolean(DialogWordInfo.KEY_READ_ONLY, getIntent().getBooleanExtra(EXTRA_READ_ONLY, false));
                arg.putInt(DialogWordInfo.KEY_LIST_POSITION, pos);
                dlgInfo.setArguments(arg);
                dlgInfo.show(getFragmentManager(), "dlginfo");
            }
        });

        setMyCustomActionBar();
    }

    public static void setWordAdded(int pos, Activity callingActivity) {
        data.get(pos).put(ATTRIBUTE_ADDED, true);
        adapter.notifyDataSetChanged();
        callingActivity.setResult(RESULT_OK);
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

    private class MyViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object o, String s) {
            switch (view.getId()) {
                case R.id.tvItem:
                case R.id.tvItemWords:
                    ((TextView) view).setText((String) o);
                    return true;
                case R.id.imgItem:
                    if ((Boolean) o == true) ((ImageView) view).setImageResource(R.drawable.checked_64);
                    else ((ImageView) view).setImageResource(R.drawable.unchecked_64);
                    return true;
            }
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_DATA, data);
        outState.putInt(BUNDLE_COLLECTION, collection);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return adapter;
    }
}
