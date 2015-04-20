package zhouq.lrcview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Administrator on 2015/3/1.
 */
public class GradientTextView extends TextView {
    private LinearGradient mLinearGradient;
    private Matrix mGradientMatrix;
    private Paint mPaint;
    private float mViewWidth = 0;
    private float mTranslate = 0;
    final private int UNITIME = 100;

    private boolean isUpdatedContent;
    private float incrementX = 0;

    public void setScrollContent(int duration, String content) {
        this.mDuration = duration;
        this.setText(content);

        isUpdatedContent = true;
        Log.d(TAG, "GradientText : duration = " + duration + ", content = " + content);
        invalidate();
    }

    private int mDuration = 3000;

    private boolean mAnimating = true;

    public GradientTextView(Context context) {
        super(context);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final String TAG = GradientTextView.class.getSimpleName() + "-wang";

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged  :: w = " + w + ", h = " + h + ", oldw = " + oldw + ", oldh = " + oldh);
        Log.d(TAG, "onSizeChanged : getMeasuredWith = " + getMeasuredWidth());

        mPaint = getPaint();
        mLinearGradient = new LinearGradient(-mViewWidth, 0, 0, 0,
                new int[]{Color.YELLOW, Color.YELLOW, Color.WHITE},
                new float[]{0, 0.9f, 1}, Shader.TileMode.CLAMP);

        mPaint.setShader(mLinearGradient);
        mGradientMatrix = new Matrix();


        mViewWidth = w;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isUpdatedContent) {
            isUpdatedContent = false;
            int times = mDuration / UNITIME;
            if (times == 0) {
                incrementX = mViewWidth;
            } else {
                incrementX = mViewWidth / times;
            }
            mTranslate = -mViewWidth;
        }

        if (mAnimating && mGradientMatrix != null) {
            mTranslate += incrementX;
            Log.d(TAG + "-wang", "onDraw : incrementX = " + incrementX + ", mTranslate = " + mTranslate + ", width = " + mViewWidth);

            mGradientMatrix.setTranslate(mTranslate, 0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);

            if (mTranslate < mViewWidth) {
                postInvalidateDelayed(UNITIME);
            }
        }
    }
}
