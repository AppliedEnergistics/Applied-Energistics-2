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
import com.google.gson.JsonPrimitive;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;

public class EntropyRecipeSerializer implements RecipeSerializer<EntropyRecipe> {

    public static final EntropyRecipeSerializer INSTANCE = new EntropyRecipeSerializer();

    private EntropyRecipeSerializer() {
    }

    @Override
    public EntropyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        EntropyRecipeBuilder builder = new EntropyRecipeBuilder();

        // Set id and mode
        builder.setId(recipeId);
        builder.setMode(EntropyMode.valueOf(GsonHelper.getAsString(json, "mode").toUpperCase(Locale.ROOT)));

        //// Parse inputs
        JsonObject inputJson = GsonHelper.getAsJsonObject(json, "input");

        // Input block
        JsonObject inputBlockObject = GsonHelper.getAsJsonObject(inputJson, "block", new JsonObject());
        String inputBlockId = GsonHelper.getAsString(inputBlockObject, "id", null);
        if (inputBlockId != null) {
            Block block = getRequiredEntry(BuiltInRegistries.BLOCK, inputBlockId);
            builder.setInputBlock(block);
            parseStateMatchers(block.getStateDefinition(), inputBlockObject, builder::addBlockStateMatcher);
        }

        // input fluid
        JsonObject inputFluidObject = GsonHelper.getAsJsonObject(inputJson, "fluid", new JsonObject());
        String inputFluidId = GsonHelper.getAsString(inputFluidObject, "id", null);
        if (inputFluidId != null) {
            Fluid fluid = getRequiredEntry(BuiltInRegistries.FLUID, inputFluidId);
            builder.setInputFluid(fluid);
            parseStateMatchers(fluid.getStateDefinition(), inputFluidObject, builder::addFluidStateMatcher);
        }

        //// Parse outputs
        JsonObject outputJson = GsonHelper.getAsJsonObject(json, "output");

        // Output block
        JsonObject outputBlockObject = GsonHelper.getAsJsonObject(outputJson, "block", new JsonObject());
        String outputBlockId = GsonHelper.getAsString(outputBlockObject, "id", null);
        if (outputBlockId != null) {
            Block block = getRequiredEntry(BuiltInRegistries.BLOCK, outputBlockId);
            builder.setOutputBlock(block);

            boolean outputBlockKeep = GsonHelper.getAsBoolean(outputBlockObject, "keep", false);
            builder.setOutputBlockKeep(outputBlockKeep);

            parseStateAppliers(block.getStateDefinition(), outputBlockObject, builder::addBlockStateAppliers);
        }

        // Output fluid
        JsonObject outputFluidObject = GsonHelper.getAsJsonObject(outputJson, "fluid", new JsonObject());
        String outputFluidId = GsonHelper.getAsString(outputFluidObject, "id", null);
        if (outputFluidId != null) {
            Fluid fluid = getRequiredEntry(BuiltInRegistries.FLUID, outputFluidId);
            builder.setOutputFluid(fluid);

            boolean outputFluidKeep = GsonHelper.getAsBoolean(outputFluidObject, "keep", false);
            builder.setOutputFluidKeep(outputFluidKeep);

            parseStateAppliers(fluid.getStateDefinition(), outputFluidObject, builder::addFluidStateAppliers);
        }

