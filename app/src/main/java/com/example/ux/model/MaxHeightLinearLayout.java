package com.example.ux.model;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.example.ux.R;

public class MaxHeightLinearLayout extends LinearLayout {
    private final int maxHeight; // 최대 높이 (단위: px)

    public MaxHeightLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightLinearLayout);

        // XML에서 maxHeight 속성을 읽어와서 px 단위로 변환
        // 예를 들어, attrs에서 maxHeight를 dp 단위로 받아와서 px로 변환
        maxHeight = a.getDimensionPixelSize(R.styleable.MaxHeightLinearLayout_maxHeight, 0);
        a.recycle(); // 리소스 해제
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(heightSize, maxHeight), MeasureSpec.AT_MOST);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
