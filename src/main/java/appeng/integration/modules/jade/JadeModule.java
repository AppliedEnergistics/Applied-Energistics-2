package appeng.integration.modules.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.integration.modules.igtooltip.TooltipProviders;

@WailaPlugin
public class JadeModule implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        TooltipProviders.loadCommon(new CommonRegistration() {
            @Override
            public <T extends BlockEntity> void addBlockEntityData(Class<T> blockEntityClass,
                    ServerDataProvider<? super T> provider) {
                var adapter = new ServerDataProviderAdapter<>(provider, blockEntityClass);
                registration.registerBlockDataProvider(adapter, blockEntityClass);
            }
        });
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        TooltipProviders.loadClient(new ClientRegistration() {
            @Override
            public <T extends BlockEntity> void addBlockEntityBody(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, BodyProvider<? super T> provider,
                    int priority) {
                var adapter = new BodyProviderAdapter<>(id, priority, provider, blockEntityClass);
                registration.registerComponentProvider(adapter, TooltipPosition.BODY, blockClass);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityIcon(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, IconProvider<? super T> provider,
                    int priority) {
                var adapter = new IconProviderAdapter<>(id, priority, registration.getElementHelper(), provider,
                        blockEntityClass);
                registration.registerIconProvider(adapter, blockClass);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityName(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, NameProvider<? super T> provider,
                    int priority) {
                var adapter = new NameProviderAdapter<>(id, priority, provider, blockEntityClass);
                registration.registerComponentProvider(adapter, TooltipPosition.HEAD, blockClass);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityModName(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, ModNameProvider<? super T> provider,
                    int priority) {
                var adapter = new ModNameProviderAdapter<>(id, provider, blockEntityClass);
                registration.registerComponentProvider(adapter, TooltipPosition.TAIL, blockClass);
            }
        });
    }

}
