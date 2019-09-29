package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.graphql.login.LoginHandler;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public interface Connector {
    default Response sending(String query) {
        return post(query, startRequest());
    }

    default Response sendingWithLogin(String query, LoginHandler loginHandler) {
        RequestSpecification request = startRequest();
        loginHandler.setLogin(request);
        return post(query, request);
    }

    Response post(String query, RequestSpecification request);

    default Response get(String query, RequestSpecification request) {

    }

    private RequestSpecification startRequest() {
        RequestSpecification request = given();
        if (Boolean.valueOf(System.getenv("graphql.debug"))) {
            request.log().all();
        }
        return request;
    }
}
