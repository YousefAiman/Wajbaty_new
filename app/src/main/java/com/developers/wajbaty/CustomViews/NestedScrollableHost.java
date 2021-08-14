package com.developers.wajbaty.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

public class NestedScrollableHost extends FrameLayout {

    private int touchSlop = 0;
    private float initialX = 0.0f;
    private float initialY = 0.0f;

    public NestedScrollableHost(@NonNull Context context) {
        super(context);
        this.init();
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private ViewPager2 parentViewPager() {
        View v = (View) this.getParent();
        while (v != null && !(v instanceof ViewPager2))
            v = (View) v.getParent();
        return (ViewPager2) v;
    }

    private View child() {
        return (this.getChildCount() > 0 ? this.getChildAt(0) : null);
    }

    private void init() {
        this.touchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();
    }

    private boolean canChildScroll(int orientation, Float delta) {
        int direction = (int) (Math.signum(-delta));
        View child = this.child();

        if (child == null)
            return false;

        if (orientation == 0)
            return child.canScrollHorizontally(direction);
        if (orientation == 1)
            return child.canScrollVertically(direction);

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        this.handleInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private void handleInterceptTouchEvent(MotionEvent ev) {
        ViewPager2 vp = this.parentViewPager();
        if (vp == null)
            return;

        int orientation = vp.getOrientation();

        // Early return if child can't scroll in same direction as parent
        if (!this.canChildScroll(orientation, -1.0f) && !this.canChildScroll(orientation, 1.0f))
            return;

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            this.initialX = ev.getX();
            this.initialY = ev.getY();
            this.getParent().requestDisallowInterceptTouchEvent(true);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = ev.getX() - this.initialX;
            float dy = ev.getY() - this.initialY;
            boolean isVpHorizontal = (orientation == ViewPager2.ORIENTATION_HORIZONTAL);

            // assuming ViewPager2 touch-slop is 2x touch-slop of child
            float scaleDx = Math.abs(dx) * (isVpHorizontal ? 0.5f : 1.0f);
            float scaleDy = Math.abs(dy) * (isVpHorizontal ? 1.0f : 0.5f);

            if (scaleDx > this.touchSlop || scaleDy > this.touchSlop) {
                if (isVpHorizontal == (scaleDy > scaleDx)) {
                    // Gesture is perpendicular, allow all parents to intercept
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    // Gesture is parallel, query child if movement in that direction is possible
                    // Child cannot scroll, allow all parents to intercept
                    this.getParent().requestDisallowInterceptTouchEvent(this.canChildScroll(orientation, (isVpHorizontal ? dx : dy)));
                }
            }
        }
    }
}