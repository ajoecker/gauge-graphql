package com.github.ajoecker.gauge.graphql;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * A simple connector to send graphql posts via restassured
 */
public final class GraphQLConnector extends Connector {

    public Response post(String query, RequestSpecification request) {
        return request.contentType(ContentType.JSON)
                .body(Map.of("query", query))
                .when()
                .post(getEndpoint());
    }

}
