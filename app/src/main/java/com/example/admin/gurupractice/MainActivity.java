package com.example.admin.gurupractice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class MainActivity extends AppCompatActivity {

    private ImageView netImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        netImage = findViewById(R.id.glideView);
    }

    /**
     * glide网络加载图片
     * */
    public void loadImage(View view){
        Log.i("glide","loadImage");
        String url = "http://img.blog.csdn.net/20161116231741273";
        Glide.with(this)
                .load(url)//可以是网络url，本地图片，应用资源，二进制流，uri对象等
                .placeholder(R.drawable.loading)//添加占位符
                .diskCacheStrategy(DiskCacheStrategy.NONE)//禁用缓存功能
                .into(netImage);//要显示的imageview对象
    }
}
