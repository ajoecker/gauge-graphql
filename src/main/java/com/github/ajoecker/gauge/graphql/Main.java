package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.graphql.login.BasicAuthentication;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class Main {
    public static void main(String[] args) {
        Connector connector = new Connector() {
            @Override
            public Response post(String query, RequestSpecification request) {
                throw new RuntimeException(new NoSuchMethodException("do not know how post is"));
            }
        };

        connector.setEndpoint("http://dunning-stage.nexible.de/api/v1");

        GaugeGraphQL gaugeGraphQL = new GaugeGraphQL(connector, new BasicAuthentication());
        gaugeGraphQL.login("nx", "w5gzbQZM9PnaSYeq");
        gaugeGraphQL.get("contracts/GER-V-KR-312");
        gaugeGraphQL.thenMustBe("status", "Kein Mahnverfahren");
    }
}
