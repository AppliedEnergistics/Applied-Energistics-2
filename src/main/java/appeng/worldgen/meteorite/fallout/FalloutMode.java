package appeng.worldgen.meteorite.fallout;

public enum FalloutMode {

    /**
     * No fallout, e.g. when without a crater.
     */
    NONE,

    /**
     * Default
     */
    DEFAULT,

    /**
     * For sandy terrain
     */
    SAND,

    /**
     * For terracotta (mesa)
     */
    TERRACOTTA,

    /**
     * Icy/snowy terrain
     */
    ICE_SNOW;
}
