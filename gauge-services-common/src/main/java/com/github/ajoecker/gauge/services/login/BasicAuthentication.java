package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.google.common.base.Strings;
import io.restassured.specification.RequestSpecification;

/**
 * {@link LoginHandler} that works based on simple basic authentication.
 * <p>
 * It retrieves user and password information from the environment variables
 * <code>gauge.service.user</code> and <code>gauge.service.password</code>.
 */
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
        this.user = System.getenv("gauge.service.user");
        this.password = System.getenv("gauge.service.password");
    }
}
