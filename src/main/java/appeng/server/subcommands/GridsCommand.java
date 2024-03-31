package appeng.server.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.networking.GridHelper;
import appeng.core.network.clientbound.DumpedGridContent;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.hooks.ticking.TickHandler;
import appeng.me.Grid;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.server.ISubCommand;
import appeng.util.Platform;

public class GridsCommand implements ISubCommand {
    private static final Logger LOG = LoggerFactory.getLogger(GridsCommand.class);

    public static String buildDumpCommand(int gridSerial) {
        return "/ae2 grids dump " + gridSerial;
    }

    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("dump").executes(ctx -> {
            dumpGrids(ctx.getSource());
            return 1;
        }).then(Commands.argument("gridSerial", IntegerArgumentType.integer()).executes(context -> {
            var gridSerial = context.getArgument("gridSerial", Integer.class);

            // Find the starting grid
            for (var grid : TickHandler.instance().getGridList()) {
                if (grid.getSerialNumber() == gridSerial) {
                    dumpGrid(grid, context.getSource());
                    return 1;
                }
            }

            throw new SimpleCommandExceptionType(new LiteralMessage("No such grid found")).create();
        })));
    }

    private void dumpGrids(CommandSourceStack source) throws CommandSyntaxException {

        var grids = TickHandler.instance().getGridList();

        source.sendSystemMessage(Component.literal("Dumping " + grids.size() + " grids"));

        dumpGrids(0, grids, source);

    }

    private void dumpGrid(Grid startGrid, CommandSourceStack source) throws CommandSyntaxException {

        // Collect all reachable grids
        var reachableGrids = Collections.newSetFromMap(new IdentityHashMap<Grid, Boolean>());
        reachableGrids.add(startGrid);
        var openSet = Collections.newSetFromMap(new IdentityHashMap<Grid, Boolean>());
        openSet.add(startGrid);

        while (!openSet.isEmpty()) {
            var it = openSet.iterator();
            var grid = it.next();
            it.remove();
            for (var node : grid.getNodes()) {
                if (node.getOwner() instanceof AEBasePart basePart) {
                    visitGridInFrontOfPart(basePart, reachableGrids, openSet);
                } else if (node.getOwner() instanceof PatternProviderLogicHost patternProvider) {
                    for (var targetSide : patternProvider.getTargets()) {
                        visitGridAt(
                                patternProvider.getBlockEntity().getLevel(),
                                patternProvider.getBlockEntity().getBlockPos().relative(targetSide),
                                reachableGrids,
                                openSet);
                    }
                } else if (node.getOwner() instanceof MEP2PTunnelPart meTunnel) {
                    var tunnelGrid = (Grid) meTunnel.getMainNode().getGrid();
                    if (tunnelGrid != null && reachableGrids.add(tunnelGrid)) {
                        openSet.add(tunnelGrid);
                    }
                }
            }
        }

        dumpGrids(startGrid.getSerialNumber(), reachableGrids, source);
    }

    private static void visitGridInFrontOfPart(AEBasePart part, Set<Grid> reachableGrids, Set<Grid> openSet) {
        var partSide = part.getSide();
        if (partSide == null) {
            return;
        }
        // Storage buses that are attached to devices on different grids are interesting to us
        var hostBe = part.getBlockEntity();
        var targetPos = hostBe.getBlockPos().relative(partSide);
        visitGridAt(hostBe.getLevel(), targetPos, reachableGrids, openSet);
    }

    private static void visitGridAt(Level level, BlockPos pos, Set<Grid> reachableGrids, Set<Grid> openSet) {
        var targetGridHost = GridHelper.getNodeHost(level, pos);
        if (targetGridHost != null) {
            for (var side : Platform.DIRECTIONS_WITH_NULL) {
                var nodeOnSide = targetGridHost.getGridNode(side);
                if (nodeOnSide != null) {
                    var nodeGrid = (Grid) nodeOnSide.getGrid();
                    if (reachableGrids.add(nodeGrid)) {
                        openSet.add(nodeGrid);
                    }
                }
            }
        }
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> data,
            CommandSourceStack sender) {
    }

    private void dumpGrids(int baseSerialNumber, Collection<Grid> grids, CommandSourceStack source)
            throws CommandSyntaxException {
        source.sendSystemMessage(Component.literal("Dumping " + grids.size() + " grids"));
        LOG.info("Dumping {} grids for {}", grids.size(), source);

        if (source.isPlayer()) {
            var player = source.getPlayerOrException();
            PacketDistributor.PLAYER.with(source.getPlayerOrException())
                    .send(new DumpedGridContent(baseSerialNumber, DumpedGridContent.Type.FIRST_CHUNK, new byte[0]));

            try (var out = new SendToPlayerStream(player, baseSerialNumber)) {
                dumpGrids(grids, out);
            }
        } else {
            var targetPath = Paths.get("grids.zip");
            try (var out = Files.newOutputStream(targetPath)) {
                dumpGrids(grids, out);
            } catch (IOException e) {
                LOG.error("Failed to dump grids.", e);
                source.sendFailure(Component.literal("Failed to export grids: " + e));
            }
        }

    }

    private void dumpGrids(Iterable<Grid> grids, OutputStream out) {
        try (var zipOut = new ZipOutputStream(out)) {

            for (var grid : grids) {
                var entry = new ZipEntry("grid_" + grid.getSerialNumber() + ".json");
                zipOut.putNextEntry(entry);

                try (var writer = new JsonWriter(new OutputStreamWriter(zipOut, StandardCharsets.UTF_8) {
                    @Override
                    public void close() throws IOException {
                        flush();
                    }
                })) {
                    writer.setIndent(" ");
                    grid.debugDump(writer);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SendToPlayerStream extends OutputStream {
        private static final int FLUSH_AFTER = 512 * 1024;
        private final ByteArrayOutputStream bout; // 512kb buffer
        private final ServerPlayer player;
        private final int baseSerialNumber;
        private boolean closed;

        public SendToPlayerStream(ServerPlayer player, int baseSerialNumber) {
            this.player = player;
            this.baseSerialNumber = baseSerialNumber;
            bout = new ByteArrayOutputStream(FLUSH_AFTER);
        }

        @Override
        public void write(int b) {
            Preconditions.checkState(!closed, "stream already closed");
            bout.write(b);
            if (bout.size() > FLUSH_AFTER) {
                PacketDistributor.PLAYER.with(player)
                        .send(new DumpedGridContent(baseSerialNumber, DumpedGridContent.Type.CHUNK,
                                bout.toByteArray()));
                bout.reset();
            }
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) {
            Preconditions.checkState(!closed, "stream already closed");
            bout.write(b, off, len);
            if (bout.size() > FLUSH_AFTER) {
                PacketDistributor.PLAYER.with(player)
                        .send(new DumpedGridContent(baseSerialNumber, DumpedGridContent.Type.CHUNK,
                                bout.toByteArray()));
                bout.reset();
            }
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                PacketDistributor.PLAYER.with(player)
                        .send(new DumpedGridContent(baseSerialNumber, DumpedGridContent.Type.LAST_CHUNK,
                                bout.toByteArray()));
                bout.reset();
            }
        }
    }
}
