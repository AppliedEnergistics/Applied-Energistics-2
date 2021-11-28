package appeng.parts.storagebus;

public enum StorageBusConnectedTo {
    /**
     * Storage bus is connected to nothing.
     */
    NOTHING,
    /**
     * Storage bus is connected to a ME network.
     */
    ME_NETWORK,
    /**
     * Storage bus is connected to an interface's local storage (it has configured stocking)
     */
    INTERFACE_LOCAL_STORAGE,
    /**
     * Storage bus is connected to an external storage.
     */
    EXTERNAL_STORAGE
}
