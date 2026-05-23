package com.example.wordlearningapp.util;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.wordlearningapp.R;
import com.example.wordlearningapp.api.ApiClient;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;

/**
 * 单词图片加载器 - 从服务器加载示意图
 */
public class WordImageLoader {

    private static final String TAG = "WordImageLoader";

    /**
     * 从服务器加载单词图片
     * @param imageView 显示图片的ImageView
     * @param wordImage 数据库中的图片路径（如 "/images/words/abandon.png"）
     * @param noImageTextView "暂无示意图"的TextView（可选）
     */
    public static void loadWordImage(ImageView imageView, String wordImage, android.widget.TextView noImageTextView) {
        if (imageView == null) {
            Log.e(TAG, "ImageView为null");
            return;
        }

        Context context = imageView.getContext();

        if (wordImage == null || wordImage.isEmpty()) {
            imageView.setVisibility(ImageView.GONE);
            if (noImageTextView != null) {
                noImageTextView.setVisibility(android.widget.TextView.VISIBLE);
            }
            Log.d(TAG, "图片路径为空，隐藏ImageView");
            return;
        }

        // 构建完整的图片URL
        String imageUrl = buildImageUrl(wordImage);
        Log.d(TAG, "准备加载图片URL: " + imageUrl);

        // 先设置默认状态
        imageView.setVisibility(ImageView.VISIBLE);
        if (noImageTextView != null) {
            noImageTextView.setVisibility(android.widget.TextView.GONE);
        }

        // 使用 Glide 加载图片，添加监听器处理加载失败的情况
        try {
            Log.d(TAG, "开始Glide加载流程...");

            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .error(R.mipmap.ic_launcher) // 加载失败显示默认图片
                            .placeholder(R.mipmap.ic_launcher) // 加载中显示默认图片
                    )
                    .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "图片加载失败: " + imageUrl);
                            Log.e(TAG, "错误详情: " + (e != null ? e.getMessage() : "null"));
                            if (e != null && e.getRootCauses() != null) {
                                for (Throwable cause : e.getRootCauses()) {
                                    Log.e(TAG, "根本原因: " + cause.getMessage());
                                }
                            }
                            imageView.setVisibility(ImageView.GONE);
                            if (noImageTextView != null) {
                                noImageTextView.setVisibility(android.widget.TextView.VISIBLE);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "图片加载成功: " + imageUrl);
                            imageView.setVisibility(ImageView.VISIBLE);
                            if (noImageTextView != null) {
                                noImageTextView.setVisibility(android.widget.TextView.GONE);
                            }
                            return false;
                        }
                    })
                    .into(imageView);

            Log.d(TAG, "Glide请求已发起");

        } catch (Exception e) {
            Log.e(TAG, "Glide加载异常: " + e.getMessage(), e);
            imageView.setVisibility(ImageView.GONE);
            if (noImageTextView != null) {
                noImageTextView.setVisibility(android.widget.TextView.VISIBLE);
            }
        }
    }

    /**
     * 从服务器加载单词图片（不带noImageTextView的简化版本）
     */
    public static void loadWordImage(ImageView imageView, String wordImage) {
        loadWordImage(imageView, wordImage, null);
    }

    /**
     * 构建完整的图片URL
     * @param relativePath 相对路径（如 "/images/words/abandon.png" 或 "images/words/abandon.png"）
     * @return 完整URL
     */
    private static String buildImageUrl(String relativePath) {
        String baseUrl = ApiClient.get().getBaseUrl();

        // 确保相对路径以 / 开头
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }

        // 拼接完整URL
        String fullUrl = baseUrl + relativePath;

        Log.d(TAG, "构建图片URL: " + fullUrl);
        return fullUrl;
    }

    /**
     * 清除图片缓存（可选功能）
     */
    public static void clearCache(Context context) {
        new Thread(() -> {
            try {
                Glide.get(context).clearDiskCache();
                Log.d(TAG, "磁盘缓存已清除");
            } catch (Exception e) {
                Log.e(TAG, "清除磁盘缓存失败: " + e.getMessage());
            }
        }).start();

        // 清除内存缓存
        Glide.get(context).clearMemory();
        Log.d(TAG, "内存缓存已清除");
    }

    /**
     * 预加载图片到缓存（可选功能）
     */
    public static void preloadImage(Context context, String wordImage) {
        if (wordImage == null || wordImage.isEmpty()) {
            return;
        }

        String imageUrl = buildImageUrl(wordImage);

        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload();

        Log.d(TAG, "预加载图片: " + imageUrl);
    }
}
