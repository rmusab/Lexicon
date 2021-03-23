package kz.develop.lexicon;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

/**
 * Created by Rav_4 on 08.07.2014.
 */
public class WordActivity extends FragmentActivity {

    int setID, count;
    public static Cursor cursor;
    ProgressBar pbWordPages;
    ViewPager pager;
    PagerAdapter pagerAdapter;

    private final Handler mHandler = new Handler();
    private ColorAnimationDrawable myColorAnimationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word);
        pbWordPages = (ProgressBar) findViewById(R.id.pbWordPages);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setID = getIntent().getIntExtra(MainActivity.EXTRA_POSITION, -1)+1;
        count = getCount();
        pager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new myFragmentPagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(count - 1);
        pager.setPadding(30, 20, 30, 10);
        pager.setClipToPadding(false);
        pager.setPageMargin(10);
        /*pager.setPageMargin(1);
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setColorFilter(getResources().getColor(R.color.soft_grey), PorterDuff.Mode.SRC_IN);
        pager.setPageMarginDrawable(drawable);*/
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setPageProgress(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        setPageProgress(0);

        setMyCustomActionBar();
    }

    private int getCount() {
        cursor = MainActivity.db.query(DB.TABLE_LIST, new String[] {DB.COLUMN_SET, DB.COLUMN_ID_LIST, DB.COLUMN_COLLECTION_ID}
                , DB.COLUMN_SET + " = ?"
                , new String[] {String.valueOf(setID)}
                ,null,null,null);
        return cursor.getCount();
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

    private void setPageProgress(int position) {
        double val = Double.valueOf(position+1) / Double.valueOf(count);
        val *= 100d;
        pbWordPages.setProgress((int) val);
    }

    class myFragmentPagerAdapter extends FragmentPagerAdapter {

        public myFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return WordFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return count;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.word, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_add_word:
                intent = new Intent(this, WordAddActivity.class);
                intent.putExtra(WordAddActivity.EXTRA_SET_ID, setID);
                startActivityForResult(intent, 0);
                return true;
            case R.id.action_start:
                intent = new Intent(this, TrainingActivity.class);
                intent.putExtra(TrainingActivity.EXTRA_SET_ID, setID);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Intent intent = getIntent();
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) cursor.close();
    }
}
