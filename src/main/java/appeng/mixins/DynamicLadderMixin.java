package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import appeng.hooks.IDynamicLadder;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public abstract class DynamicLadderMixin extends Entity {

    protected DynamicLadderMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "onClimbable", cancellable = true, at = @At("HEAD"))
    public void onClimbable(CallbackInfoReturnable<Boolean> cri) {
        if (this.isSpectator()) {
            return;
        }

        BlockPos blockPos = this.blockPosition();
        BlockState blockState = this.getFeetBlockState();

        if (blockState.getBlock() instanceof IDynamicLadder dynamicLadder) {
            cri.setReturnValue(dynamicLadder.isLadder(blockState, level, blockPos, (LivingEntity) (Object) this));
        }
    }

}
