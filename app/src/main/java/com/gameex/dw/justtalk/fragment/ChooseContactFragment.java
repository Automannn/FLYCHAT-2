package com.gameex.dw.justtalk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.ChooseContactAdapter;
import com.gameex.dw.justtalk.util.RecScrollHelper;
import com.gameex.dw.justtalk.publicInterface.FragmentCallBack;
import com.gameex.dw.justtalk.publicInterface.RecyclerItemClick;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gjiazhe.wavesidebar.WaveSideBar;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.UserInfo;

import static com.gameex.dw.justtalk.activity.CreateGroupActivity.sActivity;

/**
 * 建群时选择联系人fragment
 */
public class ChooseContactFragment extends Fragment {
    private static final String TAG = "ChooseContactFragment";
    private static String[] indexStr = new String[]{"↑", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    public static final String ARG_PARAM = "user_infos";

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
        assert getArguments() != null;
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
        View view = inflater.inflate(R.layout.fragment_search_to_create_group, container, false);
        mSearchView = view.findViewById(R.id.search_view_create_group);
//        mSearchView.onActionViewExpanded();
        mSearchView.setQueryHint("搜索好友");
        mSearchView.setIconifiedByDefault(false);
        View searchLine = view.findViewById(R.id.search_plate);
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
        mSearchView.setOnCloseListener(() -> false);
        mSearchView.setOnQueryTextFocusChangeListener((view, b) -> {
            if (b) {
                mSlideBar.setVisibility(View.GONE);
            } else {
                mSlideBar.setVisibility(View.VISIBLE);
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
        mSearchView.setOnSearchClickListener(view -> LogUtil.d(TAG, "initData: " + "Search been clicked"));
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
        animator.setAddDuration(300);
        animator.setChangeDuration(300);
        animator.setMoveDuration(300);
        animator.setRemoveDuration(300);
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(sActivity));
        //联系人adapter
        ChooseContactAdapter adapter = new ChooseContactAdapter(sActivity, mUserInfos);
        adapter.setItemClick(new RecyclerItemClick() {
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
        mRecyclerView.setAdapter(adapter);

        mSlideBar.setIndexItems(indexStr);
        mSlideBar.setOnSelectIndexItemListener(index -> {
            for (int i = 0; i < mUserInfos.size(); i++) {
                if (mUserInfos.get(i).getExtra("index").equals(index)) {
                    RecScrollHelper.scrollToPosition(mRecyclerView, i);
                    return;
                }
            }
        });
    }

}
