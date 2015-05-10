package appeng.integration.modules;

import appeng.api.AEApi;
import appeng.integration.IIntegrationModule;
import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class EE3 implements IIntegrationModule {

    @Override
    public void init() throws Throwable {
        ItemStack certusQuartzCrystal = AEApi.instance().definitions().materials().certusQuartzCrystal().maybeStack(1).get();
        Block skyStone = AEApi.instance().definitions().blocks().skyStone().maybeBlock().get();

        EnergyValueRegistryProxy.addPreAssignedEnergyValue(certusQuartzCrystal, 256.0F);    // Set the same as Nether Quarts
        EnergyValueRegistryProxy.addPreAssignedEnergyValue(skyStone, 64.0F);                // Set the same as Obsidian
    }

    @Override
    public void postInit() {

    }
}
