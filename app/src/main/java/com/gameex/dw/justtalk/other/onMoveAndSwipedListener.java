package com.gameex.dw.justtalk.other;

/**
 * created by zhang
 * 手指移动监听接口
 */
public interface onMoveAndSwipedListener {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
