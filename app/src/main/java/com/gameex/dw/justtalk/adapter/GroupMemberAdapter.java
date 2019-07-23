package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.holder.GroupMemberHolder;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 群成员列表适配器
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberHolder>
        /*implements onMoveAndSwipedListener //手指移动监听接口*/ {

    //private View parentView;
    private Context mContext;
    private List<GroupMemberInfo> mInfos;
    private String mGroupInfoJson;

    public GroupMemberAdapter(Context context, List<GroupMemberInfo> infos
            , String groupInfo) {
        mContext = context;
        mInfos = infos;
        mGroupInfoJson = groupInfo;
    }

    @NonNull
    @Override
    public GroupMemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //parentView = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_group_members, parent, false);
        return new GroupMemberHolder(view, mInfos, mContext, mGroupInfoJson);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberHolder holder, int position) {
        //item加载动画
//        Animation load = AnimationUtils.loadAnimation(mContext, R.anim.recycler_item_load_translate_scale);
//        holder.view.startAnimation(load);
        GroupMemberInfo memberInfo = mInfos.get(position);
        UserInfo userInfo = memberInfo.getUserInfo();
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
        holder.texts.get(0).setText(memberInfo.getDisplayName());
        holder.texts.get(1).setText(DataUtil.msFormMMDD(memberInfo.getJoinGroupTime()));
        switch (memberInfo.getType()) {
            case group_owner:
                holder.texts.get(2).setVisibility(View.VISIBLE);
                holder.texts.get(2).setText("群主");
                break;
            case group_keeper:
                holder.texts.get(2).setVisibility(View.VISIBLE);
                holder.texts.get(2).setText("管理员");
                break;
            default:
                holder.texts.get(2).setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            List<String> pay = (List<String>) payloads.get(0);
            mGroupInfoJson = pay.get(0);
            mInfos = (List<GroupMemberInfo>) GroupMemberInfo.fromJsonToCollection(pay.get(1));
            holder.setMembers(mInfos);
            holder.setGroupInfo(GroupInfo.fromJson(mGroupInfoJson));
            GroupMemberInfo memberInfo = mInfos.get(position);
            switch (memberInfo.getType()) {
                case group_owner:
                    holder.texts.get(2).setVisibility(View.VISIBLE);
                    holder.texts.get(2).setText("群主");
                    break;
                case group_keeper:
                    holder.texts.get(2).setVisibility(View.VISIBLE);
                    holder.texts.get(2).setText("管理员");
                    break;
                default:
                    holder.texts.get(2).setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mInfos == null ? 0 : mInfos.size();
    }

//    @Override
//    public boolean onItemMove(int fromPosition, int toPosition) {
//        Collections.swap(mInfos, fromPosition, toPosition);
//        notifyItemMoved(fromPosition, toPosition);
//        return true;
//    }

//    @Override
//    public void onItemDismiss(int position) {
//        mInfos.remove(position);
//        notifyItemRemoved(position);
//
//        Snackbar.make(parentView, "已删除"/*mContext.getString(R.string.item_swipe_dismissed)*/, Snackbar.LENGTH_SHORT)
//                .setAction("撤销"/*mContext.getString(R.string.item_swipe_undo)*/, v -> {
//                    mInfos.add(position, mInfos.get(position));
//                    notifyDataSetChanged();
//                }).show();
//    }
}
