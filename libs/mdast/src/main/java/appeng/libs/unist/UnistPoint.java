package appeng.libs.unist;

/**
 * Represents one point in the source file.
 */
public interface UnistPoint {
    /**
     * The 1-based index of the line in the source-file.
     */
    int line();

    /**
     * The 1-based index of the column in the source-file.
     */
    int column();

    /**
     * The 0-based offset to the character in the source-file.
     */
    int offset();
}
