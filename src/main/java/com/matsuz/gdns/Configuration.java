package com.matsuz.gdns;


import com.moandjiezana.toml.Toml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Configuration {
    private static final Configuration instance = new Configuration();
    private static final Toml toml = new Toml();

    private Path configPath;

    private Configuration() {
    }

    public static Configuration getInstance() {
        return instance;
    }

    public void setConfigPath(Path path) {
        this.configPath = path;
    }

    private String getStaticFileContent() throws IOException {
        InputStream in = Files.newInputStream(configPath);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // bytes copy
        final int BLOCK_SIZE = 1024;
        byte[] bytes = new byte[BLOCK_SIZE];
        int len;
        while ((len = in.read(bytes)) != -1) out.write(bytes, 0, len);

        in.close();
        out.close();

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    List<Map<String, Object>> getIPv4ConfigList() {
        try {
            List<Toml> ipv4Entries = toml.read(getStaticFileContent()).getTables("ipv4");
            List<Map<String, Object>> result = new ArrayList<>();
            ipv4Entries.forEach(e -> result.add(e.toMap()));
            return result;
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    List<Map<String, Object>> getIPv6ConfigList() {
        try {
            List<Toml> ipv4Entries = toml.read(getStaticFileContent()).getTables("ipv6");
            List<Map<String, Object>> result = new ArrayList<>();
            ipv4Entries.forEach(e -> result.add(e.toMap()));
            return result;
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    List<Map<String, Object>> getPtrConfigList() {
        try {
            List<Toml> ipv4Entries = toml.read(getStaticFileContent()).getTables("ptr");
            List<Map<String, Object>> result = new ArrayList<>();
            ipv4Entries.forEach(e -> result.add(e.toMap()));
            return result;
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }
}
