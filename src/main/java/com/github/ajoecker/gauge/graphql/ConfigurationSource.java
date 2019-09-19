package com.github.ajoecker.gauge.graphql;

import com.google.common.base.Strings;

/**
 * Interface to define multiple configurations.
 */
public interface ConfigurationSource {
    /**
     * Returns the string that masks a variable in the graphql query file.
     * <p>
     * Default string is <code>$$</code> and is read in a Gauge project via the environment variable
     * <code>graphql.variable.mask</code>. If this variable is not existing or empty, the default value is taken.
     * <p>
     * For example
     * <pre>
     *     {
     *     popular_artists(size: $$size$$) {
     *         artists {
     *             name
     *             nationality
     *         }
     *     }
     * }
     * </pre>
     *
     * @return the mask or <code>$$</code> if non is defined
     */
    default String variableMask() {
        String mask = System.getenv("graphql.variable.mask");
        return Strings.isNullOrEmpty(mask) ? "$$" : mask;
    }

    /**
     * Returns the string that seperates multiple values e.g. when verifying a response.
     * <p>
     * The default value is <code>,</code> and is read in a Gauge project via the environment variable
     * <code>graphql.seperator</code>. If this variable is not existing or empty, the default value is taken.
     * <p>
     * For example
     * <pre>
     *     * Then "popular_artists.artists.name" must contain "Pablo Picasso, Banksy"
     * </pre>
     *
     * @return the seperator between multiple values
     */
    default String seperator() {
        String seperator = System.getenv("graphql.seperator");
        return Strings.isNullOrEmpty(seperator) ? "," : seperator;
    }

    /**
     * Returns the string that seperates variables in the spec file to be replaced for an actual query.
     * <p>
     * The default value is <code>:</code> and is read in a Gauge project via the environment variable
     * <code>graphql.variable.seperator</code>. If this variable is not existing or empty, the default value is taken.
     * <p>
     * For example
     * <pre>
     *     * When sending &lt;file:/src/test/resources/query.graphql&gt; with "size:4"
     * </pre>
     *
     * @return the seperator between variables
     */
    default String variableSeperator() {
        String vSep = System.getenv("graphql.variable.seperator");
        return Strings.isNullOrEmpty(vSep) ? ":" : vSep;
    }

    /**
     * Masks a string with {@link #variableMask()} as prefix and suffix of this string
     *
     * @param s the string to mask
     * @return the masked string
     */
    default String mask(String s) {
        return variableMask() + s + variableMask();
    }
}
