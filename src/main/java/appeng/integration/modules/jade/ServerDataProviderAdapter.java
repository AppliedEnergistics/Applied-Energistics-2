package appeng.integration.modules.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.IServerDataProvider;

import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.AppEng;

class ServerDataProviderAdapter<T> implements IServerDataProvider<BlockEntity> {
    private static final ResourceLocation ID = AppEng.makeId("server_data");

    private final ServerDataProvider<? super T> provider;

    private final Class<T> objectClass;

    public ServerDataProviderAdapter(ServerDataProvider<? super T> provider, Class<T> objectClass) {
        this.provider = provider;
        this.objectClass = objectClass;
    }

    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity,
            boolean showDetails) {
        var obj = objectClass.cast(blockEntity);
        provider.provideServerData(player, obj, tag);
    }
}
