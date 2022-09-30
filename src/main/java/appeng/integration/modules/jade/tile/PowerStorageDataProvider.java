package appeng.integration.modules.jade.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import appeng.api.integrations.waila.AEJadeIds;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.core.localization.InGameTooltip;
import appeng.integration.modules.jade.BaseDataProvider;
import appeng.util.Platform;

/**
 * Shows stored power and max stored power for an {@link IAEPowerStorage} block entity.
 */
public final class PowerStorageDataProvider extends BaseDataProvider {

    /**
     * Power key used for the transferred {@link net.minecraft.nbt.CompoundTag}
     */
    private static final String TAG_CURRENT_POWER = "currentPower";
    private static final String TAG_MAX_POWER = "maxPower";

    @Override
    public ResourceLocation getUid() {
        return AEJadeIds.POWER_STORAGE_PROVIDER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        if (tag.contains(TAG_MAX_POWER, Tag.TAG_DOUBLE)) {
            var currentPower = tag.getDouble(TAG_CURRENT_POWER);
            var maxPower = tag.getDouble(TAG_MAX_POWER);

            var formatCurrentPower = Platform.formatPower(currentPower, false);
            var formatMaxPower = Platform.formatPower(maxPower, false);

            tooltip.add(InGameTooltip.Stored.text(formatCurrentPower, formatMaxPower));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity,
            boolean showDetails) {
        if (blockEntity instanceof IAEPowerStorage storage) {
            if (storage.getAEMaxPower() > 0) {
                tag.putDouble(TAG_CURRENT_POWER, storage.getAECurrentPower());
                tag.putDouble(TAG_MAX_POWER, storage.getAEMaxPower());
            }
        }
    }

}
