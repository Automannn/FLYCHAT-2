package com.gameex.dw.justtalk.titleBar;

/**
 * 自定义searchView搜索状态监听
 */
public interface OnSearchQueryListen {
    boolean onQuerySubmit(String query);

    boolean onQueryChange(String newText);
}
