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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import appeng.core.AppEng;
import appeng.items.tools.powered.EntropyManipulatorItem;
import appeng.recipes.AERecipeTypes;

/**
 * A special recipe used for the {@link EntropyManipulatorItem}.
 */
public class EntropyRecipe implements Recipe<RecipeInput> {

    public static final MapCodec<EntropyRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            EntropyMode.CODEC.fieldOf("mode").forGetter(EntropyRecipe::getMode),
            Input.CODEC.fieldOf("input").forGetter(EntropyRecipe::getInput),
            Output.CODEC.fieldOf("output").forGetter(EntropyRecipe::getOutput)).apply(builder, EntropyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntropyRecipe> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(EntropyMode.class),
            EntropyRecipe::getMode,
            Input.STREAM_CODEC,
            EntropyRecipe::getInput,
            Output.STREAM_CODEC,
            EntropyRecipe::getOutput,
            EntropyRecipe::new);

    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final Identifier TYPE_ID = AppEng.makeId("entropy");

    private final EntropyMode mode;
    private final Input input;
    private final Output output;

    EntropyRecipe(EntropyMode mode, Input input, Output output) {
        this.mode = mode;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(RecipeInput inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<EntropyRecipe> getSerializer() {
        return EntropyRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<EntropyRecipe> getType() {
        return AERecipeTypes.ENTROPY;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public EntropyMode getMode() {
        return this.mode;
    }

    @Nullable
    public BlockState getOutputBlockState(BlockState originalBlockState) {
        return output.block().map(blockOutput -> blockOutput.apply(originalBlockState)).orElse(null);
    }

    @Nullable
    public FluidState getOutputFluidState(FluidState originalFluidState) {
        return output.fluid().map(fluidOutput -> fluidOutput.apply(originalFluidState)).orElse(null);
    }

    public List<ItemStack> getDrops() {
        return this.output.drops();
    }

    public boolean matches(EntropyMode mode, BlockState blockState, FluidState fluidState) {
        if (this.getMode() != mode) {
            return false;
        }

        return input.matches(blockState, fluidState);
    }

    /**
     * Copies a property from one stateholder to another (if that stateholder also has that property).
     */
    private static <T extends Comparable<T>, SH extends StateHolder<?, SH>> SH copyProperty(SH from, SH to,
            Property<T> property) {
        if (to.hasProperty(property)) {
            return to.setValue(property, from.getValue(property));
        } else {
            return to;
        }
    }

    public Input getInput() {
        return input;
    }

    public Output getOutput() {
        return output;
    }

    public record Input(Optional<BlockInput> block, Optional<FluidInput> fluid) {
        public static Codec<Input> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BlockInput.CODEC.optionalFieldOf("block").forGetter(Input::block),
                FluidInput.CODEC.optionalFieldOf("fluid").forGetter(Input::fluid)).apply(builder, Input::new));

        public static StreamCodec<RegistryFriendlyByteBuf, Input> STREAM_CODEC = StreamCodec.composite(
                BlockInput.STREAM_CODEC.apply(ByteBufCodecs::optional),
                Input::block,
                FluidInput.STREAM_CODEC.apply(ByteBufCodecs::optional),
                Input::fluid,
                Input::new);

        public boolean matches(BlockState blockState, FluidState fluidState) {
            if (block.isPresent()) {
                var inputBlock = block.get().block();

                if (blockState.getBlock() != inputBlock) {
                    return false;
                }
                var stateDefinition = inputBlock.getStateDefinition();
                if (!PropertyUtils.doPropertiesMatch(stateDefinition, blockState, block.get().properties())) {
                    return false;
                }
            }

            if (fluid.isPresent()) {
                var inputFluid = fluid.get().fluid();
                if (fluidState.getType() != inputFluid) {
                    return false;
                }
                var stateDefinition = inputFluid.getStateDefinition();
                if (!PropertyUtils.doPropertiesMatch(stateDefinition, fluidState, fluid.get().properties())) {
                    return false;
                }
            }

            return true;
        }
    }

    public record BlockInput(Block block, Map<String, PropertyValueMatcher> properties) {
        public static Codec<BlockInput> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("id").forGetter(BlockInput::block),
                PropertyValueMatcher.MAP_CODEC.optionalFieldOf("properties", Map.of())
                        .forGetter(BlockInput::properties))
                .apply(builder, BlockInput::new));

        public static StreamCodec<RegistryFriendlyByteBuf, BlockInput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.BLOCK),
                BlockInput::block,
                ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8,
                        PropertyValueMatcher.STREAM_CODEC),
                BlockInput::properties,
                BlockInput::new);
    }

    public record FluidInput(Fluid fluid, Map<String, PropertyValueMatcher> properties) {
        public static Codec<FluidInput> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidInput::fluid),
                PropertyValueMatcher.MAP_CODEC.optionalFieldOf("properties", Map.of())
                        .forGetter(FluidInput::properties))
                .apply(builder, FluidInput::new));

        public static StreamCodec<RegistryFriendlyByteBuf, FluidInput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.FLUID),
                FluidInput::fluid,
                ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8,
                        PropertyValueMatcher.STREAM_CODEC),
                FluidInput::properties,
                FluidInput::new);
    }

    public record Output(Optional<BlockOutput> block, Optional<FluidOutput> fluid, List<ItemStack> drops) {

        public static Codec<Output> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BlockOutput.CODEC.optionalFieldOf("block").forGetter(Output::block),
                FluidOutput.CODEC.optionalFieldOf("fluid").forGetter(Output::fluid),
                ItemStack.CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(Output::drops))
                .apply(builder, Output::new));

        public static StreamCodec<RegistryFriendlyByteBuf, Output> STREAM_CODEC = StreamCodec.composite(
                BlockOutput.STREAM_CODEC.apply(ByteBufCodecs::optional),
                Output::block,
                FluidOutput.STREAM_CODEC.apply(ByteBufCodecs::optional),
                Output::fluid,
                ItemStack.OPTIONAL_LIST_STREAM_CODEC,
                Output::drops,
                Output::new);
    }

    public record BlockOutput(Block block, boolean keepProperties, Map<String, String> properties) {

        public static Codec<BlockOutput> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("id").forGetter(BlockOutput::block),
                Codec.BOOL.optionalFieldOf("", false).forGetter(BlockOutput::keepProperties),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("properties", Map.of())
                        .forGetter(BlockOutput::properties))
                .apply(builder, BlockOutput::new));

        public static StreamCodec<RegistryFriendlyByteBuf, BlockOutput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.BLOCK),
                BlockOutput::block,
                ByteBufCodecs.BOOL,
                BlockOutput::keepProperties,
                ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8,
                        ByteBufCodecs.STRING_UTF8),
                BlockOutput::properties,
                BlockOutput::new);

        public BlockState apply(BlockState originalBlockState) {
            BlockState state = block.defaultBlockState();

            if (keepProperties) {
                for (Property<?> property : originalBlockState.getProperties()) {
                    state = copyProperty(originalBlockState, state, property);
                }
            }

            var stateDefinition = originalBlockState.getBlock().getStateDefinition();
            state = PropertyUtils.applyProperties(stateDefinition, state, properties);

            return state;
        }
    }

    public record FluidOutput(Fluid fluid, boolean keepProperties, Map<String, String> properties) {

        public static Codec<FluidOutput> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidOutput::fluid),
                Codec.BOOL.optionalFieldOf("", false).forGetter(FluidOutput::keepProperties),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("properties", Map.of())
                        .forGetter(FluidOutput::properties))
                .apply(builder, FluidOutput::new));

        public static StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.FLUID),
                FluidOutput::fluid,
                ByteBufCodecs.BOOL,
                FluidOutput::keepProperties,
                ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8,
                        ByteBufCodecs.STRING_UTF8),
                FluidOutput::properties,
                FluidOutput::new);

        public FluidState apply(FluidState originalFluidState) {
            FluidState state = fluid.defaultFluidState();

            if (keepProperties) {
                for (Property<?> property : originalFluidState.getProperties()) {
                    state = copyProperty(originalFluidState, state, property);
                }
            }

            var stateDefinition = state.getType().getStateDefinition();
            state = PropertyUtils.applyProperties(stateDefinition, state, properties);

            return state;
        }

        public static void toNetwork(FriendlyByteBuf buffer, FluidOutput output) {
            buffer.writeById(BuiltInRegistries.FLUID::getId, output.fluid);
            buffer.writeBoolean(output.keepProperties);
            buffer.writeMap(output.properties, FriendlyByteBuf::writeUtf, (fbb, value) -> fbb.writeUtf(value));
        }

        public static FluidOutput fromNetwork(FriendlyByteBuf buffer) {
            var fluid = buffer.readById(BuiltInRegistries.FLUID::byId);
            var keepProperties = buffer.readBoolean();
            var properties = buffer.readMap(FriendlyByteBuf::readUtf, fbb -> fbb.readUtf());
            return new FluidOutput(fluid, keepProperties, properties);
        }
    }

}
