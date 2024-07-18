package appeng.integration.modules.igtooltip.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import appeng.api.client.AEKeyRendering;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.InGameTooltip;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

/**
 * Shows information about the current crafting lock.
 */
public final class PatternProviderDataProvider
        implements BodyProvider<PatternProviderLogicHost>, ServerDataProvider<PatternProviderLogicHost> {

    private static final String NBT_LOCK_REASON = "craftingLockReason";
    private static final String NBT_LOCK_UNTIL_RESULT_STACK = "craftingLockUntilResultStack";

    @Override
    public void buildTooltip(PatternProviderLogicHost host, TooltipContext context, TooltipBuilder tooltip) {
        var lockReason = context.serverData().getString(NBT_LOCK_REASON);
        if (!lockReason.isEmpty()) {
            tooltip.addLine(Component.Serializer.fromJson(lockReason));
        }
        var stack = context.serverData().getCompound(NBT_LOCK_UNTIL_RESULT_STACK);
        if (!stack.isEmpty()) {
            var genericStack = GenericStack.readTag(stack);
            Component stackName;
            Component stackAmount;
            if (genericStack == null) {
                stackName = Component.literal("ERROR");
                stackAmount = Component.literal("ERROR");
            } else {
                stackName = AEKeyRendering.getDisplayName(genericStack.what());
                stackAmount = Component
                        .literal(genericStack.what().formatAmount(genericStack.amount(), AmountFormat.FULL));
            }
            tooltip.addLine(
                    InGameTooltip.CraftingLockedUntilResult.text(stackName, stackAmount).withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void provideServerData(Player player, PatternProviderLogicHost host, CompoundTag serverData) {
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
                if (stack != null) {
                    serverData.put(NBT_LOCK_UNTIL_RESULT_STACK, GenericStack.writeTag(stack));
                } else {
                    // Put a non-empty compound tag, so we get "ERROR" when handling this on the client
                    final CompoundTag errorDummy = new CompoundTag();
                    errorDummy.putString("error", "error");
                    serverData.put(NBT_LOCK_UNTIL_RESULT_STACK, errorDummy);
                }
                return;
            }
        }

        if (reason != null) {
            serverData.putString(NBT_LOCK_REASON,
                    Component.Serializer.toJson(reason.copy().withStyle(ChatFormatting.RED)));
        }
    }
}
