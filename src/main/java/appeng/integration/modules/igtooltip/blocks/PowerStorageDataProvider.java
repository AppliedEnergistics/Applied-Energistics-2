package appeng.integration.modules.igtooltip.blocks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.core.localization.InGameTooltip;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.util.Platform;

/**
 * Shows stored power and max stored power for an {@link IAEPowerStorage} block entity.
 */
public final class PowerStorageDataProvider implements BodyProvider<BlockEntity>, ServerDataProvider<BlockEntity> {

    /**
     * Power key used for the transferred {@link CompoundTag}
     */
    private static final String TAG_CURRENT_POWER = "currentPower";
    private static final String TAG_MAX_POWER = "maxPower";
    private static final String TAG_GRID_CURRENT_POWER = "gridCurrentPower";
    private static final String TAG_GRID_MAX_POWER = "gridMaxPower";

    @Override
    public void buildTooltip(BlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        var tag = context.serverData();
        if (tag.contains(TAG_MAX_POWER)) {
            var currentPower = tag.getDoubleOr(TAG_CURRENT_POWER, 0.0);
            var maxPower = tag.getDoubleOr(TAG_MAX_POWER, 0.0);

            var formatCurrentPower = Platform.formatPower(currentPower, false);
            var formatMaxPower = Platform.formatPower(maxPower, false);

            tooltip.addLine(InGameTooltip.Stored.text(formatCurrentPower, formatMaxPower));
        }

        if (tag.contains(TAG_GRID_MAX_POWER)) {
            var gridCurrentPower = tag.getDoubleOr(TAG_GRID_CURRENT_POWER, 0.0);
            var gridMaxPower = tag.getDoubleOr(TAG_GRID_MAX_POWER, 0.0);

            var formatGridCurrentPower = Platform.formatPower(gridCurrentPower, false);
            var formatGridMaxPower = Platform.formatPower(gridMaxPower, false);

            tooltip.addLine(InGameTooltip.NetworkStored.text(formatGridCurrentPower, formatGridMaxPower));
        }
    }

    @Override
    public void provideServerData(Player player, BlockEntity object, CompoundTag serverData) {
        if (object instanceof IAEPowerStorage storage) {
            if (storage.getAEMaxPower() > 0) {
                serverData.putDouble(TAG_CURRENT_POWER, storage.getAECurrentPower());
                serverData.putDouble(TAG_MAX_POWER, storage.getAEMaxPower());
            }
        }

        if (object instanceof IGridConnectedBlockEntity gridConnectedBlockEntity) {
            var gridNode = gridConnectedBlockEntity.getActionableNode();
            if (gridNode != null) {
                var grid = gridNode.getGrid();
                var energyService = grid.getEnergyService();
                serverData.putDouble(TAG_GRID_CURRENT_POWER, energyService.getStoredPower());
                serverData.putDouble(TAG_GRID_MAX_POWER, energyService.getMaxStoredPower());
            }
        }
    }
}
