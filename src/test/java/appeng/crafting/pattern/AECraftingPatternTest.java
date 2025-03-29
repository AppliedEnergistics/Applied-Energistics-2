package appeng.crafting.pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
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
import appeng.util.RecursiveTagReplace;

@BootstrapMinecraft
@LoadTranslations
@MockitoSettings(strictness = Strictness.LENIENT)
class AECraftingPatternTest {
    private final RegistryAccess registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    private static final ResourceKey<Recipe<?>> TEST_RECIPE_ID = ResourceKey.create(Registries.RECIPE,
            AppEng.makeId("test_recipe"));

    @Mock
    MockedStatic<AppEng> appEngMock;

    @BeforeEach
    void setUp() {
        var appEngInstance = mock(AppEng.class);
        var clientLevel = mock(Level.class);
        when(appEngInstance.getClientLevel()).thenReturn(clientLevel);
        appEngMock.when(AppEng::instance).thenReturn(appEngInstance);
    }

    private final RecipeHolder<CraftingRecipe> TEST_RECIPE = buildRecipe(
            ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.MISC, Items.STICK)
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
            public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                    ICondition... conditions) {
                result.set((ShapedRecipe) recipe);
            }

            @Override
            public void includeRootAdvancement() {
            }
        }, TEST_RECIPE_ID);
        return new RecipeHolder<>(TEST_RECIPE_ID, Objects.requireNonNull(result.get()));
    }

    @Test
    void testDecodeWithoutComponent() {
        var item = AEItems.CRAFTING_PATTERN.stack();
        var tag = item.save(registries);
        assertNull(decode((CompoundTag) tag));
    }

    /**
     * Sanity check that an encoded pattern that contains item-ids that are now invalid (i.e. mod removed, item removed
     * from mod, etc.) do not crash when being decoded.
     */
    @Test
    void testDecodeWithRemovedIngredientItemIds() {
        var encoded = createTestPattern();
        var encodedTag = (CompoundTag) encoded.save(registries);

        // Replace the diamond ID string with an unknown ID string
        assertEquals(1, RecursiveTagReplace.replace(encodedTag, "minecraft:torch", "minecraft:does_not_exist"));
        var brokenPatternStack = ItemStack.parse(registries, encodedTag).get();

        assertNull(decode(encodedTag));
        assertThat(getExtraTooltip(brokenPatternStack)).containsExactly(
                "Invalid Pattern",
                "Crafts: 1 x Stick",
                "with: 1 x minecraft:does_not_exist",
                " and 1 x Diamond",
                "Substitutes alternate items",
                "Uses fluids directly",
                "Recipe: ae2:test_recipe");
    }

    private List<String> getExtraTooltip(ItemStack stack) {
        var lines = new ArrayList<Component>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, lines::add, TooltipFlag.ADVANCED);
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
        var level = mock(ServerLevel.class, Mockito.RETURNS_DEEP_STUBS);
        var recipeManager = mock(RecipeManager.class, Mockito.RETURNS_DEEP_STUBS);
        when(level.recipeAccess()).thenReturn(recipeManager);
        when(recipeManager.recipeMap().byType(RecipeType.CRAFTING)).thenReturn(List.of(TEST_RECIPE));

        var pattern = ItemStack.parse(registries, tag).get();
        var details = PatternDetailsHelper.decodePattern(AEItemKey.of(pattern), level);
        if (details == null) {
            return null;
        }
        return assertInstanceOf(AECraftingPattern.class, details);
    }
}
