package appeng.api.behaviors;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

final class NoopPlacementStrategy implements PlacementStrategy {
    static final NoopPlacementStrategy INSTANCE = new NoopPlacementStrategy();

    @Override
    public void clearBlocked() {
    }

    @Override
    public long placeInWorld(AEKey what, long amount, Actionable type, boolean placeAsEntity) {
        return 0;
    }
}
