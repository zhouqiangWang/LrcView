package zhouq.lrcview.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;


import java.util.List;

import zhouq.lrcview.R;

/**
 * Created by Administrator on 2015/4/10.
 */
public class LrcView extends FrameLayout {

    private Context mContext;

    private LyricPraser mLyricPraser;
    private List<LyricPraser.LrcRow> mLrcRows;

    private LrcScrollView otherLrcView;

    private GradientTextView highLightText;

    /**
     * Support GradientTextView or not.
     */
    private boolean hasHighlightScroll;

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        int highTextSize, highTextColor, otherTextSize, otherTextColor;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LrcView);

        highTextColor = a.getColor(R.styleable.LrcView_highlight_text_color,
                Color.YELLOW);
        highTextSize = (int) a.getDimension(R.styleable
                .LrcView_highlight_text_size, 40);
        otherTextColor = a.getColor(R.styleable.LrcView_otherline_text_color,
                Color.WHITE);
        otherTextSize = (int) a.getDimension(R.styleable
                .LrcView_otherline_text_size,30);

        hasHighlightScroll = a.getBoolean(R.styleable.LrcView_highlight_scroll,
                true);

        LayoutInflater.from(context).inflate(R.layout.lrc_view, this);
        otherLrcView = (LrcScrollView) findViewById(R.id.lrc_scroll_view);
        highLightText = (GradientTextView) findViewById(R.id.gradient_text);

        otherLrcView.setCurColorForHightLight(highTextColor);
        otherLrcView.setCurColorForOtherLine(otherTextColor);
        otherLrcView.setCurSizeForHightLight(highTextSize);
        otherLrcView.setCurSizeForOtherLine(otherTextSize);

        otherLrcView.setHasHighlightScroll(hasHighlightScroll);
        if (!hasHighlightScroll) {
            highLightText.setVisibility(View.GONE);
        }

    }

    public void setSupportHighlightScroll(boolean supportHighlightScroll) {
        this.hasHighlightScroll = supportHighlightScroll;
    }

    public void reset() {
        if (hasHighlightScroll) {
            highLightText.setText("No Lyric");
        }
        otherLrcView.reset();
    }

    public void setOnSeekToListener(LrcScrollView.OnSeekToListener onSeekToListener) {
        otherLrcView.setOnSeekToListener(onSeekToListener);
    }

    private int mCurIndex = -1;

    public void seekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }

        for (int i = mLrcRows.size() - 1; i >= 0; i--) {

            if (progress >= mLrcRows.get(i).getBeginTime()) {
                if (mCurIndex != i) {
                    mCurIndex = i;
                    Log.d("LrcView", "mCurIndex" + mCurIndex);

                    otherLrcView.seekTo(mCurIndex, fromSeekBar, fromSeekBarByUser);
                    if (hasHighlightScroll) {
                        highLightText.setScrollContent(mLrcRows.get(i).getTotalTime()
                                , mLrcRows.get(i).getContent());
                    }

                }
                break;
            }
        }

    }

    public void setLrcContent(int duration, String path) {
        mLyricPraser = new LyricPraser(mContext, duration, path);

        mLrcRows = mLyricPraser.getLrcRows();
        otherLrcView.setLrcRows(mLrcRows);
        invalidate();
    }

}
