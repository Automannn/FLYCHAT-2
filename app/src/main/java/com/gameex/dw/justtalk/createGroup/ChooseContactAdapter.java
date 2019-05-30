package com.gameex.dw.justtalk.createGroup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.publicInterface.RecyclerItemClick;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.model.UserInfo;

public class ChooseContactAdapter extends RecyclerView.Adapter<ChooseContactAdapter.ChooseContactHolder> {
    private static final String TAG = "ChooseContactAdapter";

    private Context mContext;
    private List<UserInfo> mUserInfos;
    private RecyclerItemClick mItemClick;

    private List<Map<Integer, Boolean>> mUserChoosed;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    void setItemClick(RecyclerItemClick itemClick) {
        mItemClick = itemClick;
    }

    ChooseContactAdapter(Context context, List<UserInfo> userInfos) {
        mContext = context;
        mUserInfos = userInfos;
        mUserChoosed = new ArrayList<>();
    }

    @NonNull
    @Override
    public ChooseContactHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.choose_contact_item, viewGroup, false);
        mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userChoosedStr = mPref.getString("user_choosed", null);
        if (userChoosedStr != null) {
            Gson gson = new Gson();
            mUserChoosed = gson.fromJson(userChoosedStr
                    , new TypeToken<List<Map<Integer, Boolean>>>() {
                    }.getType());
        }
        return new ChooseContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChooseContactHolder holder
            , final int position) {

        @SuppressLint("UseSparseArrays") Map<Integer, Boolean> map = new HashMap<>();
        map.put(position, true);
        if (mUserChoosed.contains(map)) {
            holder.userCheck.setImageResource(R.drawable.play_hook_check_vector);
        } else {
            holder.userCheck.setImageResource(R.drawable.play_hook_uncheck_vector);
        }
        UserInfo userInfo = mUserInfos.get(position);
        if (position == 0 || !mUserInfos.get(position - 1).
                getExtra("index").equals(userInfo.getExtra("index"))) {
            holder.index.setVisibility(View.VISIBLE);
            holder.index.setText(userInfo.getExtra("index"));
        } else {
            holder.index.setVisibility(View.GONE);
        }
        if (userInfo.getExtra("icon_uri") != null) {
            Glide.with(mContext)
                    .load(Uri.parse(userInfo.getExtra("icon_uri")))
                    .into(holder.icon);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.icon_user)
                    .into(holder.icon);
        }
        holder.name.setText(TextUtils.isEmpty(userInfo.getNickname())
                ? userInfo.getUserName() : userInfo.getNickname());
    }

    @Override
    public int getItemCount() {
        return mUserInfos == null ? 0 : mUserInfos.size();
    }

    class ChooseContactHolder extends RecyclerView.ViewHolder {
        TextView index;
        LinearLayout layout;
        CircularImageView icon;
        TextView name;
        ImageView userCheck;

        ChooseContactHolder(@NonNull View itemView) {
            super(itemView);
            index = itemView.findViewById(R.id.index_text_contact);
            layout = itemView.findViewById(R.id.contact_info_layout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    @SuppressLint("UseSparseArrays") Map<Integer, Boolean> map = new HashMap<>();
                    map.put(getAdapterPosition(), true);
                    if (mUserChoosed.contains(map)) {
                        mUserChoosed.remove(map);
                        startVectorCheck(userCheck, false);
                    } else {
                        mUserChoosed.add(map);
                        startVectorCheck(userCheck, true);
                    }
                    if (mItemClick != null) {
                        mItemClick.onClick(getAdapterPosition());
                    }
                    mEditor = mPref.edit();
                    Gson gson = new Gson();
                    String userChoosedStr = gson.toJson(mUserChoosed);
                    mEditor.putString("user_choosed", userChoosedStr);
                    mEditor.apply();
                }
            });
            icon = itemView.findViewById(R.id.icon_contact);
            name = itemView.findViewById(R.id.name_contact);
            userCheck = itemView.findViewById(R.id.user_check);
        }
    }

    /**
     * 播放打勾或取消打勾的动画
     *
     * @param check     ImageView
     * @param isChecked 是否选中
     */
    private void startVectorCheck(ImageView check, boolean isChecked) {
        if (isChecked) {
            check.setImageResource(R.drawable.play_hook_check_vector);
        } else {
            check.setImageResource(R.drawable.play_hook_uncheck_vector);
        }
        Drawable drawable = check.getDrawable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable instanceof AnimatedVectorDrawable) {
                ((AnimatedVectorDrawable) drawable).start();
            }
        }
    }
}
