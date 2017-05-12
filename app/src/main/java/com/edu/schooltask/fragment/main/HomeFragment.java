package com.edu.schooltask.fragment.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.edu.schooltask.R;
import com.edu.schooltask.activity.LoginActivity;
import com.edu.schooltask.activity.ReleaseActivity;
import com.edu.schooltask.adapter.BannerViewPagerAdapter;
import com.edu.schooltask.adapter.HomeAdapter;
import com.edu.schooltask.base.BaseFragment;
import com.edu.schooltask.beans.User;
import com.edu.schooltask.event.GetSchoolOrderEvent;
import com.edu.schooltask.event.LoginSuccessEvent;
import com.edu.schooltask.event.TabSelectedEvent;
import com.edu.schooltask.http.HttpUtil;
import com.edu.schooltask.item.HomeItem;
import com.edu.schooltask.item.OrderItem;
import com.edu.schooltask.other.BannerViewPagerPointer;
import com.edu.schooltask.view.ViewPagerTab;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 夜夜通宵 on 2017/5/3.
 */

public class HomeFragment extends BaseFragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private List<HomeItem> items = new ArrayList<>();
    private List<HomeItem> nearTaskItems = new ArrayList<>();
    private List<HomeItem> twoHandItems = new ArrayList<>();
    private List<HomeItem> jobItems = new ArrayList<>();

    private int type = 0;

    View headView;
    private ViewPagerTab homeTab;   //this
    private ViewPagerTab homeViewPagerTab;  //recyclerview

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
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.home_srl);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSchoolOrder();
            }
        });
        recyclerView = (RecyclerView)view.findViewById(R.id.home_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HomeAdapter(items, mDataCache);
        adapter.bindToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        initBanner();
        initButton();
        initPointer();
        getSchoolOrder();
        twoHandItems.add(new HomeItem(HomeItem.LOAD_TIP, "开发中..."));
        jobItems.add(new HomeItem(HomeItem.LOAD_TIP, "开发中..."));
    }

    private void initBanner(){
        View bannerView = LayoutInflater.from(getContext()).inflate(R.layout.rv_banner,null);
        final ViewPager bannerViewPager = (ViewPager) bannerView.findViewById(R.id.home_banner_vp);
        List<ImageView> bannerViewPagerList = new ArrayList<>();
        bannerViewPagerList.addAll(getBanner(bannerViewPager.getContext()));
        BannerViewPagerAdapter bannerViewPagerAdapter = new BannerViewPagerAdapter(bannerViewPagerList);
        bannerViewPager.setAdapter(bannerViewPagerAdapter);
        bannerViewPager.setCurrentItem(Integer.MAX_VALUE / 2);
        final BannerViewPagerPointer bannerViewPagerPointer = new BannerViewPagerPointer(bannerViewPager.getContext(), bannerViewPager,
                (LinearLayout) bannerView.findViewById(R.id.home_banner_vp_pointer), bannerViewPagerList.size());
        bannerViewPager.addOnPageChangeListener(bannerViewPagerPointer);
        new Timer().schedule(new TimerTask() {  //定时滚动图片
            @Override
            public void run() {
                bannerViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!bannerViewPagerPointer.isDraging)   //不在拖动状态则下一页
                            bannerViewPager.setCurrentItem(bannerViewPager.getCurrentItem()+1);
                    }
                });
            }
        },5000,5000);
        adapter.addHeaderView(bannerView);
    }

    private void initButton(){
        View buttonView = LayoutInflater.from(getContext()).inflate(R.layout.rv_btn,null);
        Button releaseBtn = (Button) buttonView.findViewById(R.id.home_release_btn);
        Button acceptBtn = (Button) buttonView.findViewById(R.id.home_accept_btn);
        releaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(HttpUtil.isNetworkConnected(getContext())){  //网络已连接
                    if(mDataCache.getUser() != null){
                        openActivity(ReleaseActivity.class);
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
        });
        adapter.addHeaderView(buttonView);
    }

    private void initPointer(){
        final View pointerView = LayoutInflater.from(getContext()).inflate(R.layout.rv_pt,null);
        homeViewPagerTab = (ViewPagerTab) pointerView.findViewById(R.id.home_tab);
        homeViewPagerTab.addTab("附近任务");
        homeViewPagerTab.addTab("二手交易");
        homeViewPagerTab.addTab("最新兼职");
        homeViewPagerTab.setSelect(0);
        homeViewPagerTab.setEventBus(true);
        headView  = adapter.getHeaderLayout();

        homeTab = (ViewPagerTab) view.findViewById(R.id.home_tab);
        homeTab.addTab("附近任务");
        homeTab.addTab("二手交易");
        homeTab.addTab("最新兼职");
        homeTab.setSelect(0);
        homeTab.setEventBus(true);


        adapter.addHeaderView(pointerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(pointerView.getY() + headView.getY() <= 0){
                    if(!homeTab.isShown()) homeTab.setVisibility(View.VISIBLE);
                }
                else{
                    if(homeTab.isShown()) homeTab.setVisibility(View.GONE);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void getSchoolOrder(){
        User user = mDataCache.getUser();
        if(user != null){
            swipeRefreshLayout.setRefreshing(true);
            HttpUtil.getSchoolOrder(user.getToken(), user.getSchool());
        }
        else{

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTabSelected(TabSelectedEvent event){
        homeTab.setSelect(event.position);
        homeViewPagerTab.setSelect(event.position);
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
        getSchoolOrder();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetSchoolOrder(GetSchoolOrderEvent event){
        if(swipeRefreshLayout.isRefreshing())swipeRefreshLayout.setRefreshing(false);
        nearTaskItems.clear();
        if (event.isOk()){
            JSONObject jsonObject = event.getData();
            try {
                JSONArray orderArray = jsonObject.getJSONArray("orders");
                JSONArray userArray = jsonObject.getJSONArray("users");
                for(int i=0; i<orderArray.length(); i++){
                    JSONObject userJSON = userArray.getJSONObject(i);
                    User user = new User(userJSON.getString("userid"),userJSON.getString("name"),
                            userJSON.getInt("sex"));
                    JSONObject orderJSON = orderArray.getJSONObject(i);
                    OrderItem orderItem = new OrderItem(orderJSON.getString("orderid"),
                            orderJSON.getString("school"), orderJSON.getString("content"),
                            (float)orderJSON.getDouble("cost"), orderJSON.getString("releasetime"),
                            orderJSON.getInt("imagenum"), user);
                    nearTaskItems.add(new HomeItem(HomeItem.NEAR_TASK_ITEM, orderItem));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            toastShort(event.getError());
            nearTaskItems.add(new HomeItem(HomeItem.LOAD_TIP, "加载失败，请重试"));
        }
        if(type == 0){
            items.clear();
            items.addAll(nearTaskItems);
            adapter.notifyDataSetChanged();
        }

    }

    private List<ImageView> getBanner(Context context){
        List<ImageView> list = new ArrayList<>();
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.background);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        ImageView imageView2 = new ImageView(context);
        imageView2.setImageResource(R.drawable.ic_action_home);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        ImageView imageView3 = new ImageView(context);
        imageView3.setImageResource(R.drawable.ic_action_order);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        list.add(imageView);
        list.add(imageView2);
        list.add(imageView3);
        return list;
    }
}