package app.drewromanyk.com.minesweeper.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * Created by drewi_000 on 1/11/2015.
 */
public class VScrollView extends ScrollView {
    private HorizontalScrollView sv;

    public VScrollView(Context context) {
        super(context);
    }

    public VScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSv(HorizontalScrollView sv) {
        this.sv = sv;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        sv.dispatchTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        super.onInterceptTouchEvent(event);
        sv.onInterceptTouchEvent(event);
        return true;
    }
}
