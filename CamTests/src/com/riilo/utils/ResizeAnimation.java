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


/*public class ResizeAnimation extends Animation {
	View view;
	int startH;
	int endH;
	int diff;

	public ResizeAnimation(View v) {
		view = v;
		startH = v.getLayoutParams().height;
		endH = startH*2;
		diff = endH - startH;
		setDuration(500);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		view.getLayoutParams().height = startH + (int) (diff * interpolatedTime);
		view.requestLayout();
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	public boolean willChangeBounds() {
		return true;
	}
}*/