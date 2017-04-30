package com.matsuz.gdns;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;


public class Configuration {
    private static final Configuration instance = new Configuration();
    private static final Gson gson = new GsonBuilder().setLenient().create();

    private Path configPath;

    private Configuration() {
    }

    public static Configuration getInstance() {
        return instance;
    }

    public void setConfigPath(Path path) {
        this.configPath = path;
    }

    List<Map<String, String>> getConfigList() throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.lines(configPath, StandardCharsets.UTF_8).forEach(e -> {
            if (!e.trim().startsWith("//")) sb.append(e);
        });
        return (List<Map<String, String>>) gson.fromJson(sb.toString(), List.class);
    }
}
