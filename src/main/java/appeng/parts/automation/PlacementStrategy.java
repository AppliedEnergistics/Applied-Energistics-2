package appeng.parts.automation;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

public interface PlacementStrategy {
    void clearBlocked();

    long placeInWorld(AEKey what, long amount, Actionable type, boolean placeAsEntity);
}
