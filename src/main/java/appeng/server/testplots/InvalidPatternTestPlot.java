package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.server.testworld.PlotBuilder;

public class InvalidPatternTestPlot {

    /**
     * Encode a crafting pattern, then manipulate the recipe ID to an ID that does not exist.
     */
    @TestPlot("pattern_invalid_recipe_id")
    public static void patternInvalidRecipeId(PlotBuilder builder) {
        builder.blockEntity(BlockPos.ZERO, AEBlocks.SMOOTH_SKY_STONE_CHEST, chest -> {
            var oakLog = Blocks.OAK_LOG.asItem().getDefaultInstance();
            var pattern = CraftingPatternHelper.encodeShapelessCraftingRecipe(chest.getLevel(), oakLog);
            pattern.getTag().putString("recipe", "invalidid");
            chest.getInternalInventory().addItems(pattern);
        });

        builder.test(helper -> {
            var chest = (SkyChestBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
            var pattern = chest.getInternalInventory().getStackInSlot(0);
            helper.check(!pattern.isEmpty(), "pattern should be present");

            var details = PatternDetailsHelper.decodePattern(pattern, helper.getLevel(), false);
            helper.check(details == null, "pattern should fail decoding");
            helper.succeed();
        });
    }

    @TestPlot("pattern_ingredients_changed")
    public static void patternIngredientsChanged(PlotBuilder builder) {

    }

    @TestPlot("pattern_invalid_input_id")
    public static void patternInvalidInputId(PlotBuilder builder) {

    }

    @TestPlot("pattern_invalid_output_id")
    public static void patternInvalidOutputId(PlotBuilder builder) {

    }

}
