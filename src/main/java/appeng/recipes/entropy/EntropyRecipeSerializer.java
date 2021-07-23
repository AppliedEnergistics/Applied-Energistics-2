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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class EntropyRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<EntropyRecipe> {

    public static final EntropyRecipeSerializer INSTANCE = new EntropyRecipeSerializer();

    static {
        INSTANCE.setRegistryName(EntropyRecipe.TYPE_ID);
    }

    private EntropyRecipeSerializer() {
    }

    @Override
    public EntropyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        EntropyRecipeBuilder builder = new EntropyRecipeBuilder();

        // Set id and mode
        builder.setId(recipeId);
        builder.setMode(EntropyMode.valueOf(JSONUtils.getAsString(json, "mode").toUpperCase(Locale.ROOT)));

        //// Parse inputs
        JsonObject inputJson = JSONUtils.getAsJsonObject(json, "input");

        // Input block
        JsonObject inputBlockObject = JSONUtils.getAsJsonObject(inputJson, "block", new JsonObject());
        String inputBlockId = JSONUtils.getAsString(inputBlockObject, "id", null);
        if (inputBlockId != null) {
            Block block = getRequiredEntry(ForgeRegistries.BLOCKS, inputBlockId);
            builder.setInputBlock(block);
            parseStateMatchers(block.getStateDefinition(), inputBlockObject, builder::addBlockStateMatcher);
        }

        // input fluid
        JsonObject inputFluidObject = JSONUtils.getAsJsonObject(inputJson, "fluid", new JsonObject());
        String inputFluidId = JSONUtils.getAsString(inputFluidObject, "id", null);
        if (inputFluidId != null) {
            Fluid fluid = getRequiredEntry(ForgeRegistries.FLUIDS, inputFluidId);
            builder.setInputFluid(fluid);
            parseStateMatchers(fluid.getStateDefinition(), inputFluidObject, builder::addFluidStateMatcher);
        }

        //// Parse outputs
        JsonObject outputJson = JSONUtils.getAsJsonObject(json, "output");

        // Output block
        JsonObject outputBlockObject = JSONUtils.getAsJsonObject(outputJson, "block", new JsonObject());
        String outputBlockId = JSONUtils.getAsString(outputBlockObject, "id", null);
        if (outputBlockId != null) {
            Block block = getRequiredEntry(ForgeRegistries.BLOCKS, outputBlockId);
            builder.setOutputBlock(block);

            boolean outputBlockKeep = JSONUtils.getAsBoolean(outputBlockObject, "keep", false);
            builder.setOutputBlockKeep(outputBlockKeep);

            parseStateAppliers(block.getStateDefinition(), outputBlockObject, builder::addBlockStateAppliers);
        }

        // Output fluid
        JsonObject outputFluidObject = JSONUtils.getAsJsonObject(outputJson, "fluid", new JsonObject());
        String outputFluidId = JSONUtils.getAsString(outputFluidObject, "id", null);
        if (outputFluidId != null) {
            Fluid fluid = getRequiredEntry(ForgeRegistries.FLUIDS, outputFluidId);
            builder.setOutputFluid(fluid);

            boolean outputFluidKeep = JSONUtils.getAsBoolean(outputFluidObject, "keep", false);
            builder.setOutputFluidKeep(outputFluidKeep);

            parseStateAppliers(fluid.getStateDefinition(), outputFluidObject, builder::addFluidStateAppliers);
        }

        // Parse additional drops
        if (outputJson.has("drops")) {
            JsonArray dropList = JSONUtils.getAsJsonArray(outputJson, "drops");
            List<ItemStack> drops = new ArrayList<>(dropList.size());

            for (JsonElement jsonElement : dropList) {
                JsonObject object = jsonElement.getAsJsonObject();
                String itemid = JSONUtils.getAsString(object, "item");
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemid));
                int count = JSONUtils.getAsInt(object, "count", 1);
                drops.add(new ItemStack(item, count));
            }

            builder.setDrops(drops);
        }

        return builder.build();
    }

    private static <T extends IForgeRegistryEntry<T>> T getRequiredEntry(IForgeRegistry<T> registry, String id) {
        T entry = registry.getValue(new ResourceLocation(id));
        if (entry == null) {
            throw new IllegalArgumentException("Unknown id " + id + " for " + registry.getRegistryName());
        }
        return entry;
    }

    @Nullable
    @Override
    public EntropyRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        EntropyRecipeBuilder builder = new EntropyRecipeBuilder();

        builder.setId(recipeId);
        builder.setMode(buffer.readEnum(EntropyMode.class));

        if (buffer.readBoolean()) {
            Block inputBlock = buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS);
            builder.setInputBlock(inputBlock);
            int matcherSize = buffer.readInt();
            for (int i = 0; i < matcherSize; i++) {
                builder.addBlockStateMatcher(StateMatcher.read(inputBlock.getStateDefinition(), buffer));
            }
        }

        if (buffer.readBoolean()) {
            Fluid fluid = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
            builder.setInputFluid(fluid);
            int matcherSize = buffer.readInt();
            for (int i = 0; i < matcherSize; i++) {
                builder.addFluidStateMatcher(StateMatcher.read(fluid.getStateDefinition(), buffer));
            }
        }

        if (buffer.readBoolean()) {
            Block block = buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS);
            builder.setOutputBlock(block);
            builder.setOutputBlockKeep(buffer.readBoolean());
            int appliersSize = buffer.readInt();
            for (int i = 0; i < appliersSize; i++) {
                builder.addBlockStateAppliers(StateApplier.readFromPacket(block.getStateDefinition(), buffer));
            }
        }

        if (buffer.readBoolean()) {
            Fluid fluid = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
            builder.setOutputFluid(fluid);
            builder.setOutputFluidKeep(buffer.readBoolean());
            int appliersSize = buffer.readInt();
            for (int i = 0; i < appliersSize; i++) {
                builder.addFluidStateAppliers(StateApplier.readFromPacket(fluid.getStateDefinition(), buffer));
            }
        }

        // We use an empty list later when null, so avoid instantiating an empty ArrayList.
        int dropSize = buffer.readInt();
        if (dropSize > 0) {
            List<ItemStack> drops = new ArrayList<>(dropSize);
            for (int i = 0; i < dropSize; i++) {
                drops.add(buffer.readItem());
            }
            builder.setDrops(drops);
        }

        return builder.build();
    }

    @Override
    public void toNetwork(PacketBuffer buffer, EntropyRecipe recipe) {
        buffer.writeEnum(recipe.getMode());

        buffer.writeBoolean(recipe.getInputBlock() != null);
        if (recipe.getInputBlock() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, recipe.getInputBlock());

            List<StateMatcher> inputBlockMatchers = recipe.getInputBlockMatchers();
            buffer.writeInt(inputBlockMatchers.size());
            for (StateMatcher stateMatcher : inputBlockMatchers) {
                stateMatcher.writeToPacket(buffer);
            }
        }

        buffer.writeBoolean(recipe.getInputFluid() != null);
        if (recipe.getInputFluid() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, recipe.getInputFluid());

            List<StateMatcher> inputFluidMatchers = recipe.getInputFluidMatchers();
            buffer.writeInt(inputFluidMatchers.size());
            for (StateMatcher stateMatcher : inputFluidMatchers) {
                stateMatcher.writeToPacket(buffer);
            }
        }

        buffer.writeBoolean(recipe.getOutputBlock() != null);
        if (recipe.getOutputBlock() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, recipe.getOutputBlock());
            buffer.writeBoolean(recipe.getOutputBlockKeep());

            List<StateApplier<?>> appliers = recipe.getOutputBlockStateAppliers();
            buffer.writeInt(appliers.size());
            for (StateApplier<?> blockStateApplier : appliers) {
                blockStateApplier.writeToPacket(buffer);
            }
        }

        buffer.writeBoolean(recipe.getOutputFluid() != null);
        if (recipe.getOutputFluid() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, recipe.getOutputFluid());
            buffer.writeBoolean(recipe.getOutputFluidKeep());

            List<StateApplier<?>> appliers = recipe.getOutputFluidStateAppliers();
            buffer.writeInt(appliers.size());
            for (StateApplier<?> fluidStateApplier : appliers) {
                fluidStateApplier.writeToPacket(buffer);
            }
        }

        buffer.writeInt(recipe.getDrops().size());
        for (ItemStack itemStack : recipe.getDrops()) {
            buffer.writeItem(itemStack);
        }
    }

    private static void parseStateMatchers(StateContainer<?, ?> stateContainer, JsonObject propertiesContainer,
            Consumer<StateMatcher> consumer) {
        JsonObject properties = JSONUtils.getAsJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value == null) {
                return;
            }

            if (value.isJsonPrimitive()) {
                consumer.accept(SingleValueMatcher.create(stateContainer, key, value.getAsString()));
            } else if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                List<String> list = new ArrayList<>();
                for (JsonElement e : array) {
                    list.add(e.getAsString());
                }
                consumer.accept(MultipleValuesMatcher.create(stateContainer, key, list));
            } else if (value.isJsonObject() && value.getAsJsonObject().has("min")
                    && value.getAsJsonObject().has("max")) {
                String min = value.getAsJsonObject().get("min").getAsString();
                String max = value.getAsJsonObject().get("max").getAsString();

                consumer.accept(RangeValueMatcher.create(stateContainer, key, min, max));
            } else {
                throw new IllegalArgumentException("Invalid matcher: " + value);
            }
        });
    }

    private static void parseStateAppliers(StateContainer<?, ?> stateContainer, JsonObject propertiesContainer,
            Consumer<StateApplier<?>> consumer) {
        JsonObject properties = JSONUtils.getAsJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();

            consumer.accept(StateApplier.create(stateContainer, key, value));
        });
    }

}
