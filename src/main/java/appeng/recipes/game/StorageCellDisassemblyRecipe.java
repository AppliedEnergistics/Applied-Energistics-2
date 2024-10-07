package appeng.recipes.game;

import appeng.core.AELog;
import appeng.recipes.AERecipeTypes;
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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * Used to handle disassembly of the (Portable) Storage Cells.
 */
public class StorageCellDisassemblyRecipe extends CustomRecipe {
    public static final MapCodec<StorageCellDisassemblyRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> {
        return builder.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("cell").forGetter(StorageCellDisassemblyRecipe::getStorageCell),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("portable_cell").forGetter(StorageCellDisassemblyRecipe::getPortableStorageCell),
            ItemStack.CODEC.listOf().optionalFieldOf("cell_disassembly_items").forGetter(it -> Optional.ofNullable(it.getCellDisassemblyItems())),
            ItemStack.CODEC.listOf().optionalFieldOf("portable_disassembly_items").forGetter(it -> Optional.ofNullable(it.getPortableCellDisassemblyItems()))
        ).apply(builder, (cell, portable, cDisassembly, pDisassembly) ->
            new StorageCellDisassemblyRecipe(cell, portable, cDisassembly.orElse(null), pDisassembly.orElse(null))
        );
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellDisassemblyRecipe> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
        StorageCellDisassemblyRecipe::getStorageCell,
        ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
        StorageCellDisassemblyRecipe::getPortableStorageCell,
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
        it -> Optional.ofNullable(it.getCellDisassemblyItems()),
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
        it -> Optional.ofNullable(it.getPortableCellDisassemblyItems()),
        (cell, portable, cDisassembly, pDisassembly) ->
            new StorageCellDisassemblyRecipe(cell, portable, cDisassembly.orElse(null), pDisassembly.orElse(null))
    );

    private final List<ItemStack> portableDisassemblyItems;
    private final List<ItemStack> disassemblyItems;
    private final Item portableStorageCell;
    private final Item storageCell;

    public StorageCellDisassemblyRecipe(Item storageCell, Item portableStorageCell, List<ItemStack> disassemblyItems, List<ItemStack> portableDisassemblyItems) {
        super(CraftingBookCategory.MISC);
        this.portableDisassemblyItems = portableDisassemblyItems;
        this.disassemblyItems = disassemblyItems;
        this.portableStorageCell = portableStorageCell;
        this.storageCell = storageCell;
    }

    public Item getStorageCell() {
        return this.storageCell;
    }

    public List<ItemStack> getCellDisassemblyItems() {
        return disassemblyItems.stream().map(ItemStack::copy).toList();
    }

    public Item getPortableStorageCell() {
        return this.portableStorageCell;
    }

    public List<ItemStack> getPortableCellDisassemblyItems() {
        return portableDisassemblyItems.stream().map(ItemStack::copy).toList();
    }

    /**
     * @return True when any Disassembly Output is specified.
     */
    public boolean canDisassemble() {
        return this.disassemblyItems != null && !this.disassemblyItems.isEmpty();
    }

    /**
     * Used to get the disassembly recipe based on the provided ResourceLocation. If not found will do a lookup for recipes that specify provided storageCell.
     * @param level
     * @param location ResourceLocation of the recipe to get.
     * @param cell Fallback Item to look for.
     * @return If a single recipe is found - CraftingUnitTransformRecipe, otherwise null.
     */
    public static StorageCellDisassemblyRecipe getDisassemblyRecipe(Level level, ResourceLocation location, Item cell) {
        var recipeManager = level.getRecipeManager();
        var recipeHolder = recipeManager.byKey(location);

        // Checking the direct recipe first - if invalid, search for a correct one.
        if (
            recipeHolder.isPresent() &&
            recipeHolder.get().value() instanceof StorageCellDisassemblyRecipe recipe &&
            recipe.canDisassemble() &&
            (recipe.getStorageCell() == cell || recipe.getPortableStorageCell() == cell)
        ) return recipe;

        var recipes = recipeManager
            .byType(AERecipeTypes.CELL_DISASSEMBLY)
            .stream()
            .filter(it ->
                (it.value().getStorageCell() == cell || it.value().getPortableStorageCell() == cell) &&
                it.value().canDisassemble())
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
