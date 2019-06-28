package com.gameex.dw.justtalk.holder;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.LogUtil;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.SilenceInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;

public class GroupMemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "";
    /**
     * 当前view,用于播放动画
     */
    View view;
    /**
     * 群组成员信息集合
     */
    private List<GroupMemberInfo> members;

    /**
     * 用户头像
     */
    @BindView(R.id.icon)
    public CircularImageView icon;
    /**
     * 用户名，用户入群时间(用户最后一次上线时间/在线状态)，用户在本群内的职位
     */
    @BindViews({R.id.name, R.id.status, R.id.career})
    public List<TextView> texts;
    /**
     * 群组信息对象
     */
    private GroupInfo groupInfo;
    /**
     * 上下文
     */
    private Context context;
    /**
     * 长按群成员item弹窗
     */
    private Dialog dialog;

    public void setMembers(List<GroupMemberInfo> members) {
        this.members = members;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    public GroupMemberHolder(@NonNull View v, List<GroupMemberInfo> infos, Context context, String groupInfoJson) {
        super(v);
        view = v;
        members = infos;
        this.context = context;
        groupInfo = GroupInfo.fromJson(groupInfoJson);
        ButterKnife.bind(this, v);
        v.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserBasicInfoActivity.class);
            intent.putExtra("user_info_json", members.get(getAdapterPosition()).toJson());
            context.startActivity(intent);
        });
        v.setOnLongClickListener(view -> {
            UserInfo myInfo = JMessageClient.getMyInfo();
            GroupMemberInfo myMemberInfo = groupInfo.getGroupMember(myInfo.getUserName(), null);
            switch (myMemberInfo.getType()) {
                case group_owner:
                    showGroupMemberFunction(members.get(getAdapterPosition()), true);
                    break;
                case group_keeper:
                    showGroupMemberFunction(members.get(getAdapterPosition()), false);
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    /**
     * 操作群成员的弹窗
     */
    private void showGroupMemberFunction(GroupMemberInfo member, boolean isOwner) {
        dialog = new Dialog(context, R.style.qr_code_dialog_style);
        dialog.setContentView(R.layout.dialog_group_member_function);
        ImageView close = dialog.findViewById(R.id.close);
        close.setOnClickListener(this);
        TextView line1 = dialog.findViewById(R.id.line1); //第一条分割线
        TextView line2 = dialog.findViewById(R.id.line2); //第二条分割线
        TextView exit = dialog.findViewById(R.id.exit);
        exit.setOnClickListener(this);
        TextView delete = dialog.findViewById(R.id.delete); //删除成员（退出群组）
        delete.setOnClickListener(this);
        TextView setKeeper = dialog.findViewById(R.id.set_keeper);  //设为管理员
        setKeeper.setOnClickListener(this);
        TextView removeKeeper = dialog.findViewById(R.id.remove_keeper);    //取消管理员
        removeKeeper.setOnClickListener(this);
        TextView banned = dialog.findViewById(R.id.banned); //禁言（默认禁言5分钟）
        banned.setOnClickListener(this);
        TextView cancelBinned = dialog.findViewById(R.id.cancel_banned);
        cancelBinned.setOnClickListener(this);
        dialog.setCanceledOnTouchOutside(true);
        if (JMessageClient.getMyInfo().getUserName().equals(
                member.getUserInfo().getUserName())) {   //若是登陆用户自己
            exit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.GONE);
            setKeeper.setVisibility(View.GONE);
            banned.setVisibility(View.GONE);
            line1.setVisibility(View.GONE);
            line2.setVisibility(View.GONE);
        } else {
            switch (member.getType()) {
                case group_owner:   //若是群主，则不显示
                    return;
                case group_keeper:  //若是管理员，则重设选项
                    setKeeper.setVisibility(View.GONE);
                    removeKeeper.setVisibility(View.VISIBLE);
                    break;
            }
            if (!isOwner) {
                setKeeper.setVisibility(View.GONE);
                removeKeeper.setVisibility(View.GONE);
            }
            groupInfo.getGroupMemberSilence(member.getUserInfo().getUserName(), null, new RequestCallback<SilenceInfo>() {
                @Override
                public void gotResult(int i, String s, SilenceInfo silenceInfo) {
                    if (i == 0) {
                        if (silenceInfo != null) {
                            banned.setVisibility(View.GONE);
                            cancelBinned.setVisibility(View.VISIBLE);
                        }
                    } else {
                        LogUtil.d(TAG, "showGroupMemberFunction: " +
                                "responseCode = " + i + " ;desc = " + s);
                    }
                }
            });
        }
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        UserInfo userInfo = members.get(getAdapterPosition()).getUserInfo();
        List<UserInfo> userInfos = new ArrayList<>();
        switch (view.getId()) {
            case R.id.close:    //关闭弹窗
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;
            case R.id.exit: //退出群组
                JMessageClient.exitGroup(groupInfo.getGroupID(), new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            dialog.dismiss();
                            Toasty.success(context, "已退出").show();
                        } else {
                            LogUtil.d(TAG, "OnClick-exit: " +
                                    "responseCode = " + i + " ;desc = " + s);
                            Toasty.error(context, "发生了意想不到的错误").show();
                        }
                    }
                });
                break;
            case R.id.delete:   //移除群组
                List<String> usernames = new ArrayList<>();
                usernames.add(userInfo.getUserName());
                JMessageClient.removeGroupMembers(groupInfo.getGroupID(), usernames, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            dialog.dismiss();
                            Toasty.success(context, "已移除").show();
                        } else {
                            LogUtil.d(TAG, "OnClick-delete: " +
                                    "responseCode = " + i + " ;desc = " + s);
                            Toasty.error(context, "发生了意想不到的错误").show();
                        }
                    }
                });
                break;
            case R.id.set_keeper:   //设为管理员
                userInfos.add(userInfo);
                groupInfo.addGroupKeeper(userInfos, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            dialog.dismiss();
//                            texts.get(2).setVisibility(View.VISIBLE);
//                            texts.get(2).setText("管理员");
                            Toasty.success(context, "已设为管理员").show();
                        } else {
                            LogUtil.d(TAG, "OnClick-set_keeper: " +
                                    "responseCode = " + i + " ;desc = " + s);
                            Toasty.error(context, "发生了意想不到的错误").show();
                        }
                    }
                });
                break;
            case R.id.remove_keeper:    //取消管理员
                userInfos.add(userInfo);
                groupInfo.removeGroupKeeper(userInfos, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            dialog.dismiss();
//                            texts.get(2).setVisibility(View.GONE);
                            Toasty.success(context, "已取消管理员身份").show();
                        } else {
                            LogUtil.d(TAG, "OnClick-remove_keeper: " +
                                    "responseCode = " + i + " ;desc = " + s);
                            Toasty.error(context, "发生了意想不到的错误").show();
                        }
                    }
                });
                break;
            case R.id.banned:   //禁言
                userInfos.add(userInfo);
                long l = 300000;
                groupInfo.addGroupSilenceWithTime(userInfos, l, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            dialog.dismiss();
                            Toasty.success(context, "禁言5分钟").show();
                        } else {
                            LogUtil.d(TAG, "OnClick-banned: " +
                                    "responseCode = " + i + " ;desc = " + s);
                            Toasty.error(context, "发生了意想不到的错误").show();
                        }
                    }
                });
                break;
            case R.id.cancel_banned:    //取消禁言
                userInfos.add(userInfo);
                groupInfo.delGroupSilence(userInfos, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            dialog.dismiss();
                            Toasty.success(context, "已取消禁言").show();
                        } else {
                            LogUtil.d(TAG, "OnClick-cancel_banned: " +
                                    "responseCode = " + i + " ;desc = " + s);
                            Toasty.error(context, "发生了意想不到的错误").show();
                        }
                    }
                });
                break;
        }
    }
}
