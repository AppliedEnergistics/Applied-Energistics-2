package appeng.integration.modules.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.integration.modules.jade.tile.CableBusDataProvider;

@WailaPlugin
public class JadeModule implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        CableBusDataProvider.register(registration);

        var blockEntityProvider = new BlockEntityDataProvider();
        registration.registerBlockDataProvider(blockEntityProvider, AEBaseBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        CableBusDataProvider.register(registration);

        var blockEntityProvider = new BlockEntityDataProvider();
        registration.registerBlockComponent(blockEntityProvider, AEBaseEntityBlock.class);
    }
}
