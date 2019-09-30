package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.ServiceUtil;
import com.thoughtworks.gauge.BeforeSuite;

public class GaugeRestSetup {
    @BeforeSuite
    public void before() {
        Registry.setConnector(new RestConnector());
        Registry.setLoginHandler(ServiceUtil.orDefault("gauge.service.loginhandler", Registry.LoginType.token.toString()));
    }
}
