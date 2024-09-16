package appeng.recipes.quartzcutting;

import java.util.ArrayList;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.apache.commons.lang3.mutable.MutableBoolean;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import appeng.datagen.providers.tags.ConventionTags;

public class QuartzCuttingRecipe implements CraftingRecipe {
    static final int MAX_HEIGHT = 3;
    static final int MAX_WIDTH = 3;
    public static final MapCodec<QuartzCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(QuartzCuttingRecipe::getResult),
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((r) -> {
                var ingredients = r.toArray(Ingredient[]::new);
                if (ingredients.length == 0) {
                    return DataResult.error(() -> "No ingredients for quartz cutting recipe");
                } else {
                    return ingredients.length > MAX_HEIGHT * MAX_WIDTH ? DataResult.error(() -> {
                        return "Too many ingredients for quartz cutting recipe. The maximum is: %s"
                                .formatted(MAX_HEIGHT * MAX_WIDTH);
                    }) : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
                }
            }, DataResult::success).forGetter(QuartzCuttingRecipe::getIngredients))
            .apply(builder, QuartzCuttingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuartzCuttingRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, QuartzCuttingRecipe::getResult,
            StreamCodec.of(
                    (buffer, value) -> {
                        buffer.writeVarInt(value.size());
                        for (var ingredient : value) {
                            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
                        }
                    },
                    buffer -> {
                        int count = buffer.readVarInt();
                        NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
                        ingredients.replaceAll(ignored -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
                        return ingredients;
                    }),
            QuartzCuttingRecipe::getIngredients,
            QuartzCuttingRecipe::new);

    final ItemStack result;
    final NonNullList<Ingredient> ingredients;
    private final boolean isSimple;

    public QuartzCuttingRecipe(ItemStack result, NonNullList<Ingredient> ingredients) {
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    public RecipeSerializer<?> getSerializer() {
        return QuartzCuttingRecipeSerializer.INSTANCE;
    }

    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    public NonNullList<Ingredient> getIngredients() {
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

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    private ItemStack getResult() {
        return result;
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
            } else if (item.hasCraftingRemainingItem()) {
                remainingItems.set(i, item.getCraftingRemainingItem());
            }
        }

        return remainingItems;
    }
}
