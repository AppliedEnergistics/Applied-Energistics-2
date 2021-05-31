package appeng.capabilities;

import appeng.api.implementations.items.IAEWrench;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class NullWrench implements IAEWrench {

    @Override
    public boolean canWrench(ItemStack wrench, PlayerEntity player, BlockPos pos) {
        return false;
    }

}
