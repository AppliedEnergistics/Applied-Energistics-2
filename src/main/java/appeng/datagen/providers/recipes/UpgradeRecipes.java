package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.recipes.game.AddItemUpgradeRecipe;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.recipes.game.RemoveItemUpgradeRecipe;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import appeng.recipes.game.StorageCellUpgradeRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UpgradeRecipes extends AE2RecipeProvider {
    public UpgradeRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    // Defaults will always be Cell Component for upgrade/disassembly. Additional options are for modpack developers.
    record UnitTransformTier(BlockDefinition<?> baseBlock, ItemDefinition<?> upgradeItem) {
    }

    record CellDisassemblyTier(ItemDefinition<?> cell, ItemDefinition<?> component) {
    }

    record CellUpgradeTier(String suffix, ItemDefinition<?> cell, ItemLike component) {
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        itemUpgradeRecipe(consumer);

        // Crafting Unit Transformation
        craftingUnitTransform(consumer, List.of(
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_1K, AEItems.CELL_COMPONENT_1K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_4K, AEItems.CELL_COMPONENT_4K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_16K, AEItems.CELL_COMPONENT_16K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_64K, AEItems.CELL_COMPONENT_64K),
                new UnitTransformTier(AEBlocks.CRAFTING_STORAGE_256K, AEItems.CELL_COMPONENT_256K),
                new UnitTransformTier(AEBlocks.CRAFTING_ACCELERATOR, AEItems.ENGINEERING_PROCESSOR),
                new UnitTransformTier(AEBlocks.CRAFTING_MONITOR, AEParts.STORAGE_MONITOR)));

        storageCellUpgradeRecipes(consumer);
    }

    private void storageCellUpgradeRecipes(RecipeOutput output) {
        storageCellUpgradeRecipes(
                output,
                List.of(
                        new CellUpgradeTier("1k", AEItems.ITEM_CELL_1K, AEItems.CELL_COMPONENT_1K),
                        new CellUpgradeTier("4k", AEItems.ITEM_CELL_4K, AEItems.CELL_COMPONENT_4K),
                        new CellUpgradeTier("16k", AEItems.ITEM_CELL_16K, AEItems.CELL_COMPONENT_16K),
                        new CellUpgradeTier("64k", AEItems.ITEM_CELL_64K, AEItems.CELL_COMPONENT_64K),
                        new CellUpgradeTier("256k", AEItems.ITEM_CELL_256K, AEItems.CELL_COMPONENT_256K)
                ),
                List.of(AEItems.ITEM_CELL_HOUSING)
        );
        storageCellUpgradeRecipes(
                output,
                List.of(
                        new CellUpgradeTier("1k", AEItems.FLUID_CELL_1K, AEItems.CELL_COMPONENT_1K),
                        new CellUpgradeTier("4k", AEItems.FLUID_CELL_4K, AEItems.CELL_COMPONENT_4K),
                        new CellUpgradeTier("16k", AEItems.FLUID_CELL_16K, AEItems.CELL_COMPONENT_16K),
                        new CellUpgradeTier("64k", AEItems.FLUID_CELL_64K, AEItems.CELL_COMPONENT_64K),
                        new CellUpgradeTier("256k", AEItems.FLUID_CELL_256K, AEItems.CELL_COMPONENT_256K)
                ),
                List.of(AEItems.FLUID_CELL_HOUSING)
        );
        storageCellUpgradeRecipes(
                output,
                List.of(
                        new CellUpgradeTier("1k", AEItems.PORTABLE_ITEM_CELL1K, AEItems.CELL_COMPONENT_1K),
                        new CellUpgradeTier("4k", AEItems.PORTABLE_ITEM_CELL4K, AEItems.CELL_COMPONENT_4K),
                        new CellUpgradeTier("16k", AEItems.PORTABLE_ITEM_CELL16K, AEItems.CELL_COMPONENT_16K),
                        new CellUpgradeTier("64k", AEItems.PORTABLE_ITEM_CELL64K, AEItems.CELL_COMPONENT_64K),
                        new CellUpgradeTier("256k", AEItems.PORTABLE_ITEM_CELL256K, AEItems.CELL_COMPONENT_256K)
                ),
                List.of(AEBlocks.ME_CHEST, AEBlocks.ENERGY_CELL, AEItems.ITEM_CELL_HOUSING)
        );
        storageCellUpgradeRecipes(
                output,
                List.of(
                        new CellUpgradeTier("1k", AEItems.PORTABLE_FLUID_CELL1K, AEItems.CELL_COMPONENT_1K),
                        new CellUpgradeTier("4k", AEItems.PORTABLE_FLUID_CELL4K, AEItems.CELL_COMPONENT_4K),
                        new CellUpgradeTier("16k", AEItems.PORTABLE_FLUID_CELL16K, AEItems.CELL_COMPONENT_16K),
                        new CellUpgradeTier("64k", AEItems.PORTABLE_FLUID_CELL64K, AEItems.CELL_COMPONENT_64K),
                        new CellUpgradeTier("256k", AEItems.PORTABLE_FLUID_CELL256K, AEItems.CELL_COMPONENT_256K)
                ),
                List.of(AEBlocks.ME_CHEST, AEBlocks.ENERGY_CELL, AEItems.FLUID_CELL_HOUSING)
        );
    }

    private void storageCellUpgradeRecipes(RecipeOutput output, List<CellUpgradeTier> tiers, List<ItemLike> additionalDisassemblyItems) {
        for (int i = 0; i < tiers.size(); i++) {
            var fromTier = tiers.get(i);
            var inputCell = fromTier.cell().asItem();
            var inputId = fromTier.cell().id();
            var resultComponent = fromTier.component().asItem();

            cellDisassembly(output, additionalDisassemblyItems, fromTier);

            // Allow a direct upgrade to any higher tier
            for (int j = i + 1; j < tiers.size(); j++) {
                var toTier = tiers.get(j);
                var resultCell = toTier.cell().asItem();
                var inputComponent = toTier.component().asItem();

                var recipeId = inputId.withPath(path -> "upgrade/" + path + "_to_" + toTier.suffix);

                output.accept(
                        recipeId,
                        new StorageCellUpgradeRecipe(
                                inputCell, inputComponent,
                                resultCell, resultComponent),
                        null);
            }
        }
    }

    private void itemUpgradeRecipe(RecipeOutput output) {
        output.accept(AppEng.makeId("add_item_upgrade"), AddItemUpgradeRecipe.INSTANCE, null);
        output.accept(AppEng.makeId("remove_item_upgrade"), RemoveItemUpgradeRecipe.INSTANCE, null);
    }

    private void cellDisassembly(RecipeOutput consumer, List<ItemLike> additionalReturn, CellUpgradeTier tier) {
        List<ItemStack> results = new ArrayList<>();
        for (var itemLike : additionalReturn) {
            results.add(itemLike.asItem().getDefaultInstance());
        }
        results.add(tier.component.asItem().getDefaultInstance());

        consumer.accept(
                tier.cell.id().withPrefix("cell_upgrade/"),
                new StorageCellDisassemblyRecipe(
                        tier.cell.asItem(),
                        results
                ),
                null
        );
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
