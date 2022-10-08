package appeng.integration.modules.jade;

import mcp.mobius.waila.api.BlockAccessor;

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
