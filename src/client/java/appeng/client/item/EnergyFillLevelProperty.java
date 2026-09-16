package appeng.client.item;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import appeng.block.networking.EnergyCellBlockItem;
import appeng.core.AppEng;

public class EnergyFillLevelProperty implements RangeSelectItemModelProperty {
    public static final Identifier ID = AppEng.makeId("energy_fill_level");

    public static final MapCodec<EnergyFillLevelProperty> CODEC = MapCodec.unit(EnergyFillLevelProperty::new);

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (stack.getItem() instanceof EnergyCellBlockItem energyCell) {
            var access = ItemAccess.forStack(stack);
            double curPower = energyCell.getAECurrentPower(access);
            double maxPower = energyCell.getAEMaxPower(access);

            return (float) (curPower / maxPower);
        }

        return 0;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }
}
