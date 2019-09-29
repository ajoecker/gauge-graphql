package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.graphql.login.LoginHandler;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public abstract class Connector {
    private String endpoint;

    public Connector() {
        setEndpoint(System.getenv("graphql.endpoint"));
    }

    public final String getEndpoint() {
        return endpoint;
    }

    public final void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public final Response sending(String query) {
        return post(query, startRequest());
    }

    public final Response get(String query) {
        return get(query, startRequest());
    }

    public final Response sendingWithLogin(String query, LoginHandler loginHandler) {
        RequestSpecification request = startRequest();
        loginHandler.setLogin(request);
        return post(query, request);
    }

    public final Response getWithLogin(String query, LoginHandler loginHandler) {
        RequestSpecification request = startRequest();
        loginHandler.setLogin(request);
        return get(query, request);
    }

    abstract Response post(String query, RequestSpecification request);

    public final Response get(String query, RequestSpecification request) {
        return request.contentType(ContentType.JSON)
                .when()
                .get(getEndpoint() + "/" + query);
    }

    private RequestSpecification startRequest() {
        RequestSpecification request = given();
        if (Boolean.valueOf(System.getenv("graphql.debug"))) {
            request.log().all();
        }
        return request;
    }
}
