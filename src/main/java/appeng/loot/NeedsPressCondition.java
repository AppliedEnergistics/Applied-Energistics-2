package appeng.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import appeng.core.AELog;
import appeng.core.AppEng;

/**
 * Loot condition that is true if the player has never possessed a certain type of press. This is tracked via the press
 * achievement.
 */
public record NeedsPressCondition(NeededPressType needed) implements LootItemCondition {

    public static final LootItemConditionType TYPE = new LootItemConditionType(new NeedsPressConditionSerializer());

    /**
     * The advancement ID that is used for tracking acquiring presses.
     */
    public static final ResourceLocation ADVANCEMENT_ID = AppEng.makeId("main/presses");

    @Override
    public LootItemConditionType getType() {
        return TYPE;
    }

    public NeededPressType getNeeded() {
        return needed;
    }

    @Override
    public boolean test(LootContext lootContext) {
        var player = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (player instanceof ServerPlayer serverPlayer) {
            var advancement = serverPlayer.getServer().getAdvancements().getAdvancement(ADVANCEMENT_ID);
            if (advancement == null) {
                AELog.warn("Missing advancement %s", ADVANCEMENT_ID);
                return false;
            }

            var progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);

            // Checking for individual criterions stops when the advancement has been achieved fully
            // The criterions need to be defined with AND for the individual tracking to work.
            if (progress.isDone()) {
                return false; // Has the advancement -> no more presses needed
            }

            // Check if the player doesn't have the advancement trigger for the press
            var criterion = progress
                    .getCriterion(needed.getCriterionName());

            if (criterion == null) {
                AELog.warn("Missing criterion %s in advancement %s", needed.getCriterionName(), advancement);
                return false;
            }

            return !criterion.isDone();
        }
        return false;
    }
}
