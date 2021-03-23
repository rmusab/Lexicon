package kz.develop.lexicon;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.AnimationUtils;

import java.util.Random;

/**
    Author: Cyril Mottier
    Data: November 2012
    Article source: http://cyrilmottier.com/2012/11/27/actionbar-on-the-move/

    Данный класс представляет собой потомка класса Drawable. Класс Drawable имеет при себе
    вложенный(nested, статический) интерфейс Drawable.Callback с абстрактными методами, которые
    должны быть реализованы либо в самом ActionBar или в имеющим к нему доступ Activity.
    Объект(или ссылка this), в котором был реализован данный интерфейс, должен быть передан
    объекту Drawable при помощи метода setCallback(Drawable.Callback). Это даёт возможность
    объекту Drawable отрисовывать себя самого в ActionBar через описанные методы интерфейса в
    ActionBar. Animatable - это интерфейс, который должен быть реализован в любом классе-потомке от
    Drawable, который хочет быть анимацией. Его абстрактные методы: isRunning(), start(), stop().

    scheduleSelf(Runnable what, long when) - это метод, который выполняет Runnable, в котором реализована
    отрисовка, по истечении времени when.
    invalidateSelf() - метод, заново отрисовывает Drawable, используя заранее прикреплённый и описанный
    интерфейс Drawable.Callback.

    Если API>=17(Build.VERSION.SDK_INT), то мы можем просто прикрепить к ActionBar объект класса
    ColorAnimationDrawable в качестве BackgroundDrawable, так как в этом случае ActionBar сам реализует
    интерфейс Drawable.Callback у себя в классе и прикрепит ссылку на себя к объекту ColorAnimationDrawable.
    Если API<17, то нам нужно самим в Activity реализовать объект интерфейса Drawable.Callback и прикрепить
    его к объекту ColorAnimationDrawable.

    Для начала анимации нужно вызвать метод start() реализованного в нашем классе интерфейса Animatable.

    Пример использования в Activity:

     public class MainActivity extends Activity {

     private final Handler mHandler = new Handler();
     private ColorAnimationDrawable mActionBarBackground;

     @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mActionBarBackground = new ColorAnimationDrawable();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
        mActionBarBackground.setCallback(mDrawableCallback);
        } else {
            getActionBar().setBackgroundDrawable(mActionBarBackground);
        }
            mActionBarBackground.start();
     }

     @Override
     protected void onResume() {
        super.onResume();
        mActionBarBackground.start();
     }

     @Override
     protected void onPause() {
        super.onPause();
        mActionBarBackground.stop();
     }

     private Drawable.Callback mDrawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            mHandler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            mHandler.removeCallbacks(what);
        }
     };

     }
 */

public class ColorAnimationDrawable extends Drawable implements Animatable {

    private static final long FRAME_DURATION = 20;
    private static final long ANIMATION_DURATION = 5000;

    private static final int ACCCENT_COLOR = 0x33FFFFFF;
    private static final int DIM_COLOR = 0x33000000;
    private static final int DIM_ALPHA = 51;

    private static final Random mRandom = new Random();

    private final Paint mPaint = new Paint();

    private boolean mIsRunning;

    private int mStartColor;
    private int mEndColor;
    private int mCurrentColor;
    private int mDimColor = DIM_COLOR;

    private long mStartTime;

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();

        mPaint.setColor(mCurrentColor);
        canvas.drawRect(bounds, mPaint);

        mPaint.setColor(ACCCENT_COLOR);
        canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + 1, mPaint);

        mPaint.setColor(mDimColor);
        canvas.drawRect(bounds.left, bounds.bottom - 2, bounds.right, bounds.bottom, mPaint);
    }

    public void setColor(int color) {
        stop();
        mCurrentColor = color;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        stop();
        mCurrentColor = Color.argb(alpha, Color.red(mCurrentColor), Color.green(mCurrentColor), Color.blue(mCurrentColor));
        Double ratio;
        ratio = Double.valueOf(alpha) / Double.valueOf(255);
        int dimAlpha = (int) (ratio * DIM_ALPHA);
        mDimColor = Color.argb(dimAlpha, 0, 0, 0);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        oops("setColorFilter(ColorFilter)");
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void start() {
        if (!isRunning()) {
            mIsRunning = true;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStartColor = randomColor();
            mEndColor = randomColor();

            scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
            invalidateSelf();
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            unscheduleSelf(mUpdater);
            mIsRunning = false;
        }
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    private void oops(String message) {
        throw new UnsupportedOperationException("ColorAnimationDrawable doesn't support " + message);
    }

    private static int randomColor() {
        return mRandom.nextInt() & 0x00FFFFFF;
    }

    private static int evaluate(float fraction, int startValue, int endValue) {
        return (int) (startValue + fraction * (endValue - startValue));
    }

    private final Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            long duration = now - mStartTime;
            if (duration >= ANIMATION_DURATION) {
                mStartColor = mEndColor;
                mEndColor = randomColor();
                mStartTime = now;
                mCurrentColor = mStartColor;
                scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
            } else {
                float fraction = duration / (float) ANIMATION_DURATION;
                //@formatter:off
                mCurrentColor = Color.rgb(
                        evaluate(fraction, Color.red(mStartColor), Color.red(mEndColor)),     // red
                        evaluate(fraction, Color.green(mStartColor), Color.green(mEndColor)), // green
                        evaluate(fraction, Color.blue(mStartColor), Color.blue(mEndColor)));  // blue
                //@formatter:on
                scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
                invalidateSelf();
            }
        }
    };
}
