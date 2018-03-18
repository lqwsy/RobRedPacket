package com.example.admin.gurupractice;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

import java.io.File;

/**
 * Created by admin on 2018/3/18.
 */

public class MyGlideMoudle implements GlideModule{

    private static final int DISK_CACHE_SIZE = 100 * 1024 * 1024;
    public static final int MAX_MEMORY_CACHE_SIZE = 10 * 1024 * 1024;
    private final String path = "";//设置缓存路径

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        final File cacheDir = new File(path);
        builder.setDiskCache(new DiskCache.Factory(){
            @Override
            public DiskCache build() {
                return DiskLruCacheWrapper.get(cacheDir,DISK_CACHE_SIZE);
            }
        });
        //设置缓存大小，一般使用glide内部默认值
        builder.setMemoryCache(new LruResourceCache(MAX_MEMORY_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
