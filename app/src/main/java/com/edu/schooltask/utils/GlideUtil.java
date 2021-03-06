package com.edu.schooltask.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.edu.schooltask.R;
import com.edu.schooltask.data.DataCache;

import de.hdodenhof.circleimageview.CircleImageView;
import server.api.SchoolTask;

/**
 * Created by 夜夜通宵 on 2017/5/14.
 */

public class GlideUtil {

    public static void setHead(Context context, String userId, ImageView imageView,
                               boolean skipMemoryCache){
        DataCache dataCache = new DataCache(context);
        int head = dataCache.getData("head");
        Glide.with(context)
                .load(SchoolTask.HEAD_URL + userId + ".png")
                .dontAnimate()
                .signature(new StringSignature(head+""))
                .diskCacheStrategy(skipMemoryCache ? DiskCacheStrategy.NONE : DiskCacheStrategy.ALL)
                .skipMemoryCache(skipMemoryCache)
                .placeholder(R.drawable.head)
                .error(R.drawable.head)
                .into(imageView);
    }

    public static void setBackground(Context context, String userId, ImageView imageView){
        DataCache dataCache = new DataCache(context);
        int bg = dataCache.getData("bg");
        Glide.with(context)
                .load(SchoolTask.BG_URL + userId + ".png")
                .signature(new StringSignature(bg+""))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.background)
                .error(R.drawable.background)
                .into(imageView);
    }
}
