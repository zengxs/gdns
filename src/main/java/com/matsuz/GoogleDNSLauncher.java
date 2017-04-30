package com.matsuz;

import com.matsuz.gdns.Configuration;
import com.matsuz.gdns.UDPServer;

import java.net.URISyntaxException;
import java.nio.file.Paths;


public class GoogleDNSLauncher {
    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        // 设置Static Map配置文件
        Configuration.getInstance().setConfigPath(Paths.get("config.json"));

        UDPServer.run();

        System.out.println("Hello World!");
    }
}
