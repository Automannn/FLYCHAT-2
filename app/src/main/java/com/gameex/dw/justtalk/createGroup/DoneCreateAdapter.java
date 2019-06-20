package com.gameex.dw.justtalk.createGroup;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        View view = inflater.inflate(R.layout.recycler_item_done_create, viewGroup, false);
        return new DoneCreateHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoneCreateHolder holder, int position) {
        UserInfo userInfo = mUserInfos.get(position);
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
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
