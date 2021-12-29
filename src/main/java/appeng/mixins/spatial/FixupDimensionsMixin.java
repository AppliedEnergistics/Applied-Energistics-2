package appeng.mixins.spatial;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;

import appeng.hooks.FixupDimensionHook;

@Mixin(LevelStorageSource.class)
public class FixupDimensionsMixin {
    @Inject(method = "readWorldGenSettings", at = @At("HEAD"))
    private static <T> void startDimensionFixup(Dynamic<T> nbt, DataFixer fixer, int version,
            CallbackInfoReturnable<Pair<WorldGenSettings, Lifecycle>> cri) {
        // Strips out the AE2 dimension before DFU runs because DFU is based on pattern matching, more or less
        // and can choke HARD on the AE2 dimension, potentially removing the Vanilla dimensions.
        FixupDimensionHook.removeDimension(nbt);
    }

    @ModifyArg(method = "readWorldGenSettings", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/Dynamic;)Lcom/mojang/serialization/DataResult;"), index = 0)
    private static <T> Dynamic<T> injectBack(Dynamic<T> nbt) {
        FixupDimensionHook.addDimension(nbt);
        return nbt;
    }
}
