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
    public void onReceive(IncomingMessage incomingMsg) {
        if (incomingMsg == null || api == null) {
            return;
        }

        try {
            User from = incomingMsg.getFrom();
            if (from == null) {
                return;
            }

            String userIdStr = from.getId();
            if (userIdStr == null) {
                return;
            }
            userIdStr = userIdStr.trim();
            if (userIdStr.length() == 0) {
                return;
            }

            long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return;
            }

            String text = incomingMsg.getText();
            if (text == null) {
                text = "";
            }
            text = text.trim();

            String title = "message";
            String appId = incomingMsg.getAppId();
            if (appId == null) {
                appId = "";
            }

            api.sendNotification(userId, NandboxClient.NotificationType.Push, title, text, appId);
        } catch (Exception e) {
            try {
                e.printStackTrace();
            } catch (Exception ex) {
            }
        }
    }
}
