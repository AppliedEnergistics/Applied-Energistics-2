package appeng.client.item;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.core.AppEng;

/**
 * Provides the color representing the storage cell state as a color for item model tinting. If the item also implements
 * energy, an off-color is returned when the item has no more power.
 */
public record StorageCellStateTintSource() implements ItemTintSource {
    public static final ResourceLocation ID = AppEng.makeId("storage_cell_state");

    public static final MapCodec<StorageCellStateTintSource> MAP_CODEC = MapCodec.unit(StorageCellStateTintSource::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        var cellState = getCellState(stack);
        return ARGB.opaque(cellState.getStateColor());
    }

    private CellState getCellState(ItemStack stack) {
        if (stack.getItem() instanceof IAEItemPowerStorage powerStorage) {
            // If the cell is out of power, always display empty
            if (powerStorage.getAECurrentPower(stack) <= 0) {
                return CellState.ABSENT;
            }
        }

        // Determine LED color
        var cellInv = StorageCells.getCellInventory(stack, null);
        return cellInv != null ? cellInv.getStatus() : CellState.EMPTY;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
