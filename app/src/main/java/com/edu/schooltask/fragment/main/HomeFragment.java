package com.edu.schooltask.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.edu.schooltask.R;
import com.edu.schooltask.activity.LoginActivity;
import com.edu.schooltask.activity.ReleaseTaskActivity;
import com.edu.schooltask.activity.TaskListActivity;
import com.edu.schooltask.activity.WaitAcceptOrderActivity;
import com.edu.schooltask.adapter.HomeAdapter;
import com.edu.schooltask.base.BaseActivity;
import com.edu.schooltask.base.BaseFragment;
import com.edu.schooltask.beans.UserInfo;
import com.edu.schooltask.event.LoginSuccessEvent;
import com.edu.schooltask.event.LogoutEvent;
import com.edu.schooltask.event.TabSelectedEvent;
import com.edu.schooltask.other.GlideImageLoader;
import com.edu.schooltask.utils.GsonUtil;
import com.edu.schooltask.utils.NetUtil;
import com.edu.schooltask.item.HomeItem;
import com.edu.schooltask.item.TaskItem;
import com.edu.schooltask.utils.UserUtil;
import com.edu.schooltask.view.CustomLoadMoreView;
import com.edu.schooltask.view.ViewPagerTab;
import com.youth.banner.Banner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import server.api.SchoolTask;
import server.api.task.get.GetSchoolTaskEvent;

/**
 * Created by 夜夜通宵 on 2017/5/3.
 */