        // Parse additional drops
        if (outputJson.has("drops")) {
            JsonArray dropList = GsonHelper.getAsJsonArray(outputJson, "drops");
            List<ItemStack> drops = new ArrayList<>(dropList.size());

            for (JsonElement jsonElement : dropList) {
                JsonObject object = jsonElement.getAsJsonObject();
                String itemid = GsonHelper.getAsString(object, "item");
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemid));
                int count = GsonHelper.getAsInt(object, "count", 1);
                drops.add(new ItemStack(item, count));
            }

            builder.setDrops(drops);
        }

        return builder.build();
    }

    private static <T> T getRequiredEntry(Registry<T> registry, String id) {
        T entry = registry.getOptional(new ResourceLocation(id)).orElse(null);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown id " + id + " for " + registry);
        }
        return entry;
    }

    @Nullable
    @Override
    public EntropyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        EntropyRecipeBuilder builder = new EntropyRecipeBuilder();

        builder.setId(recipeId);
        builder.setMode(buffer.readEnum(EntropyMode.class));

        if (buffer.readBoolean()) {
            Block inputBlock = BuiltInRegistries.BLOCK.byId(buffer.readVarInt());
            builder.setInputBlock(inputBlock);
            int matcherSize = buffer.readInt();
            for (int i = 0; i < matcherSize; i++) {
                builder.addBlockStateMatcher(StateMatcher.read(inputBlock.getStateDefinition(), buffer));
            }
        }

        if (buffer.readBoolean()) {
            Fluid fluid = BuiltInRegistries.FLUID.byId(buffer.readVarInt());
            builder.setInputFluid(fluid);
            int matcherSize = buffer.readInt();
            for (int i = 0; i < matcherSize; i++) {
                builder.addFluidStateMatcher(StateMatcher.read(fluid.getStateDefinition(), buffer));
            }
        }

        if (buffer.readBoolean()) {
            Block block = BuiltInRegistries.BLOCK.byId(buffer.readVarInt());
            builder.setOutputBlock(block);
            builder.setOutputBlockKeep(buffer.readBoolean());
            int appliersSize = buffer.readInt();
            for (int i = 0; i < appliersSize; i++) {
                builder.addBlockStateAppliers(StateApplier.readFromPacket(block.getStateDefinition(), buffer));
            }
        }

        if (buffer.readBoolean()) {
            Fluid fluid = BuiltInRegistries.FLUID.byId(buffer.readVarInt());
            ;
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
    public void toNetwork(FriendlyByteBuf buffer, EntropyRecipe recipe) {
        buffer.writeEnum(recipe.getMode());

        buffer.writeBoolean(recipe.getInputBlock() != null);
        if (recipe.getInputBlock() != null) {
            buffer.writeVarInt(BuiltInRegistries.BLOCK.getId(recipe.getInputBlock()));

            List<StateMatcher> inputBlockMatchers = recipe.getInputBlockMatchers();
            buffer.writeInt(inputBlockMatchers.size());
            for (StateMatcher stateMatcher : inputBlockMatchers) {
                stateMatcher.writeToPacket(buffer);
            }
        }

        buffer.writeBoolean(recipe.getInputFluid() != null);
        if (recipe.getInputFluid() != null) {
            buffer.writeVarInt(BuiltInRegistries.FLUID.getId(recipe.getInputFluid()));

            List<StateMatcher> inputFluidMatchers = recipe.getInputFluidMatchers();
            buffer.writeInt(inputFluidMatchers.size());
            for (StateMatcher stateMatcher : inputFluidMatchers) {
                stateMatcher.writeToPacket(buffer);
            }
        }

        buffer.writeBoolean(recipe.getOutputBlock() != null);
        if (recipe.getOutputBlock() != null) {
            buffer.writeVarInt(BuiltInRegistries.BLOCK.getId(recipe.getOutputBlock()));
            buffer.writeBoolean(recipe.getOutputBlockKeep());

            List<StateApplier<?>> appliers = recipe.getOutputBlockStateAppliers();
            buffer.writeInt(appliers.size());
            for (StateApplier<?> blockStateApplier : appliers) {
                blockStateApplier.writeToPacket(buffer);
            }
        }

        buffer.writeBoolean(recipe.getOutputFluid() != null);
        if (recipe.getOutputFluid() != null) {
            buffer.writeVarInt(BuiltInRegistries.FLUID.getId(recipe.getOutputFluid()));
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

    private static void parseStateMatchers(StateDefinition<?, ?> stateDefinition, JsonObject propertiesContainer,
            Consumer<StateMatcher> consumer) {
        JsonObject properties = GsonHelper.getAsJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value == null) {
                return;
            }

            if (value.isJsonPrimitive()) {
                consumer.accept(SingleValueMatcher.create(stateDefinition, key, value.getAsString()));
            } else if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                List<String> list = new ArrayList<>();
                for (JsonElement e : array) {
                    list.add(e.getAsString());
                }
                consumer.accept(MultipleValuesMatcher.create(stateDefinition, key, list));
            } else if (value.isJsonObject() && value.getAsJsonObject().has("min")
                    && value.getAsJsonObject().has("max")) {
                String min = value.getAsJsonObject().get("min").getAsString();
                String max = value.getAsJsonObject().get("max").getAsString();

                consumer.accept(RangeValueMatcher.create(stateDefinition, key, min, max));
            } else {
                throw new IllegalArgumentException("Invalid matcher: " + value);
            }
        });
    }

    private static void parseStateAppliers(StateDefinition<?, ?> stateDefinition, JsonObject propertiesContainer,
            Consumer<StateApplier<?>> consumer) {
        JsonObject properties = GsonHelper.getAsJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();

            consumer.accept(StateApplier.create(stateDefinition, key, value));
        });
    }

    public void toJson(EntropyRecipe recipe, JsonObject json) {
        json.addProperty("mode", recipe.getMode().name().toLowerCase(Locale.ROOT));
        json.add("input", serializeInput(recipe));
        json.add("output", serializeOutput(recipe));
    }

    private JsonObject serializeInput(EntropyRecipe recipe) {
        var input = new JsonObject();
        if (recipe.getInputBlock() != null) {
            var jsonBlock = new JsonObject();
            jsonBlock.addProperty("id", BuiltInRegistries.BLOCK.getKey(recipe.getInputBlock()).toString());
            serializeStateMatchers(recipe.getInputBlockMatchers(), jsonBlock);
            input.add("block", jsonBlock);
        }

        if (recipe.getInputFluid() != null) {
            var jsonFluid = new JsonObject();
            jsonFluid.addProperty("id", BuiltInRegistries.FLUID.getKey(recipe.getInputFluid()).toString());
            serializeStateMatchers(recipe.getInputFluidMatchers(), jsonFluid);
            input.add("fluid", jsonFluid);
        }
        return input;
    }

    private JsonElement serializeOutput(EntropyRecipe recipe) {
        var output = new JsonObject();
        if (recipe.getOutputBlock() != null) {
            var jsonBlock = new JsonObject();
            jsonBlock.addProperty("id", BuiltInRegistries.BLOCK.getKey(recipe.getOutputBlock()).toString());
            if (recipe.getOutputBlockKeep()) {
                jsonBlock.addProperty("keep", true);
            }
            serializeStateAppliers(recipe.getOutputBlockStateAppliers(), jsonBlock);
            output.add("block", jsonBlock);
        }

        if (recipe.getOutputFluid() != null) {
            var jsonFluid = new JsonObject();
            jsonFluid.addProperty("id", BuiltInRegistries.FLUID.getKey(recipe.getOutputFluid()).toString());
            if (recipe.getOutputFluidKeep()) {
                jsonFluid.addProperty("keep", true);
            }
            serializeStateAppliers(recipe.getOutputFluidStateAppliers(), jsonFluid);
            output.add("fluid", jsonFluid);
        }

        if (!recipe.getDrops().isEmpty()) {
            var jsonDrops = new JsonArray();

            for (var drop : recipe.getDrops()) {
                var jsonDrop = new JsonObject();
                jsonDrop.addProperty("item", BuiltInRegistries.ITEM.getKey(drop.getItem()).toString());
                if (drop.getCount() > 1) {
                    jsonDrop.addProperty("count", drop.getCount());
                }
                jsonDrops.add(jsonDrop);
            }

            output.add("drops", jsonDrops);
        }
        return output;
    }

    private void serializeStateMatchers(List<StateMatcher> matchers, JsonObject json) {
        if (matchers.isEmpty()) {
            return;
        }

        var properties = new JsonObject();
        for (var matcher : matchers) {
            JsonElement serializedMatcher;
            if (matcher instanceof SingleValueMatcher<?>singleMatcher) {
                serializedMatcher = new JsonPrimitive(singleMatcher.getValueName());
            } else if (matcher instanceof MultipleValuesMatcher<?>multiMatcher) {
                var values = new JsonArray();
                for (var valueName : multiMatcher.getValueNames()) {
                    values.add(valueName);
                }
                serializedMatcher = values;
            } else if (matcher instanceof RangeValueMatcher<?>rangeMatcher) {
                var range = new JsonObject();
                range.addProperty("min", rangeMatcher.getMinValueName());
                range.addProperty("max", rangeMatcher.getMaxValueName());
                serializedMatcher = range;
            } else {
                throw new IllegalStateException("Don't know how to serialize state matcher " + matcher);
            }

            String propertyName = matcher.getProperty().getName();
            properties.add(propertyName, serializedMatcher);
        }
        json.add("properties", properties);
    }

    private void serializeStateAppliers(List<StateApplier<?>> appliers, JsonObject json) {
        if (appliers.isEmpty()) {
            return;
        }

        var properties = new JsonObject();
        for (var applier : appliers) {
            var property = applier.getProperty();
            properties.addProperty(property.getName(), applier.getValueName());
        }
        json.add("properties", properties);
    }

}
