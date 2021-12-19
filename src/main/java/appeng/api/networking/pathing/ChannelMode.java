package appeng.api.networking.pathing;

/**
 * Defines how AE2's channel capacities work.
 */
public enum ChannelMode {
    /**
     * Cables carry infinite channels, effectively disabling pathfinding and channel requirements.
     */
    INFINITE(0, 0),
    /**
     * Default channel capacity per cable.
     */
    DEFAULT(8, 1),
    /**
     * Double capacity per cable.
     */
    X2(16, 2),
    /**
     * Triple capacity per cable.
     */
    X3(24, 3),
    /**
     * Quadruple capacity per cable.
     */
    X4(32, 4);

    private final int adHocNetworkChannels;

    private final int cableCapacityFactor;

    ChannelMode(int adHocNetworkChannels, int cableCapacityFactor) {
        this.adHocNetworkChannels = adHocNetworkChannels;
        this.cableCapacityFactor = cableCapacityFactor;
    }

    /**
     * @return The maximum number of channels supported by ad-hoc networks. 0 disables any requirements.
     */
    public int getAdHocNetworkChannels() {
        return adHocNetworkChannels;
    }

    /**
     * @return Multiplier for the default capacity of cables. Must be a power of two. 0 disables cable capacity
     *         requirements altogether.
     */
    public int getCableCapacityFactor() {
        return cableCapacityFactor;
    }
}
