package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.holder.ChooseOwnerHolder;
import com.gameex.dw.justtalk.util.UserInfoUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 群管理中群主权限转让功能的群成员列表适配器
 */
public class ChooseOwnerAdapter extends RecyclerView.Adapter<ChooseOwnerHolder> {

    Context mContext;
    List<GroupMemberInfo> mMemberInfos;

    OnItemClick mOnItemClick;

    public ChooseOwnerAdapter() {
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setMemberInfos(List<GroupMemberInfo> memberInfos) {
        mMemberInfos = memberInfos;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        mOnItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ChooseOwnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_choose_owner, parent, false);
        ChooseOwnerHolder holder = new ChooseOwnerHolder(view);
        view.setOnClickListener(view1 -> {
            UserInfo userInfo = mMemberInfos.get((Integer) view1.getTag()).getUserInfo();
            if (mOnItemClick != null) {
                mOnItemClick.onClick(userInfo.getUserName());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseOwnerHolder holder, int position) {
        GroupMemberInfo memberInfo = mMemberInfos.get(position);
        UserInfo userInfo = memberInfo.getUserInfo();
        holder.itemView.setTag(position);
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
        holder.name.setText(memberInfo.getDisplayName());
    }

    @Override
    public int getItemCount() {
        return mMemberInfos == null ? 0 : mMemberInfos.size();
    }

    public interface OnItemClick {
        void onClick(String username);
    }
}
