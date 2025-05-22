package appeng.integration.modules.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.AppEng;

class ServerDataProviderAdapter<T> implements IServerDataProvider<BlockAccessor> {
    private static final ResourceLocation ID = AppEng.makeId("server_data");

    private final ServerDataProvider<? super T> provider;

    private final Class<T> objectClass;

    public ServerDataProviderAdapter(ServerDataProvider<? super T> provider, Class<T> objectClass) {
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        var obj = objectClass.cast(blockAccessor.getBlockEntity());
        var player = blockAccessor.getPlayer();
        provider.provideServerData(player, obj, compoundTag);
    }
}
