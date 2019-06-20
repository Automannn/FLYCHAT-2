package com.gameex.dw.justtalk.payPackage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.managePack.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

public class RechargeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "RechargeActivity";
    /**
     * 选择支付方式
     */
    private static final int TO_CHOOSE_PAY_WAY = 101;
    /**
     * 金额面板数据
     */
    private int[] mYuanArray = {10, 50, 100, 500, 1000, 5000, 10000};
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

        //返回
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(this);

        //金额面板
        GridView gridView = findViewById(R.id.grid);
        mAdapter = new SimpleAdapter(this, mMapList, R.layout.grid_item_recharge
                , new String[]{"yuan"}, new int[]{R.id.yuan});
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener((adapterView, view, position, l) -> {
            LinearLayout layout;
            if (mYuan == null) {
                layout = (LinearLayout) mAdapter.getView(position, view, null);
                mCheckBox = (CheckBox) layout.getChildAt(0);
                mCheckBox.setChecked(true);
                mYuan = mYuanArray[position];
            } else if (mYuan == mYuanArray[position]) {
                mCheckBox.setChecked(false);
                mYuan = null;
            } else {
                mCheckBox.setChecked(false);
                layout = (LinearLayout) mAdapter.getView(position, view, null);
                mCheckBox = (CheckBox) layout.getChildAt(0);
                mCheckBox.setChecked(true);
                mYuan = mYuanArray[position];
            }
        });

        //下一步
        Button next = findViewById(R.id.next_step);
        next.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.next_step:
                if (mYuan == null) {
                    Toast.makeText(this, "请选择金额", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.setClass(this, PayOrderActivity.class);
                intent.putExtra("money_amount", mYuan);
                startActivityForResult(intent, TO_CHOOSE_PAY_WAY);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TO_CHOOSE_PAY_WAY && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getBooleanExtra("pay_success", false)) {
                    Intent intent = new Intent();
                    intent.putExtra("order_success", true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
