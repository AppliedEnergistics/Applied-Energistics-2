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

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
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
        String modeName = JSONUtils.getString(json, "mode");
        EntropyMode mode = EntropyMode.valueOf(modeName.toUpperCase(Locale.ROOT));

        JsonObject inputJson = JSONUtils.getJsonObject(json, "input");
        JsonObject inputBlockObject = JSONUtils.getJsonObject(inputJson, "block", new JsonObject());
        String inputBlockId = JSONUtils.getString(inputBlockObject, "id", null);
        JsonObject inputFluidObject = JSONUtils.getJsonObject(inputJson, "fluid", new JsonObject());
        String inputFluidId = JSONUtils.getString(inputFluidObject, "id", null);

        JsonObject outputJson = JSONUtils.getJsonObject(json, "output");
        JsonObject outputBlockObject = JSONUtils.getJsonObject(outputJson, "block", new JsonObject());
        String outputBlockId = JSONUtils.getString(outputBlockObject, "id", null);
        JsonObject outputFluidObject = JSONUtils.getJsonObject(outputJson, "fluid", new JsonObject());
        String outputFluidId = JSONUtils.getString(outputFluidObject, "id", null);

        // We use an empty list later when null, so avoid instantiating an empty ArrayList.
        List<ItemStack> drops = null;

        if (outputJson.has("drops")) {
            JsonArray dropList = JSONUtils.getJsonArray(outputJson, "drops");
            drops = new ArrayList<>(dropList.size());

            for (JsonElement jsonElement : dropList) {
                JsonObject object = jsonElement.getAsJsonObject();
                String itemid = JSONUtils.getString(object, "item");
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemid));
                int count = JSONUtils.getInt(object, "count", 1);
                drops.add(new ItemStack(item, count));
            }
        }

        final Block inputBlock = inputBlockId != null
                ? ForgeRegistries.BLOCKS.getValue(new ResourceLocation(inputBlockId))
                : null;
        final Fluid inputFluid = inputFluidId != null
                ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputFluidId))
                : null;
        final Block outputBlock = outputBlockId != null
                ? ForgeRegistries.BLOCKS.getValue(new ResourceLocation(outputBlockId))
                : null;
        final Fluid outputFluid = outputFluidId != null
                ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(outputFluidId))
                : null;

        CompoundNBT inputBlockNbt = reserialiseInputProperties(inputBlockObject);
        CompoundNBT inputFluidNbt = reserialiseInputProperties(inputFluidObject);

        CompoundNBT outputBlockNbt = reserialiseOutputProperties(outputBlockObject);
        CompoundNBT outputFluidNbt = reserialiseOutputProperties(outputFluidObject);

        boolean outputBlockKeep = JSONUtils.getBoolean(outputBlockObject, "keep", false);
        boolean outputFluidKeep = JSONUtils.getBoolean(outputFluidObject, "keep", false);

        return new EntropyRecipe(recipeId, mode, inputBlock, inputBlockNbt, inputFluid, inputFluidNbt, outputBlock,
                outputBlockNbt, outputBlockKeep, outputFluid, outputFluidNbt, outputFluidKeep, drops);
    }

    @Nullable
    @Override
    public EntropyRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        EntropyMode mode = buffer.readEnumValue(EntropyMode.class);

        Block inputBlock = null;
        CompoundNBT inputBlockProperties = null;
        if (buffer.readBoolean()) {
            inputBlock = buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS);
            inputBlockProperties = buffer.readCompoundTag();
        }

        Fluid inputFluid = null;
        CompoundNBT inputFluidProperties = null;
        if (buffer.readBoolean()) {
            inputFluid = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
            inputFluidProperties = buffer.readCompoundTag();
        }

        Block outputBlock = null;
        CompoundNBT outputBlockProperties = null;
        boolean outputBlockKeep = false;
        if (buffer.readBoolean()) {
            outputBlock = buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS);
            outputBlockProperties = buffer.readCompoundTag();
            outputBlockKeep = buffer.readBoolean();
        }

        Fluid outputFluid = null;
        CompoundNBT outputFluidProperties = null;
        boolean outputFluidKeep = false;
        if (buffer.readBoolean()) {
            outputFluid = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
            outputFluidProperties = buffer.readCompoundTag();
            outputFluidKeep = buffer.readBoolean();
        }

        // We use an empty list later when null, so avoid instantiating an empty ArrayList.
        List<ItemStack> drops = null;
        int dropSize = buffer.readInt();
        if (dropSize > 0) {
            drops = new ArrayList<>(dropSize);
            for (int i = 0; i < dropSize; i++) {
                drops.add(buffer.readItemStack());
            }
        }

        return new EntropyRecipe(recipeId, mode, inputBlock, inputBlockProperties, inputFluid, inputFluidProperties,
                outputBlock, outputBlockProperties, outputBlockKeep, outputFluid, outputFluidProperties,
                outputFluidKeep, drops);
    }

    @Override
    public void write(PacketBuffer buffer, EntropyRecipe recipe) {
        buffer.writeEnumValue(recipe.getMode());

        buffer.writeBoolean(recipe.getInputBlock() != null);
        if (recipe.getInputBlock() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, recipe.getInputBlock());
            buffer.writeCompoundTag(recipe.getInputBlockProperties());
        }

        buffer.writeBoolean(recipe.getInputFluid() != null);
        if (recipe.getInputFluid() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, recipe.getInputFluid());
            buffer.writeCompoundTag(recipe.getInputFluidProperties());
        }

        buffer.writeBoolean(recipe.getOutputBlock() != null);
        if (recipe.getOutputBlock() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, recipe.getOutputBlock());
            buffer.writeCompoundTag(recipe.getOutputBlockProperties());
        }

        buffer.writeBoolean(recipe.getOutputFluid() != null);
        if (recipe.getOutputFluid() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, recipe.getOutputFluid());
            buffer.writeCompoundTag(recipe.getOutputFluidProperties());
            buffer.writeBoolean(recipe.getOutputBlockKeep());
        }

        buffer.writeInt(recipe.getDrops().size());
        for (ItemStack itemStack : recipe.getDrops()) {
            buffer.writeItemStack(itemStack);
        }
    }

    /**
     * Serialises a json structure into a NBT one allowing us to easily send it throug a packetbuffer.
     * 
     * This also prepares the data to easily work with the resulting {@link CompoundNBT} as there is no easy way to work
     * with mixed data types in NBT. So this converts everything to strings.
     * 
     * It can handle preparing single value blockstates like "level=2", a list of possible values like "level=[1,3,5]"
     * and range with a min and max like "level={min:1, max:4}" to be converted to matchers later in the recipe.
     * 
     * @param propertiesContainer
     * @return
     */
    private CompoundNBT reserialiseInputProperties(JsonObject propertiesContainer) {
        CompoundNBT nbt = new CompoundNBT();
        JsonObject properties = JSONUtils.getJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value == null) {
                return;
            }

            CompoundNBT entryNBT = new CompoundNBT();

            if (value.isJsonPrimitive()) {
                entryNBT.putString("value", value.getAsString());
            } else if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                ListNBT list = new ListNBT();
                array.forEach(e -> {
                    list.add(StringNBT.valueOf(e.getAsString()));
                });
                entryNBT.put("values", list);
            } else if (value.isJsonObject() && value.getAsJsonObject().has("min")
                    && value.getAsJsonObject().has("max")) {
                String min = value.getAsJsonObject().get("min").getAsString();
                String max = value.getAsJsonObject().get("max").getAsString();
                CompoundNBT range = new CompoundNBT();
                range.putString("min", min);
                range.putString("max", max);

                entryNBT.put("range", range);
            }

            nbt.put(key, entryNBT);
        });

        return nbt;
    }

    /**
     * Serialises the json again as NBT with everything as string values.
     * 
     * It only supports primitive values as a single blockstate properties cannot represent multiple states at once.
     * 
     * @param propertiesContainer
     * @return
     */
    private CompoundNBT reserialiseOutputProperties(JsonObject propertiesContainer) {
        CompoundNBT nbt = new CompoundNBT();
        JsonObject properties = JSONUtils.getJsonObject(propertiesContainer, "properties", new JsonObject());

        properties.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value == null) {
                return;
            }

            if (value.isJsonPrimitive()) {
                nbt.putString(key, value.getAsString());
            } else {
                throw new IllegalArgumentException("Only single values are allowed.");
            }
        });

        return nbt;
    }

}
