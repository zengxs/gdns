package com.matsuz;

import com.matsuz.gdns.Configuration;
import com.matsuz.gdns.UDPServer;

import java.nio.file.Paths;


public class GoogleDNSLauncher {
    public static void main(String[] args) throws InterruptedException {
        // 设置Static配置文件
        Configuration.getInstance().setConfigPath(Paths.get("static.toml"));

        UDPServer.run();
    }
}
