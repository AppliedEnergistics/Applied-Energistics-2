package appeng.datagen.providers.models;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.IPartItem;
import appeng.client.api.model.parts.ClientPart;
import appeng.client.api.model.parts.PartModel;
import appeng.datagen.providers.IAE2DataProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class AE2ModelProvider extends ModelProvider implements IAE2DataProvider {
    private final PackOutput.PathProvider partModelOutput;
    @Nullable
    private PartModelCollector partModelCollector;

    public static Factory<DataProvider> create(String modId, ModelSubProviderFactory... subProviders) {
        var subProviderList = List.of(subProviders);
        return output -> new AE2ModelProvider(output, modId, subProviderList);
    }

    // This matches the super-class constructor of ModelSubProvider
    @FunctionalInterface
    public interface ModelSubProviderFactory {
        ModelSubProvider create(BlockModelGenerators blockModels, ItemModelGenerators itemModels, PartModelOutput partModels);
    }

    private final List<ModelSubProviderFactory> subProviders;

    public AE2ModelProvider(PackOutput packOutput, String modid, List<ModelSubProviderFactory> subProviders) {
        super(packOutput, modid);
        this.subProviders = subProviders;
        this.partModelOutput = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "ae2/parts");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        var partModelCollector = new PartModelCollector(this::getKnownItems);
        this.partModelCollector = partModelCollector;
        CompletableFuture<?> future;
        try {
            future = super.run(output);
        } finally {
            this.partModelCollector = null;
        }
        partModelCollector.finalizeAndValidate();
        return CompletableFuture.allOf(future, partModelCollector.save(output, partModelOutput));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (var subProvider : subProviders) {
            subProvider.create(blockModels, itemModels, partModelCollector).register();
        }
    }

    @Override
    public String getName() {
        return super.getName() + " " + getClass().getName();
    }

    public static final class PartModelCollector implements PartModelOutput {
        private final Map<IPartItem<?>, ClientPart> parts = new HashMap<>();
        private final Map<IPartItem<?>, IPartItem<?>> copies = new HashMap<>();
        private final Supplier<Stream<? extends Holder<Item>>> knownItems;

        public PartModelCollector(Supplier<Stream<? extends Holder<Item>>> knownItems) {
            this.knownItems = knownItems;
        }

        @Override
        public void accept(IPartItem<?> part, PartModel.Unbaked model) {
            this.register(part, new ClientPart(model, ClientPart.Properties.DEFAULT));
        }

        @Override
        public void accept(IPartItem<?> part, PartModel.Unbaked model, ClientPart.Properties properties) {
            this.register(part, new ClientPart(model, properties));
        }

        @Override
        public void copy(IPartItem<?> from, IPartItem<?> to) {
            this.copies.put(to, from);
        }

        public void register(IPartItem<?> part, ClientPart clientPart) {
            var existingPart = this.parts.put(part, clientPart);
            if (existingPart != null) {
                throw new IllegalStateException("Duplicate part model definition for " + IPartItem.getId(part));
            }
        }

        public void finalizeAndValidate() {
            this.copies.forEach((to, from) -> {
                var clientPart = this.parts.get(from);
                if (clientPart == null) {
                    throw new IllegalStateException("Missing donor: " + from + " -> " + to);
                } else {
                    this.register(to, clientPart);
                }
            });

            var missingPartModels = knownItems.get()
                    .filter(holder -> holder.value() instanceof IPartItem<?> partItem && !isCable(partItem) && !this.parts.containsKey(partItem))
                    .map(holder -> holder.unwrapKey().orElseThrow().location())
                    .toList();
            if (!missingPartModels.isEmpty()) {
                throw new IllegalStateException("Missing part model definitions for: " + missingPartModels);
            }
        }

        private boolean isCable(IPartItem<?> partItem) {
            return partItem.createPart() instanceof ICablePart;
        }

        public CompletableFuture<?> save(CachedOutput output, PackOutput.PathProvider pathProvider) {
            return DataProvider.saveAll(
                    output, ClientPart.CODEC, partItem -> pathProvider.json(BuiltInRegistries.ITEM.getKey(partItem.asItem())), this.parts
            );
        }
    }
}
