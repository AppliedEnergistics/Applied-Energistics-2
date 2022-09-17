package appeng.container.interfaces;

import mezz.jei.api.gui.IGhostIngredientHandler.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface IJEIGhostIngredients {
    List<Target<?>> getPhantomTargets(Object ingredient);

    default Map<Target<?>, Object> getFakeSlotTargetMap() {
        return new HashMap<>();
    }

}