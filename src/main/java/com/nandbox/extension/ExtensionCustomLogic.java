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
        if (incomingMsg == null) {
            return;
        }

        try {
            Chat chat = incomingMsg.getChat();
            User from = incomingMsg.getFrom();
            if (chat == null || from == null) {
                return;
            }

            String chatId = chat.getId();
            String text = incomingMsg.getText();
            String reference = Utils.getUniqueId();
            String userId = from.getId();
            String appId = incomingMsg.getAppId();
            Integer chatSettings = incomingMsg.getChatSettings();

            if (text == null) {
                text = "";
            }
            text = text.trim();

            String response;
            if (text.length() == 0) {
                response = "message\n";
            } else {
                response = "message\n" + text;
            }

            api.sendText(
                chatId,
                response,
                reference,
                null,
                userId,
                0,
                false,
                chatSettings,
                null,
                null,
                null,
                appId
            );
        } catch (Exception e) {
            try {
                e.printStackTrace();
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onReceive(JSONObject obj) {
        if (obj == null) {
            return;
        }
        try {
            if (obj.containsKey("message") || obj.containsKey("chat") || obj.containsKey("from")) {
                return;
            }
        } catch (Exception e) {
        }
    }
}
