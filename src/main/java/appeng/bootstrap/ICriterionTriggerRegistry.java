
package appeng.bootstrap;


import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;


public interface ICriterionTriggerRegistry
{
	void register( ICriterionTrigger<? extends ICriterionInstance> trigger );
}
