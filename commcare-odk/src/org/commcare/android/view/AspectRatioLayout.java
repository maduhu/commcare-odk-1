package org.commcare.android.view;

import org.commcare.dalvik.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * A layout that will enforce a given aspect ratio. Layout should define the attributes ratio_width and ratio_height.
 * @author jschweers
 */
public class AspectRatioLayout extends FrameLayout {
    float mRatioWidth;
    float mRatioHeight;

    public AspectRatioLayout(Context context) {
        super(context);
    }

    public AspectRatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeRatio(context, attrs);
    }

    public AspectRatioLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeRatio(context, attrs);
    }
    
    private void initializeRatio(Context context, AttributeSet attrs) {
        String namespace = "http://schemas.android.com/apk/lib/" + this.getClass().getPackage().getName();
        mRatioWidth = attrs.getAttributeFloatValue(namespace, "ratio_width", 1);
        mRatioHeight = attrs.getAttributeFloatValue(namespace, "ratio_height", 1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec((int) (widthMeasureSpec * mRatioHeight / mRatioWidth), MeasureSpec.EXACTLY)
        );
    }
}
