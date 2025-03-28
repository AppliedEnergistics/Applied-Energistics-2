package appeng.recipes.quartzcutting;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.apache.commons.lang3.mutable.MutableBoolean;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import appeng.core.ConventionTags;

public class QuartzCuttingRecipe implements CraftingRecipe {
    public static final MapCodec<QuartzCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(QuartzCuttingRecipe::getResult),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(QuartzCuttingRecipe::getIngredients))
            .apply(builder, QuartzCuttingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuartzCuttingRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, QuartzCuttingRecipe::getResult,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), QuartzCuttingRecipe::getIngredients,
            QuartzCuttingRecipe::new);

    final ItemStack result;
    final List<Ingredient> ingredients;
    private final boolean isSimple;

    public QuartzCuttingRecipe(ItemStack result, List<Ingredient> ingredients) {
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    public RecipeSerializer<QuartzCuttingRecipe> getSerializer() {
        return QuartzCuttingRecipeSerializer.INSTANCE;
    }

    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    public List<Ingredient> getIngredients() {
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

    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    private ItemStack getResult() {
        return result;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
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
                    result.hurtAndBreak(1, serverPlayer.serverLevel(), serverPlayer, ignored -> broken.setTrue());
                } else {
                    var currentServer = ServerLifecycleHooks.getCurrentServer();
                    if (currentServer != null) {
                        result.hurtAndBreak(1, currentServer.overworld(), null, ignored -> broken.setTrue());
                    }
                }
                remainingItems.set(i, broken.getValue() ? ItemStack.EMPTY : result);
            } else if (!item.getCraftingRemainder().isEmpty()) {
                remainingItems.set(i, item.getCraftingRemainder());
            }
        }

        return remainingItems;
    }
}
