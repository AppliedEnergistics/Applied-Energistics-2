package appeng.client.gui.style;

/**
 * Transforms applied to the texture when {@linkplain Blitter blitting it}. This does not change the source or
 * destination dimensions.
 */
public enum TextureTransform {
    /**
     * No transform.
     */
    NONE,
    /**
     * Mirror horizontally.
     */
    MIRROR_H,
    /**
     * Mirror vertically.
     */
    MIRROR_V
}
