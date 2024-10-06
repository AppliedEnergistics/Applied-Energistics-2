package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.recipes.game.CraftingUnitUpgradeRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UpgradeRecipes extends AE2RecipeProvider {
    public UpgradeRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    // Defaults will always be Cell Component for upgrade/disassembly. Additional options are for modpack developers.
    record UnitUpgradeTier(BlockDefinition<?> baseBlock, ItemDefinition<?> upgradeItem) {};

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        List<UnitUpgradeTier> recipes = List.of(
            new UnitUpgradeTier(AEBlocks.CRAFTING_STORAGE_1K, AEItems.CELL_COMPONENT_1K),
            new UnitUpgradeTier(AEBlocks.CRAFTING_STORAGE_4K, AEItems.CELL_COMPONENT_4K),
            new UnitUpgradeTier(AEBlocks.CRAFTING_STORAGE_16K, AEItems.CELL_COMPONENT_16K),
            new UnitUpgradeTier(AEBlocks.CRAFTING_STORAGE_64K, AEItems.CELL_COMPONENT_64K),
            new UnitUpgradeTier(AEBlocks.CRAFTING_STORAGE_256K, AEItems.CELL_COMPONENT_256K)
        );

        recipes.forEach(recipe -> consumer.accept(
            AppEng.makeId("upgrade/" + recipe.baseBlock.id().getPath()),
            new CraftingUnitUpgradeRecipe(
                recipe.baseBlock.id(),
                List.of(recipe.upgradeItem.asItem()),
                List.of(recipe.upgradeItem.stack()),
                null
            ), null
        ));

    }

    @Override
    public String getName() {
        return "AE2 Crafting Unit Upgrade/Disassembly Recipes";
    }
}
