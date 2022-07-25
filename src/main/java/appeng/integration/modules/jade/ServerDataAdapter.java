package appeng.integration.modules.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.IServerDataProvider;

import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.api.integrations.waila.AEJadeIds;

public class ServerDataAdapter<T> implements IServerDataProvider<BlockEntity> {

    private final InGameTooltipProvider<? super T> provider;

    private final Class<T> objectClass;

    public ServerDataAdapter(InGameTooltipProvider<? super T> provider, Class<T> objectClass) {
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public ResourceLocation getUid() {
        return AEJadeIds.BLOCK_ENTITIES_PROVIDER;
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity,
            boolean showDetails) {
        var obj = objectClass.cast(blockEntity);
        provider.provideServerData(player, obj, tag);
    }

}
