package appeng.crafting.pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.ICondition;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.util.BootstrapMinecraft;
import appeng.util.LoadTranslations;

@BootstrapMinecraft
@LoadTranslations
class AECraftingPatternTest {
    private static final ResourceLocation TEST_RECIPE_ID = AppEng.makeId("test_recipe");

    private final RecipeHolder<CraftingRecipe> TEST_RECIPE = buildRecipe(
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STICK)
                    .pattern("xy")
                    .define('x', Items.TORCH)
                    .define('y', Items.DIAMOND));

    private RecipeHolder<CraftingRecipe> buildRecipe(ShapedRecipeBuilder builder) {
        var result = new AtomicReference<ShapedRecipe>();
        builder.unlockedBy("xxx", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));
        builder.save(new RecipeOutput() {
            @Override
            public Advancement.Builder advancement() {
                return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
            }

            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                    ICondition... conditions) {
                result.set((ShapedRecipe) recipe);
            }
        }, TEST_RECIPE_ID);
        return new RecipeHolder<>(TEST_RECIPE_ID, Objects.requireNonNull(result.get()));
    }

    @Test
    void testDecodeWithEmptyTag() {
        assertNull(decode(new CompoundTag()));
    }

    /**
     * Sanity check that an encoded pattern that contains item-ids that are now invalid (i.e. mod removed, item removed
     * from mod, etc.) do not crash when being decoded.
     */
    @Test
    void testDecodeWithRemovedIngredientItemIds() {
        var encoded = createTestPattern();
        var encodedTag = encoded.getOrCreateTag();

        // Replace the diamond ID string with an unknown ID string
        assertEquals(1, RecursiveTagReplace.replace(encodedTag, "minecraft:diamond", "minecraft:does_not_exist"));

        assertNull(decode(encodedTag));
        assertThat(getExtraTooltip(encoded)).containsExactly(
                "Invalid Pattern",
                "Crafts: 1 x Stick",
                "with: 1 x Torch",
                " and 1 x minecraft:does_not_exist",
                "Substitutes alternate items",
                "Uses fluids directly",
                "Recipe: ae2:test_recipe");
    }

    private List<String> getExtraTooltip(ItemStack stack) {
        var lines = new ArrayList<Component>();
        stack.getItem().appendHoverText(stack, null, lines, TooltipFlag.ADVANCED);
        return lines.stream().map(Component::getString).toList();
    }

    private ItemStack createTestPattern() {
        return PatternDetailsHelper.encodeCraftingPattern(
                TEST_RECIPE,
                new ItemStack[] {
                        new ItemStack(Items.TORCH),
                        new ItemStack(Items.DIAMOND),
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                },
                new ItemStack(Items.STICK),
                true,
                true);
    }

    private AECraftingPattern decode(CompoundTag tag) {
        var level = mock(Level.class);
        var recipeManager = mock(RecipeManager.class);
        when(level.getRecipeManager()).thenReturn(recipeManager);
        when(recipeManager.byType(RecipeType.CRAFTING)).thenReturn(Map.of(TEST_RECIPE_ID, TEST_RECIPE));

        var details = PatternDetailsHelper.decodePattern(AEItemKey.of(AEItems.CRAFTING_PATTERN, tag), level);
        if (details == null) {
            return null;
        }
        return assertInstanceOf(AECraftingPattern.class, details);
    }
}
