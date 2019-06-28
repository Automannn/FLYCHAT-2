package com.gameex.dw.justtalk.holder;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;

public class SearchUserHodler extends RecyclerView.ViewHolder {

    /**
     * 用头像
     */
    @BindView(R.id.icon)
    public CircularImageView icon;
    /**
     * displayName: nickname>username
     */
    @BindView(R.id.name)
    public TextView name;
    /**
     * 添加
     */
    @BindView(R.id.add)
    public TextView add;

    @OnClick(R.id.add)
    void add() {
        List<String> usernames = new ArrayList<>();
        usernames.add(mUserInfos.get(getAdapterPosition()).getUserName());
        JMessageClient.addGroupMembers(mGroupId, usernames, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if (i == 0) {
                    add.setText("已添加");
                    add.setEnabled(false);
                    Toasty.info(itemView.getContext(), "添加成功").show();
                } else {
                    Toasty.error(itemView.getContext(), "出错了").show();
                }
            }
        });
    }

    private List<UserInfo> mUserInfos;
    private long mGroupId;

    public SearchUserHodler(@NonNull View itemView, List<UserInfo> userInfos, long groupId) {
        super(itemView);
        mUserInfos = userInfos;
        mGroupId = groupId;
        ButterKnife.bind(this, itemView);
    }
}
