package appeng.recipes.transform;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.recipes.AERecipeTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public final class TransformRecipe implements Recipe<TransformRecipeInput> {
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("transform");
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final RecipeType<TransformRecipe> TYPE = AERecipeTypes.TRANSFORM;

    public static final MapCodec<TransformRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(
                        Ingredient.CODEC
                                .listOf()
                                .fieldOf("ingredients")
                                .flatXmap(ingredients -> {
                                    return DataResult
                                            .success(NonNullList.of(Ingredient.of(), ingredients.toArray(Ingredient[]::new)));
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

    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public TransformCircumstance getCircumstance() {
        return circumstance;
    }

    @Override
    public boolean matches(TransformRecipeInput container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(TransformRecipeInput container, HolderLookup.Provider registries) {
        ItemStack result = getResultItem().copy();
        if (AEItems.QUANTUM_ENTANGLED_SINGULARITY.is(result) && result.getCount() > 1) {
            QuantumBridgeBlockEntity.assignFrequency(result);
        }
        return result;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new TransformRecipeDisplay(
                        ingredients.stream().map(Ingredient::display).toList(),
                        circumstance,
                        new SlotDisplay.ItemStackSlotDisplay(output)
                )
        );
    }

    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public RecipeSerializer<TransformRecipe> getSerializer() {
        return TransformRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<TransformRecipe> getType() {
        return TYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
