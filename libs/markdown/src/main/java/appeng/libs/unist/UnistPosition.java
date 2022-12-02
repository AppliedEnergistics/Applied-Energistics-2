package appeng.libs.unist;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the location of a node in a source file.
 * <p>
 * If the syntactic unit represented by a node is not present in the source file at the time of parsing, the
 * node is said to be generated, and it must not have positional information.
 */
public interface UnistPosition {
    /**
     * The place of the first character of the parsed source region.
     */
    UnistPoint start();

    /**
     * The place of the first character after the parsed source region, whether it exists or not.
     */
    UnistPoint end();

    /**
     * The start column at each index (plus start line) in the source region, for elements that span multiple lines.
     */
    int @Nullable [] indent(); // number >= 1
}
