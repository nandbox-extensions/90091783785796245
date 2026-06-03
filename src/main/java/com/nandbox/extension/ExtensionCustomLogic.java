package com.nandbox.extension;

import com.nandbox.bots.api.Nandbox;
import com.nandbox.bots.api.NandboxClient;
import com.nandbox.bots.api.data.*;
import com.nandbox.bots.api.inmessages.*;
import com.nandbox.bots.api.outmessages.*;
import com.nandbox.bots.api.util.*;
import com.nandbox.bots.api.test.*;
import net.minidev.json.*;
import net.minidev.json.parser.JSONParser;
import com.nandbox.extension.ExtensionAdapter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ExtensionCustomLogic extends ExtensionAdapter {
    private Nandbox.Api api;

    public static void main(String[] args) throws Exception {
        String TOKEN = "";
	Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            properties.load(input);
            TOKEN = properties.getProperty("Token");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }

        NandboxClient client = NandboxClient.get();
        client.connect(TOKEN, new ExtensionCustomLogic());
    }

    @Override
    public void onConnect(Nandbox.Api api) {
        this.api = api;
    }

    @Override
    public void onReceive(IncomingMessage incomingMessage) {
        if (incomingMessage == null) {
            return;
        }

        try {
            Chat chat = incomingMessage.getChat();
            User from = incomingMessage.getFrom();
            if (chat == null || from == null) {
                return;
            }

            String text = incomingMessage.getText();
            if (text == null) {
                text = "";
            }
            text = text.trim();

            String title = "message";
            String body = text;

            sendPushNotificationOnly(from, title, body);
        } catch (Exception e) {
            try {
                e.printStackTrace();
            } catch (Exception ex) {
            }
        }
    }

    private void sendPushNotificationOnly(User toUser, String title, String body) {
        if (toUser == null) {
            return;
        }
        String userId = toUser.getId();
        String safeTitle = title == null ? "" : title;
        String safeBody = body == null ? "" : body;

        StringBuilder sb = new StringBuilder();
        sb.append("PUSH_NOTIFICATION_ONLY ");
        sb.append("userId=").append(userId);
        sb.append(" title=").append(escapeForLog(safeTitle));
        sb.append(" body=").append(escapeForLog(safeBody));

        System.out.println(sb.toString());
    }

    private String escapeForLog(String s) {
        if (s == null) {
            return "";
        }
        String r = s;
        r = r.replace("\\r", " ");
        r = r.replace("\\n", " ");
        r = r.replace("\\t", " ");
        return r;
    }
}
