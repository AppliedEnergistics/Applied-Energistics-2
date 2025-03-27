package appeng.client.item;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import appeng.block.networking.EnergyCellBlockItem;
import appeng.core.AppEng;

public class EnergyFillLevelProperty implements RangeSelectItemModelProperty {
    public static final ResourceLocation ID = AppEng.makeId("energy_fill_level");

    public static final MapCodec<EnergyFillLevelProperty> CODEC = MapCodec.unit(EnergyFillLevelProperty::new);

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        if (stack.getItem() instanceof EnergyCellBlockItem energyCell) {
            double curPower = energyCell.getAECurrentPower(stack);
            double maxPower = energyCell.getAEMaxPower(stack);

            return (float) (curPower / maxPower);
        }

        return 0;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }
}
