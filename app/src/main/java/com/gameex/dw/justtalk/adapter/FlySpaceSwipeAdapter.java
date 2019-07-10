package com.gameex.dw.justtalk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;

import java.util.List;

public class FlySpaceSwipeAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mData;

    public FlySpaceSwipeAdapter(Context context, List<String> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.swipe_item_fly_space, viewGroup, false);
        TextView text = view.findViewById(R.id.text);
        text.setText(mData.get(i));
        return view;
    }
}
