package appeng.datagen.providers.recipes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.recipes.game.StorageCellDisassemblyRecipe;

public class UpgradeRecipes extends AE2RecipeProvider {
    public UpgradeRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    // Defaults will always be Cell Component for upgrade/disassembly. Additional options are for modpack developers.
    record UnitTransformTier(BlockDefinition<?> baseBlock, ItemDefinition<?> upgradeItem) {
    }

    record CellDisassemblyTier(ItemDefinition<?> cell, ItemDefinition<?> portable, ItemDefinition<?> component,
            boolean fluid) {
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        List<UnitTransformTier> tiers = List.of(
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_1K, AEItems.CELL_COMPONENT_1K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_4K, AEItems.CELL_COMPONENT_4K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_16K, AEItems.CELL_COMPONENT_16K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_64K, AEItems.CELL_COMPONENT_64K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_256K, AEItems.CELL_COMPONENT_256K));

        List<CellDisassemblyTier> storageTiers = List.of(
            new CellDisassemblyTier(AEItems.ITEM_CELL_1K, AEItems.PORTABLE_ITEM_CELL1K, AEItems.CELL_COMPONENT_1K, false),
            new CellDisassemblyTier(AEItems.ITEM_CELL_4K, AEItems.PORTABLE_ITEM_CELL4K, AEItems.CELL_COMPONENT_4K, false),
            new CellDisassemblyTier(AEItems.ITEM_CELL_16K, AEItems.PORTABLE_ITEM_CELL16K, AEItems.CELL_COMPONENT_16K, false),
            new CellDisassemblyTier(AEItems.ITEM_CELL_64K, AEItems.PORTABLE_ITEM_CELL64K, AEItems.CELL_COMPONENT_64K, false),
            new CellDisassemblyTier(AEItems.ITEM_CELL_256K, AEItems.PORTABLE_ITEM_CELL256K, AEItems.CELL_COMPONENT_256K, false),
            new CellDisassemblyTier(AEItems.FLUID_CELL_1K, AEItems.PORTABLE_FLUID_CELL1K, AEItems.CELL_COMPONENT_1K, true),
            new CellDisassemblyTier(AEItems.FLUID_CELL_4K, AEItems.PORTABLE_FLUID_CELL4K, AEItems.CELL_COMPONENT_4K, true),
            new CellDisassemblyTier(AEItems.FLUID_CELL_16K, AEItems.PORTABLE_FLUID_CELL16K, AEItems.CELL_COMPONENT_16K, true),
            new CellDisassemblyTier(AEItems.FLUID_CELL_64K, AEItems.PORTABLE_FLUID_CELL64K, AEItems.CELL_COMPONENT_64K, true),
            new CellDisassemblyTier(AEItems.FLUID_CELL_256K, AEItems.PORTABLE_FLUID_CELL256K, AEItems.CELL_COMPONENT_256K, true)
        );

        tiers.forEach(tier -> consumer.accept(
                AppEng.makeId("upgrade/" + tier.baseBlock.id().getPath()),
                new CraftingUnitTransformRecipe(
                        tier.baseBlock.id(),
                        List.of(tier.upgradeItem.asItem()),
                        List.of(tier.upgradeItem.stack()),
                        null),
                null));

        storageTiers.forEach(tier -> consumer.accept(
                AppEng.makeId("upgrade/" + tier.cell.id().getPath()),
                new StorageCellDisassemblyRecipe(
                        tier.cell.asItem(),
                        tier.portable.asItem(),
                        getCellDisassemblyItems(tier.component, tier.fluid),
                        getPortableDisassemblyItems(tier.component, tier.fluid)),
                null));
    }

    private List<ItemStack> getPortableDisassemblyItems(ItemDefinition<?> component, boolean fluid) {
        return List.of(AEBlocks.ME_CHEST.stack(), AEBlocks.ENERGY_CELL.stack(),
                fluid ? AEItems.FLUID_CELL_HOUSING.stack() : AEItems.ITEM_CELL_HOUSING.stack(), component.stack());
    }

    private List<ItemStack> getCellDisassemblyItems(ItemDefinition<?> component, boolean fluid) {
        return List.of(fluid ? AEItems.FLUID_CELL_HOUSING.stack() : AEItems.ITEM_CELL_HOUSING.stack(),
                component.stack());
    }

    @Override
    public String getName() {
        return "AE2 Storage Upgrade/Disassembly Recipes";
    }
}
