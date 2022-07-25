package appeng.integration.modules.jade;

import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import appeng.integration.modules.igtooltip.InGameTooltipProviders;

@WailaPlugin
public class JadeModule implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        for (var blockProvider : InGameTooltipProviders.getBlockProviders()) {
            register(registration, blockProvider);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        for (var blockProvider : InGameTooltipProviders.getBlockProviders()) {
            register(registration, blockProvider);
        }
    }

    private <T extends BlockEntity> void register(IWailaCommonRegistration registration,
            InGameTooltipProviders.BlockRegistration<T> blockProvider) {
        var adapter = new ServerDataAdapter<>(
                blockProvider.provider(),
                blockProvider.blockEntityClass());
        registration.registerBlockDataProvider(adapter, blockProvider.blockEntityClass());
    }

    private <T extends BlockEntity> void register(IWailaClientRegistration registration,
            InGameTooltipProviders.BlockRegistration<T> blockProvider) {
        var adapter = new BlockEntityDataProvider<>(
                registration.getElementHelper(),
                blockProvider.provider(),
                blockProvider.blockEntityClass());
        registration.registerBlockComponent(adapter, blockProvider.blockClass());
        registration.registerBlockIcon(adapter, blockProvider.blockClass());

        // Register a post-processing adapter to modify other tooltip providers
        registration.registerBlockComponent(new PostProcessAdapter<>(
                blockProvider.provider(),
                blockProvider.blockEntityClass()), blockProvider.blockClass());
    }

}
