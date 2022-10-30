package appeng.libs.micromark.html;

import appeng.libs.micromark.Token;
import org.jetbrains.annotations.Nullable;

/**
 * HTML compiler context
 */
public interface CompileContext {
    /**
     * Configuration passed by the user.
     */
    CompileOptions getOptions();

    default boolean has(HtmlContextProperty<?> property) {
        return get(property) != null;
    }

    /**
     * Get data from the key-value store.
     */
    @Nullable
    <T> T get(HtmlContextProperty<T> property);

    /**
     * Set data in the extension data.
     */
    <T> void set(HtmlContextProperty<T> property, T value);

    /**
     * Remove data from the extension data.
     */
    void remove(HtmlContextProperty<?> property);

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

    void setSlurpOneLineEnding(boolean enable);

    void setSlurpAllLineEndings(boolean enable);
}
