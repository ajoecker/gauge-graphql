package com.github.ajoecker.gauge.graphql.login;

import com.github.ajoecker.gauge.graphql.Connector;
import com.google.common.base.Strings;
import io.restassured.specification.RequestSpecification;

public class BasicAuthentication implements LoginHandler {
    private String user;
    private String password;

    @Override
    public void setLogin(RequestSpecification request) {
        if (!Strings.isNullOrEmpty(user) && !Strings.isNullOrEmpty(password)) {
            request.auth().basic(user, password);
        }
    }

    @Override
    public void loginWithCredentials(String user, String password, Connector connector) {
        this.user = user;
        this.password = password;
    }

    @Override
    public void loginWithNoCredentials(Connector connector) {
        this.user = System.getenv("graphql.user");
        this.password = System.getenv("graphql.password");
    }
}
