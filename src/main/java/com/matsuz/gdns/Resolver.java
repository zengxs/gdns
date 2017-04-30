package com.matsuz.gdns;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.xbill.DNS.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class Resolver {
    private static final OkHttpClient client;

    static {
        client = new OkHttpClient();
    }

    private Resolver() {
    }

    public static Message resolve(Name name, int rrType, String edns) throws IOException {
        // compare with static map
        if (rrType == Type.A || rrType == Type.AAAA) {
            List<Map<String, Object>> configList = null;
            if (rrType == Type.A)
                configList = Configuration.getInstance().getIPv4ConfigList();
            else if (rrType == Type.AAAA)
                configList = Configuration.getInstance().getIPv6ConfigList();
            if (configList != null)
                for (Map<String, Object> entry : configList) {
                    String regex = entry.get("pattern") + "\\.?";
                    if (Pattern.matches(regex, name.toString())) {
                        Message response = new Message();
                        response.addRecord(Record.fromString(
                                name, rrType, DClass.IN, 0,
                                (String) entry.get("address"), Name.root
                        ), Section.ANSWER);
                        return response;
                    }
                }
        }

        // request google public dns
        Message response = new Message();
        ResolveResult result = edns == null ? fetch(name, rrType) : fetch(name, rrType, edns);
        addRecords(response, result.answer, Section.ANSWER);
        addRecords(response, result.authority, Section.AUTHORITY);

        Header respHeader = response.getHeader();
        if (result.tc) respHeader.setFlag(Flags.TC);
        if (result.rd) respHeader.setFlag(Flags.RD);
        if (result.ra) respHeader.setFlag(Flags.RA);
        if (result.ad) respHeader.setFlag(Flags.AD);
        if (result.cd) respHeader.setFlag(Flags.CD);
        respHeader.setRcode(result.status);

        return response;
    }

    public static ResolveResult fetch(Name name, int rrType, String edns) throws IOException {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder.scheme("https").host("dns.google.com").addPathSegments("/resolve")
                .addQueryParameter("name", name.toString())
                .addQueryParameter("type", Type.string(rrType));
        if (edns != null)
            urlBuilder.addQueryParameter("edns_client_subnet", edns);

        Request httpRequest = new Request.Builder()
                .url(urlBuilder.build())
                .build();
        Response httpResponse = client.newCall(httpRequest).execute();
        return ResolveResult.fromString(httpResponse.body().string());
    }

    public static ResolveResult fetch(Name name, int rrType) throws IOException {
        return fetch(name, rrType, null);
    }

    private static void addRecords(Message message, List<ResolveResult.Record> records, int section) throws IOException {
        if (records == null) return;

        for (ResolveResult.Record record : records) {
            Record rec;
            if (section == Section.QUESTION)
                rec = Record.newRecord(Name.fromString(record.name), record.type, DClass.IN);
            else
                rec = Record.fromString(
                        Name.fromString(record.name),
                        record.type,
                        DClass.IN,
                        record.ttl == null ? 0 : record.ttl,
                        record.data,
                        Name.root
                );
            message.addRecord(rec, section);
        }
    }
}
