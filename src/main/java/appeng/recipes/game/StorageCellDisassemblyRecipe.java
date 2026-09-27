package appeng.recipes.game;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.MechanicsRecipe;

/**
 * Used to handle disassembly of the (Portable) Storage Cells.
 */
public class StorageCellDisassemblyRecipe extends MechanicsRecipe<SingleRecipeInput> {
    public static final MapCodec<StorageCellDisassemblyRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
            .group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("cell")
                            .forGetter(StorageCellDisassemblyRecipe::storageCell),
                    ItemStackTemplate.CODEC.listOf().fieldOf("cell_disassembly_items")
                            .forGetter(StorageCellDisassemblyRecipe::cellDisassemblyItems))
            .apply(builder, StorageCellDisassemblyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellDisassemblyRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
                    StorageCellDisassemblyRecipe::storageCell,
                    ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    StorageCellDisassemblyRecipe::cellDisassemblyItems,
                    StorageCellDisassemblyRecipe::new);

    public static final RecipeSerializer<StorageCellDisassemblyRecipe> SERIALIZER = new RecipeSerializer<>(CODEC,
            STREAM_CODEC);

    private final List<ItemStackTemplate> disassemblyItems;
    private final Item storageCell;

    public StorageCellDisassemblyRecipe(Item storageCell, List<ItemStackTemplate> disassemblyItems) {
        this.storageCell = storageCell;
        this.disassemblyItems = disassemblyItems;
    }

    public Item storageCell() {
        return this.storageCell;
    }

    public List<ItemStackTemplate> cellDisassemblyItems() {
        return disassemblyItems;
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
                return holder.value().cellDisassemblyItems().stream().map(ItemStackTemplate::create).toList();
            }
        }

        return List.of();
    }

    @Override
    public RecipeSerializer<StorageCellDisassemblyRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<StorageCellDisassemblyRecipe> getType() {
        return AERecipeTypes.CELL_DISASSEMBLY;
    }
}
