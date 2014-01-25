package com.riilo.utils;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * an animation for resizing the view.
 */
public class ResizeAnimation extends Animation {
    private View mView;
    private float mToHeight;
    private float mFromHeight;

    public ResizeAnimation(View v, float fromHeight, float toHeight) {
        mToHeight = toHeight;
        mFromHeight = fromHeight;
        mView = v;
        setDuration(300);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float height =
                (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
        LinearLayout.LayoutParams p = (android.widget.LinearLayout.LayoutParams) mView.getLayoutParams();
        p.weight *= 2;
        p.height = (int) height;
        mView.requestLayout();
    }
}