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

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class EntropyRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<EntropyRecipe> {

    public static final EntropyRecipeSerializer INSTANCE = new EntropyRecipeSerializer();

    static {
        INSTANCE.setRegistryName(EntropyRecipe.TYPE_ID);
    }

    private EntropyRecipeSerializer() {
    }

    @Override
    public EntropyRecipe read(ResourceLocation recipeId, JsonObject json) {
        EntropyRecipeBuilder builder = new EntropyRecipeBuilder();

        // Set id and mode
        builder.setId(recipeId);
        builder.setMode(EntropyMode.valueOf(JSONUtils.getString(json, "mode").toUpperCase(Locale.ROOT)));

        //// Parse inputs
        JsonObject inputJson = JSONUtils.getJsonObject(json, "input");

        // Input block
        JsonObject inputBlockObject = JSONUtils.getJsonObject(inputJson, "block", new JsonObject());
        String inputBlockId = JSONUtils.getString(inputBlockObject, "id", null);
        if (inputBlockId != null) {
            builder.setInputBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(inputBlockId)));
            this.addInputBlockStateMatchers(inputBlockObject, builder);
        }

        // input fluid
        JsonObject inputFluidObject = JSONUtils.getJsonObject(inputJson, "fluid", new JsonObject());
        String inputFluidId = JSONUtils.getString(inputFluidObject, "id", null);
        if (inputFluidId != null) {
            builder.setInputFluid(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputFluidId)));
            this.addInputFluidStateMatchers(inputFluidObject, builder);
        }

        //// Parse outputs
        JsonObject outputJson = JSONUtils.getJsonObject(json, "output");

        // Output block
        JsonObject outputBlockObject = JSONUtils.getJsonObject(outputJson, "block", new JsonObject());
        String outputBlockId = JSONUtils.getString(outputBlockObject, "id", null);
        if (outputBlockId != null) {
            builder.setOutputBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(outputBlockId)));

            boolean outputBlockKeep = JSONUtils.getBoolean(outputBlockObject, "keep", false);
            builder.setOutputBlockKeep(outputBlockKeep);

            this.addOutputBlockStateAppliers(outputBlockObject, builder);
        }

        // Output fluid
        JsonObject outputFluidObject = JSONUtils.getJsonObject(outputJson, "fluid", new JsonObject());
        String outputFluidId = JSONUtils.getString(outputFluidObject, "id", null);
        if (outputFluidId != null) {
            builder.setOutputFluid(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(outputFluidId)));

            boolean outputFluidKeep = JSONUtils.getBoolean(outputFluidObject, "keep", false);
            builder.setOutputFluidKeep(outputFluidKeep);

            this.addOutputFluidStateAppliers(outputFluidObject, builder);
        }

        // Parse additional drops
        if (outputJson.has("drops")) {
            JsonArray dropList = JSONUtils.getJsonArray(outputJson, "drops");
            List<ItemStack> drops = new ArrayList<>(dropList.size());

            for (JsonElement jsonElement : dropList) {
                JsonObject object = jsonElement.getAsJsonObject();
                String itemid = JSONUtils.getString(object, "item");
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemid));
                int count = JSONUtils.getInt(object, "count", 1);
                drops.add(new ItemStack(item, count));
            }

            builder.setDrops(drops);
        }

        return builder.build();
    }

    @Nullable
    @Override
    public EntropyRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        EntropyRecipeBuilder builder = new EntropyRecipeBuilder();

        builder.setId(recipeId);
        builder.setMode(buffer.readEnumValue(EntropyMode.class));

        if (buffer.readBoolean()) {
            builder.setInputBlock(buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS));
            int matcherSize = buffer.readInt();
            for (int i = 0; i < matcherSize; i++) {
                builder.addBlockStateMatcher(StateMatcher.read(buffer));
            }
        }

        if (buffer.readBoolean()) {
            builder.setInputFluid(buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS));
            int matcherSize = buffer.readInt();
            for (int i = 0; i < matcherSize; i++) {
                builder.addFluidStateMatcher(StateMatcher.read(buffer));
            }
        }

        if (buffer.readBoolean()) {
            builder.setOutputBlock(buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS));
            builder.setOutputBlockKeep(buffer.readBoolean());
            int appliersSize = buffer.readInt();
            for (int i = 0; i < appliersSize; i++) {
                builder.addBlockStateAppliers(BlockStateApplier.read(buffer));
            }
        }

        if (buffer.readBoolean()) {
            builder.setOutputFluid(buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS));
            builder.setOutputFluidKeep(buffer.readBoolean());
            int appliersSize = buffer.readInt();
            for (int i = 0; i < appliersSize; i++) {
                builder.addFluidStateAppliers(FluidStateApplier.read(buffer));
            }
        }

        // We use an empty list later when null, so avoid instantiating an empty ArrayList.
        List<ItemStack> drops = null;
        int dropSize = buffer.readInt();
        if (dropSize > 0) {
            drops = new ArrayList<>(dropSize);
            for (int i = 0; i < dropSize; i++) {
                drops.add(buffer.readItemStack());
            }
            builder.setDrops(drops);
        }

        return builder.build();
    }

    @Override
    public void write(PacketBuffer buffer, EntropyRecipe recipe) {
        buffer.writeEnumValue(recipe.getMode());

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

            List<BlockStateApplier> appliers = recipe.getOutputBlockStateAppliers();
            buffer.writeInt(appliers.size());
            for (BlockStateApplier blockStateApplier : appliers) {
                blockStateApplier.writeToPacket(buffer);
            }
        }

        buffer.writeBoolean(recipe.getOutputFluid() != null);
        if (recipe.getOutputFluid() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, recipe.getOutputFluid());
            buffer.writeBoolean(recipe.getOutputFluidKeep());

            List<FluidStateApplier> appliers = recipe.getOutputFluidStateAppliers();
            buffer.writeInt(appliers.size());
            for (FluidStateApplier fluidStateApplier : appliers) {
                fluidStateApplier.writeToPacket(buffer);
            }
        }

        buffer.writeInt(recipe.getDrops().size());
        for (ItemStack itemStack : recipe.getDrops()) {
            buffer.writeItemStack(itemStack);
        }
    }

    private void addInputBlockStateMatchers(JsonObject propertiesContainer, EntropyRecipeBuilder builder) {
        JsonObject properties = JSONUtils.getJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value == null) {
                return;
            }

            if (value.isJsonPrimitive()) {
                builder.addBlockStateMatcher(new SingleValueMatcher(key, value.getAsString()));
            } else if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                List<String> list = new ArrayList<>();
                for (JsonElement e : array) {
                    list.add(e.getAsString());
                }
                builder.addBlockStateMatcher(new MultipleValuesMatcher(key, list));
            } else if (value.isJsonObject() && value.getAsJsonObject().has("min")
                    && value.getAsJsonObject().has("max")) {
                String min = value.getAsJsonObject().get("min").getAsString();
                String max = value.getAsJsonObject().get("max").getAsString();

                builder.addBlockStateMatcher(new RangeValueMatcher(key, min, max));
            }
        });
    }

    private void addInputFluidStateMatchers(JsonObject propertiesContainer, EntropyRecipeBuilder builder) {
        JsonObject properties = JSONUtils.getJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value == null) {
                return;
            }

            if (value.isJsonPrimitive()) {
                builder.addFluidStateMatcher(new SingleValueMatcher(key, value.getAsString()));
            } else if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                List<String> list = new ArrayList<>();
                for (JsonElement e : array) {
                    list.add(e.getAsString());
                }
                builder.addFluidStateMatcher(new MultipleValuesMatcher(key, list));
            } else if (value.isJsonObject() && value.getAsJsonObject().has("min")
                    && value.getAsJsonObject().has("max")) {
                String min = value.getAsJsonObject().get("min").getAsString();
                String max = value.getAsJsonObject().get("max").getAsString();

                builder.addFluidStateMatcher(new RangeValueMatcher(key, min, max));
            }
        });
    }

    private void addOutputBlockStateAppliers(JsonObject propertiesContainer, EntropyRecipeBuilder builder) {
        JsonObject properties = JSONUtils.getJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();

            builder.addBlockStateAppliers(new BlockStateApplier(key, value));
        });
    }

    private void addOutputFluidStateAppliers(JsonObject propertiesContainer, EntropyRecipeBuilder builder) {
        JsonObject properties = JSONUtils.getJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();

            builder.addFluidStateAppliers(new FluidStateApplier(key, value));
        });
    }

}
