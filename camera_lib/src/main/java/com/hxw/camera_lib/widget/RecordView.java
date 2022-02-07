package com.hxw.camera_lib.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.hxw.camera_lib.R;

public class RecordView extends View {
    private final int mStrokeWidth;
    private final int mMaxDuration;
    private int mWidth;
    private int mHeight;
    private int mRadius;
    private long mProgressValue = 0;
    private RectF mArcRectF;
    private final Paint mCentreWhitePaint;
    private final Paint mCentreRedPaint;
    private final Paint mEdgePaint;
    private final Paint mProgressPaint;

    public int getMaxDuration() {
        return mMaxDuration;
    }

    public void updateProgress(long second) {
        if (second > mMaxDuration) {
            return;
        }
        mProgressValue = second;
        postInvalidate();
    }

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView);
        int strokeColor = typedArray.getColor(R.styleable.RecordView_stroke_color, Color.WHITE);
        mStrokeWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordView_stroke_width, dp2px(3));
        mMaxDuration = typedArray.getInteger(R.styleable.RecordView_max_second, 10);
        typedArray.recycle();

        mCentreRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCentreRedPaint.setStyle(Paint.Style.FILL);
        mCentreRedPaint.setColor(Color.RED);

        mCentreWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCentreWhitePaint.setStyle(Paint.Style.FILL);
        mCentreWhitePaint.setColor(Color.WHITE);

        mEdgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEdgePaint.setStyle(Paint.Style.FILL);
        mEdgePaint.setColor(Color.GRAY);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setColor(strokeColor);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = w;
        mRadius = Math.min(mWidth, mHeight) / 2;
        mArcRectF = new RectF(mStrokeWidth / 2f, mStrokeWidth / 2f,
                mWidth - mStrokeWidth / 2f, mHeight - mStrokeWidth / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius, mEdgePaint);
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius - dp2px(8), mCentreWhitePaint);
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, dp2px(10), mCentreRedPaint);
        if (mProgressValue != 0) {
            float sweepAngle = 360f * mProgressValue / mMaxDuration;
            canvas.drawArc(mArcRectF, -90, sweepAngle, false, mProgressPaint);
        }
    }

    public static int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
