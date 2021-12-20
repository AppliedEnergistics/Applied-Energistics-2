package appeng.me.service;

/**
 * Information about why an Ad-Hoc-Network may be unavailable.
 */
public enum AdHocNetworkError {
    /**
     * There are nested P2P Tunnels present on the Ad-Hoc-Network.
     */
    NESTED_P2P_TUNNEL,
    /**
     * The Ad-Hoc-Network exceeds the number of supported channels (by default 8).
     */
    TOO_MANY_CHANNELS
}
