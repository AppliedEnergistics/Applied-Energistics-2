package appeng.integration.modules.igtooltip.blocks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;
import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.core.localization.InGameTooltip;
import appeng.util.Platform;

/**
 * Shows stored power and max stored power for an {@link IAEPowerStorage} block entity.
 */
public final class PowerStorageDataProvider implements InGameTooltipProvider<BlockEntity> {

    /**
     * Power key used for the transferred {@link net.minecraft.nbt.CompoundTag}
     */
    private static final String TAG_CURRENT_POWER = "currentPower";
    private static final String TAG_MAX_POWER = "maxPower";

    @Override
    public void buildTooltip(BlockEntity object, InGameTooltipContext context, InGameTooltipBuilder tooltip) {
        var tag = context.serverData();
        if (tag.contains(TAG_MAX_POWER, Tag.TAG_DOUBLE)) {
            var currentPower = tag.getDouble(TAG_CURRENT_POWER);
            var maxPower = tag.getDouble(TAG_MAX_POWER);

            var formatCurrentPower = Platform.formatPower(currentPower, false);
            var formatMaxPower = Platform.formatPower(maxPower, false);

            tooltip.addLine(InGameTooltip.Stored.text(formatCurrentPower, formatMaxPower));
        }
    }

    @Override
    public void provideServerData(ServerPlayer player, BlockEntity object, CompoundTag serverData) {
        if (object instanceof IAEPowerStorage storage) {
            if (storage.getAEMaxPower() > 0) {
                serverData.putDouble(TAG_CURRENT_POWER, storage.getAECurrentPower());
                serverData.putDouble(TAG_MAX_POWER, storage.getAEMaxPower());
            }
        }
    }
}
