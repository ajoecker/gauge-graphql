package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Connector;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * A simple connector to send graphql posts via restassured
 */
public final class RestConnector extends Connector {
    public Response post(String query, RequestSpecification request) {
        System.out.println("sending " + query + " to " + getEndpoint());
        return request.contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(query)
                .when()
                .post(getEndpoint());
    }
}
