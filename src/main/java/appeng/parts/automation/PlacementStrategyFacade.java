package appeng.parts.automation;

import java.util.Map;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

class PlacementStrategyFacade implements PlacementStrategy {
    private final Map<AEKeyType, PlacementStrategy> strategies;

    public PlacementStrategyFacade(Map<AEKeyType, PlacementStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void clearBlocked() {
        for (var strategy : strategies.values()) {
            strategy.clearBlocked();
        }
    }

    @Override
    public long placeInWorld(AEKey what, long amount, Actionable type, boolean placeAsEntity) {
        var strategy = strategies.get(what.getType());
        return strategy != null ? strategy.placeInWorld(what, amount, type, placeAsEntity) : 0;
    }
}
