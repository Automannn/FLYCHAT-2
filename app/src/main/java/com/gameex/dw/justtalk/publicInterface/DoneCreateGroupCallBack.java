package com.gameex.dw.justtalk.publicInterface;

import android.net.Uri;

import java.util.List;

public interface DoneCreateGroupCallBack {

    void sendGroupIcon(Uri groupIcon);

    void sendGroupName(String groupName);

    void sendUris(List<Uri> uris);
}
