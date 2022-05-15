package appeng.crafting.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class CraftingPatternItemTest {

    private static final ResourceLocation TEST_RECIPE_ID = AppEng.makeId("test_recipe");

    private final ShapedRecipe TEST_RECIPE = buildRecipe(ShapedRecipeBuilder.shaped(Items.STICK)
            .pattern("xy")
            .define('x', Items.TORCH)
            .define('y', Items.DIAMOND));

    private ShapedRecipe buildRecipe(ShapedRecipeBuilder builder) {
        var result = new AtomicReference<ShapedRecipe>();
        builder.unlockedBy("xxx", RecipeProvider.has(builder.getResult()));
        builder.save(finishedRecipe -> {
            var recipe = RecipeSerializer.SHAPED_RECIPE.fromJson(TEST_RECIPE_ID, finishedRecipe.serializeRecipe());
            result.set(recipe);
        }, TEST_RECIPE_ID);
        return Objects.requireNonNull(result.get());
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
        var encodedTag = encoded.getTag();

        var inputTag = encodedTag.getList("in", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals("minecraft:torch", inputTag.getString("id"));
        inputTag.putString("id", "minecraft:unknown_item_id");

        var decoded = decode(encodedTag);
        assertNull(decoded);
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
        var recipeMap = new HashMap<ResourceLocation, CraftingRecipe>();
        recipeMap.put(TEST_RECIPE_ID, TEST_RECIPE);
        when(recipeManager.byType(RecipeType.CRAFTING)).thenReturn(recipeMap);

        return AEItems.CRAFTING_PATTERN.asItem().decode(
                AEItemKey.of(AEItems.CRAFTING_PATTERN, tag), level);
    }

    private static class TestRecipe implements CraftingRecipe {
        public boolean acceptAssemble = true;

        @Override
        public RecipeType<?> getType() {
            return RecipeType.CRAFTING;
        }

        @Override
        public boolean matches(CraftingContainer container, Level level) {
            for (int i = 2; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) {
                    return false;
                }
            }
            return container.getItem(0).getItem() == Items.TORCH
                    && container.getItem(1).getItem() == Items.DIAMOND;
        }

        @Override
        public ItemStack assemble(CraftingContainer container) {
            return getResultItem();
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width >= 3 && height >= 3;
        }

        @Override
        public ItemStack getResultItem() {
            return new ItemStack(Items.STICK);
        }

        @Override
        public ResourceLocation getId() {
            return TEST_RECIPE_ID;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            throw new UnsupportedOperationException();
        }
    }
}
