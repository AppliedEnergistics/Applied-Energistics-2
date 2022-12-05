package appeng.integration.modules.igtooltip.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.client.AEStackRendering;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.stacks.AmountFormat;
import appeng.core.localization.InGameTooltip;
import appeng.helpers.iface.PatternProviderLogicHost;

/**
 * Shows information about the current crafting lock.
 */
public final class PatternProviderDataProvider
        implements BodyProvider<PatternProviderLogicHost>, ServerDataProvider<PatternProviderLogicHost> {

    private static final String NBT_LOCK_REASON = "craftingLockReason";

    @Override
    public void buildTooltip(PatternProviderLogicHost host, TooltipContext context, TooltipBuilder tooltip) {
        var lockReason = context.serverData().getString(NBT_LOCK_REASON);
        if (!lockReason.isEmpty()) {
            tooltip.addLine(Component.Serializer.fromJson(lockReason));
        }
    }

    @Override
    public void provideServerData(ServerPlayer player, PatternProviderLogicHost host, CompoundTag serverData) {
        var logic = host.getLogic();

        Component reason = null;
        switch (logic.getCraftingLockedReason()) {
            case LOCK_UNTIL_PULSE -> {
                reason = InGameTooltip.CraftingLockedUntilPulse.text();
            }
            case LOCK_WHILE_HIGH -> {
                reason = InGameTooltip.CraftingLockedByRedstoneSignal.text();
            }
            case LOCK_WHILE_LOW -> {
                reason = InGameTooltip.CraftingLockedByLackOfRedstoneSignal.text();
            }
            case LOCK_UNTIL_RESULT -> {
                var stack = logic.getUnlockStack();
                Component stackName;
                Component stackAmount;
                if (stack != null) {
                    stackName = AEStackRendering.getDisplayName(stack.what());
                    stackAmount = new TextComponent(stack.what().formatAmount(stack.amount(), AmountFormat.FULL));
                } else {
                    stackName = new TextComponent("ERROR");
                    stackAmount = new TextComponent("ERROR");

                }
                reason = InGameTooltip.CraftingLockedUntilResult.text(stackName, stackAmount);
            }
        }

        if (reason != null) {
            serverData.putString(NBT_LOCK_REASON,
                    Component.Serializer.toJson(reason.copy().withStyle(ChatFormatting.RED)));
        }
    }
}
