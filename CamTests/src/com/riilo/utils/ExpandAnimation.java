package com.riilo.utils;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class ExpandAnimation extends Animation {

    private final float mStartWeight;
    private final float mDeltaWeight;
    private LinearLayout mContent;

    public ExpandAnimation(LinearLayout content, float startWeight, float endWeight) {
    	this.mContent = content;
        mStartWeight = startWeight;
        mDeltaWeight = endWeight - startWeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContent.getLayoutParams();
        lp.weight = (mStartWeight + (mDeltaWeight * interpolatedTime));
        mContent.setLayoutParams(lp);
        mContent.requestLayout();
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}