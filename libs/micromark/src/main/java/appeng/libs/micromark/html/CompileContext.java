package appeng.libs.micromark.html;

import appeng.libs.micromark.Token;

/**
 * HTML compiler context
 */
public interface CompileContext {
    /**
     * Configuration passed by the user.
     */
    CompileOptions getOptions();

    /**
     * Set data into the key-value store.
     */
    void setData(String key, Object value);

    /**
     * Get data from the key-value store.
     */
    Object getData(String key);

    /**
     * Output an extra line ending if the previous value wasn’t EOF/EOL.
     */
    void lineEndingIfNeeded();

    /**
     * Make a value safe for injection in HTML (except w/ `ignoreEncode`).
     */
    String encode(String value);

    /**
     * Capture some of the output data.
     */
    void buffer();

    /**
     * Stop capturing and access the output data.
     */
    String resume();

    /**
     * Output raw data.
     */
    void raw(String value);

    /**
     * Output (parts of) HTML tags.
     */
    void tag(String value);

    /**
     * Get the string value of a token
     */
    String sliceSerialize(Token token);
}
