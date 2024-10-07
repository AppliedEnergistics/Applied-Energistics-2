package appeng.recipes.game;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.core.AELog;
import appeng.recipes.AERecipeTypes;

/**
 * Used to handle disassembly of the (Portable) Storage Cells.
 */
public class StorageCellDisassemblyRecipe extends CustomRecipe {
    public static final MapCodec<StorageCellDisassemblyRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> {
        return builder.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("cell")
                        .forGetter(StorageCellDisassemblyRecipe::getStorageCell),
                ItemStack.CODEC.listOf().optionalFieldOf("cell_disassembly_items")
                        .forGetter(it -> Optional.ofNullable(it.getCellDisassemblyItems())))
                .apply(builder,
                        (cell, cDisassembly) -> new StorageCellDisassemblyRecipe(cell, cDisassembly.orElse(null)));
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellDisassemblyRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
                    StorageCellDisassemblyRecipe::getStorageCell,
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
                    it -> Optional.ofNullable(it.getCellDisassemblyItems()),
                    (cell, cDisassembly) -> new StorageCellDisassemblyRecipe(cell, cDisassembly.orElse(null)));

    private final List<ItemStack> disassemblyItems;
    private final Item storageCell;

    public StorageCellDisassemblyRecipe(Item storageCell, List<ItemStack> disassemblyItems) {
        super(CraftingBookCategory.MISC);
        this.disassemblyItems = disassemblyItems;
        this.storageCell = storageCell;
    }

    public Item getStorageCell() {
        return this.storageCell;
    }

    public List<ItemStack> getCellDisassemblyItems() {
        return disassemblyItems.stream().map(ItemStack::copy).toList();
    }

    /**
     * @return True when any Disassembly Output is specified.
     */
    public boolean canDisassemble() {
        return this.disassemblyItems != null && !this.disassemblyItems.isEmpty();
    }

    /**
     * Used to get the disassembly recipe based on the provided ResourceLocation. If not found will do a lookup for
     * recipes that specify provided storageCell.
     * 
     * @param level
     * @param location ResourceLocation of the recipe to get.
     * @param cell     Fallback Item to look for.
     * @return If a single recipe is found - CraftingUnitTransformRecipe, otherwise null.
     */
    public static StorageCellDisassemblyRecipe getDisassemblyRecipe(Level level, ResourceLocation location, Item cell) {
        var recipeManager = level.getRecipeManager();
        var recipeHolder = recipeManager.byKey(location);

        // Checking the direct recipe first - if invalid, search for a correct one.
        if (recipeHolder.isPresent() &&
                recipeHolder.get().value() instanceof StorageCellDisassemblyRecipe recipe &&
                recipe.canDisassemble() &&
                recipe.getStorageCell() == cell)
            return recipe;

        var recipes = recipeManager
                .byType(AERecipeTypes.CELL_DISASSEMBLY)
                .stream()
                .filter(it -> (it.value().getStorageCell() == cell && it.value().canDisassemble()))
                .toList();

        if (recipes.size() != 1) {
            if (recipes.size() > 1) {
                AELog.debug("Multiple disassembly recipes found for %s. Disassembly is impossible.", cell);
                recipes.forEach(recipe -> AELog.debug("Recipe: %s", recipe.id()));
            }
            return null;
        }

        return recipes.getFirst().value();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StorageCellDisassemblyRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return AERecipeTypes.CELL_DISASSEMBLY;
    }
}
