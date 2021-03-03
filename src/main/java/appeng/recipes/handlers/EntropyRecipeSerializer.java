package appeng.recipes.handlers;

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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import appeng.recipes.handlers.EntropyRecipe.EntropyMode;

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
        String inputBlockId = JSONUtils.getString(inputJson, "block", null);
        String inputFluidId = JSONUtils.getString(inputJson, "fluid", null);

        JsonObject outputJson = JSONUtils.getJsonObject(json, "output");
        String outputBlockId = JSONUtils.getString(outputJson, "block", null);
        String outputFluidId = JSONUtils.getString(outputJson, "fluid", null);

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

        return new EntropyRecipe(recipeId, mode, inputBlock, inputFluid, outputBlock, outputFluid, drops);
    }

    @Nullable
    @Override
    public EntropyRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        EntropyMode mode = buffer.readEnumValue(EntropyMode.class);

        Block inputBlock = null;
        if (buffer.readBoolean()) {
            inputBlock = buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS);
        }

        Fluid inputFluid = null;
        if (buffer.readBoolean()) {
            inputFluid = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
        }

        Block outputBlock = null;
        if (buffer.readBoolean()) {
            outputBlock = buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS);
        }

        Fluid outputFluid = null;
        if (buffer.readBoolean()) {
            outputFluid = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
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

        return new EntropyRecipe(recipeId, mode, inputBlock, inputFluid, outputBlock, outputFluid, drops);
    }

    @Override
    public void write(PacketBuffer buffer, EntropyRecipe recipe) {
        buffer.writeEnumValue(recipe.getMode());

        buffer.writeBoolean(recipe.getInputBlock() != null);
        if (recipe.getInputBlock() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, recipe.getInputBlock());
        }

        buffer.writeBoolean(recipe.getInputFluid() != null);
        if (recipe.getInputFluid() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, recipe.getInputFluid());
        }

        buffer.writeBoolean(recipe.getOutputBlock() != null);
        if (recipe.getOutputBlock() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, recipe.getOutputBlock());
        }

        buffer.writeBoolean(recipe.getOutputFluid() != null);
        if (recipe.getOutputFluid() != null) {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, recipe.getOutputFluid());
        }

        buffer.writeInt(recipe.getDrops().size());
        for (ItemStack itemStack : recipe.getDrops()) {
            buffer.writeItemStack(itemStack);
        }
    }

}
