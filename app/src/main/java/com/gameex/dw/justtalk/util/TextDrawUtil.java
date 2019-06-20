package com.gameex.dw.justtalk.util;

import com.amulyakhare.textdrawable.TextDrawable;

public class TextDrawUtil {
    /**
     * 创建简单的圆形textDrawable
     *
     * @param text text
     * @return textDrawable
     */
    public static TextDrawable getRoundTextDraw(String[] text) {
        return TextDrawable.builder()
                .buildRound(text[0], DataUtil.getRandColor(text[1]));
    }
}
