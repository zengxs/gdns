package com.matsuz.stupidns;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;

/**
 * Stupid DNS Server Starter
 *
 * @author Matsuz
 */
public class Server {
    DatagramChannel channel;
    Selector selector;

    static OkHttpClient client;
    static Gson gson;

    Server() {
        try {
            channel = DatagramChannel.open(); // open a udp channel
            channel.configureBlocking(false); // 设置为非阻塞通道
            channel.socket().bind(new InetSocketAddress(53)); // 绑定端口

            // 打开一个选择器
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        client = new OkHttpClient();
        gson = new Gson();
    }

    /**
     * Java NIO Network Programming
     * See: http://blog.csdn.net/chenxuegui1234/article/details/17981203
     */
    void work() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(65535);
        while (true) {
            try {
                // 进行选择
                int n = selector.select();
                if (n > 0) {
                    Iterator iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = (SelectionKey) iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            DatagramChannel datagramChannel = (DatagramChannel) key.channel();

                            // read
                            byteBuffer.clear();
                            InetSocketAddress socket = (InetSocketAddress) datagramChannel.receive(byteBuffer);
                            byteBuffer.clear();  // work fine after add this line, why?
                            Message request = new Message(byteBuffer);

                            // write
                            byteBuffer.clear();  // clear buffer
                            byteBuffer.put(resolve(
                                    request, socket.getAddress()
                            ).toWire());
                            byteBuffer.flip();

                            // send
                            datagramChannel.send(byteBuffer, socket);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().work();
    }

    private Message resolve(Message request, InetAddress address) {
        Message response = new Message();
        Header reqHeader = request.getHeader(), respHeader = response.getHeader();

        // message initialize
        respHeader.setID(reqHeader.getID()); // set transaction id
        respHeader.setFlag(Flags.QR); // mark the message as a response

        try {
            Request httpRequest = new Request.Builder()
                    .url(toUrl(request.getQuestion().getName(), request.getQuestion().getType(), address))
                    .build();
            Response httpResponse = client.newCall(httpRequest).execute();

            DNSJsonResult result = gson.fromJson(httpResponse.body().string(), DNSJsonResult.class);
            respHeader.setRcode(result.status); // Status
            // flags
            if (result.tc) respHeader.setFlag(Flags.TC);
            if (result.rd) respHeader.setFlag(Flags.RD);
            if (result.ra) respHeader.setFlag(Flags.RA);
            if (result.ad) respHeader.setFlag(Flags.AD);
            if (result.cd) respHeader.setFlag(Flags.CD);

            // message body
            addRecord(response, result.question, Section.QUESTION);
            addRecord(response, result.answer, Section.ANSWER);
            addRecord(response, result.authority, Section.AUTHORITY);

        } catch (IOException | JsonParseException | IllegalArgumentException e) {
            respHeader.setRcode(Rcode.SERVFAIL);    // Serve Failure
            System.err.printf("Name: %s, Type: %s\n",
                    request.getQuestion().getName().toString(),
                    Type.string(request.getQuestion().getType()));
            e.printStackTrace();
        }

        return response;
    }

    private static void addRecord(Message message, List<DNSJsonResult.Record> records, int section) throws IOException {
        if (records == null) return;

        Record rec;
        for (DNSJsonResult.Record record : records) {
            if (section == Section.QUESTION)
                rec = Record.newRecord(Name.fromString(record.name), record.type, DClass.IN);
            else
                rec = Record.fromString(
                        Name.fromString(record.name),
                        record.type, // dns type
                        DClass.IN, // default class is IN
                        record.ttl == null ? 0 : record.ttl, // default ttl is 0
                        record.data, // rdata
                        Name.empty); // $ORIGIN name
            message.addRecord(rec, section);
        }
    }

    private static HttpUrl toUrl(Name name, int type, InetAddress address) {
        return new HttpUrl.Builder()
                /* https://dns.google.com/resolve */
                .scheme("https").host("dns.google.com").addPathSegments("/resolve")
                .addQueryParameter("name", name.toString())
                .addQueryParameter("type", Type.string(type))
                .addQueryParameter("edns_client_subnet", address.getHostAddress())
                .build();
    }
}
