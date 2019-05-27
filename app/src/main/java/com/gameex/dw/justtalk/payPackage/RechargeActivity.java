package com.gameex.dw.justtalk.payPackage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.payOrder.PayOrderActivity;
import com.gameex.dw.justtalk.util.WindowUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RechargeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RechargeActivity";
    /**
     * 父布局
     */
    private LinearLayout mLayout;
    /**
     * 返回
     */
    private ImageView mBack;
    /**
     * 金额面板
     */
    private GridView mGridView;
    /**
     * 下一步
     */
    private Button mNext;
    /**
     * 金额面板数据
     */
    private int[] mYuanArray = {10, 30, 50, 100, 200, 500, 1000};
    /**
     * gridView适配器
     */
    private SimpleAdapter mAdapter;
    /**
     * 金额面板数据集合
     */
    private List<Map<String, String>> mMapList;
    /**
     * 记录每次选中的金额
     */
    private Integer mYuan;
    /**
     * 记录上一次选中的checkBox
     */
    private CheckBox mCheckBox;
    /**
     * 没绑定银行卡时的popupWindow
     */
    private PopupWindow mNoBankCardPup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGrid();
        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        setContentView(R.layout.activity_recharge);

        mLayout = findViewById(R.id.linear);
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mGridView = findViewById(R.id.grid);
        mAdapter = new SimpleAdapter(this, mMapList, R.layout.recharge_item
                , new String[]{"yuan"}, new int[]{R.id.yuan});
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                LinearLayout layout;
                if (mYuan == null) {
                    layout = (LinearLayout) mAdapter.getView(position, view, null);
                    mCheckBox = (CheckBox) layout.getChildAt(0);
                    mCheckBox.setChecked(true);
                    Toast.makeText(RechargeActivity.this, mYuanArray[position] + ""
                            , Toast.LENGTH_SHORT).show();
                    mYuan = mYuanArray[position];
                } else if (mYuan == mYuanArray[position]) {
                    mCheckBox.setChecked(false);
                    mYuan = null;
                } else {
                    mCheckBox.setChecked(false);
                    layout = (LinearLayout) mAdapter.getView(position, view, null);
                    mCheckBox = (CheckBox) layout.getChildAt(0);
                    mCheckBox.setChecked(true);
                    Toast.makeText(RechargeActivity.this, mYuanArray[position] + ""
                            , Toast.LENGTH_SHORT).show();
                    mYuan = mYuanArray[position];
                }
            }
        });
        mNext = findViewById(R.id.next_step);
        mNext.setOnClickListener(this);

        initNoBankCardPup();
    }

    /**
     * 初始化Grid数据
     */
    private void initGrid() {
        mMapList = new ArrayList<>();
        for (int yuan : mYuanArray) {
            Map<String, String> map = new HashMap<>();
            map.put("yuan", yuan + ".0元");
            mMapList.add(map);
        }
    }

    /**
     * 没有绑定银行卡时弹出
     */
    private void initNoBankCardPup() {
        View view = this.getLayoutInflater().inflate(R.layout.no_bank_card_pup, null);
        TextView cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        TextView confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(this);
        mNoBankCardPup = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mNoBankCardPup.setFocusable(true);
        mNoBankCardPup.setOutsideTouchable(false);
        mNoBankCardPup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
//                    mNoBankCardPup.dismiss();
//                    return true;
//                }
                return false;
            }
        });
        mNoBankCardPup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowUtil.setWindowBackgroundAlpha(RechargeActivity.this, 1f);
            }
        });
        mNoBankCardPup.setAnimationStyle(R.style.scale_alpha_style);
        mNoBankCardPup.update();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.next_step:
                if (mYuan == null) {
                    intent.setClass(this, PayOrderActivity.class);
                    startActivity(intent);
                    Toast.makeText(this, "请选择金额", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mNoBankCardPup != null) {
                    mNoBankCardPup.showAtLocation(mLayout, Gravity.CENTER, 0, 0);
                    WindowUtil.showBackgroundAnimator(this, 0.5f);
                }
                Toast.makeText(this, "充值金额" + mYuan + ".0元", Toast.LENGTH_SHORT).show();
                break;
            case R.id.cancel:
                if (mNoBankCardPup.isShowing()) {
                    mNoBankCardPup.dismiss();
                }
                break;
            case R.id.confirm:
                if (mNoBankCardPup.isShowing()) {
                    mNoBankCardPup.dismiss();
                }
                intent.setClass(this, AddBankCardActivity.class);
                startActivity(intent);
                break;
        }
    }
}
