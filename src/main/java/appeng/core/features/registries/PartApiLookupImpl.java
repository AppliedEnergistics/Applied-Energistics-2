package appeng.core.features.registries;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import appeng.api.parts.IPart;
import appeng.api.parts.PartApiLookup;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.Api;
import appeng.parts.CableBusContainer;
import appeng.tile.networking.CableBusTileEntity;

public class PartApiLookupImpl implements PartApiLookup {
    // These two are used as identity hash maps - BlockApiLookup has identity semantics.
    // We use ApiProviderMap because it ensures non-null keys and values.
    private static final ApiProviderMap<BlockApiLookup<?, ?>, Function<?, AEPartLocation>> mappings = ApiProviderMap
            .create();
    private static final ApiProviderMap<BlockApiLookup<?, ?>, ApiProviderMap<Class<?>, PartApiProvider<?, ?, ?>>> providers = ApiProviderMap
            .create();
    private static final Set<BlockApiLookup<?, ?>> cableBusRegisteredLookups = ConcurrentHashMap.newKeySet();

    @Override
    public <A, C> void registerCustomContext(BlockApiLookup<A, C> lookup, Function<C, AEPartLocation> mappingFunction) {
        mappings.putIfAbsent(lookup, mappingFunction);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public <A, C, P extends IPart> void register(BlockApiLookup<A, C> lookup, PartApiProvider<A, C, P> provider,
            Class<P> partClass) {
        Objects.requireNonNull(lookup, "Registered lookup may not be null.");

        if (partClass.isInterface()) {
            throw new IllegalArgumentException(
                    "Part lookup cannot be registered for interface:" + partClass.getCanonicalName());
        }

        providers.putIfAbsent(lookup, ApiProviderMap.create());
        ApiProviderMap<Class<?>, PartApiProvider<?, ?, ?>> toProviderMap = providers.get(lookup);

        if (toProviderMap.putIfAbsent(partClass, provider) != null) {
            throw new IllegalArgumentException(
                    "Duplicate provider registration for part class " + partClass.getCanonicalName());
        }

        if (cableBusRegisteredLookups.add(lookup)) {
            registerLookup(lookup);
        }
    }

    private <A, C> void registerLookup(BlockApiLookup<A, C> lookup) {
        TileEntityType<?> cableBusType = ((AEBaseTileBlock<?>) Api.instance().definitions().blocks().multiPart()
                .block()).createNewTileEntity(null).getType();

        lookup.registerForBlockEntities((be, context) -> {
            @Nullable
            AEPartLocation location = mapContext(lookup, context);

            if (location == null) {
                return null;
            } else {
                CableBusTileEntity cableBus = (CableBusTileEntity) be;
                CableBusContainer cb = cableBus.getCableBus();
                IPart part = cb.getPart(location);

                if (part != null) {
                    return find(lookup, context, part);
                } else {
                    return null;
                }
            }
        }, cableBusType);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <C> AEPartLocation mapContext(BlockApiLookup<?, C> lookup, C context) {
        Function<C, AEPartLocation> mapping = (Function<C, AEPartLocation>) mappings.get(lookup);

        if (mapping != null) {
            return mapping.apply(context);
        } else if (context instanceof AEPartLocation) {
            return (AEPartLocation) context;
        } else if (context instanceof Direction) {
            return AEPartLocation.fromFacing((Direction) context);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <A, C> A find(BlockApiLookup<A, C> lookup, C context, IPart part) {
        ApiProviderMap<Class<?>, PartApiProvider<?, ?, ?>> toProviderMap = providers.get(lookup);

        if (lookup == null)
            return null;

        for (Class<?> klass = part.getClass(); klass != Object.class; klass = klass.getSuperclass()) {
            PartApiProvider<A, C, IPart> provider = (PartApiProvider<A, C, IPart>) toProviderMap.get(klass);

            if (provider != null) {
                A instance = provider.find(part, context);

                if (instance != null) {
                    return instance;
                }
            }
        }

        return null;
    }
}
