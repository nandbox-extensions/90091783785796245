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
        if (incomingMessage == null || api == null) {
            return;
        }

        try {
            User from = incomingMessage.getFrom();
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

            String appId = incomingMessage.getAppId();
            if (appId == null) {
                appId = "";
            }

            String text = incomingMessage.getText();
            if (text == null) {
                text = "";
            }
            text = text.trim();

            String title = "message";

            Object notificationType = resolveNotificationType();
            if (notificationType == null) {
                return;
            }

            invokeSendNotification(userId, notificationType, title, text, appId);
        } catch (Exception e) {
            try {
                e.printStackTrace();
            } catch (Exception ex) {
            }
        }
    }

    private Object resolveNotificationType() {
        try {
            Class ntClass = Class.forName("com.nandbox.bots.api.data.NotificationType");
            if (ntClass.isEnum()) {
                try {
                    return Enum.valueOf(ntClass, "PUSH");
                } catch (Exception e) {
                }
                try {
                    return Enum.valueOf(ntClass, "PUSH_NOTIFICATION");
                } catch (Exception e) {
                }
                try {
                    return Enum.valueOf(ntClass, "NOTIFICATION");
                } catch (Exception e) {
                }
                try {
                    Object[] values = ntClass.getEnumConstants();
                    if (values != null && values.length > 0) {
                        return values[0];
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private void invokeSendNotification(long userId, Object notificationType, String title, String message, String appId) {
        if (api == null) {
            return;
        }

        String safeTitle = title == null ? "" : title;
        String safeMessage = message == null ? "" : message;
        String safeAppId = appId == null ? "" : appId;

        try {
            java.lang.reflect.Method m = api.getClass().getMethod(
                    "sendNotification",
                    new Class[] { Long.TYPE, notificationType.getClass(), String.class, String.class, String.class }
            );
            m.invoke(api, new Object[] { new Long(userId), notificationType, safeTitle, safeMessage, safeAppId });
            return;
        } catch (Exception e) {
        }

        try {
            java.lang.reflect.Method[] methods = api.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                java.lang.reflect.Method mm = methods[i];
                if (!"sendNotification".equals(mm.getName())) {
                    continue;
                }
                Class[] p = mm.getParameterTypes();
                if (p == null || p.length != 5) {
                    continue;
                }
                if (p[0] != Long.TYPE) {
                    continue;
                }
                if (p[2] != String.class || p[3] != String.class || p[4] != String.class) {
                    continue;
                }
                if (!p[1].isInstance(notificationType)) {
                    continue;
                }
                mm.invoke(api, new Object[] { new Long(userId), notificationType, safeTitle, safeMessage, safeAppId });
                return;
            }
        } catch (Exception e) {
            try {
                e.printStackTrace();
            } catch (Exception ex) {
            }
        }
    }
}
