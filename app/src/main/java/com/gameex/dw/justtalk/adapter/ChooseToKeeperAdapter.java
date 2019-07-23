package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.holder.ChooseToKeeperHolder;
import com.gameex.dw.justtalk.util.UserInfoUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 设置群管理的管理员列表适配器
 */
public class ChooseToKeeperAdapter extends RecyclerView.Adapter<ChooseToKeeperHolder> {

    private Context mContext;
    private List<GroupMemberInfo> mMemberInfos;

    private OnItemClick mItemClick;
    private List<Integer> mPositions = new ArrayList<>();

    public ChooseToKeeperAdapter() {
    }

    public ChooseToKeeperAdapter(Context context, List<GroupMemberInfo> memberInfos) {
        mContext = context;
        mMemberInfos = memberInfos;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setMemberInfos(List<GroupMemberInfo> memberInfos) {
        mMemberInfos = memberInfos;
    }

    public void setItemClick(OnItemClick itemClick) {
        mItemClick = itemClick;
    }

    @NonNull
    @Override
    public ChooseToKeeperHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_choose_to_keeper, parent, false);
        ChooseToKeeperHolder holder = new ChooseToKeeperHolder(view);
        view.setOnClickListener(view1 -> {
            int position = (int) view1.getTag();
            UserInfo userInfo = mMemberInfos.get(position).getUserInfo();
            if (mItemClick != null) {
                if (mPositions.contains(position)) {
                    mPositions.remove(position);
                    holder.isCheck.setImageResource(R.drawable.icon_unchecked);
                } else {
                    holder.isCheck.setImageResource(R.drawable.icon_checked);
                    mPositions.add(position);
                }
                mItemClick.OnClick(userInfo);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseToKeeperHolder holder, int position) {
        GroupMemberInfo memberInfo = mMemberInfos.get(position);
        if (memberInfo.getType() == GroupMemberInfo.Type.group_member) {
            holder.itemView.setVisibility(View.VISIBLE);
            UserInfo userInfo = memberInfo.getUserInfo();
            holder.itemView.setTag(position);
            UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
            holder.name.setText(memberInfo.getDisplayName());
            if (mPositions.contains(position))
                holder.isCheck.setImageResource(R.drawable.icon_checked);
            else holder.isCheck.setImageResource(R.drawable.icon_unchecked);
        } else {
            holder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mMemberInfos == null ? 0 : mMemberInfos.size();
    }

    public interface OnItemClick {
        void OnClick(UserInfo userInfo);
    }
}
