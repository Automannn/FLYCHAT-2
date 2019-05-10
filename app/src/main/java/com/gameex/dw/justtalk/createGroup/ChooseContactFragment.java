package com.gameex.dw.justtalk.createGroup;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gameex.dw.justtalk.ObjPack.User;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.RecScrollHelper;
import com.gameex.dw.justtalk.publicInterface.FragmentCallBack;
import com.gameex.dw.justtalk.publicInterface.RecyclerItemClick;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gjiazhe.wavesidebar.WaveSideBar;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

import static com.gameex.dw.justtalk.createGroup.CreateGroupActivity.sActivity;

public class ChooseContactFragment extends Fragment {
    private static final String TAG = "ChooseContactFragment";
    private static String[] indexStr = new String[]{"↑", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    public static final String ARG_PARAM = "user_infos";

    /**
     *
     */
    private Activity mActivity;
    /**
     * 搜索栏
     */
    private SearchView mSearchView;
    /**
     * 联系人列表
     */
    private RecyclerView mRecyclerView;
    /**
     * 索引栏
     */
    private WaveSideBar mSlideBar;
    /**
     * 联系人adapter
     */
    private ChooseContactAdapter mAdapter;
    /**
     * 联系人集合
     */
    private List<UserInfo> mUserInfos;
    /**
     * 容纳被选中的userInfo
     */
    private List<UserInfo> mUserChoosed = new ArrayList<>();
    /**
     * 回传activity被选中的联系人的接口
     */
    private FragmentCallBack mCallBack;

    public static ChooseContactFragment newInstance(String userInfosStr) {
        ChooseContactFragment fragment = new ChooseContactFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM, userInfosStr);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mUserInfos = (List<UserInfo>) UserInfo.fromJsonToCollection(
                getArguments().getString(ARG_PARAM));
        mCallBack = (FragmentCallBack) getActivity();
        LogUtil.d(TAG, "onAttach: " + "mUserInfos.size = " + mUserInfos.size());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater
            , @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_to_create_group, container, false);
        mSearchView = view.findViewById(R.id.search_view_create_group);
//        mSearchView.onActionViewExpanded();
        mSearchView.setQueryHint("搜索好友");
        mSearchView.setIconifiedByDefault(false);
        View searchLine = view.findViewById(android.support.v7.appcompat.R.id.search_plate);
        searchLine.setBackground(getResources().getDrawable(R.drawable.white_accent_underline_bg));
        mRecyclerView = view.findViewById(R.id.contact_recycler_create_group);
        mSlideBar = view.findViewById(R.id.glide_side_bar_create_group);
        initData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
    }

    private void initData() {
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mSlideBar.setVisibility(View.GONE);
                } else {
                    mSlideBar.setVisibility(View.VISIBLE);
                }
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                LogUtil.d(TAG, "initData: " + "onQueryTextSubmit = " + s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                LogUtil.d(TAG, "initData: " + "onQueryTextChange = " + s);
                return false;
            }
        });
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "initData: " + "Search been clicked");
            }
        });
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                LogUtil.d(TAG, "initData: " + "onSuggestionSelect = " + i);
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                LogUtil.d(TAG, "initData: " + "onSuggestionClick = " + i);
                return false;
            }
        });

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(500);
        animator.setChangeDuration(500);
        animator.setMoveDuration(500);
        animator.setRemoveDuration(500);
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(sActivity));
        mAdapter = new ChooseContactAdapter(sActivity, mUserInfos);
        mAdapter.setItemClick(new RecyclerItemClick() {
            @Override
            public void onClick(int position) {
                UserInfo userInfo = mUserInfos.get(position);
                boolean isChoosed = false;
                for (int i = 0; i < mUserChoosed.size(); i++) {
                    UserInfo userChoose = mUserChoosed.get(i);
                    if (userChoose.getUserName().equals(userInfo.getUserName())) {
                        mUserChoosed.remove(userChoose);
                        isChoosed = true;
                        break;
                    }
                }
                if (!isChoosed) {
                    mUserChoosed.add(mUserInfos.get(position));
                }
                mCallBack.sendMessage(UserInfo.collectionToJson(mUserChoosed));
            }

            @Override
            public void onLongClick(int position) {

            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mSlideBar.setIndexItems(indexStr);
        mSlideBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String index) {
                for (int i = 0; i < mUserInfos.size(); i++) {
                    if (mUserInfos.get(i).getExtra("index").equals(index)) {
                        RecScrollHelper.scrollToPosition(mRecyclerView, i);
                        return;
                    }
                }
            }
        });
    }

}
