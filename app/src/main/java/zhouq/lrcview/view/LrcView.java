package zhouq.lrcview.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.TextView;


import java.util.List;

import zhouq.lrcview.R;

/**
 * Created by Administrator on 2015/4/10.
 */
public class LrcView extends FrameLayout{

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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LrcView);

        hasHighlightScroll = a.getBoolean(R.styleable.LrcView_highlight_scroll,
                true);

        LayoutInflater.from(context).inflate(R.layout.lrc_view,this);
        otherLrcView = (LrcScrollView) findViewById(R.id.lrc_scroll_view);
        highLightText = (GradientTextView) findViewById(R.id.gradient_text);

        if (!hasHighlightScroll){
            highLightText.setVisibility(View.GONE);
        }

    }

    public void setSupportHighlightScroll(boolean supportHighlightScroll) {
        this.hasHighlightScroll = supportHighlightScroll;
    }

    public void reset(){
        if (hasHighlightScroll){
            highLightText.setText("No Lyric");
        }
        otherLrcView.reset();
    }

    public void setOnSeekToListener(LrcScrollView.OnSeekToListener onSeekToListener) {
        otherLrcView.setOnSeekToListener(onSeekToListener);
    }

    private int mCurIndex;
    public void seekTo(int progress,boolean fromSeekBar,boolean fromSeekBarByUser) {
        if(mLrcRows == null || mLrcRows.size() == 0){
            return;
        }

        for (int i = mLrcRows.size()-1; i >= 0; i--) {

            if(progress >= mLrcRows.get(i).getBeginTime()){
                if(mCurIndex != i){
                    mCurIndex = i;
                    Log.d("LrcView", "mCurIndex" + mCurIndex);

                    otherLrcView.seekTo(mCurIndex, fromSeekBar, fromSeekBarByUser);
                    if (hasHighlightScroll){
                        highLightText.setScrollContent(mLrcRows.get(i).getTotalTime()
                                ,mLrcRows.get(i).getContent());
                    }

                }
                break;
            }
        }

    }

    public void setLrcContent(int duration, String path){
        mLyricPraser = new LyricPraser(mContext, duration, path);

        mLrcRows = mLyricPraser.getLrcRows();
        otherLrcView.setLrcRows(mLrcRows);
        invalidate();
    }

}
