package appeng.recipes.transform;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.init.InitRecipeTypes;

public final class TransformRecipe implements Recipe<RecipeInput> {
    public static final ResourceLocation TYPE_ID = AppEng.makeId("transform");
    public static final RecipeType<TransformRecipe> TYPE = InitRecipeTypes.register(TYPE_ID.toString());

    public static final MapCodec<TransformRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(
                Ingredient.CODEC_NONEMPTY
                        .listOf()
                        .fieldOf("ingredients")
                        .flatXmap(ingredients -> {
                            return DataResult
                                    .success(NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)));
                        }, DataResult::success)
                        .forGetter(r -> r.ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.output),
                TransformCircumstance.CODEC
                        .optionalFieldOf("circumstance", TransformCircumstance.fluid(FluidTags.WATER))
                        .forGetter(t -> t.circumstance))
                .apply(builder, TransformRecipe::new);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, TransformRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)),
            TransformRecipe::getIngredients,
            ItemStack.STREAM_CODEC,
            TransformRecipe::getResultItem,
            TransformCircumstance.STREAM_CODEC,
            TransformRecipe::getCircumstance,
            TransformRecipe::new);

    public final NonNullList<Ingredient> ingredients;
    public final ItemStack output;
    public final TransformCircumstance circumstance;

    public TransformRecipe(NonNullList<Ingredient> ingredients, ItemStack output,
            TransformCircumstance circumstance) {
        this.ingredients = ingredients;
        this.output = output;
        this.circumstance = circumstance;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public TransformCircumstance getCircumstance() {
        return circumstance;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
        ItemStack result = getResultItem(registries).copy();
        if (AEItems.QUANTUM_ENTANGLED_SINGULARITY.isSameAs(result) && result.getCount() > 1) {
            QuantumBridgeBlockEntity.assignFrequency(result);
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TransformRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
