package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.ServiceUtil;
import com.thoughtworks.gauge.BeforeSuite;

public class GraphQLSetup {
    @BeforeSuite
    public void before() {
        Registry.setConnector(new GraphQLConnector());
        Registry.setLoginHandler(ServiceUtil.orDefault("gauge.service.loginhandler", Registry.LoginType.token.toString()));
    }
}
