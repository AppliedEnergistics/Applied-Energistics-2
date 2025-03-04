package appeng.recipes.game;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import appeng.recipes.AERecipeTypes;

/**
 * Used to handle disassembly of the (Portable) Storage Cells.
 */
public class StorageCellDisassemblyRecipe implements Recipe<SingleRecipeInput> {
    public static final MapCodec<StorageCellDisassemblyRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
            .group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("cell")
                            .forGetter(StorageCellDisassemblyRecipe::getStorageCell),
                    ItemStack.CODEC.listOf().fieldOf("cell_disassembly_items")
                            .forGetter(StorageCellDisassemblyRecipe::getCellDisassemblyItems))
            .apply(builder, StorageCellDisassemblyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellDisassemblyRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
                    StorageCellDisassemblyRecipe::getStorageCell,
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    StorageCellDisassemblyRecipe::getCellDisassemblyItems,
                    StorageCellDisassemblyRecipe::new);

    private final List<ItemStack> disassemblyItems;
    private final Item storageCell;

    public StorageCellDisassemblyRecipe(Item storageCell, List<ItemStack> disassemblyItems) {
        this.storageCell = storageCell;
        this.disassemblyItems = disassemblyItems;
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
        return !this.disassemblyItems.isEmpty();
    }

    /**
     * Used to get the disassembly result based on recipes for the given cell.
     *
     * @param cell The cell item being disassembled.
     * @return An empty list to indicate the cell cannot be disassembled. Note that stacks in the list must be copied by
     *         the caller.
     */
    public static List<ItemStack> getDisassemblyResult(ServerLevel level, Item cell) {
        var recipeManager = level.recipeAccess();

        for (var holder : recipeManager.recipeMap().byType(AERecipeTypes.CELL_DISASSEMBLY)) {
            if (holder.value().storageCell == cell) {
                return holder.value().getCellDisassemblyItems();
            }
        }

        return List.of();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<StorageCellDisassemblyRecipe> getSerializer() {
        return StorageCellDisassemblyRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<StorageCellDisassemblyRecipe> getType() {
        return AERecipeTypes.CELL_DISASSEMBLY;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
