package com.github.ajoecker.gauge.graphql;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * A simple connector to send graphql posts via restassured
 */
public final class GraphQLConnector implements Connector {
    @Override
    public Response post(String query, RequestSpecification request) {
        return request.contentType(ContentType.JSON)
                .body(Map.of("query", query))
                .when()
                .post(System.getenv("graphql.endpoint"));
    }

    @Override
    public Response get(String query, RequestSpecification request) {
        return request.contentType(ContentType.JSON)
                .when()
                .get(System.getenv("graphql.endpoint"), query);
    }

    private RequestSpecification startRequest() {
        RequestSpecification request = given();
        if (Boolean.valueOf(System.getenv("graphql.debug"))) {
            request.log().all();
        }
        return request;
    }
}
