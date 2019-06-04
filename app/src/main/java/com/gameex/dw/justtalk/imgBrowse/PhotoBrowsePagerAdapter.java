package com.gameex.dw.justtalk.imgBrowse;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class PhotoBrowsePagerAdapter extends PagerAdapter {
    private List<String> photoBrowseList;
    private Context mContext;

    PhotoBrowsePagerAdapter(List<String> photoBrowseList, Context context) {
        this.photoBrowseList = photoBrowseList;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return photoBrowseList == null ? 0 : photoBrowseList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        String url = photoBrowseList.get(position);
        final PhotoView photoView = new PhotoView(mContext);
        photoView.enable(); //开启图片缩放功能
        photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);  //设置缩放类型
        photoView.setMaxScale(2.5f);    //设置最大缩放倍数
        Glide.with(mContext)
                .load(url)
                .into(photoView);
        photoView.setOnClickListener(v -> {
            photoView.disenable();
            PhotoBrowseActivity.sPhotoBrowseActivity.finish();
        });
        container.addView(photoView);
        return photoView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
