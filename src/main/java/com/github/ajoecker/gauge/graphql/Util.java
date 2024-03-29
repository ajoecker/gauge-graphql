package com.github.ajoecker.gauge.graphql;

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableCell;
import com.thoughtworks.gauge.TableRow;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Utility class
 */
public final class Util {
    private static final String COMMA_SEPERATED;

    // test-friendly
    static ConfigurationSource configurationSource = new ConfigurationSource() {
    };

    static {
        COMMA_SEPERATED = "\\s*" + seperator() + "\\s*";
    }

    private Util() {
        // utility class --> static
    }

    /**
     * Returns the seperator defined in {@link #configurationSource}
     *
     * @return the seperator
     * @see ConfigurationSource#seperator()
     */
    public static String seperator() {
        return configurationSource.seperator();
    }

    /**
     * Replaces all variables in the given query based on the given variables
     *
     * @param query     the query containing variables
     * @param variables the values of the variables
     * @param response  an optional response from previous request for retrieving paths for current request to substitute variables
     * @return the actual query
     */
    public static String replaceVariablesInQuery(String query, String variables, Optional<ExtractableResponse<Response>> response) {
        String[] split = split(variables);
        for (String s : split) {
            String[] keyValue = s.split(configurationSource.variableSeperator());
            String replacement = extractReplacement(response, keyValue[1]);
            query = doReplace(query, keyValue[0], replacement);
        }
        return query;
    }

    private static String extractReplacement(Optional<ExtractableResponse<Response>> response, String replacement) {
        if (response.isPresent() && configurationSource.isMasked(replacement)) {
            return extractPathFromPreviousRequest(response.get(), replacement);
        }
        return replacement;
    }

    private static String extractPathFromPreviousRequest(ExtractableResponse<Response> response, String replacement) {
        String variablePath = configurationSource.unmask(replacement);
        Object path = response.path(prefix(variablePath));
        if (path instanceof List) {
            throw new IllegalArgumentException("variable path " + variablePath + " is not a single value, but a list: " + path);
        }
        return path.toString();
    }

    private static String doReplace(String query, String key, String replacement) {
        return query.replace(configurationSource.mask(key.trim()), replacement.trim());
    }

    /**
     * Replaces all variables in the given query based on the given variables
     *
     * @param query     the query containing variables
     * @param variables the values of the variables as a gauge table
     * @param response  an optional response from previous request for retrieving paths for current request to substitute variables
     * @return the actual query
     */
    public static String replaceVariablesInQuery(String query, Table variables, Optional<ExtractableResponse<Response>> response) {
        List<TableRow> tableRows = variables.getTableRows();
        for (TableRow row : tableRows) {
            query = doReplace(query, row.getCell("name"), extractReplacement(response, row.getCell("value")));
        }
        return query;
    }

    /**
     * Splits the given string based on {@link #COMMA_SEPERATED}
     *
     * @param stringValue value to be splitted
     * @return the split array
     */
    static String[] split(String stringValue) {
        return stringValue.trim().split(COMMA_SEPERATED);
    }

    /**
     * Parses a {@link List} of {@link Map}s out of a string in the format of
     * <pre>
     *     {name: Pablo Picasso, nationality: Spanish}, {name: Banksy, nationality: British}
     * </pre>
     *
     * @param value map like string
     * @return list of maps with a single key mapping
     */
    static List<Map<String, String>> parseMap(String value) {
        String[] values = value.trim().split("}" + COMMA_SEPERATED);
        return stream(values).map(Util::toMap).collect(Collectors.toList());
    }

    private static Map<String, String> toMap(String full) {
        String prepared = full.replace("{", "").replace("}", "");
        return stream(prepared.split(COMMA_SEPERATED))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim()));
    }

    /**
     * Returns whether the given string is a map representation as expected in {@link #parseMap(String)}
     *
     * @param value the value
     * @return whether the value is a map representation
     */
    static boolean isMap(String value) {
        return value.contains("{") && value.contains("}");
    }

    /**
     * Returns a {@link Map} out from the given Gauge {@link TableRow}
     *
     * @param tableRow the table row
     * @return a map with the key = the table header and value = table cell value
     */
    static Map<String, String> fromTable(TableRow tableRow) {
        return tableRow.getTableCells().stream().collect(Collectors.toMap(TableCell::getColumnName, TableCell::getValue));
    }

    /**
     * Prefixes the path with <code>data.</code> if the path does not already start with that prefix
     *
     * @param dataPath the json path
     * @return json path with guaranteed <code>data.</code> at beginning
     */
    static String prefix(String dataPath) {
        return !dataPath.startsWith("data.") ? "data." + dataPath : dataPath;
    }
}
