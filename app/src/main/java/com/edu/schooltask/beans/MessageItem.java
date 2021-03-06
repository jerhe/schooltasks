package com.edu.schooltask.beans;

import android.util.Log;

import com.edu.schooltask.beans.Poll;
import com.edu.schooltask.beans.UserInfoBase;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 夜夜通宵 on 2017/5/28.
 */

public class MessageItem extends DataSupport implements Serializable{
    UserInfoBase user;  //消息来源
    String lastMessage; //最新消息
    String lastTime;    //最新消息的时间
    int count;  //未读消息数

    public MessageItem(Poll poll, int type){
        this.user = type == PrivateMessage.SEND ? poll.getToUser() : poll.getFromUser();
        this.lastMessage = poll.getContent();
        this.lastTime = poll.getCreateTime();
        count = type == PrivateMessage.SEND ? 0 : 1;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public UserInfoBase getUser() {
        return user;
    }

    public void setUser(UserInfoBase user) {
        this.user = user;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
