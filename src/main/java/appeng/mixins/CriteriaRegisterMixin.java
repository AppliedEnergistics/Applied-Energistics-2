package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;

@Mixin(CriteriaTriggers.class)
public interface CriteriaRegisterMixin {

    @Invoker("register")
    static <T extends ICriterionTrigger<?>> T callRegister(T object) {
        throw new AssertionError("Mixin dummy");
    }

}
