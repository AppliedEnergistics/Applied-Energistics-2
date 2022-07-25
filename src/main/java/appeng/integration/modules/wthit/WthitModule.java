package appeng.integration.modules.wthit;

import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;

import appeng.integration.modules.igtooltip.InGameTooltipProviders;

public class WthitModule implements IWailaPlugin {

    public void register(IRegistrar registrar) {
        for (var blockProvider : InGameTooltipProviders.getBlockProviders()) {
            register(registrar, blockProvider);
        }
    }

    private <T extends BlockEntity> void register(IRegistrar registrar,
            InGameTooltipProviders.BlockRegistration<T> blockProvider) {
        var adapter = new BlockEntityDataProvider<>(
                blockProvider.provider(),
                blockProvider.blockEntityClass());
        registrar.addBlockData(adapter, blockProvider.blockEntityClass());
        registrar.addComponent(adapter, TooltipPosition.HEAD, blockProvider.blockEntityClass());
        registrar.addComponent(adapter, TooltipPosition.BODY, blockProvider.blockEntityClass());
        registrar.addComponent(adapter, TooltipPosition.TAIL, blockProvider.blockEntityClass());
        registrar.addIcon(adapter, blockProvider.blockClass());
    }

}
