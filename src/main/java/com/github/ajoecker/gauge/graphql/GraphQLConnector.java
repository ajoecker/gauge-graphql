package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.graphql.login.LoginHandler;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * A simple connector to send graphql posts via restassured
 */
public final class GraphQLConnector {
    public Response sending(String query) {
        return post(query, startRequest());
    }

    public Response sendingWithLogin(String query, LoginHandler loginHandler) {
        RequestSpecification request = startRequest();
        loginHandler.setLogin(request);
        return post(query, request);
    }

    private Response post(String query, RequestSpecification request) {
        return request.contentType(ContentType.JSON)
                .body(Map.of("query", query))
                .when()
                .post(System.getenv("graphql.endpoint"));
    }

    private RequestSpecification startRequest() {
        RequestSpecification request = given();
        if (Boolean.valueOf(System.getenv("graphql.debug"))) {
            request.log().all();
        }
        return request;
    }
}
