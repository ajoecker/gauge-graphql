package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.BasicAuthentication;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.github.ajoecker.gauge.services.login.TokenBasedLogin;

public final class Registry {
    public enum LoginType {
        token, basic
    }

    private static LoginHandler loginHandler;
    private static Connector connector;

    private Registry() {

    }

    public static void setConnector(Connector connector) {
        Registry.connector = connector;
    }

    static Connector getConnector() {
        return connector;
    }

    static LoginHandler getLoginHandler() {
        return loginHandler;
    }

    public static void setLoginHandler(String type) {
        switch (LoginType.valueOf(type)) {
            case basic:
                loginHandler = new BasicAuthentication();
                return;

            case token:
                loginHandler = new TokenBasedLogin();
                return;

            default:
                throw new IllegalArgumentException("unknown type for login: " + type);

        }
    }
}
