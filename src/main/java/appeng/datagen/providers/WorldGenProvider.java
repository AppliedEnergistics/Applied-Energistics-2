package appeng.datagen.providers;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;

import appeng.core.AppEng;

public class WorldGenProvider implements DataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldGenProvider.class);

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public WorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    public CompletableFuture<?> run(CachedOutput writer) {
        return registries.thenComposeAsync(provider -> {
            var dynamicOps = RegistryOps.create(JsonOps.INSTANCE, provider);
            var futures = RegistryDataLoader.WORLDGEN_REGISTRIES
                    .stream()
                    .map((info) -> this.writeRegistryEntries(writer, provider, dynamicOps, info))
                    .toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
        });
    }

    private <T> CompletableFuture<Void> writeRegistryEntries(CachedOutput writer, HolderLookup.Provider provider,
            DynamicOps<JsonElement> ops, RegistryDataLoader.RegistryData<T> registryData) {
        var registryKey = registryData.key();
        var registry = provider.lookupOrThrow(registryKey);
        var pathResolver = this.output.createPathProvider(PackOutput.Target.DATA_PACK,
                registryKey.location().getPath());

        var futures = registry.listElements().flatMap(regEntry -> {
            var key = regEntry.key();

            if (!key.location().getNamespace().equals(AppEng.MOD_ID)) {
                return Stream.empty();
            }

            var path = pathResolver.json(key.location());
            return writeToPath(path, writer, ops, registryData.elementCodec(), regEntry.value())
                    .stream();
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    private static <E> Optional<CompletableFuture<?>> writeToPath(Path path, CachedOutput cache,
            DynamicOps<JsonElement> json, Encoder<E> encoder,
            E value) {
        var optional = encoder.encodeStart(json, value).resultOrPartial((error) -> {
            LOGGER.error("Couldn't serialize element {}: {}", path, error);
        });

        return optional.map(data -> DataProvider.saveStable(cache, data, path));
    }

    public String getName() {
        return "AE2 Worldgen";
    }
}
