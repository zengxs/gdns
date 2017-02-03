package com.matsuz.stupidns;

import com.moandjiezana.toml.Toml;
import org.xbill.DNS.Name;

import java.io.File;
import java.util.Map;

/**
 * class for configuration file structure
 *
 * @author Matsuz
 */
public class TomlConfig {

    public static class Server {
        public String address;
        public int port;
    }

    public static class Address {
        public String type;
        public String address;
    }

    public Server server;
    public Map<String, Address> address;
}
