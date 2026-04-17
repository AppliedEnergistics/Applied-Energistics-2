package appeng.recipes.transform;

import java.util.List;

import appeng.recipes.MechanicsRecipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.recipes.AERecipeTypes;

public final class TransformRecipe extends MechanicsRecipe<TransformRecipeInput> {
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final Identifier TYPE_ID = AppEng.makeId("transform");
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final RecipeType<TransformRecipe> TYPE = AERecipeTypes.TRANSFORM;

    public static final MapCodec<TransformRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(
                Ingredient.CODEC
                        .listOf()
                        .fieldOf("ingredients")
                        .forGetter(TransformRecipe::ingredients),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(TransformRecipe::result),
                TransformCircumstance.CODEC
                        .optionalFieldOf("circumstance", TransformCircumstance.fluid(FluidTags.WATER))
                        .forGetter(TransformRecipe::circumstance))
                .apply(builder, TransformRecipe::new);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, TransformRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
            TransformRecipe::ingredients,
            ItemStackTemplate.STREAM_CODEC,
            TransformRecipe::result,
            TransformCircumstance.STREAM_CODEC,
            TransformRecipe::circumstance,
            TransformRecipe::new);

    public static final RecipeSerializer<TransformRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public final List<Ingredient> ingredients;
    public final ItemStackTemplate output;
    public final TransformCircumstance circumstance;

    public TransformRecipe(List<Ingredient> ingredients, ItemStackTemplate output, TransformCircumstance circumstance) {
        this.ingredients = ingredients;
        this.output = output;
        this.circumstance = circumstance;
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    public TransformCircumstance circumstance() {
        return circumstance;
    }

    public ItemStackTemplate result() {
        return output;
    }

    public ItemStack createResult() {
        ItemStack result = result().create();
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
                        new SlotDisplay.ItemStackSlotDisplay(output)));
    }

    @Override
    public RecipeSerializer<TransformRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<TransformRecipe> getType() {
        return TYPE;
    }
}
