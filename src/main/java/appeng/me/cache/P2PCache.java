package appeng.me.cache;

import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;

import javax.annotation.Nonnull;

// FIXME FABRIC DUMMY
public class P2PCache implements IGridCache {
    @Override
    public void onUpdateTick() {

    }

    @Override
    public void removeNode(@Nonnull IGridNode gridNode, @Nonnull IGridHost machine) {

    }

    @Override
    public void addNode(@Nonnull IGridNode gridNode, @Nonnull IGridHost machine) {

    }

    @Override
    public void onSplit(@Nonnull IGridStorage destinationStorage) {

    }

    @Override
    public void onJoin(@Nonnull IGridStorage sourceStorage) {

    }

    @Override
    public void populateGridStorage(@Nonnull IGridStorage destinationStorage) {

    }
}
