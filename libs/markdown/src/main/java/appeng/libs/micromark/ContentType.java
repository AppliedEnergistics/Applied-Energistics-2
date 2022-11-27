package appeng.libs.micromark;

/**
 * Enumeration of the content types of {@link Token}.
 */
public enum ContentType {
    /**
     * Technically `document` is also a content type, which includes containers
     * (lists, block quotes) and flow.
     * As `ContentType` is used on tokens to define the type of subcontent but
     * `document` is the highest level of content, so it’s not listed here.
     * <p>
     * Containers in markdown come from the margin and include more constructs
     * on the lines that define them.
     * Take for example a block quote with a paragraph inside it (such as
     * `> asd`).
     */
    DOCUMENT,
    /**
     * `flow` represents the sections, such as headings, code, and content, which
     * is also parsed per line
     * An example is HTML, which has a certain starting condition (such as
     * `<script>` on its own line), then continues for a while, until an end
     * condition is found (such as `</style>`).
     * If that line with an end condition is never found, that flow goes until
     * the end.
     */
    FLOW,
    /**
     * `content` is zero or more definitions, and then zero or one paragraph.
     * It’s a weird one, and needed to make certain edge cases around definitions
     * spec compliant.
     * Definitions are unlike other things in markdown, in that they behave like
     * `text` in that they can contain arbitrary line endings, but *have* to end
     * at a line ending.
     * If they end in something else, the whole definition instead is seen as a
     * paragraph.
     * <p>
     * The content in markdown first needs to be parsed up to this level to
     * figure out which things are defined, for the whole document, before
     * continuing on with `text`, as whether a link or image reference forms or
     * not depends on whether it’s defined.
     * This unfortunately prevents a true streaming markdown to HTML compiler.
     */
    CONTENT,
    /**
     * `text` contains phrasing content such as attention (emphasis, strong),
     * media (links, images), and actual text.
     */
    TEXT,
    /**
     * `string` is a limited `text` like content type which only allows character
     * references and character escapes.
     * It exists in things such as identifiers (media references, definitions),
     * titles, or URLs.
     */
    STRING
}
