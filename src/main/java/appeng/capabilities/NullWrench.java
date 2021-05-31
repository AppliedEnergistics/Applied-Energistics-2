package appeng.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import appeng.api.implementations.items.IAEWrench;

public class NullWrench implements IAEWrench {

    @Override
    public boolean canWrench(ItemStack wrench, PlayerEntity player, BlockPos pos) {
        return false;
    }

}
