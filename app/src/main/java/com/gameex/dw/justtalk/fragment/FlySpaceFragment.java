package com.gameex.dw.justtalk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.FlySpaceSwipeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import link.fls.swipestack.SwipeStack;

public class FlySpaceFragment extends Fragment {

    @BindView(R.id.swipe_stack)
    SwipeStack mStack;

    @OnClick({R.id.left, R.id.add, R.id.right})
    void doClick(View view) {
        switch (view.getId()) {
            case R.id.left:
                mStack.swipeTopViewToLeft();
                break;
            case R.id.add:
                mDatas.add("NEW");
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.right:
                mStack.swipeTopViewToRight();
                break;
        }
    }

    private FlySpaceSwipeAdapter mAdapter;
    private List<String> mDatas;

    public static FlySpaceFragment newInstance() {
        FlySpaceFragment fragment = new FlySpaceFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fly_space, container, false);
        ButterKnife.bind(this, view);
        initTestDatas();
        initData();
        return view;
    }

    /**
     * 初始化SwipeStackView
     */
    private void initData() {
        mAdapter = new FlySpaceSwipeAdapter(getContext(), mDatas);
        mStack.setAdapter(mAdapter);
        mStack.setListener(new SwipeStack.SwipeStackListener() {
            @Override
            public void onViewSwipedToLeft(int position) {
                String leftToast = mDatas.get(position);
                Toasty.normal(Objects.requireNonNull(getContext()), "left = " + leftToast).show();
            }

            @Override
            public void onViewSwipedToRight(int position) {
                String rightToast = mDatas.get(position);
                Toasty.normal(Objects.requireNonNull(getContext()), "right = " + rightToast).show();
            }

            @Override
            public void onStackEmpty() {
                Toasty.normal(Objects.requireNonNull(getContext()), "没有数据").show();
            }
        });
    }

    /**
     * 初始化测试数据
     */
    private void initTestDatas() {
        mDatas = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            mDatas.add("Test" + ++i);
        }
    }
}
