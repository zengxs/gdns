package com.matsuz;

import com.matsuz.gdns.Configuration;
import com.matsuz.gdns.UDPServer;

import java.nio.file.Paths;


public class GoogleDNSLauncher {
    public static void main(String[] args) throws InterruptedException {
        // 设置端口
        parsePort(args);
        // 设置Static配置文件
        Configuration.getInstance().setConfigPath(Paths.get("static.toml"));

        UDPServer.run();
    }

    private static void parsePort(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                if ("-p".equals(args[i]))
                    UDPServer.setPort(Integer.parseInt(args[++i]));
            }
        } catch (Exception e) {
            System.out.println("Usage: java -jar " + args[0] + " [-p PORT]");
            System.exit(1);
        }
    }
}
