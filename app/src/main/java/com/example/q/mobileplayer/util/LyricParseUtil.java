package com.example.q.mobileplayer.util;

import android.util.Log;

import com.example.q.mobileplayer.bean.Lyric;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Q on 2016/6/4.
 */
public class LyricParseUtil {
    //歌词的集合
    private ArrayList<Lyric> lyricArrayList;
    public void readLyricFile(File file) {
        if (file == null || !file.exists()) {
            //歌词文件不存在
        } else {
            lyricArrayList=new ArrayList<>();
            try {
                BufferedReader BR=new BufferedReader(new InputStreamReader(new FileInputStream(file),"GBK"));
                String line;
                while((line=BR.readLine())!=null){
                    //1.解析歌词
                    line=analyzeLyric(line);
                }
                //2.歌词时间排序
                //3.计算美剧歌词高亮显示时间
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String analyzeLyric(String line) {
        //1.得到左边的第一个括号的位置和右边第一个括号的位置
        int pos1=line.indexOf("[");//0，如果没有就返回-1；
        int pos2=line.indexOf("]");//9,如果没有就返回-1;
        if (pos1==0&&pos2!=-1){
            //定义long 类型的数组装时间戳
            long timePoints[] =new long[getTagContent(line)];
            String contentStr=line.substring(pos1+1,pos2);
            timePoints[0]=timeStrToLong(contentStr);
            if(timePoints[0]==1){
                return"";
            }
            String content=line;//[02:03.01][03:03.01][02:06.01]歌词....
            int i=1;
            //当这个while结束的时候，时间戳都得到了;
            while (pos1==0&&pos2!=-1){
                //[03:03.01][02:06.01]歌词....
                content=content.substring(pos2+1);
                i++;
                pos1=line.indexOf("[");//0，如果没有就返回-1；
                pos2=line.indexOf("]");//9,如果没有就返回-1;
                if(pos2!=-1){
                    contentStr=content.substring(pos1+1,pos2);//[02:06.01]
                    timePoints[i]=timeStrToLong(content);
                    if (timePoints[i]==-1){
                        return "";
                    }
                    i++;
                }
            }
            for (int j=0;j<timePoints.length;j++){
                if (timePoints[j]!=0){

                }
            }
        }
        return "";
    }

    //将02:04.12转换成毫秒
    private long timeStrToLong(String content) {
        //切割字符串
        //1.  02和04.12
        //2.04.12切割成04和12
        long result;
        try {
            String s1[]=content.split(":");
            String s2[]=s1[1].split("\\.");
            //分
            long min= Long.parseLong(s1[0]);
            //秒
            long second=Long.parseLong(s2[0]);
            //10倍毫秒
            long mil=Long.parseLong(s2[1]);//
            result=min*60*1000+second*1000+mil*10;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
        return result;

    }

    //判断有一行多少句歌词，至少要返回1；
    //[02:03.01][03:03.01][02:06.01]歌词....
    public int getTagContent(String line) {
        String left[]=line.split("\\[");
        String right[]=line.split("\\]");
        Log.e("Lyric","left:"+left);
        Log.e("Lyric","right:"+right);
        if (left.length==0&&right.length==0){
                return 1;
        }else if (left.length>right.length){
            return left.length;
        }else {
            return right.length;
        }
    }
}
