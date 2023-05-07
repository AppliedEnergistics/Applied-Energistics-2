package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformLogic;

/**
 * Process transform recipes.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract ItemStack getItem();

    private int ae2_transformTime = 0;
    private int ae2_delay = 0;

    @Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
    void handleExplosion(DamageSource src, float dmg, CallbackInfoReturnable<Boolean> ci) {
        if (!level.isClientSide && src.is(DamageTypeTags.IS_EXPLOSION) && !isRemoved()) {
            var self = (ItemEntity) (Object) this;
            // Just a hashmap lookup - short-circuit to not cause perf issues by iterating entities / recipes
            // unnecessarily.
            if (TransformLogic.canTransformInExplosion(self)
                    && TransformLogic.tryTransform(self, TransformCircumstance::isExplosion)) {
                ci.setReturnValue(false);
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "tick")
    void handleEntityTransform(CallbackInfo ci) {
        if (this.isRemoved()) {
            return;
        }
        var self = (ItemEntity) (Object) this;
        // Just a hashmap lookup - short-circuit to not cause perf issues by iterating entities / recipes unnecessarily.
        if (!TransformLogic.canTransformInAnyFluid(self)) {
            return;
        }

        final int j = Mth.floor(this.getX());
        final int i = Mth.floor((this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D);
        final int k = Mth.floor(this.getZ());

        FluidState state = this.level.getFluidState(new BlockPos(j, i, k));
        boolean isValidFluid = !state.isEmpty() && TransformLogic.canTransformInFluid(self, state);

        if (level.isClientSide()) {
            if (isValidFluid && this.ae2_delay++ > 30 && AEConfig.instance().isEnableEffects()) {
                // Client side we only render some cool animations.
                AppEng.instance().spawnEffect(EffectType.Lightning, this.level, this.getX(), this.getY(), this.getZ(),
                        null);
                this.ae2_delay = 0;
            }
        } else {
            if (isValidFluid) {
                this.ae2_transformTime++;
                if (this.ae2_transformTime > 60 && !TransformLogic.tryTransform(self, c -> c.isFluid(state))) {
                    this.ae2_transformTime = 0;
                }
            } else {
                this.ae2_transformTime = 0;
            }
        }
    }
}
