package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.graphql.login.LoginHandler;
import com.github.ajoecker.gauge.graphql.login.TokenBasedLogin;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.ajoecker.gauge.graphql.Util.*;
import static org.hamcrest.Matchers.*;

/**
 * The class provides the implementation of the gauge specs to validate different graphql queries.
 */
public class GaugeGraphQL {
    private Response response;
    private final LoginHandler loginHandler = new TokenBasedLogin();
    private final GraphQLConnector graphQLConnector = new GraphQLConnector();
    private Optional<ExtractableResponse<Response>> previousResponse;

    @Step("When sending <query>")
    public void sending(String query) {
        response = graphQLConnector.sendingWithLogin(query, loginHandler);
        previousResponse = Optional.of(response.then().extract());
    }

    @Step({"When sending <query> with <variables>", "And sending <query> with <variables>"})
    public void sendingWithVariables(String query, Object variables) {
        if (variables instanceof String) {
            sending(replaceVariablesInQuery(query, (String) variables, previousResponse));
        } else if (variables instanceof Table) {
            sending(replaceVariablesInQuery(query, (Table) variables, previousResponse));
        } else {
            throw new IllegalArgumentException("unknown variable types " + variables.getClass() + " for " + variables);
        }
    }

    @Step("Given <user> logs in with password <password>")
    public void login(String user, String password) {
        loginHandler.loginWithCredentials(user, password, graphQLConnector);
    }

    @Step("Given user logs in")
    public void loginWIthNoCredentials() {
        loginHandler.loginWithNoCredentials(graphQLConnector);
    }

    @Step({"Then <path> must contain <value>", "And <path> must contain <value>"})
    public void thenMustContains(String dataPath, Object value) {
        compare(value, items -> {
            if (response.then().extract().path(prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, hasItems(items));
            } else {
                assertResponse(dataPath, hasItem(items[0]));
            }
        });
    }

    @Step({"Then <path> must be <value>", "And <path> must be <value>"})
    public void thenMustBe(String dataPath, Object value) {
        compare(value, items -> {
            if (response.then().extract().path(prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, containsInAnyOrder(items));
            } else {
                assertResponse(dataPath, is(items[0]));
            }
        });
    }

    @Step("Use <endpoint>")
    public void useEndpoint(String enpoint) {
        graphQLConnector.setEndpoint(enpoint);
    }

    private void compare(Object value, Consumer<Object[]> match) {
        if (value instanceof String) {
            compareStringValue((String) value, match);
        } else if (value instanceof Table) {
            List<Map<String, String>> expected = ((Table) value).getTableRows().stream().map(Util::fromTable).collect(Collectors.toList());
            match.accept(expected.toArray(new Map[expected.size()]));
        }
    }

    private void compareStringValue(String value, Consumer<Object[]> match) {
        String stringValue = value;
        if (isMap(stringValue)) {
            List<Map<String, String>> expected = parseMap(stringValue);
            match.accept(expected.toArray(new Map[expected.size()]));
        } else {
            List<String> expected = Arrays.asList(split(stringValue));
            match.accept(expected.toArray(new String[expected.size()]));
        }
    }

    @Step({"Then <dataPath> must be empty", "And <dataPath> must be empty"})
    public void thenEmpty(String dataPath) {
        assertResponse(dataPath, empty());
    }

    private void assertResponse(String path, Matcher<?> matcher) {
        response.then().assertThat().body(prefix(path), matcher);
    }

    @AfterScenario
    public void clearResponse() {
        previousResponse = Optional.empty();
    }
}
