package com.github.ajoecker.gauge.graphql;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class Main {
    public static void main(String[] args) {
        Connector connector = new Connector() {
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
                        .get("http://dunning-stage.nexible.de/api/v1/" + query);
            }
        };



        RequestSpecification given = given();
        given.log().all();
        given.auth().basic("nx", "w5gzbQZM9PnaSYeq");
        Response response = connector.get("contracts/GER-V-KR-312", given);
        JsonPath jsonPath = response.then().extract().jsonPath();
        System.out.println(jsonPath.prettyPrint());
    }
}
