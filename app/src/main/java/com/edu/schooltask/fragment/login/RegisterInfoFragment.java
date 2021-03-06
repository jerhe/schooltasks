package com.edu.schooltask.fragment.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.edu.schooltask.R;
import com.edu.schooltask.activity.SetPayPwdActivity;
import com.edu.schooltask.base.BaseFragment;
import com.edu.schooltask.beans.UserInfo;
import com.edu.schooltask.event.LoginSuccessEvent;
import com.edu.schooltask.event.RegisterNextEvent;
import com.edu.schooltask.filter.NameFilter;
import com.edu.schooltask.filter.PasswordFilter;
import com.edu.schooltask.filter.SchoolFilter;
import com.edu.schooltask.other.SchoolAutoComplement;
import com.edu.schooltask.utils.GsonUtil;
import com.edu.schooltask.utils.KeyBoardUtil;
import com.edu.schooltask.utils.StringUtil;
import com.edu.schooltask.utils.UserUtil;
import com.edu.schooltask.view.InputText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import server.api.SchoolTask;
import server.api.register.RegisterEvent;

/**
 * Created by 夜夜通宵 on 2017/5/3.
 */

public class RegisterInfoFragment extends BaseFragment {
    @BindView(R.id.ri_school) InputText schoolText;
    @BindView(R.id.ri_name) InputText nameText;
    @BindView(R.id.ri_pwd) InputText pwdText;
    @BindView(R.id.ri_finish) Button finishBtn;

    @OnClick(R.id.ri_finish)
    public void register(){
        String school = schoolText.getText();
        String name = nameText.getText();
        String pwd = pwdText.getText();

        int emptyIndex = StringUtil.isEmpty(school, name, pwd);
        if(emptyIndex != -1){
            String[] strings = {"学校", "姓名", "密码"};
            toastShort("请输入" + strings[emptyIndex]);
            return;
        }
        if(StringUtil.checkLength(pwd, 6)){
            toastShort("密码长度至少为6位");
            return;
        }

        KeyBoardUtil.hideKeyBoard(getActivity());
        if(TextUtils.isEmpty(id)){
            toastShort("发生错误");
            return;
        }
        SchoolTask.register(id, name, school, pwd);
    }

    private String id;

    public RegisterInfoFragment() {
        super(R.layout.fragment_register_info);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void init(){
        ButterKnife.bind(this, view);

        //设置过滤
        schoolText.setInputFilter(new SchoolFilter());
        nameText.setInputFilter(new NameFilter());
        pwdText.setInputFilter(new PasswordFilter());

        schoolText.getInputText().addTextChangedListener(
                new SchoolAutoComplement(schoolText.getInputText(), mDataCache.getSchool()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegister(RegisterEvent event){
        if (event.isOk()){
            UserInfo registerUser = GsonUtil.toUserInfo(event.getData());
            UserUtil.saveLoginUser(registerUser);
            EventBus.getDefault().post(new LoginSuccessEvent());
            toastShort("注册成功");
            openActivity(SetPayPwdActivity.class);  //注册成功进入设置支付密码界面
            finish();
        }
        else{
            toastShort(event.getError());
        }
    }

    //下一步事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void registerNextEvent(RegisterNextEvent event){
        this.id = event.getId();
    }
}
