package appeng.mixins;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Criteria.class)
public interface CriteriaRegisterMixin {

    @Invoker("register")
    static <T extends Criterion<?>> T callRegister(T object) {
        throw new AssertionError("Mixin dummy");
    }

}
