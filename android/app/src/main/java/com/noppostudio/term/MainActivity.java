package com.noppostudio.term;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // SSHPluginを登録することでJSから呼び出せるようになる
        registerPlugin(SSHPlugin.class);
    }
}