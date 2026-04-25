package com.noppostudio.term;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

@CapacitorPlugin(name = "SSH")
public class SSHPlugin extends Plugin {
    private Session session;
    private ChannelShell channel;
    private OutputStream out;

    @PluginMethod
    public void connect(PluginCall call) {
        new Thread(() -> {
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(call.getString("user"), call.getString("host"), 22);
                session.setPassword(call.getString("pass"));

                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();

                channel = (ChannelShell) session.openChannel("shell");
                InputStream in = channel.getInputStream();
                out = channel.getOutputStream();
                channel.connect();

                JSObject ret = new JSObject();
                ret.put("status", "connected");
                call.resolve(ret);

                // 受信ループ：サーバーからの文字を拾い続けてJSに投げる
                byte[] buffer = new byte[1024];
                int i;
                while ((i = in.read(buffer)) != -1) {
                    JSObject data = new JSObject();
                    data.put("value", new String(buffer, 0, i));
                    notifyListeners("data", data);
                }
            } catch (Exception e) {
                call.reject(e.getMessage());
            }
        }).start();
    }

    @PluginMethod
    public void sendCommand(PluginCall call) {
        String data = call.getString("data");
        try {
            if (out != null) {
                out.write(data.getBytes());
                out.flush();
                call.resolve();
            }
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }
}