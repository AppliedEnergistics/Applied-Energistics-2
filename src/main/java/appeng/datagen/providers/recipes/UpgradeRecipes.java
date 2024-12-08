package appeng.datagen.providers.recipes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
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

    record CellDisassemblyTier(ItemDefinition<?> cell, ItemDefinition<?> component) {
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        // Crafting Unit Transformation
        craftingUnitTransform(consumer, List.of(
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_1K, AEItems.CELL_COMPONENT_1K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_4K, AEItems.CELL_COMPONENT_4K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_16K, AEItems.CELL_COMPONENT_16K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_64K, AEItems.CELL_COMPONENT_64K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_256K, AEItems.CELL_COMPONENT_256K),
                new UnitTransformTier(AEBlocks.CRAFTING_ACCELERATOR, AEItems.ENGINEERING_PROCESSOR),
                new UnitTransformTier(AEBlocks.CRAFTING_MONITOR, AEParts.STORAGE_MONITOR)));

        // Item Storage Cells
        cellDisassembly(consumer, false, false, List.of(
                new CellDisassemblyTier(AEItems.ITEM_CELL_1K, AEItems.CELL_COMPONENT_1K),
                new CellDisassemblyTier(AEItems.ITEM_CELL_4K, AEItems.CELL_COMPONENT_4K),
                new CellDisassemblyTier(AEItems.ITEM_CELL_16K, AEItems.CELL_COMPONENT_16K),
                new CellDisassemblyTier(AEItems.ITEM_CELL_64K, AEItems.CELL_COMPONENT_64K),
                new CellDisassemblyTier(AEItems.ITEM_CELL_256K, AEItems.CELL_COMPONENT_256K)));

        // Fluid Storage Cells
        cellDisassembly(consumer, false, true, List.of(
                new CellDisassemblyTier(AEItems.FLUID_CELL_1K, AEItems.CELL_COMPONENT_1K),
                new CellDisassemblyTier(AEItems.FLUID_CELL_4K, AEItems.CELL_COMPONENT_4K),
                new CellDisassemblyTier(AEItems.FLUID_CELL_16K, AEItems.CELL_COMPONENT_16K),
                new CellDisassemblyTier(AEItems.FLUID_CELL_64K, AEItems.CELL_COMPONENT_64K),
                new CellDisassemblyTier(AEItems.FLUID_CELL_256K, AEItems.CELL_COMPONENT_256K)));

        // Portable Item Storage Cells
        cellDisassembly(consumer, true, false, List.of(
                new CellDisassemblyTier(AEItems.PORTABLE_ITEM_CELL1K, AEItems.CELL_COMPONENT_1K),
                new CellDisassemblyTier(AEItems.PORTABLE_ITEM_CELL4K, AEItems.CELL_COMPONENT_4K),
                new CellDisassemblyTier(AEItems.PORTABLE_ITEM_CELL16K, AEItems.CELL_COMPONENT_16K),
                new CellDisassemblyTier(AEItems.PORTABLE_ITEM_CELL64K, AEItems.CELL_COMPONENT_64K),
                new CellDisassemblyTier(AEItems.PORTABLE_ITEM_CELL256K, AEItems.CELL_COMPONENT_256K)));

        // Portable Fluid Storage Cells
        cellDisassembly(consumer, true, true, List.of(
                new CellDisassemblyTier(AEItems.PORTABLE_FLUID_CELL1K, AEItems.CELL_COMPONENT_1K),
                new CellDisassemblyTier(AEItems.PORTABLE_FLUID_CELL4K, AEItems.CELL_COMPONENT_4K),
                new CellDisassemblyTier(AEItems.PORTABLE_FLUID_CELL16K, AEItems.CELL_COMPONENT_16K),
                new CellDisassemblyTier(AEItems.PORTABLE_FLUID_CELL64K, AEItems.CELL_COMPONENT_64K),
                new CellDisassemblyTier(AEItems.PORTABLE_FLUID_CELL256K, AEItems.CELL_COMPONENT_256K)));
    }

    private void cellDisassembly(RecipeOutput consumer, boolean portable, boolean fluid,
            List<CellDisassemblyTier> tiers) {
        for (CellDisassemblyTier tier : tiers) {
            List<ItemStack> results;
            if (portable) {
                results = List.of(AEBlocks.ME_CHEST.stack(), AEBlocks.ENERGY_CELL.stack(),
                        fluid ? AEItems.FLUID_CELL_HOUSING.stack() : AEItems.ITEM_CELL_HOUSING.stack(),
                        tier.component.stack());
            } else {
                results = List.of(fluid ? AEItems.FLUID_CELL_HOUSING.stack() : AEItems.ITEM_CELL_HOUSING.stack(),
                        tier.component.stack());
            }

            consumer.accept(
                    tier.cell.id().withPrefix("cell_upgrade/"),
                    new StorageCellDisassemblyRecipe(
                            tier.cell.asItem(),
                            results),
                    null);
        }
    }

    private void craftingUnitTransform(RecipeOutput consumer, List<UnitTransformTier> tiers) {
        for (UnitTransformTier tier : tiers) {
            consumer.accept(
                    tier.baseBlock.id().withPrefix("crafting_unit_upgrade/"),
                    new CraftingUnitTransformRecipe(
                            tier.baseBlock.block(),
                            tier.upgradeItem.asItem()),
                    null);
        }
    }

    @Override
    public String getName() {
        return "AE2 Storage Upgrade/Disassembly Recipes";
    }
}
