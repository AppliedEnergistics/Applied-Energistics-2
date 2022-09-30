package appeng.integration.modules.jade;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import appeng.api.integrations.waila.AEJadeIds;
import appeng.integration.modules.jade.tile.ChargerDataProvider;
import appeng.integration.modules.jade.tile.CraftingMonitorDataProvider;
import appeng.integration.modules.jade.tile.DebugDataProvider;
import appeng.integration.modules.jade.tile.GridNodeStateDataProvider;
import appeng.integration.modules.jade.tile.PowerStorageDataProvider;

/**
 * Delegation provider for tiles through {@link snownee.jade.api.IBlockComponentProvider}
 */
public final class BlockEntityDataProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {
    /**
     * Contains all providers
     */
    private final List<BaseDataProvider> providers;

    /**
     * Initializes the provider list with all wanted providers
     */
    public BlockEntityDataProvider() {
        this.providers = List.of(
                new ChargerDataProvider(),
                new PowerStorageDataProvider(),
                new GridNodeStateDataProvider(),
                new CraftingMonitorDataProvider(),
                new DebugDataProvider());
    }

    @Override
    public ResourceLocation getUid() {
        return AEJadeIds.BLOCK_ENTITIES_PROVIDER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // Removes the built-in Forge-Energy progressbar
        tooltip.remove(new ResourceLocation("minecraft:fe"));
        // Removes the built-in Forge fluid bars, because usually we have 6 tanks...
        tooltip.remove(new ResourceLocation("minecraft:fluid"));

        for (var provider : providers) {
            provider.appendTooltip(tooltip, accessor, config);
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity,
            boolean showDetails) {
        for (var provider : providers) {
            provider.appendServerData(tag, player, level, blockEntity, showDetails);
        }
    }

}
