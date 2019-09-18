package com.github.gauge.graphql;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.github.gauge.graphql.Util.isMap;
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
    public void replaceVariablesInQuery() {
        String query = "{\n" +
                "    popular_artists(size: $$size$$) {\n" +
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
        String s = Util.replaceVariablesInQuery(query, "size:2");
        assertEquals(s, queryReplaced);
    }

    @Test
    public void replaceVariablesInQueryWithOwnFormat() {
        String query = "{\n" +
                "    popular_artists(size: ##size##) {\n" +
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
        String s = Util.replaceVariablesInQuery(query, "size:2");
        assertEquals(s, queryReplaced);
    }
}
