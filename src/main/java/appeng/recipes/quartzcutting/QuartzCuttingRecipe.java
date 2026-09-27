package appeng.recipes.quartzcutting;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.apache.commons.lang3.mutable.MutableBoolean;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import appeng.core.ConventionTags;
import appeng.core.definitions.AEItems;

public class QuartzCuttingRecipe extends NormalCraftingRecipe {
    public static final MapCodec<QuartzCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(QuartzCuttingRecipe::result),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(QuartzCuttingRecipe::ingredients))
            .apply(builder, QuartzCuttingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuartzCuttingRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            ItemStackTemplate.STREAM_CODEC, QuartzCuttingRecipe::result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), QuartzCuttingRecipe::ingredients,
            QuartzCuttingRecipe::new);

    public static final RecipeSerializer<QuartzCuttingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    final ItemStackTemplate result;
    final List<Ingredient> ingredients;
    private final boolean isSimple;

    public QuartzCuttingRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
            ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    public RecipeSerializer<QuartzCuttingRecipe> getSerializer() {
        return SERIALIZER;
    }

    public ItemStackTemplate result() {
        return this.result;
    }

    public List<Ingredient> ingredients() {
        return this.ingredients;
    }

    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else if (!this.isSimple) {
            var nonEmptyItems = new ArrayList<ItemStack>(input.ingredientCount());
            for (var item : input.items()) {
                if (!item.isEmpty()) {
                    nonEmptyItems.add(item);
                }
            }

            return RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        } else {
            if (input.size() == 1 && this.ingredients.size() == 1) {
                return this.ingredients.getFirst().test(input.getItem(0));
            }
            return input.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return this.result.create();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new ShapelessCraftingRecipeDisplay(
                        this.ingredients.stream().map(Ingredient::display).toList(),
                        new SlotDisplay.ItemStackSlotDisplay(this.result),
                        new SlotDisplay.ItemSlotDisplay(AEItems.CERTUS_QUARTZ_KNIFE.asItem())));
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(ingredients);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        boolean damagedKnife = false;

        for (int i = 0; i < remainingItems.size(); i++) {
            ItemStack item = input.getItem(i);

            if (!damagedKnife && item.is(ConventionTags.QUARTZ_KNIFE)) {
                damagedKnife = true;
                var result = item.copy();

                var broken = new MutableBoolean(false);
                if (CommonHooks.getCraftingPlayer() instanceof ServerPlayer serverPlayer) {
                    result.hurtAndBreak(1, serverPlayer.level(), serverPlayer, ignored -> broken.setTrue());
                } else {
                    var currentServer = ServerLifecycleHooks.getCurrentServer();
                    if (currentServer != null) {
                        result.hurtAndBreak(1, currentServer.overworld(), null, ignored -> broken.setTrue());
                    }
                }
                remainingItems.set(i, broken.getValue() ? ItemStack.EMPTY : result);
            } else if (item.getCraftingRemainder() != null) {
                remainingItems.set(i, item.getCraftingRemainder().create());
            }
        }

        return remainingItems;
    }
}
