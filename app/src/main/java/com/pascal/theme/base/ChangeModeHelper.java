package com.pascal.theme.base;

import android.content.Context;
import android.content.SharedPreferences;

import com.pascal.changemodeSimple.R;


/**
 * 夜间模式辅助类
 */
public class ChangeModeHelper {

    /*
    * 新添主题加在此处.
    * 必须从0开始,否则未设置主题的时候会异常
    * */
    public static enum MODE_INDEX_ENUM{
        MODE_DAY(0),
        MODE_NIGHT(1);

        private int index;
        private MODE_INDEX_ENUM(int index) {
            this.index = index;
        }
        public int getIndex(){
            return index;
        }
    }
    /*
    *必须与上面主题标位一一对应
    *在styles.xml与attrs申明主题信息
    *
    * */
    public static int[] MODE_RES_ID={
            R.style.DayTheme,
            R.style.NightTheme
    };

    private static String Mode = "mode";
    public static void setChangeMode(Context ctx, int mode){
        SharedPreferences sp = ctx.getSharedPreferences("config_mode", Context.MODE_PRIVATE);
        sp.edit().putInt(Mode, mode).commit();
    }
    public static int getChangeMode(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences("config_mode", Context.MODE_PRIVATE);
        return sp.getInt(Mode, MODE_INDEX_ENUM.MODE_DAY.getIndex());
    }
}
