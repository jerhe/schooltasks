package com.edu.schooltask.utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 夜夜通宵 on 2017/5/9.
 */

public class KeyBoardUtil {

    //弹出输入法
    public static void showKeyBoard(final EditText editText){
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            public void run(){
                InputMethodManager inputManager = (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 100);
    }

    //隐藏输入法
    public static void hideKeyBoard(Activity activity){
        InputMethodManager imm =  (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(),0);}
    }
}
