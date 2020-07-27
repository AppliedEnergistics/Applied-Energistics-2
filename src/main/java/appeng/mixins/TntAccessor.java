package appeng.mixins;

import net.minecraft.block.TNTBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(TNTBlock.class)
public interface TntAccessor {

    @Invoker
    static void callPrimeTnt(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        throw new AssertionError("Mixin dummy");
    }

}
