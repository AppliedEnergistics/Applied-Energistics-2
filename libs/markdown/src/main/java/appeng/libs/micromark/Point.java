package appeng.libs.micromark;

import appeng.libs.unist.UnistPoint;

/**
 * A location in the document (`line`/`column`/`offset`) and chunk (`_index`, `_bufferIndex`).
 * <p>
 * `_bufferIndex` is `-1` when `_index` points to a code chunk and it’s a non-negative integer when pointing to a string chunk.
 *
 * @param _index Position in a list of chunks
 * @param _bufferIndex Position in a string chunk (or `-1` when pointing to a numeric chunk).
 */
public record Point(int line, int column, int offset, int _index, int _bufferIndex) implements UnistPoint {
}
