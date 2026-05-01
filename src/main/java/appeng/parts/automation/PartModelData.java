package appeng.parts.automation;

import net.neoforged.neoforge.model.data.ModelProperty;

public final class PartModelData {
    private PartModelData() {
    }

    public static final ModelProperty<StatusIndicatorState> STATUS_INDICATOR = new ModelProperty<>();
    public static final ModelProperty<PlaneConnections> CONNECTIONS = new ModelProperty<>();
    public static final ModelProperty<Long> P2P_FREQUENCY = new ModelProperty<>();
    public static final ModelProperty<Boolean> LEVEL_EMITTER_ON = new ModelProperty<>();
    public static final ModelProperty<Boolean> CABLE_ANCHOR_SHORT = new ModelProperty<>();
    public static final ModelProperty<Boolean> MONITOR_LOCKED = new ModelProperty<>();

    public enum StatusIndicatorState {
        ACTIVE,
        POWERED,
        UNPOWERED
    }
}
