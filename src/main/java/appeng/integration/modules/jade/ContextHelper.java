package appeng.integration.modules.jade;

import snownee.jade.api.BlockAccessor;

import appeng.api.integrations.igtooltip.TooltipContext;

class ContextHelper {
    private ContextHelper() {
    }

    public static TooltipContext getContext(BlockAccessor accessor) {
        return new TooltipContext(
                accessor.getServerData(),
                accessor.getHitResult().getLocation(),
                accessor.getPlayer());
    }
}
