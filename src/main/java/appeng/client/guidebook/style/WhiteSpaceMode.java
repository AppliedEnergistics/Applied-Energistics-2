package appeng.client.guidebook.style;

/**
 * See https://developer.mozilla.org/en-US/docs/Web/CSS/white-space
 */
public enum WhiteSpaceMode {
    NORMAL(true, true),
    NOWRAP(true, true),
    PRE(false, false),
    PRE_WRAP(false, false),
    PRE_LINE(true, false),
    BREAK_SPACES(false, false);

    /**
     * Controls collapsing of white-space according to the CSS algo.
     */
    private final boolean collapseWhitespace;

    /**
     * Controls collapsing of segment breaks according to the CSS algo.
     */
    private final boolean collapseSegmentBreaks;

    WhiteSpaceMode(boolean collapseWhitespace, boolean collapseSegmentBreaks) {
        this.collapseWhitespace = collapseWhitespace;
        this.collapseSegmentBreaks = collapseSegmentBreaks;
    }

    public boolean isCollapseWhitespace() {
        return collapseWhitespace;
    }

    public boolean isCollapseSegmentBreaks() {
        return collapseSegmentBreaks;
    }
}
