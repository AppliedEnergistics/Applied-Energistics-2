package appeng.client;

import appeng.api.util.IConfigurableObject;
import appeng.blockentity.crafting.MolecularAssemblerAnimationStatus;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.network.bidirectional.ConfigValuePacket;
import appeng.core.network.clientbound.*;
import appeng.core.particles.EnergyParticleData;
import appeng.core.particles.ParticleTypes;
import appeng.hooks.CompassManager;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.LinkStatusAwareMenu;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AEClientboundPacketHandler {
    public void handleGuiDataSyncPacket(GuiDataSyncPacket packet, Minecraft minecraft, Player player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu baseMenu && c.containerId == packet.containerId()) {
            baseMenu.receiveServerSyncData(
                    new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(packet.syncData()), player.registryAccess()));
        }
    }

    public void handleMatterCannonPacket(MatterCannonPacket packet, Minecraft minecraft, Player player) {
        try {
            for (int a = 1; a < packet.len(); a++) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.MATTER_CANNON, packet.x() + packet.dx() * a,
                        packet.y() + packet.dy() * a, packet.z() + packet.dz() * a, 0, 0, 0);
            }
        } catch (Exception ignored) {
        }
    }

    public void handleSetLinkStatusPacket(SetLinkStatusPacket packet, Minecraft minecraft, Player player) {
        if (player.containerMenu instanceof LinkStatusAwareMenu linkStatusAwareMenu) {
            linkStatusAwareMenu.setLinkStatus(packet.linkStatus());
        }
    }

    public void handlePatternAccessTerminalPacket(PatternAccessTerminalPacket packet, Minecraft minecraft, Player player) {
        if (Minecraft.getInstance().screen instanceof PatternAccessTermScreen<?> patternAccessTerminal) {
            if (packet.fullUpdate()) {
                patternAccessTerminal.postFullUpdate(packet.inventoryId(), packet.sortBy(), packet.group(), packet.inventorySize(), packet.slots());
            } else {
                patternAccessTerminal.postIncrementalUpdate(packet.inventoryId(), packet.slots());
            }
        }
    }

    public void handleBlockTransitionEffectPacket(BlockTransitionEffectPacket packet, Minecraft minecraft, Player player) {
        spawnParticles(packet, player.level());

        playBreakOrPickupSound(packet);
    }

    private void spawnParticles(BlockTransitionEffectPacket packet, Level level) {

        EnergyParticleData data = new EnergyParticleData(false, packet.direction());
        for (int zz = 0; zz < 32; zz++) {
            // Distribute the spawn point across the entire block's area
            double x = packet.pos().getX() + level.getRandom().nextFloat();
            double y = packet.pos().getY() + level.getRandom().nextFloat();
            double z = packet.pos().getZ() + level.getRandom().nextFloat();
            double speedX = 0.1f * packet.direction().getStepX();
            double speedY = 0.1f * packet.direction().getStepY();
            double speedZ = 0.1f * packet.direction().getStepZ();

            level.addParticle(data, false, true, x, y, z, speedX, speedY, speedZ);
        }
    }

    private void playBreakOrPickupSound(BlockTransitionEffectPacket packet) {

        SoundEvent soundEvent;
        float volume;
        float pitch;
        if (packet.soundMode() == BlockTransitionEffectPacket.SoundMode.FLUID) {
            // This code is based on what BucketItem does
            Fluid fluid = packet.blockState().getFluidState().getType();
            soundEvent = fluid.getFluidType().getSound(SoundActions.BUCKET_FILL);
            if (soundEvent == null) {
                if (fluid.is(FluidTags.LAVA)) {
                    soundEvent = SoundEvents.BUCKET_FILL_LAVA;
                } else {
                    soundEvent = SoundEvents.BUCKET_FILL;
                }
            }
            volume = 1;
            pitch = 1;
        } else if (packet.soundMode() == BlockTransitionEffectPacket.SoundMode.BLOCK) {
            SoundType soundType = packet.blockState().getSoundType();
            soundEvent = soundType.getBreakSound();
            volume = soundType.volume;
            pitch = soundType.pitch;
        } else {
            return;
        }

        SimpleSoundInstance sound = new SimpleSoundInstance(soundEvent, SoundSource.BLOCKS, (volume + 1.0F) / 2.0F,
                pitch * 0.8F,
                SoundInstance.createUnseededRandom(),
                packet.pos());
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    public void handleCraftingStatusPacket(CraftingStatusPacket packet, Minecraft minecraft, Player player) {
        if (player.containerMenu == null || player.containerMenu.containerId != packet.containerId()) {
            return; // Packet received for an invalid container id, i.e. after closing it client-side
        }

        Screen screen = Minecraft.getInstance().screen;

        if (screen instanceof CraftingCPUScreen<?> cpuScreen) {
            cpuScreen.postUpdate(packet.status());
        }
    }

    public void handleCraftConfirmPlanPacket(CraftConfirmPlanPacket packet, Minecraft minecraft, Player player) {
        if (player.containerMenu instanceof CraftConfirmMenu menu) {
            menu.setPlan(packet.plan());
        }
    }

    public void handleNetworkStatusPacket(NetworkStatusPacket packet, Minecraft minecraft, Player player) {
        final Screen gs = Minecraft.getInstance().screen;

        if (gs instanceof NetworkStatusScreen) {
            ((NetworkStatusScreen) gs).processServerUpdate(packet.status());
        }
    }

    public void handleMolecularAssemblerAnimationPacket(MolecularAssemblerAnimationPacket packet, Minecraft minecraft, Player player) {
        BlockEntity te = player.getCommandSenderWorld().getBlockEntity(packet.pos());
        if (te instanceof MolecularAssemblerBlockEntity ma) {
            ma.setAnimationStatus(new MolecularAssemblerAnimationStatus(packet.rate(), packet.what().wrapForDisplayOrFilter()));
        }
    }

    public void handleMEInventoryUpdatePacket(MEInventoryUpdatePacket packet, Minecraft minecraft, Player player) {
        if (player.containerMenu.containerId == packet.containerId()
                && player.containerMenu instanceof MEStorageMenu meMenu) {
            var clientRepo = meMenu.getClientRepo();
            if (clientRepo == null) {
                AELog.info("Ignoring ME inventory update packet because no client repo is available.");
                return;
            }

            var actualEntries = packet.getActualEntries();
            if (actualEntries != null) {
                clientRepo.handleUpdate(packet.fullUpdate(), actualEntries);
            }
        }
    }

    public void handleCompassResponsePacket(CompassResponsePacket packet, Minecraft minecraft, Player player) {
        CompassManager.INSTANCE.postResult(packet.requestedPos(), packet.closestMeteorite().orElse(null));
    }

    public void handleClearPatternAccessTerminalPacket(ClearPatternAccessTerminalPacket packet, Minecraft minecraft, Player player) {
        if (Minecraft.getInstance().screen instanceof PatternAccessTermScreen<?> patternAccessTerminal) {
            patternAccessTerminal.clear();
        }
    }

    public void handleItemTransitionEffectPacket(ItemTransitionEffectPacket packet, Minecraft minecraft, Player player) {
        EnergyParticleData data = new EnergyParticleData(true, packet.d());
        for (int zz = 0; zz < 8; zz++) {
            // Distribute the spawn point around the item's position
            double x = packet.z() + player.level().getRandom().nextFloat() * 0.5 - 0.25;
            double y = packet.z() + player.level().getRandom().nextFloat() * 0.5 - 0.25;
            double z = packet.z() + player.level().getRandom().nextFloat() * 0.5 - 0.25;
            double speedX = 0.1f * packet.d().getStepX();
            double speedY = 0.1f * packet.d().getStepY();
            double speedZ = 0.1f * packet.d().getStepZ();
            player.level().addParticle(data, false, true, x, y, z, speedX, speedY, speedZ);
        }
    }

    public void handleMockExplosionPacket(MockExplosionPacket packet, Minecraft minecraft, Player player) {
        final Level level = player.getCommandSenderWorld();
        level.addParticle(net.minecraft.core.particles.ParticleTypes.EXPLOSION, packet.x(), packet.y(), packet.z(), 1.0D, 0.0D, 0.0D);

    }

    public void handleLightningPacket(LightningPacket packet, Minecraft minecraft, Player player) {
        try {
            if (AEConfig.instance().isEnableEffects()) {
                player.getCommandSenderWorld().addParticle(ParticleTypes.LIGHTNING, packet.x(), packet.y(), packet.z(), 0.0f, 0.0f,
                        0.0f);
            }
        } catch (Exception ignored) {
        }
    }

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private static final Logger LOG = LoggerFactory.getLogger(ExportedGridContent.class);

    public void handleExportedGridContent(ExportedGridContent packet, Minecraft minecraft, Player player) {
        var saveDir = Minecraft.getInstance().gameDirectory.toPath();
        String filename;
        var spServer = Minecraft.getInstance().getSingleplayerServer();
        var connection = Minecraft.getInstance().getConnection();
        if (spServer != null) {
            saveDir = spServer.getServerDirectory();
            filename = "ae2_grid_";
        } else if (connection != null) {
            filename = "ae2_grid_from_server_";
        } else {
            LOG.error("Ignoring grid export without a connection to a server.");
            return;
        }

        saveDir = saveDir.toAbsolutePath().normalize();

        filename += packet.serialNumber() + "_" + TIMESTAMP_FORMATTER.format(LocalDateTime.now()) + ".zip";

        OpenOption[] openOptions = new OpenOption[0];
        if (packet.contentType() != ExportedGridContent.ContentType.FIRST_CHUNK) {
            openOptions = new OpenOption[] { StandardOpenOption.APPEND };
        }

        var tempPath = saveDir.resolve(filename + ".tmp");
        var finalPath = saveDir.resolve(filename);
        try (var out = Files.newOutputStream(tempPath, openOptions)) {
            out.write(packet.compressedData());
        } catch (IOException e) {
            AppEng.instance().sendSystemMessage(player,
                    Component.literal("Failed to write exported grid data to " + tempPath)
                            .withStyle(ChatFormatting.RED));
            LOG.error("Failed to write exported grid data to {}", tempPath, e);
            return;
        }

        if (packet.contentType() == ExportedGridContent.ContentType.LAST_CHUNK) {
            try {
                Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("Failed to move grid export {} into place", finalPath, e);
            }

            AppEng.instance().sendSystemMessage(player,
                    Component.literal("Saved grid data for grid #" + packet.serialNumber() + " from server to ")
                            .append(Component.literal(finalPath.toString()).withStyle(style -> {
                                return style.withUnderlined(true)
                                        .withClickEvent(new ClickEvent.OpenFile(finalPath.getParent().toString()));
                            })));
        }
    }

    public void handleCraftingJobStatusPacket(CraftingJobStatusPacket packet, Minecraft minecraft, Player player) {
        if (packet.status() == CraftingJobStatusPacket.Status.STARTED) {
            if (AEConfig.instance().isPinAutoCraftedItems()) {
                PinnedKeys.pinKey(packet.what(), PinnedKeys.PinReason.CRAFTING);
            }
        }

        PendingCraftingJobs.jobStatus(packet.jobId(), packet.what(), packet.requestedAmount(), packet.remainingAmount(), packet.status());
    }

    public void handleConfigValuePacket(ConfigValuePacket packet, Minecraft minecraft, Player player) {
        if (player.containerMenu instanceof IConfigurableObject configurableObject) {
            packet.loadSetting(configurableObject);
        }
    }

    public void register(PakcetHandlerMap handlers) {
        handlers.register(GuiDataSyncPacket.TYPE, this::handleGuiDataSyncPacket);
        handlers.register(MatterCannonPacket.TYPE, this::handleMatterCannonPacket);
        handlers.register(SetLinkStatusPacket.TYPE, this::handleSetLinkStatusPacket);
        handlers.register(PatternAccessTerminalPacket.TYPE, this::handlePatternAccessTerminalPacket);
        handlers.register(BlockTransitionEffectPacket.TYPE, this::handleBlockTransitionEffectPacket);
        handlers.register(CraftingStatusPacket.TYPE, this::handleCraftingStatusPacket);
        handlers.register(CraftConfirmPlanPacket.TYPE, this::handleCraftConfirmPlanPacket);
        handlers.register(NetworkStatusPacket.TYPE, this::handleNetworkStatusPacket);
        handlers.register(MolecularAssemblerAnimationPacket.TYPE, this::handleMolecularAssemblerAnimationPacket);
        handlers.register(MEInventoryUpdatePacket.TYPE, this::handleMEInventoryUpdatePacket);
        handlers.register(CompassResponsePacket.TYPE, this::handleCompassResponsePacket);
        handlers.register(ClearPatternAccessTerminalPacket.TYPE, this::handleClearPatternAccessTerminalPacket);
        handlers.register(ItemTransitionEffectPacket.TYPE, this::handleItemTransitionEffectPacket);
        handlers.register(MockExplosionPacket.TYPE, this::handleMockExplosionPacket);
        handlers.register(LightningPacket.TYPE, this::handleLightningPacket);
        handlers.register(ExportedGridContent.TYPE, this::handleExportedGridContent);
        handlers.register(CraftingJobStatusPacket.TYPE, this::handleCraftingJobStatusPacket);
        handlers.register(ConfigValuePacket.TYPE, this::handleConfigValuePacket);
    }
}
