package appengee3compat.core;

import appeng.api.AEApi;
import appeng.api.definitions.*;
import appeng.api.features.IGrinderEntry;
import appeng.api.features.IInscriberRecipe;
import appeng.core.AEConfig;
import appeng.items.parts.ItemFacade;
import com.google.common.base.Stopwatch;
import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;
import com.pahimar.ee3.api.knowledge.AbilityRegistryProxy;
import com.pahimar.ee3.knowledge.AbilityRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Mod(modid = AppEngEE3Compatibility.MOD_ID, acceptedMinecraftVersions = "[1.7.10]", name = AppEngEE3Compatibility.MOD_NAME, version = AEConfig.VERSION, dependencies = AppEngEE3Compatibility.MOD_DEPENDENCIES)
public final class AppEngEE3Compatibility {
    public static final String MOD_ID = "appliedenergistics2ee3compatibility";
    public static final String MOD_NAME = "Applied Energistics 2 EE3 Compatibility";
    public static final String MOD_DEPENDENCIES = "after:appliedenergistics2;";

    public static AppEngEE3Compatibility instance;

    public AppEngEE3Compatibility() {
        instance = this;
    }

    @Mod.EventHandler
    void preInit(FMLPreInitializationEvent event) {
        if (Loader.isModLoaded("EE3")) {
            Stopwatch watch = Stopwatch.createStarted();
            AELog.info("Pre Initialization ( started )");

            final IDefinitions definitions = AEApi.instance().definitions();
            final IMaterials materials = definitions.materials();
            final IItems items = definitions.items();
            final IBlocks blocks = definitions.blocks();

            // Register Base AE Materials
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(blocks.skyStone().maybeBlock().get(), 64);                         // Set the same as obsidian
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(materials.certusQuartzCrystal().maybeStack(1).get(), 256);         //
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(materials.certusQuartzCrystalCharged().maybeStack(1).get(), 256);  //
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(materials.matterBall().maybeStack(1).get(), 256);
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(materials.singularity().maybeStack(1).get(), 256000);

            // Non Learnable AE Materials
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(blocks.quartzOre().maybeBlock().get(), 256);
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(blocks.quartzOreCharged().maybeBlock().get(), 256);
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(items.cellCreative().maybeStack(1).get(), 1725);
            AbilityRegistryProxy.setAsNotLearnable(blocks.quartzOre().maybeBlock().get());
            AbilityRegistryProxy.setAsNotLearnable(blocks.quartzOreCharged().maybeBlock().get());
            AbilityRegistryProxy.setAsNotLearnable(items.cellCreative().maybeStack(1).get());

            AELog.info("Pre Initialization ( ended after " + watch.elapsed(TimeUnit.MILLISECONDS) + "ms )");
        }
    }

    @Mod.EventHandler
    void postInit(FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("EE3")) {
            Stopwatch watch = Stopwatch.createStarted();
            AELog.info("Post Initialization ( started )");

            EE3RecipeHelper.initRecipes();
            RegisterFurnaceRecipes();
            RegisterFacadeRecipes();
            RegisterGrinderRecipes();
            RegisterInscriberRecipes();
            RegisterWorldRecipes();

            AELog.info("Post Initialization ( ended after " + watch.elapsed(TimeUnit.MILLISECONDS) + "ms )");
        }
    }

    private void RegisterWorldRecipes()
    {
        final IDefinitions definitions = AEApi.instance().definitions();
        final IMaterials materials = definitions.materials();
        final IItems items = definitions.items();

        RecipeRegistryProxy.addRecipe(materials.fluixCrystal().maybeStack(2).get(), Arrays.asList(new ItemStack[]{materials.certusQuartzCrystalCharged().maybeStack(1).get(), new ItemStack(Items.redstone, 1), new ItemStack(Items.quartz, 1)}));
        RecipeRegistryProxy.addRecipe(materials.purifiedCertusQuartzCrystal().maybeStack(1).get(), Arrays.asList(new ItemStack[]{new ItemStack(items.crystalSeed().maybeItem().get(), 1)}));
        RecipeRegistryProxy.addRecipe(materials.purifiedFluixCrystal().maybeStack(1).get(), Arrays.asList(new ItemStack[]{new ItemStack(items.crystalSeed().maybeItem().get(), 1, 600)}));
        RecipeRegistryProxy.addRecipe(materials.purifiedNetherQuartzCrystal().maybeStack(1).get(), Arrays.asList(new ItemStack[]{new ItemStack(items.crystalSeed().maybeItem().get(), 1, 1200)}));
    }

    private void RegisterFurnaceRecipes()
    {
        final IDefinitions definitions = AEApi.instance().definitions();
        final IMaterials materials = definitions.materials();
        final IBlocks blocks = definitions.blocks();

        RecipeRegistryProxy.addRecipe(materials.silicon().maybeStack(1).get(), Arrays.asList(new ItemStack[]{materials.certusQuartzDust().maybeStack(1).get()}));
        RecipeRegistryProxy.addRecipe(new ItemStack(blocks.skyStone().maybeItem().get(), 1, 1), Arrays.asList(new ItemStack[]{blocks.skyStone().maybeStack(1).get()}));
    }

    private void RegisterFacadeRecipes()
    {
        final IDefinitions definitions = AEApi.instance().definitions();
        final ItemFacade facade = (ItemFacade) definitions.items().facade().maybeItem().get();
        final IItemDefinition anchorDefinition = definitions.parts().cableAnchor();


        final List<ItemStack> facades = facade.getFacades();
        for (ItemStack anchorStack : anchorDefinition.maybeStack(1).asSet())
        {
            for (ItemStack is : facades)
            {
                RecipeRegistryProxy.addRecipe(is, Arrays.asList(new ItemStack[]{anchorStack}));
            }
        }
    }

    private void RegisterGrinderRecipes()
    {
        for (IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes())
        {
            RecipeRegistryProxy.addRecipe(recipe.getOutput(), Arrays.asList(new ItemStack[]{recipe.getInput()}));
        }
    }

    private void RegisterInscriberRecipes() {
        for (IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes())
        {
            final IDefinitions definitions = AEApi.instance().definitions();
            final IMaterials materials = definitions.materials();

            List<ItemStack> input = recipe.getInputs();

            for (ItemStack top : recipe.getTopOptional().asSet())
            {
                if (!top.isItemEqual(materials.calcProcessorPress().maybeStack(1).get()) && !top.isItemEqual(materials.engProcessorPress().maybeStack(1).get()) && !top.isItemEqual(materials.logicProcessorPress().maybeStack(1).get()) && !top.isItemEqual(materials.siliconPress().maybeStack(1).get()))
                {
                    input.add(top);
                }
            }

            for (ItemStack bottom : recipe.getBottomOptional().asSet())
            {
                input.add(bottom);
            }

            RecipeRegistryProxy.addRecipe(recipe.getOutput(), input);
        }
    }
}
