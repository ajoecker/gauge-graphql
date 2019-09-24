package com.github.ajoecker.gauge.graphql;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.ajoecker.gauge.graphql.Util.isMap;
import static com.github.ajoecker.gauge.graphql.Util.replaceVariablesInQuery;
import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {
    @Test
    public void recognisesMap() {
        String mapString = "{name: Banksy, nationality: British}";
        assertTrue(isMap(mapString));
    }

    @Test
    public void ignoresNonMap() {
        String mapString = "whatever";
        assertFalse(isMap(mapString));
    }

    @Test
    public void parsesSingleMap() {
        String mapString = "{name: Banksy, nationality: British}";
        List<Map<String, String>> parse = Util.parseMap(mapString);
        assertEquals(parse, List.of(Map.of("name", "Banksy", "nationality", "British")));
    }

    @Test
    public void parsesMultiMap() {
        String mapString = "{name: Banksy, nationality: British}, {name: Pablo Picasso, nationality: Spanish}";
        List<Map<String, String>> parse = Util.parseMap(mapString);
        assertEquals(parse, List.of(
                Map.of("name", "Banksy", "nationality", "British"),
                Map.of("name", "Pablo Picasso", "nationality", "Spanish")
        ));
    }

    @Test
    public void replaceVariablesInQueryWorks() {
        String query = "{\n" +
                "    popular_artists(size: $size) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String queryReplaced = "{\n" +
                "    popular_artists(size: 2) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        assertEquals(replaceVariablesInQuery(query, "size:2", Optional.empty()), queryReplaced);
    }

    @Test
    public void replaceVariablesInQueryWithOwnFormat() {
        String query = "{\n" +
                "    popular_artists(size: ##size) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String queryReplaced = "{\n" +
                "    popular_artists(size: 2) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Util.configurationSource = new ConfigurationSource() {
            @Override
            public String variableMask() {
                return "##";
            }
        };
        assertEquals(replaceVariablesInQuery(query, "size:2", Optional.empty()), queryReplaced);
    }

    @Test
    public void replaceVariablesInQueryWithVariablesWorks() {
        String query = "{\n" +
                "    popular_artists(size: $size) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String queryReplaced = "{\n" +
                "    popular_artists(size: 2) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ExtractableResponse<Response> re = Mockito.mock(ExtractableResponse.class);
        Mockito.when(re.path("data.foo")).thenReturn(2);

        assertEquals(replaceVariablesInQuery(query, "size:$foo", Optional.of(re)), queryReplaced);
    }

    @BeforeEach
    private void reset() {
        Util.configurationSource = new ConfigurationSource() {
        };
    }
}
