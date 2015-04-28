package zhouq.lrcview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.List;

/**
 * Created by Administrator on 2015/4/19.
 */
public class LrcScrollView extends View {

    private final String TAG = "LrcView-wang";

    /**
     * 画高亮歌词的画笔**
     */
    private Paint mPaintForHighLightLrc;
    /**
     * 高亮歌词的默认字体大小**
     */
    private static final float DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC = 35;
    /**
     * 高亮歌词当前的字体大小**
     */
    private float mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC;
    /**
     * 高亮歌词的默认字体颜色*
     */
    private static final int DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC = Color.YELLOW;
    /**
     * 高亮歌词当前的字体颜色*
     */
    private int mCurColorForHightLightLrc = DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC;

    /**
     * 画其他歌词的画笔**
     */
    private Paint mPaintForOtherLrc;
    /**
     * 其他歌词的默认字体大小**
     */
    private static final float DEFAULT_SIZE_FOR_OTHER_LRC = 30;
    /**
     * 其他歌词当前的字体大小**
     */
    private float mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC;
    /**
     * 其他歌词的默认字体颜色*
     */
    private static final int DEFAULT_COLOR_FOR_OTHER_LRC = Color.WHITE;
    private float mCurFraction;
    private OnLrcClickListener onLrcClickListener;
    public void setOnLrcClickListener(OnLrcClickListener onLrcClickListener) {
        this.onLrcClickListener = onLrcClickListener;
    }

    public interface OnLrcClickListener{
        void onClick();
    }

    public void setCurSizeForOtherLine(float mCurSizeForOtherLrc) {
        this.mCurSizeForOtherLrc = mCurSizeForOtherLrc;
    }

    public void setCurColorForHightLight(int mCurColorForHightLightLrc) {
        this.mCurColorForHightLightLrc = mCurColorForHightLightLrc;
    }

    public void setCurSizeForHightLight(float mCurSizeForHightLightLrc) {
        this.mCurSizeForHightLightLrc = mCurSizeForHightLightLrc;
    }

    public void setCurColorForOtherLine(int mCurColorForOtherLrc) {
        this.mCurColorForOtherLrc = mCurColorForOtherLrc;
    }

    /**
     * 其他歌词当前的字体颜色*
     */
    private int mCurColorForOtherLrc = DEFAULT_COLOR_FOR_OTHER_LRC;


    /**
     * 画时间线的画笔**
     */
    private Paint mPaintForTimeLine;
    /**
     * 时间线的颜色*
     */
    private static final int COLOR_FOR_TIME_LINE = 0xffD02090;
    /**
     * 时间文字大小*
     */
    private static final int SIZE_FOR_TIME = 18;
    /**
     * 是否画时间线*
     */
    private boolean mIsDrawTimeLine = false;

    /**
     * 歌词间默认的行距*
     */
    private static final float DEFAULT_PADDING = 20;
    /**
     * 歌词当前的行距*
     */
    private float mCurPadding = DEFAULT_PADDING;

    /**
     * 实现歌词竖直方向平滑滚动的辅助对象*
     */
    private Scroller mScroller;

    /**
     * 移动一句歌词的持续时间*
     */
    private static final int DURATION_FOR_LRC_SCROLL = 1500;
    /**
     * 停止触摸时 如果View需要滚动 时的持续时间*
     */
    private static final int DURATION_FOR_ACTION_UP = 400;
    private int mTotleDrawRow;
    private int mCurRow = -1;
    private int mLastRow = -1;
    private final int mTouchSlop;

    public void setOnSeekToListener(OnSeekToListener onSeekToListener) {
        this.onSeekToListener = onSeekToListener;
    }

    private OnSeekToListener onSeekToListener;

    public interface OnSeekToListener {
        void onSeekTo(int progress);
    }

    public void setHasHighlightScroll(boolean hasHighlightScroll) {
        this.hasHighlightScroll = hasHighlightScroll;
    }

    private boolean hasHighlightScroll;

    public void setLrcRows(List<LyricPraser.LrcRow> lrcRows) {
        reset();
        this.mLrcRows = lrcRows;
        invalidate();
    }

    private List<LyricPraser.LrcRow> mLrcRows;

