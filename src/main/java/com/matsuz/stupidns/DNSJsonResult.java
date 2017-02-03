package com.matsuz.stupidns;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO Class for Google Public DNS json result struct
 *
 * @author Matsuz
 */
public class DNSJsonResult {

    @SerializedName("Status")
    public int status;      // Reply Code

    @SerializedName("TC")
    public boolean tc;

    @SerializedName("RD")
    public boolean rd;

    @SerializedName("RA")
    public boolean ra;

    @SerializedName("AD")
    public boolean ad;

    @SerializedName("CD")
    public boolean cd;

    public static class Record {
        public int type;
        public String name;
        @SerializedName("TTL")
        public Long ttl;
        public String data;
    }

    @SerializedName("Question")
    public List<Record> question;

    @SerializedName("Answer")
    public List<Record> answer;

    @SerializedName("Authority")
    public List<Record> authority;

    // @SerializedName("Additional")
    // public List<Record> additional;

    @SerializedName("Comment")
    public String comment;

    @SerializedName("edns_client_subnet")
    public String ednsClientSubnet;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
