package com.gameex.dw.justtalk.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.manage.BaseActivity;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.rey.material.app.Dialog;
import com.rey.material.widget.CircleCheckedTextView;
import com.rey.material.widget.EditText;

import java.util.Calendar;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import es.dmoral.toasty.Toasty;

/**
 * 飞聊空间基本信息编辑界面
 */
public class EditBasicSpaceActivity extends BaseActivity {
    private static final String TAG = "EditBasicSpaceActivity";
    /**
     * 昵称、性别、年龄、职业
     */
    @BindViews({R.id.nick_name, R.id.gender, R.id.age, R.id.career})
    TextView[] mTextViews;

    @OnClick({R.id.back, R.id.nick_name, R.id.gender, R.id.age, R.id.career, R.id.done})
    void doClick(View view) {
        switch (view.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.nick_name:    //昵称
                showEditNick();
                break;
            case R.id.gender:   //性别
                showGenderDialog();
                break;
            case R.id.age:  //年龄
                showDatePickDialog();
                break;
            case R.id.career:   //职业（距离/最近上线时间）
                //TODO: 更新职业
                break;
            case R.id.done: //完成
                JMessageClient.updateMyInfo(UserInfo.Field.all, mUserInfo, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            Toasty.success(EditBasicSpaceActivity.this, "更新成功").show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toasty.info(EditBasicSpaceActivity.this, "更新失败").show();
                            LogUtil.d(TAG, "doClick-done: " + "responseCode = " + i + " ;desc = " + s);
                        }
                    }
                });
        }
    }

    private UserInfo mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_basic_space);
        ButterKnife.bind(this);
        mUserInfo = JMessageClient.getMyInfo();
    }

    /**
     * 编辑昵称弹窗
     */
    @SuppressLint("SetTextI18n")
    private void showEditNick() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit);
        EditText editNick = dialog.findViewById(R.id.nick_name);
        dialog.cancelable(true)
                .positiveAction("确定")
                .positiveActionClickListener(view -> {
                    String nick = editNick.getText().toString();
                    if (mUserInfo == null) {
                        JMessageClient.getMyInfo();
                    }
                    mUserInfo.setNickname(nick);
                    mTextViews[0].setText(getString(R.string.nick_str) + "    " + nick);
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * 日期选择弹窗
     */
    @SuppressLint("SetTextI18n")
    private void showDatePickDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    LogUtil.d(TAG, "onDateSet: year: " + year + ", month: " + month + ", dayOfMonth: " + dayOfMonth);
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    int yearNow = Integer.parseInt(DataUtil.msFormYYYY(System.currentTimeMillis()));
                    int age = yearNow - year;
                    mTextViews[2].setText(getString(R.string.age_str) + "    " + age);
                    mUserInfo.setUserExtras("age", String.valueOf(age));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /**
     * 选择性别弹窗
     */
    private void showGenderDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_gender);
        CheckedTextView male = dialog.findViewById(R.id.male);
        CheckedTextView female = dialog.findViewById(R.id.female);
        male.setOnClickListener(view -> {
            if (male.isChecked()) male.setChecked(false);
            else {
                if (female.isChecked()) female.setChecked(false);
                male.setChecked(true);
            }
        });
        female.setOnClickListener(view -> {
            if (female.isChecked()) female.setChecked(false);
            else {
                if (male.isChecked()) male.setChecked(false);
                female.setChecked(true);
            }
        });
        dialog.setOnDismissListener(dialogInterface -> {
            if (male.isChecked()) mUserInfo.setGender(UserInfo.Gender.male);
            else if (female.isChecked()) mUserInfo.setGender(UserInfo.Gender.female);
            else mUserInfo.setGender(UserInfo.Gender.unknown);
        });
        dialog.cancelable(true).show();
    }
}