    public LrcScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // init paint
        mPaintForHighLightLrc = new Paint();
        mPaintForHighLightLrc.setColor(mCurColorForHightLightLrc);
        mPaintForHighLightLrc.setTextSize(mCurSizeForHightLightLrc);

        mPaintForOtherLrc = new Paint();
        mPaintForOtherLrc.setColor(mCurColorForOtherLrc);
        mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);

        mPaintForTimeLine = new Paint();
        mPaintForTimeLine.setColor(COLOR_FOR_TIME_LINE);
        mPaintForTimeLine.setTextSize(SIZE_FOR_TIME);

        mScroller = new Scroller(getContext());
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void reset() {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        mLrcRows = null;
        scrollTo(getScrollX(), 0);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLrcRows == null || mLrcRows.size() <= 0) {
            return;
        }

        if (mTotleDrawRow == 0) {
            //初始化将要绘制的歌词行数
            mTotleDrawRow = (int) (getHeight() / (mCurSizeForOtherLrc + mCurPadding)) + 4;
        }
        //因为不需要将所有歌词画出来
        int minRow = mCurRow - (mTotleDrawRow - 1) / 2;
        int maxRow = mCurRow + (mTotleDrawRow - 1) / 2;
        minRow = Math.max(minRow, 0); //处理上边界
        maxRow = Math.min(maxRow, mLrcRows.size() - 1); //处理下边界
        //实现渐变的最大歌词行数
        int count = Math.max(maxRow - mCurRow, mCurRow - minRow);
        //两行歌词间字体颜色变化的透明度
        int alpha = (0xFF - 0x11) / count;
        //画出来的第一行歌词的y坐标
        float rowY = getHeight() / 2 + minRow * (mCurSizeForOtherLrc + mCurPadding);
        for (int i = minRow; i <= maxRow; i++) {

            if (i == mCurRow) {//画高亮歌词
                if (hasHighlightScroll) {
                    continue;
                }
                //因为有缩放效果，所有需要动态设置歌词的字体大小
                float textSize = mCurSizeForOtherLrc +
                        (mCurSizeForHightLightLrc - mCurSizeForOtherLrc)*mCurFraction;
                mPaintForHighLightLrc.setTextSize(textSize);

                String text = mLrcRows.get(i).getContent();//获取到高亮歌词
                float textWidth = mPaintForHighLightLrc.measureText(text);//用画笔测量歌词的宽度
                if (textWidth > getWidth()) {
                    //暂无 水平滚动
                    canvas.drawText(text, 0, rowY, mPaintForHighLightLrc);
                } else {
                    //如果歌词宽度小于view的宽，则让歌词居中显示
                    float textX = (getWidth() - textWidth) / 2;
                    canvas.drawText(text, textX, rowY, mPaintForHighLightLrc);
                }
            } else {
                if (i == mLastRow) {//画高亮歌词的上一句
                    //因为有缩放效果，所有需要动态设置歌词的字体大小
                    float textSize = mCurSizeForHightLightLrc - (mCurSizeForHightLightLrc - mCurSizeForOtherLrc)*mCurFraction;
                    mPaintForOtherLrc.setTextSize(textSize);
                } else {//画其他的歌词
                    mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);
                }
                String text = mLrcRows.get(i).getContent();
                float textWidth = mPaintForOtherLrc.measureText(text);
                float textX = (getWidth() - textWidth) / 2;
                //如果计算出的textX为负数，将textX置为0(实现：如果歌词宽大于view宽，则居左显示，否则居中显示)
                textX = Math.max(textX, 0);
                //实现颜色渐变  从0xFFFFFFFF 逐渐变为 0x11FFFFFF(颜色还是白色，只是透明度变化)
                int curAlpha = 255 - (Math.abs(i - mCurRow) - 1) * alpha; //求出当前歌词颜色的透明度
                mPaintForOtherLrc.setColor(0x1000000 * curAlpha + 0xffffff);
                canvas.drawText(text, textX, rowY, mPaintForOtherLrc);
            }
            //计算出下一行歌词绘制的y坐标
            rowY += mCurSizeForOtherLrc + mCurPadding;
        }

        //画时间线和时间
        if (mIsDrawTimeLine) {
            Log.d(TAG, "misDrawTimeLine = " + mIsDrawTimeLine + ", mCurRow = " +
                    "" + mCurRow);
            float y = getHeight() / 2 + getScrollY();
            canvas.drawText(mLrcRows.get(mCurRow).getTimeStr(), 0, y - 5, mPaintForTimeLine);
            canvas.drawLine(0, y, getWidth(), y, mPaintForTimeLine);
        }

    }

    /**
     * 是否可拖动歌词*
     */
    private boolean canDrag = false;
    /**
     * 事件的第一次的y坐标*
     */
    private float firstY;
    /**
     * 事件的上一次的y坐标*
     */
    private float lastY;
    private float lastX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstY = event.getRawY();
                lastX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canDrag) {
                    if (Math.abs(event.getRawY() - firstY) > mTouchSlop && Math.abs(event.getRawY() - firstY) > Math.abs(event.getRawX() - lastX)) {
                        canDrag = true;
                        mIsDrawTimeLine = true;
                        mScroller.forceFinished(true);
                        mCurFraction = 1;
                    }
                    lastY = event.getRawY();
                }

                if (canDrag) {
                    float offset = event.getRawY() - lastY;//偏移量
                    if (getScrollY() - offset < 0) {
                        if (offset > 0) {
                            offset = offset / 3;
                        }
                    } else if (getScrollY() - offset > mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding) {
                        if (offset < 0) {
                            offset = offset / 3;
                        }
                    }
                    scrollBy(getScrollX(), -(int) offset);
                    lastY = event.getRawY();
                    int currentRow = (int) (getScrollY() / (mCurSizeForOtherLrc + mCurPadding));
                    currentRow = Math.min(currentRow, mLrcRows.size() - 1);
                    currentRow = Math.max(currentRow, 0);
                    seekTo(currentRow, false, false);
                    return true;
                }
                lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG,"Action_UP ~~ canDrag = "+canDrag);
                if (!canDrag) {
                    if(onLrcClickListener != null){
                        onLrcClickListener.onClick();
                    }
                } else {
                    if (onSeekToListener != null && mCurRow != -1) {
                        onSeekToListener.onSeekTo(mLrcRows.get(mCurRow)
                                .getBeginTime());
                    }
                    if (getScrollY() < 0) {
                        smoothScrollTo(0, DURATION_FOR_ACTION_UP);
                    } else if (getScrollY() > mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding) {
                        smoothScrollTo((int) (mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding), DURATION_FOR_ACTION_UP);
                    }

                    canDrag = false;
                    mIsDrawTimeLine = false;
                    invalidate();
                }
                break;
        }
        return true;
    }

    public void seekTo(int currentRow, boolean fromSeekBar, boolean
            fromSeekBarByUser) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }
        //如果是由seekbar的进度改变触发 并且这时候处于拖动状态，则返回
        if (fromSeekBar && canDrag) {
            return;
        }
        if (mCurRow != currentRow) {
            mLastRow = mCurRow;
            mCurRow = currentRow;
            Log.d("LrcView-wang", "mCurRow=i=" + mCurRow);
            if (fromSeekBarByUser) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                scrollTo(getScrollX(), (int) (mCurRow * (mCurSizeForOtherLrc + mCurPadding)));
            } else {
                smoothScrollTo((int) (mCurRow * (mCurSizeForOtherLrc + mCurPadding)), DURATION_FOR_LRC_SCROLL);
            }
            invalidate();
        }

    }

    /**
     * 平滑的移动到某处
     *
     * @param dstY
     */
    private void smoothScrollTo(int dstY, int duration) {
        int oldScrollY = getScrollY();
        int offset = dstY - oldScrollY;
        mScroller.startScroll(getScrollX(), oldScrollY, getScrollX(), offset, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                int oldY = getScrollY();
                int y = mScroller.getCurrY();
                if (oldY != y && !canDrag) {
                    scrollTo(getScrollX(), y);
                }
                mCurFraction = mScroller.timePassed()*3f/DURATION_FOR_LRC_SCROLL;
                mCurFraction = Math.min(mCurFraction, 1F);
                invalidate();
            }
        }
    }

}
