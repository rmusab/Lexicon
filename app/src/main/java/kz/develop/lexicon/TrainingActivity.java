package kz.develop.lexicon;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Steve Fox on 29.08.2014.
 */
public class TrainingActivity extends Activity {

    public static final String EXTRA_SET_ID = "set_id";
    public static final byte MODE_TEST = 0;
    public static final byte MODE_GATHER = 1;
    public static final byte MODE_SOUND = 2;
    public static final byte MODE_WRITE = 3;

    private final Handler mHandler = new Handler();
    private ColorAnimationDrawable myColorAnimationDrawable;
    private static int setID, count;
    private static byte curModeID = MODE_TEST;
    public static int lastIndex = -1;
    private static ArrayList<Integer> listID, listCollectionID;
    public static byte[][] main;
    private static ProgressBar pbPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training);
        setMyCustomActionBar();
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        lastIndex = -1;
        curModeID = MODE_TEST;
        setID = getIntent().getIntExtra(EXTRA_SET_ID, 1);
        Cursor c = MainActivity.db.rawQuery("select * from "+DB.TABLE_LIST+
                        " where "+DB.COLUMN_SET+" = "+String.valueOf(setID), null);
        listID = new ArrayList<Integer>();
        listCollectionID = new ArrayList<Integer>();
        if (c.moveToFirst()) {
            do {
                listID.add(c.getInt(c.getColumnIndex(DB.COLUMN_ID_LIST)));
                listCollectionID.add(c.getInt(c.getColumnIndex(DB.COLUMN_COLLECTION_ID)));
            } while (c.moveToNext());
        }
        count = c.getCount();
        c.close();
        shuffleArrays();
        main = new byte[4][2];
        pbPages = (ProgressBar) findViewById(R.id.pbPages);
        myNextFragmentListener.nextFragment();
    }

    private void shuffleArrays() {
        final Random random = new Random();
        for (int i = 0; i < count; i++) {
            int randomIndex = random.nextInt(count);
            int temp = listID.get(i);
            listID.set(i, listID.get(randomIndex));
            listID.set(randomIndex, temp);
            temp = listCollectionID.get(i);
            listCollectionID.set(i, listCollectionID.get(randomIndex));
            listCollectionID.set(randomIndex, temp);
        }
    }

    public static interface OnNextFragmentListener {
        void nextFragment();
        void setTempResult(boolean right, String word, String translation, String fullTransl);
    }

    OnNextFragmentListener myNextFragmentListener = new OnNextFragmentListener() {

        @Override
        public void nextFragment() {
            if ((lastIndex + 1) == count) {
                if (curModeID == MODE_WRITE) {
                    finish();
                    return;
                }
                curModeID++;
                lastIndex = -1;
                shuffleArrays();
            }
            lastIndex++;
            int id = listID.get(lastIndex);
            int colID = listCollectionID.get(lastIndex);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);
            switch (curModeID) {
                case MODE_TEST:
                    TestFragment test = TestFragment.getInstance(id, colID, count);
                    test.setNextFragmentListener(this);
                    ft.replace(R.id.fragContent, test);
                    break;
                case MODE_GATHER:
                    GatherFragment gather = GatherFragment.getInstance(id, colID, count);
                    gather.setNextFragmentListener(this);
                    ft.replace(R.id.fragContent, gather);
                    break;
            }
            ft.addToBackStack(null);
            ft.commit();
            double percent = Double.valueOf(count * curModeID + lastIndex + 1) / Double.valueOf(count * 4);
            percent *= 100;
            pbPages.setProgress((int) percent);
        }

        @Override
        public void setTempResult(boolean right, String word, String translation, String fullTransl) {
            TempResultFragment page = TempResultFragment.getInstance(right, word, translation, fullTransl);
            page.setNextFragmentListener(this);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragContent, page);
            ft.addToBackStack(null);
            ft.commit();
        }
    };

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
    public void onBackPressed() {
        //Blocking back button
    }
}
