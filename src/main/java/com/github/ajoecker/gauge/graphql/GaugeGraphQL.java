package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.graphql.login.LoginHandler;
import com.github.ajoecker.gauge.graphql.login.TokenBasedLogin;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import io.restassured.response.Response;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.ajoecker.gauge.graphql.Util.*;
import static org.hamcrest.Matchers.*;

/**
 * The class provides the implementation of the gauge specs to validate different graphql queries.
 */
public class GaugeGraphQL {
    private Response response;
    private final LoginHandler loginHandler;
    private final Connector connector;

    public GaugeGraphQL() {
        this(new GraphQLConnector(), new TokenBasedLogin());
    }

    GaugeGraphQL(Connector connector, LoginHandler loginHandler) {
        this.connector = connector;
        this.loginHandler = loginHandler;
    }

    @Step("When sending <query>")
    public void sending(String query) {
        response = connector.sendingWithLogin(query, loginHandler);
    }

    @Step("When sending <query> with <variables>")
    public void sendingWithVariables(String query, String variables) {
        response = connector.sendingWithLogin(replaceVariablesInQuery(query, variables), loginHandler);
    }

    @Step("Given <user> logs in with password <password>")
    public void login(String user, String password) {
        loginHandler.loginWithCredentials(user, password, connector);
    }

    @Step("Given user logs in")
    public void loginWIthNoCredentials() {
        loginHandler.loginWithNoCredentials(connector);
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

    private static String prefix(String dataPath) {
        return !dataPath.startsWith("data.") ? "data." + dataPath : dataPath;
    }

    @Step({"Then <dataPath> must be empty", "And <dataPath> must be empty"})
    public void thenEmpty(String dataPath) {
        assertResponse(dataPath, empty());
    }

    private void assertResponse(String path, Matcher<?> matcher) {
        response.then().assertThat().body(prefix(path), matcher);
    }
}
