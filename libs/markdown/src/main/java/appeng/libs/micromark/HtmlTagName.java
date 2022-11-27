package appeng.libs.micromark;

import java.util.Set;

public final class HtmlTagName {
    private HtmlTagName() {
    }

    /**
     * List of lowercase HTML tag names which when parsing HTML (flow), result
     * in more relaxed rules (condition 6): because they are known blocks, the
     * HTML-like syntax doesn’t have to be strictly parsed.
     * For tag names not in this list, a more strict algorithm (condition 7) is used
     * to detect whether the HTML-like syntax is seen as HTML (flow) or not.
     * <p>
     * This is copied from:
     * <https://spec.commonmark.org/0.30/#html-blocks>.
     */
    public static final Set<String> htmlBlockNames = Set.of(
            "address",
            "article",
            "aside",
            "base",
            "basefont",
            "blockquote",
            "body",
            "caption",
            "center",
            "col",
            "colgroup",
            "dd",
            "details",
            "dialog",
            "dir",
            "div",
            "dl",
            "dt",
            "fieldset",
            "figcaption",
            "figure",
            "footer",
            "form",
            "frame",
            "frameset",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6",
            "head",
            "header",
            "hr",
            "html",
            "iframe",
            "legend",
            "li",
            "link",
            "main",
            "menu",
            "menuitem",
            "nav",
            "noframes",
            "ol",
            "optgroup",
            "option",
            "p",
            "param",
            "section",
            "summary",
            "table",
            "tbody",
            "td",
            "tfoot",
            "th",
            "thead",
            "title",
            "tr",
            "track",
            "ul"
    );

    /**
     * List of lowercase HTML tag names which when parsing HTML (flow), result in
     * HTML that can include lines w/o exiting, until a closing tag also in this
     * list is found (condition 1).
     * <p>
     * This module is copied from:
     * <https://spec.commonmark.org/0.30/#html-blocks>.
     * <p>
     * Note that `textarea` was added in `CommonMark@0.30`.
     */
    public static final Set<String> htmlRawNames = Set.of("pre", "script", "style", "textarea");

}
