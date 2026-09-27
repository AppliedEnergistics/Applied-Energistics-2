package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.ids.AEComponents;
import appeng.blockentity.storage.SkyStoneChestBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.pattern.EncodedCraftingPattern;
import appeng.server.testworld.PlotBuilder;

@TestPlotClass
public class InvalidPatternTestPlot {

    /**
     * Encode a crafting pattern, then manipulate the recipe ID to an ID that does not exist.
     */
    @TestPlot("pattern_invalid_recipe_id")
    public static void patternInvalidRecipeId(PlotBuilder builder) {
        builder.blockEntity(BlockPos.ZERO, AEBlocks.SMOOTH_SKY_STONE_CHEST, chest -> {
            var oakLog = Blocks.OAK_LOG.asItem().getDefaultInstance();
            var pattern = CraftingPatternHelper.encodeShapelessCraftingRecipe((ServerLevel) chest.getLevel(), oakLog);
            var encodedPattern = pattern.get(AEComponents.ENCODED_CRAFTING_PATTERN);
            pattern.set(AEComponents.ENCODED_CRAFTING_PATTERN, new EncodedCraftingPattern(
                    encodedPattern.inputs(),
                    encodedPattern.result(),
                    ResourceKey.create(Registries.RECIPE, Identifier.parse("invalid")),
                    encodedPattern.canSubstitute(),
                    encodedPattern.canSubstituteFluids()));
            chest.getInternalInventory().addItems(pattern);
        });

        builder.test(helper -> {
            var chest = helper.getBlockEntity(BlockPos.ZERO, SkyStoneChestBlockEntity.class);
            var pattern = chest.getInternalInventory().getStackInSlot(0);
            helper.check(!pattern.isEmpty(), "pattern should be present");

            var details = PatternDetailsHelper.decodePattern(pattern, helper.getLevel());
            helper.check(details == null, "pattern should fail decoding");
            helper.succeed();
        });
    }

}
