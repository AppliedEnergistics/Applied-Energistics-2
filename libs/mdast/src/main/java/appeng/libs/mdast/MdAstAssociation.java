package appeng.libs.mdast;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an internal relation from one node to another.
 */
public interface MdAstAssociation {

    /**
     * An identifier field must be present. It can match another node. identifier is a source value: character escapes and character references are not parsed. Its value must be normalized.
     * <p>
     * To normalize a value, collapse markdown whitespace ([\t\n\r ]+) to a space, trim the optional initial and/or final space, and perform case-folding.
     * <p>
     * Whether the value of identifier (or normalized label if there is no identifier) is expected to be a unique identifier or not depends on the type of node including the Association.
     * An example of this is that they should be unique on Definition, whereas multiple LinkReferences can be non-unique to be associated with one definition.
     */
    String identifier();

    /**
     * A label field can be present. label is a string value: it works just like title on a link or a lang on code: character escapes and character references are parsed.
     */
    @Nullable
    String label();
}
