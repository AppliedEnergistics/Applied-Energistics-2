package appeng.mixins;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CriteriaTriggers.class)
public interface CriteriaRegisterMixin {

    @Invoker("register")
    static <T extends ICriterionTrigger<?>> T callRegister(T object) {
        throw new AssertionError("Mixin dummy");
    }

}
