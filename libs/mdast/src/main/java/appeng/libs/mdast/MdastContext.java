package appeng.libs.mdast;

import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.micromark.Token;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * mdast compiler context
 */
public interface MdastContext {
    record TokenStackEntry(Token token, @Nullable OnEnterError onError) {
    }

    @FunctionalInterface
    interface OnEnterError {
        void error(MdastContext context, @Nullable Token left, Token right);
    }

    @FunctionalInterface
    interface OnExitError {
        void error(MdastContext context, Token left, Token right);
    }

    List<MdAstNode> getStack();

    List<MdastCompiler.TokenStackEntry> getTokenStack();

    void buffer();

    /**
     * Stop capturing and access the output data.
     */
    String resume();

    /**
     * Enter a token.
     */
    default <N extends MdAstNode> N enter(N node, Token token) {
        return enter(node, token, null);
    }

    /**
     * Enter a token.
     */
    <N extends MdAstNode> N enter(N node, Token token, OnEnterError onError);

    /**
     * Exit a token.
     */
    default MdAstNode exit(Token token) {
        return exit(token, null);
    }

    /**
     * Exit a token.
     */
    MdAstNode exit(Token token, @Nullable OnExitError onError);

    /**
     * Get the string value of a token
     */
    String sliceSerialize(Token token);


    default boolean has(MdastContextProperty<?> property) {
        return get(property) != null;
    }

    /**
     * Get data from the key-value store.
     */
    @Nullable <T> T get(MdastContextProperty<T> property);

    /**
     * Set data in the extension data.
     */
    <T> void set(MdastContextProperty<T> property, T value);

    /**
     * Remove data from the extension data.
     */
    void remove(MdastContextProperty<?> property);
}
