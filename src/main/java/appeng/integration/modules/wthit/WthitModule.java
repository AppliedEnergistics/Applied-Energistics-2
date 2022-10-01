package appeng.integration.modules.wthit;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaConstants;
import mcp.mobius.waila.api.component.ItemComponent;

import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.integration.modules.igtooltip.TooltipProviders;

public class WthitModule implements IWailaPlugin {
    public void register(IRegistrar registrar) {
        TooltipProviders.loadCommon(new CommonRegistration() {
            @Override
            public <T extends BlockEntity> void addBlockEntityData(Class<T> blockEntityClass,
                    ServerDataProvider<? super T> provider) {
                registrar.addBlockData(new IServerDataProvider<>() {
                    @Override
                    public void appendServerData(CompoundTag data, IServerAccessor<BlockEntity> accessor,
                            IPluginConfig config) {
                        var obj = blockEntityClass.cast(accessor.getTarget());
                        provider.provideServerData(accessor.getPlayer(), obj, data);
                    }
                }, blockEntityClass);
            }
        });
        TooltipProviders.loadClient(new ClientRegistration() {
            @Override
            public <T extends BlockEntity> void addBlockEntityBody(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, BodyProvider<? super T> provider,
                    int priority) {
                registrar.addComponent(new IBlockComponentProvider() {
                    @Override
                    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
                        var be = blockEntityClass.cast(accessor.getBlockEntity());
                        var context = getContext(accessor);
                        provider.buildTooltip(be, context, new WthitTooltipBuilder(tooltip));
                    }
                }, TooltipPosition.BODY, blockEntityClass, priority);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityIcon(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, IconProvider<? super T> provider,
                    int priority) {
                registrar.addIcon(new IBlockComponentProvider() {
                    @Override
                    public @Nullable ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
                        var be = blockEntityClass.cast(accessor.getBlockEntity());
                        var context = getContext(accessor);
                        var icon = provider.getIcon(be, context);
                        return icon != null ? new ItemComponent(icon) : null;
                    }
                }, blockClass, priority);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityName(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, NameProvider<? super T> provider,
                    int priority) {
                registrar.addComponent(new IBlockComponentProvider() {
                    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
                        var obj = blockEntityClass.cast(accessor.getBlockEntity());
                        var context = getContext(accessor);

                        var name = provider.getName(obj, context);

                        // Replace the object name
                        if (name != null) {
                            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, name.copy().withStyle(style -> {
                                // Don't overwrite a text color if one is present
                                if (style.getColor() == null) {
                                    return style.withColor(ChatFormatting.WHITE);
                                } else {
                                    return style;
                                }
                            }));
                        }
                    }
                }, TooltipPosition.HEAD, blockEntityClass, priority);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityModName(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, ModNameProvider<? super T> provider,
                    int priority) {
                registrar.addComponent(new IBlockComponentProvider() {
                    @Override
                    public void appendTail(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
                        var obj = blockEntityClass.cast(accessor.getBlockEntity());
                        var context = getContext(accessor);

                        var modName = provider.getModName(obj, context);
                        if (modName != null) {
                            // Only add the mod name if it's already there
                            if (tooltip.getLine(WailaConstants.MOD_NAME_TAG) != null) {
                                tooltip.setLine(WailaConstants.MOD_NAME_TAG, new TextComponent(modName).withStyle(
                                        ChatFormatting.BLUE, ChatFormatting.ITALIC));
                            }
                        }
                    }
                }, TooltipPosition.TAIL, blockEntityClass, priority);
            }
        });
    }

    private static TooltipContext getContext(IBlockAccessor accessor) {
        return new TooltipContext(
                accessor.getServerData(),
                accessor.getHitResult().getLocation(),
                accessor.getPlayer());
    }
}
