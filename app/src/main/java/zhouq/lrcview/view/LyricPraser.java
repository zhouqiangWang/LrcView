package zhouq.lrcview.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015/4/9.
 */
public class LyricPraser {

    private final String TAG = "MusicLyric";

    private Context mContext;

    public int getDuration() {
        return mDuration;
    }

    private int mDuration;

    private String mSongPath;

    private List<LrcRow> lrcRows;

    public List<LrcRow> getLrcRows() {
        return lrcRows;
    }

    public LyricPraser(Context mContext, int duration, String songPath) {
        this.mContext = mContext;
        this.mDuration = duration;
        this.mSongPath = songPath;
        lrcRows = new ArrayList<LrcRow>();

        loadLyric();
    }

    private void loadLyric() {
        String lrcPath = mSongPath.substring(0, mSongPath.lastIndexOf(".") + 1)
                + "lrc";
        Log.d(TAG + "-wang", "lrcPath = " + lrcPath);

        // lrc format error, reference : http://developer.android.com/reference/java/nio/charset/Charset.html
        // InputStreamReader default constructor will use System.getProperty("file.encoding", "UTF-8"); for encoding
        // ignore UTF-8 format without BOM...
        try {
            File file = new File(lrcPath);
            FileInputStream lrcInput = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(lrcInput);
            bis.mark(4);
            byte[] first3bytes = new byte[3];
            bis.read(first3bytes,0,2);

            BufferedReader bufferedReader;
            if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE
                    ) {
                // uft-16 little endian BOM is 0xFF, 0xFE
                bis.reset();
                bufferedReader = new BufferedReader(new InputStreamReader
                        (bis,"utf-16le"));
            } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] ==
                    (byte)0xFF){
                // uft-16 big endian BOM is 0xFE, 0xFF
                bis.reset();
                bufferedReader = new BufferedReader(new InputStreamReader
                        (bis,"utf-16be"));
            } else {
                bis.read(first3bytes,0,3);
                if (first3bytes[0] == (byte)0xEF && first3bytes[1] == (byte)
                        0xBB && first3bytes[2] == (byte)0xBF ){
                    // utf-8 BOM is 0xef, 0xbb, 0xbf
                    bis.reset();
                    bufferedReader = new BufferedReader(new InputStreamReader
                            (bis,"utf-8"));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader
                            (bis,"GBK"));
                }
            }

            String lrcLine;
            while ((lrcLine = bufferedReader.readLine()) != null) {
                List<LrcRow> lineRows = CreateLrcRow(lrcLine);
                if (lineRows != null && lineRows.size() > 0) {
                    lrcRows.addAll(lineRows);
                }
            }

            Collections.sort(lrcRows);

            int listLast = lrcRows.size() - 1;
            for (int i = 0; i < listLast; i++) {
                lrcRows.get(i).setTotalTime(lrcRows.get(i + 1).getBeginTime() - lrcRows.get(i).getBeginTime());
            }
            lrcRows.get(listLast).setTotalTime(mDuration - lrcRows.get
                    (listLast).getBeginTime());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将歌词文件中的某一行 解析成一个List<LrcRow>
     * 因为一行中可能包含了多个LrcRow对象.
     * reference : http://baike.baidu.com/subview/80650/8795771.htm
     *
     * @return
     */
    private List<LrcRow> CreateLrcRow(String lrcLine) {
        if (!lrcLine.startsWith("[") || lrcLine.indexOf("]") != 9) {
            return null;
        }
        //最后一个"]"
        int lastIndexOfRightBracket = lrcLine.lastIndexOf("]");
        //歌词内容
        String content = lrcLine.substring(lastIndexOfRightBracket + 1, lrcLine.length());
        content = content.trim();
        //截取出歌词时间，并将"[" 和"]" 替换为"-"   [offset:0]
        // -03:33.02--00:36.37-
        String times = lrcLine.substring(0, lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");
        String[] timesArray = times.split("-");
        List<LrcRow> rowList = new ArrayList<LrcRow>();
        for (String time : timesArray) {
            if (TextUtils.isEmpty(time.trim())) {
                continue;
            }
            //
            try {
                LrcRow lrcRow = new LrcRow(time, formatTime(time), content);
                rowList.add(lrcRow);
            } catch (Exception e) {
                Log.w("LrcRow", e.getMessage());
            }
        }
        return rowList;
    }

    private int formatTime(String timeStr) {
        timeStr = timeStr.replace('.', ':');
        String[] times = timeStr.split(":");

        return Integer.parseInt(times[0]) * 60 * 1000
                + Integer.parseInt(times[1]) * 1000
                + Integer.parseInt(times[2]);
    }

    public class LrcRow implements Comparable<LrcRow> {

        public String getContent() {
            return content;
        }

        String content;

        public String getTimeStr() {
            return beginTimeStr;
        }

        String beginTimeStr;

        public int getBeginTime() {
            return beginTime;
        }

        int beginTime;

        public void setTotalTime(int totalTime) {
            this.totalTime = totalTime;
        }

        public int getTotalTime() {
            return totalTime;
        }

        int totalTime;

        public LrcRow(String beginTimeStr, int beginTime, String content) {
            this.beginTimeStr = beginTimeStr;
            this.beginTime = beginTime;
            this.content = content;
        }

        @Override
        public int compareTo(LrcRow another) {
            return (this.beginTime - another.beginTime);
        }
    }

    public String getLrcByTime(int time) {

        return null;
    }

    public String getLrcByIndex(int index) {

        return lrcRows.get(index).getContent();
    }
}
