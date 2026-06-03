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
            User from = incomingMessage.getFrom();
            if (from == null) {
                return;
            }

            String userId = from.getId();
            if (userId == null || userId.length() == 0) {
                return;
            }

            String text = incomingMessage.getText();
            if (text == null) {
                text = "";
            }
            text = text.trim();

            String title = "message";
            String body = text;

            sendUserNotificationViaReflection(userId, title, body);
        } catch (Exception e) {
            try {
                e.printStackTrace();
            } catch (Exception ex) {
            }
        }
    }

    private void sendUserNotificationViaReflection(String userId, String title, String body) {
        if (api == null) {
            return;
        }

        String safeTitle = title == null ? "" : title;
        String safeBody = body == null ? "" : body;

        try {
            java.lang.reflect.Method[] methods = api.getClass().getMethods();
            java.lang.reflect.Method candidate = null;

            for (int i = 0; i < methods.length; i++) {
                java.lang.reflect.Method m = methods[i];
                if (!"sendUserNotification".equals(m.getName())) {
                    continue;
                }
                Class[] p = m.getParameterTypes();
                if (p == null) {
                    continue;
                }

                if (p.length == 3 && p[0] == String.class && p[1] == String.class && p[2] == String.class) {
                    candidate = m;
                    break;
                }

                if (p.length == 4 && p[0] == String.class && p[1] == String.class && p[2] == String.class && p[3] == String.class) {
                    candidate = m;
                    break;
                }

                if (p.length == 5 && p[0] == String.class && p[1] == String.class && p[2] == String.class && p[3] == String.class && p[4] == String.class) {
                    candidate = m;
                    break;
                }
            }

            if (candidate == null) {
                System.err.println("sendUserNotification method not found in this SDK version.");
                return;
            }

            int len = candidate.getParameterTypes().length;
            if (len == 3) {
                candidate.invoke(api, new Object[] { userId, safeTitle, safeBody });
            } else if (len == 4) {
                candidate.invoke(api, new Object[] { userId, safeTitle, safeBody, Utils.getUniqueId() });
            } else {
                candidate.invoke(api, new Object[] { userId, safeTitle, safeBody, Utils.getUniqueId(), null });
            }
        } catch (Exception e) {
            try {
                e.printStackTrace();
            } catch (Exception ex) {
            }
        }
    }
}
