package appeng.init.internal;

import appeng.api.movable.BlockEntityMoveStrategies;
import appeng.blockentity.spatial.SpatialAnchorMoveStrategy;

public final class InitBlockEntityMoveStrategies {
    private InitBlockEntityMoveStrategies() {
    }

    public static void init() {
        BlockEntityMoveStrategies.add(new SpatialAnchorMoveStrategy());
    }
}
