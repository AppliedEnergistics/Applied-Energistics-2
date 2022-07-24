package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.api.integrations.waila.AEJadeIds;
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

import java.util.List;

/**
 * Delegation provider for tiles through {@link snownee.jade.api.IBlockComponentProvider}
 */
public final class BlockEntityDataProvider<T extends BlockEntity> implements IBlockComponentProvider, IServerDataProvider<T> {
    /**
     * Contains all providers
     */
    private final List<InGameTooltipProvider<? super T>> providers;

    private final Class<T> objectClass;

    public BlockEntityDataProvider(List<InGameTooltipProvider<? super T>> providers, Class<T> objectClass) {
        this.providers = providers;
        this.objectClass = objectClass;
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

        var context = new InGameTooltipContext(
                accessor.getServerData(),
                accessor.getHitResult().getLocation(),
                accessor.getPlayer()
        );
        var tooltipBuilder = new JadeTooltipBuilder(tooltip);
        var obj = objectClass.cast(accessor.getBlockEntity());
        for (var provider : providers) {
            provider.buildTooltip(obj, context, tooltipBuilder);
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity,
                                 boolean showDetails) {
        var obj = objectClass.cast(blockEntity);
        for (var provider : providers) {
            provider.provideServerData(player, obj, tag);
        }
    }

}
