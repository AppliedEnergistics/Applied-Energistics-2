package appeng.integration.modules.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import appeng.api.integrations.igtooltip.providers.ServerDataProvider;

class ServerDataProviderAdapter<T> implements IServerDataProvider<BlockAccessor> {
    private final Identifier id;
    private final ServerDataProvider<? super T> provider;

    private final Class<T> objectClass;

    public ServerDataProviderAdapter(Identifier id, ServerDataProvider<? super T> provider,
            Class<T> objectClass) {
        this.id = id;
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public Identifier getUid() {
        return id;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        var obj = objectClass.cast(blockAccessor.getBlockEntity());
        var player = blockAccessor.getPlayer();
        provider.provideServerData(player, obj, compoundTag);
    }
}
