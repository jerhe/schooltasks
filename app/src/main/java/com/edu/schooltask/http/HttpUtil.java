package com.edu.schooltask.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.edu.schooltask.base.BaseActivity;
import com.edu.schooltask.beans.User;
import com.edu.schooltask.data.DataCache;
import com.edu.schooltask.event.CheckTokenEvent;
import com.edu.schooltask.event.GetCodeEvent;
import com.edu.schooltask.event.GetMoneyEvent;
import com.edu.schooltask.event.GetSchoolOrderEvent;
import com.edu.schooltask.event.GetUserOrderEvent;
import com.edu.schooltask.event.LoginEvent;
import com.edu.schooltask.event.RegisterFinishEvent;
import com.edu.schooltask.event.ReleaseEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 夜夜通宵 on 2017/5/6.
 */

public class HttpUtil {
    private static DataCache mDataCache;
    final static String LOGIN_URL = "http://192.168.191.1:8080/SchoolTaskServer/LoginController/login";
    final static String GET_CODE_URL = "http://192.168.191.1:8080/SchoolTaskServer/RegisterController/get_code";
    final static String REGISTER_FINISH_URL = "http://192.168.191.1:8080/SchoolTaskServer/RegisterController/finish";
    final static String RELEASE_ORDER_URL ="http://192.168.191.1:8080/SchoolTaskServer/OrderController/release";
    final static String CHECK_TOKEN_URL ="http://192.168.191.1:8080/SchoolTaskServer/TokenController/checkToken";
    final static String GET_MONEY_URL ="http://192.168.191.1:8080/SchoolTaskServer/MoneyController/getMoney";
    final static String GET_USER_ORDER_URL ="http://192.168.191.1:8080/SchoolTaskServer/OrderController/getUserOrder";
    final static String GET_SCHOOL_ORDER_URL ="http://192.168.191.1:8080/SchoolTaskServer/OrderController/getSchoolOrder";

    public static void setDataCache(DataCache mDataCache){
        HttpUtil.mDataCache = mDataCache;
    }

    private static OkHttpClient client;
    private static OkHttpClient getInstance(){
        if(client == null){
            client = new OkHttpClient();
        }
        return client;
    }




    public static void login(String userId, String userPwd){
        RequestBody requestBody = new FormBody.Builder()
                .add("userid",userId)
                .add("userpwd",userPwd)
                .build();
        post(LOGIN_URL, requestBody, new BaseCallBack(new LoginEvent()));
    }

    public static void getCode(String userId){
        RequestBody requestBody = new FormBody.Builder()
                .add("userid",userId)
                .build();
        post(GET_CODE_URL, requestBody, new BaseCallBack(new GetCodeEvent()));
    }

    public static void registerFinish(String userId, String school, String name, String pwd){
        RequestBody requestBody = new FormBody.Builder()
                .add("id",userId)
                .add("school",school)
                .add("name",name)
                .add("pwd",pwd)
                .build();
        post(REGISTER_FINISH_URL, requestBody, new BaseCallBack(new RegisterFinishEvent()));
    }

    public static void release(String token, final String userId, final String school, final String title,
                               final String content, final float cost, final int limitTime, final List<String> paths){
        checkToken(token, new HttpCheckToken() {
            @Override
            public void onSuccess() {
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for(int i=0; i < paths.size(); i++) {
                    File f=new File(paths.get(i));
                    if(f!=null) {
                        builder.addFormDataPart("image"+i, f.getName(), RequestBody.create(MediaType.parse("image/png"), f));
                    }
                }
                builder.addFormDataPart("id",userId);
                builder.addFormDataPart("school", school);
                builder.addFormDataPart("title", title);
                builder.addFormDataPart("content", content);
                builder.addFormDataPart("cost", cost+"");
                builder.addFormDataPart("limittime", limitTime+"");
                MultipartBody requestBody = builder.build();
                post(RELEASE_ORDER_URL, requestBody, new BaseCallBack(new ReleaseEvent()));
            }

            @Override
            public void onFailure() {
                EventBus.getDefault().post(new ReleaseEvent(false));
            }
        });
    }

    public static void getMoney(String token, final String id){
        checkToken(token, new HttpCheckToken() {
            @Override
            public void onSuccess() {
                RequestBody requestBody = new FormBody.Builder()
                        .add("userid",id).build();
                post(GET_MONEY_URL, requestBody, new BaseCallBack(new GetMoneyEvent()));
            }

            @Override
            public void onFailure() {

            }
        });

    }

    public static void getUserOrder(String token, final String id, final int pageIndex, final int type){
        checkToken(token, new HttpCheckToken() {
            @Override
            public void onSuccess() {
                RequestBody requestBody = new FormBody.Builder()
                        .add("userid",id)
                        .add("pageindex",pageIndex+"").build();
                post(GET_USER_ORDER_URL, requestBody, new BaseCallBack(new GetUserOrderEvent(type)));
            }

            @Override
            public void onFailure() {
                EventBus.getDefault().post(new GetUserOrderEvent(false, type));
            }
        });
    }

    public static void getSchoolOrder(String token, final String school){
        checkToken(token, new HttpCheckToken() {
            @Override
            public void onSuccess() {
                RequestBody requestBody = new FormBody.Builder()
                        .add("school",school).build();
                post(GET_SCHOOL_ORDER_URL, requestBody, new BaseCallBack(new GetSchoolOrderEvent()));
            }

            @Override
            public void onFailure() {
                EventBus.getDefault().post(new GetSchoolOrderEvent(false));
            }
        });
    }
    //------------------------------------------------------------------------

    public static void checkToken(String token, final HttpCheckToken httpCheckToken){
        RequestBody requestBody = new FormBody.Builder()
                .add("token",token).build();
        post(CHECK_TOKEN_URL, requestBody, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpCheckToken.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    int code = data.getInt("result");
                    switch (code){
                        case 1:
                            User user = mDataCache.getUser();
                            user.setToken(data.getString("token"));
                            mDataCache.saveUser(user);
                        case 0:
                            httpCheckToken.onSuccess();
                            break;
                        case -1:
                            break;
                        case 2:
                            EventBus.getDefault().post(new CheckTokenEvent());
                            break;
                        default:
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void post(String url, RequestBody requestBody, BaseCallBack baseCallBack){
        Request request = new Request.Builder().url(url).post(requestBody).build();
        getInstance().newCall(request).enqueue(baseCallBack);
    }

    public static void post(String url, RequestBody requestBody, okhttp3.Callback callback){
        Request request = new Request.Builder().url(url).post(requestBody).build();
        getInstance().newCall(request).enqueue(callback);
    }

    /**
     * 判断网络连接
     * @param context
     * @return 有网络返回true,无返回false
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}