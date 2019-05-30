package com.gameex.dw.justtalk.createGroup;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import cn.jpush.im.android.api.model.UserInfo;

public class DoneCreateAdapter extends RecyclerView.Adapter<DoneCreateAdapter.DoneCreateHolder> {

    private Context mContext;
    private List<UserInfo> mUserInfos;

    DoneCreateAdapter(Context context, List<UserInfo> userInfos) {
        mContext = context;
        mUserInfos = userInfos;
    }

    @NonNull
    @Override
    public DoneCreateHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.done_create_item, viewGroup, false);
        return new DoneCreateHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoneCreateHolder holder, int position) {
        UserInfo userInfo = mUserInfos.get(position);
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

    class DoneCreateHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        CircularImageView icon;
        TextView name;

        DoneCreateHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.contact_info_layout);
            icon = itemView.findViewById(R.id.icon_contact);
            name = itemView.findViewById(R.id.name_contact);
        }
    }
}
