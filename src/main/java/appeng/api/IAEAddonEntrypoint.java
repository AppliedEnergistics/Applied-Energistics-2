package appeng.api;

/**
 * If your addons needs to be notified when AE2 is fully initialized (and has registered all of its items and fluids),
 * implement this class and register it as an entrypoint in your mod.
 * <p/>
 * Entrypoint IDs supported by AE2:
 * <ul>
 * <li><code>appliedenergistics2</code> will be called on both server and client.</li>
 * <li><code>appliedenergistics2:client</code> will be called on the client.</li>
 * <li><code>appliedenergistics2:server</code> will be called on a dedicated server.</li>
 * </ul>
 * <p/>
 * See <a href="https://fabricmc.net/wiki/documentation:entrypoint">the Fabric Wiki</a> for an explanation of
 * entrypoints.
 */
public interface IAEAddonEntrypoint {
    void onAe2Initialized();
}
