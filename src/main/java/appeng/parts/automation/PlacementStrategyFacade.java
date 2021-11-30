package appeng.parts.automation;

import java.util.Map;

import appeng.api.config.Actionable;
import appeng.api.storage.AEKeySpace;
import appeng.api.storage.data.AEKey;

class PlacementStrategyFacade implements PlacementStrategy {
    private final Map<AEKeySpace, PlacementStrategy> strategies;

    public PlacementStrategyFacade(Map<AEKeySpace, PlacementStrategy> strategies) {
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
        var strategy = strategies.get(what.getChannel());
        return strategy != null ? strategy.placeInWorld(what, amount, type, placeAsEntity) : 0;
    }
}
