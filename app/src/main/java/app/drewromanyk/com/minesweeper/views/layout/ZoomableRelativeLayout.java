package app.drewromanyk.com.minesweeper.views.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.GridLayout;
import android.widget.RelativeLayout;

/**
 * Created by drewi_000 on 12/17/2014.
 */
public class ZoomableRelativeLayout extends RelativeLayout {

    float mScaleFactor = 1;
    float mPivotX;
    float mPivotY;
    float MIN_SCALE = .5f;
    float MAX_SCALE = 1.5f;

    public ZoomableRelativeLayout(Context context) {
        super(context);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    protected void dispatchDraw(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleFactor, mScaleFactor, mPivotX, mPivotY);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    public void scale(float scaleFactor, float pivotX, float pivotY) {
        mScaleFactor = scaleFactor;
        mPivotX = pivotX;
        mPivotY = pivotY;
        this.invalidate();
    }

    public void restore() {
        mScaleFactor = 1;
        this.invalidate();
    }

    public void relativeScale(float scaleFactor, float pivotX, float pivotY)
    {
        mScaleFactor *= scaleFactor;

        if(scaleFactor >= 1)
        {
            mPivotX = mPivotX + (pivotX - mPivotX) * (1 - 1 / scaleFactor);
            mPivotY = mPivotY + (pivotY - mPivotY) * (1 - 1 / scaleFactor);
        }
        else
        {
            pivotX = getWidth()/2;
            pivotY = getHeight()/2;

            mPivotX = mPivotX + (pivotX - mPivotX) * (1 - scaleFactor);
            mPivotY = mPivotY + (pivotY - mPivotY) * (1 - scaleFactor);
        }

        this.invalidate();
    }

    public void release()
    {
        if(mScaleFactor < MIN_SCALE)
        {
            final float startScaleFactor = mScaleFactor;

            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t)
                {
                    scale(startScaleFactor + (MIN_SCALE - startScaleFactor)*interpolatedTime,mPivotX,mPivotY);
                }
            };

            a.setDuration(300);
            startAnimation(a);
        }
        else if(mScaleFactor > MAX_SCALE)
        {
            final float startScaleFactor = mScaleFactor;

            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t)
                {
                    scale(startScaleFactor + (MAX_SCALE - startScaleFactor)*interpolatedTime,mPivotX,mPivotY);
                }
            };

            a.setDuration(300);
            startAnimation(a);
        }
    }

}
