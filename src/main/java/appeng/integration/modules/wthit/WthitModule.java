package appeng.integration.modules.wthit;

import appeng.integration.modules.igtooltip.InGameTooltipProviders;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class WthitModule implements IWailaPlugin {

    public void register(IRegistrar registrar) {
        for (var blockProvider : InGameTooltipProviders.getBlockProviders()) {
            register(registrar, blockProvider);
        }
    }

    private <T extends BlockEntity> void register(IRegistrar registrar, InGameTooltipProviders.BlockRegistration<T> blockProvider) {
        var adapter = new BlockEntityDataProvider<>(
                blockProvider.provider(),
                blockProvider.blockEntityClass()
        );
        registrar.addBlockData(adapter, blockProvider.blockEntityClass());
        registrar.addComponent(adapter, TooltipPosition.HEAD, blockProvider.blockEntityClass());
        registrar.addComponent(adapter, TooltipPosition.BODY, blockProvider.blockEntityClass());
        registrar.addComponent(adapter, TooltipPosition.TAIL, blockProvider.blockEntityClass());
        registrar.addIcon(adapter, blockProvider.blockClass());
    }

}
