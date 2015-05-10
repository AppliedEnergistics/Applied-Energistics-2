package appeng.integration.modules;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.AELog;
import appeng.integration.BaseModule;
import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;
import net.minecraft.item.ItemStack;

public class EE3 extends BaseModule {

    public static EE3 instance;

    public EE3()
    {
        this.testClassExistence( com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy.class );
    }

    @Override
    public void preInit()
    {
        final IDefinitions definitions = AEApi.instance().definitions();
        final IMaterials materials = definitions.materials();
        final IItems items = definitions.items();
        final IBlocks blocks = definitions.blocks();

        EnergyValueRegistryProxy.addPreAssignedEnergyValue( new ItemStack(materials.certusQuartzCrystal().maybeItem().get(), 1, 0), 256 );       // Set the same as Nether Quarts
        EnergyValueRegistryProxy.addPreAssignedEnergyValue( new ItemStack(blocks.skyStone().maybeBlock().get(), 1, 0), 64 );                     // Set the same as Obsidian

        AELog.info("Registered EMC with EE3");
    }

    @Override
    public void init() throws Throwable
    {

    }

    @Override
    public void postInit()
    {

    }
}
