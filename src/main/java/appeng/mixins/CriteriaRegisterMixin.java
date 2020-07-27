package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

@Mixin(Criteria.class)
public interface CriteriaRegisterMixin {

    @Invoker("register")
    static <T extends Criterion<?>> T callRegister(T object) {
        throw new AssertionError("Mixin dummy");
    }

}
