package com.edu.schooltask.beans.task;

import com.edu.schooltask.beans.UserInfo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by 夜夜通宵 on 2017/5/19.
 */

public class TaskItem extends TaskInfo implements Serializable{

    UserInfo userInfo;
    String releaseTime;
    String limitTime;

    public TaskItem(){}

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(String limitTime) {
        this.limitTime = limitTime;
    }
}