public class HomeFragment extends BaseFragment {
    @BindView(R.id.home_srl) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.home_rv) RecyclerView recyclerView;
    @BindView(R.id.home_tab) ViewPagerTab homeTab;

    private LinearLayoutManager linearLayoutManager;
    private HomeAdapter adapter;
    private List<HomeItem> items = new ArrayList<>();
    private List<HomeItem> nearTaskItems = new ArrayList<>();
    private List<HomeItem> twoHandItems = new ArrayList<>();
    private List<HomeItem> jobItems = new ArrayList<>();

    private int type = 0;   //0.附近任务 1.二手交易 2.最新兼职
    private int nearTaskPageIndex = 0;
    private int twoHandPageIndex = 0;
    private int jobPageIndex = 0;


    View headView;
    private ViewPagerTab homeViewPagerTab;
    View pointerView;

    List<String> images = new ArrayList<>();

    public HomeFragment() {
        super(R.layout.fragment_home_page);
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
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new HomeAdapter(items, mDataCache, (BaseActivity)getActivity());
        adapter.setLoadMoreView(new CustomLoadMoreView());
        adapter.bindToRecyclerView(recyclerView);
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                switch (type){
                    case 0:
                        getSchoolTask();
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        },recyclerView);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                HomeItem item = items.get(position);
                switch (item.getItemType()){
                    case 1:
                        Intent intent = new Intent(getActivity(), WaitAcceptOrderActivity.class);
                        intent.putExtra("task", item);
                        startActivity(intent);
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearList();
                getSchoolTask();
            }
        });

        initBanner();
        initButton();
        initPointer();
        getSchoolTask();
        twoHandItems.add(new HomeItem(HomeItem.LOAD_TIP, "开发中..."));
        jobItems.add(new HomeItem(HomeItem.LOAD_TIP, "开发中..."));
    }

    private void initBanner(){
        View bannerView = LayoutInflater.from(getContext()).inflate(R.layout.rv_banner,null);
        Banner banner = (Banner) bannerView.findViewById(R.id.banner);
        banner.setImageLoader(new GlideImageLoader());
        images.add("http://oqqzw04zt.bkt.clouddn.com/banner1.jpg");
        images.add("http://oqqzw04zt.bkt.clouddn.com/banner1.jpg");
        images.add("http://oqqzw04zt.bkt.clouddn.com/banner1.jpg");
        banner.setImages(images);
        banner.start();
        adapter.addHeaderView(bannerView);
    }

    private void initButton(){
        View buttonView = LayoutInflater.from(getContext()).inflate(R.layout.rv_btn,null);
        ImageView releaseBtn = (ImageView) buttonView.findViewById(R.id.home_task_release);
        ImageView taskListBtn = (ImageView) buttonView.findViewById(R.id.home_task_list);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetUtil.isNetworkConnected(getContext())){  //网络已连接
                    if(UserUtil.hasLogin()){
                        switch (v.getId()){
                            case R.id.home_task_release:
                                openActivity(ReleaseTaskActivity.class);
                                break;
                            case R.id.home_task_list:
                                openActivity(TaskListActivity.class);
                                break;
                        }
                    }
                    else{
                        toastShort("请先登录");
                        openActivity(LoginActivity.class);
                    }
                }
                else{
                    toastShort("请检查网络连接");
                }
            }
        };
        releaseBtn.setOnClickListener(listener);
        taskListBtn.setOnClickListener(listener);
        adapter.addHeaderView(buttonView);
    }

    private void initPointer(){
        //RecyclerView中的tab
        pointerView = LayoutInflater.from(getContext()).inflate(R.layout.rv_pt,null);
        homeViewPagerTab = (ViewPagerTab) pointerView.findViewById(R.id.home_tab);
        homeViewPagerTab.addTab("附近任务");
        homeViewPagerTab.addTab("二手交易");
        homeViewPagerTab.addTab("最新兼职");
        homeViewPagerTab.setEventBus(true);
        homeViewPagerTab.select(0);
        headView  = adapter.getHeaderLayout();

        //悬浮的tab
        homeTab.addTab("附近任务");
        homeTab.addTab("二手交易");
        homeTab.addTab("最新兼职");
        homeTab.setEventBus(true);
        homeTab.select(0);

        adapter.addHeaderView(pointerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(pointerView.getY() + headView.getY() <= 0){
                    if(!homeTab.isShown()) homeTab.setVisibility(View.VISIBLE);
                }
                else{
                    if(homeTab.isShown()) homeTab.setVisibility(View.GONE);
                }
            }
        });
    }

    private void getSchoolTask(){
        UserInfo user = UserUtil.getLoginUser();
        if(user != null){
            SchoolTask.getSchoolTask(user.getSchool(), nearTaskPageIndex);
        }
        else{   //用户未登录则获取最新任务
            SchoolTask.getSchoolTask("*", nearTaskPageIndex);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTabSelected(TabSelectedEvent event){
        recyclerView.smoothScrollBy(0, (int)(pointerView.getY() + headView.getY()));
        homeTab.select(event.position);
        homeViewPagerTab.select(event.position);
        items.clear();
        type = event.position;
        switch (type){
            case 0:
                items.addAll(nearTaskItems);
                break;
            case 1:
                items.addAll(twoHandItems);
                break;
            case 2:
                items.addAll(jobItems);
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginSuccess(LoginSuccessEvent event){
        clearList();
        getSchoolTask();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogout(LogoutEvent event){
        clearList();
        getSchoolTask();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetSchoolTask(GetSchoolTaskEvent event){
        if(refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
        if (event.isOk()){
            nearTaskPageIndex ++;
            List<TaskItem> taskItems = GsonUtil.toTaskItemList(event.getData());
            for(TaskItem taskItem : taskItems){
                HomeItem homeItem = new HomeItem(HomeItem.TASK_ITEM, taskItem);
                if(!nearTaskItems.contains(homeItem)){
                    nearTaskItems.add(homeItem);
                }
            }
            adapter.loadMoreComplete();
            if(taskItems.size() == 0){
                adapter.loadMoreEnd();
            }
            adapter.notifyDataSetChanged();
        }
        else{
            adapter.loadMoreFail();
            toastShort(event.getError());
            if(nearTaskItems.size() == 0)
                nearTaskItems.add(new HomeItem(HomeItem.LOAD_TIP, "加载失败，请重试"));
        }
        //Tab选项
        if(type == 0){
            items.clear();
            items.addAll(nearTaskItems);
            adapter.notifyDataSetChanged();
        }

    }

    private void clearList(){
        nearTaskPageIndex = 0;
        twoHandPageIndex = 0;
        jobPageIndex = 0;
        nearTaskItems.clear();
        twoHandItems.clear();
        jobItems.clear();
    }
}
