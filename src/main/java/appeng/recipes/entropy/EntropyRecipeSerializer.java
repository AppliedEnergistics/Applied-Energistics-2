/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.recipes.entropy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EntropyRecipeSerializer implements RecipeSerializer<EntropyRecipe> {

    public static final EntropyRecipeSerializer INSTANCE = new EntropyRecipeSerializer();

    public static final Codec<EntropyRecipe> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            EntropyMode.CODEC.fieldOf("mode").forGetter(EntropyRecipe::getMode),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("inputBlock").forGetter(EntropyRecipe::getInputBlock),
            PropertyValueMatcher.MAP_CODEC.optionalFieldOf("inputBlockMatchers", Map.of()).forGetter(EntropyRecipe::getInputBlockMatchers),
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("inputFluid").forGetter(EntropyRecipe::getInputFluid),
            PropertyValueMatcher.MAP_CODEC.optionalFieldOf("inputFluidMatchers", Map.of()).forGetter(EntropyRecipe::getInputFluidMatchers),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("outputBlock").forGetter(EntropyRecipe::getOutputBlock),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("outputBlockStateAppliers", Map.of()).forGetter(EntropyRecipe::getOutputBlockStateAppliers),
            Codec.BOOL.optionalFieldOf("outputBlockKeep", false).forGetter(EntropyRecipe::getOutputBlockKeep),
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("outputFluid").forGetter(EntropyRecipe::getOutputFluid),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("outputFluidStateAppliers", Map.of()).forGetter(EntropyRecipe::getOutputFluidStateAppliers),
            Codec.BOOL.optionalFieldOf("outputFluidKeep", false).forGetter(EntropyRecipe::getOutputFluidKeep),
            ItemStack.ITEM_WITH_COUNT_CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(EntropyRecipe::getDrops)
    ).apply(builder, EntropyRecipe::new));

    private EntropyRecipeSerializer() {
    }

    @Override
    public Codec<EntropyRecipe> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public EntropyRecipe fromNetwork(FriendlyByteBuf buffer) {
        EntropyRecipeBuilder builder = new EntropyRecipeBuilder();

        builder.setMode(buffer.readEnum(EntropyMode.class));

        if (buffer.readBoolean()) {
            Block inputBlock = BuiltInRegistries.BLOCK.byId(buffer.readVarInt());
            builder.setInputBlock(inputBlock);
            builder.setBlockStateMatchers(buffer.readMap(FriendlyByteBuf::readUtf, PropertyValueMatcher::fromNetwork));
        }

        if (buffer.readBoolean()) {
            Fluid fluid = BuiltInRegistries.FLUID.byId(buffer.readVarInt());
            builder.setInputFluid(fluid);
            builder.setFluidStateMatchers(buffer.readMap(FriendlyByteBuf::readUtf, PropertyValueMatcher::fromNetwork));
        }

        if (buffer.readBoolean()) {
            Block block = BuiltInRegistries.BLOCK.byId(buffer.readVarInt());
            builder.setOutputBlock(block);
            builder.setOutputBlockKeep(buffer.readBoolean());
            builder.setBlockStateAppliers(buffer.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf));
        }

        if (buffer.readBoolean()) {
            Fluid fluid = BuiltInRegistries.FLUID.byId(buffer.readVarInt());
            builder.setOutputFluid(fluid);
            builder.setOutputFluidKeep(buffer.readBoolean());
            builder.setFluidStateAppliers(buffer.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf));
        }

        builder.setDrops(buffer.readList(FriendlyByteBuf::readItem));

        return builder.build();
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, EntropyRecipe recipe) {
        buffer.writeEnum(recipe.getMode());

        buffer.writeBoolean(recipe.getInputBlock().isPresent());
        if (recipe.getInputBlock().isPresent()) {
            buffer.writeVarInt(BuiltInRegistries.BLOCK.getId(recipe.getInputBlock().get()));
            buffer.writeMap(recipe.getInputBlockMatchers(), FriendlyByteBuf::writeUtf, PropertyValueMatcher::toNetwork);
        }

        buffer.writeBoolean(recipe.getInputFluid().isPresent());
        if (recipe.getInputFluid().isPresent()) {
            buffer.writeVarInt(BuiltInRegistries.FLUID.getId(recipe.getInputFluid().get()));
            buffer.writeMap(recipe.getInputFluidMatchers(), FriendlyByteBuf::writeUtf, PropertyValueMatcher::toNetwork);
        }

        buffer.writeBoolean(recipe.getOutputBlock().isPresent());
        if (recipe.getOutputBlock().isPresent()) {
            buffer.writeVarInt(BuiltInRegistries.BLOCK.getId(recipe.getOutputBlock().get()));
            buffer.writeBoolean(recipe.getOutputBlockKeep());
            buffer.writeMap(recipe.getOutputBlockStateAppliers(), FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
        }

        buffer.writeBoolean(recipe.getOutputFluid().isPresent());
        if (recipe.getOutputFluid().isPresent()) {
            buffer.writeVarInt(BuiltInRegistries.FLUID.getId(recipe.getOutputFluid().get()));
            buffer.writeBoolean(recipe.getOutputFluidKeep());
            buffer.writeMap(recipe.getOutputFluidStateAppliers(), FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
        }

        buffer.writeCollection(recipe.getDrops(), FriendlyByteBuf::writeItem);
    }

}
