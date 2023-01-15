package appeng.client.guidebook.command;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Implements commands that help with the workflow to create and edit structures for use in the guidebook. The commands
 * will not be used directly by users, but rather by command blocks built by
 * {@link appeng.server.testplots.GuidebookPlot}.
 */
@OnlyIn(Dist.CLIENT)
public class GuidebookStructureCommands {

    @Nullable
    private static String lastOpenedOrSavedPath;

    private static final String[] FILE_PATTERNS = { "*.snbt", "*.nbt" };

    private static final String FILE_PATTERN_DESC = "Structure NBT Files (*.snbt, *.nbt)";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = literal("ae2guide");

        registerImportCommand(rootCommand);

        registerExportCommand(rootCommand);

        dispatcher.register(rootCommand);
    }

    private static void registerImportCommand(LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        LiteralArgumentBuilder<CommandSourceStack> importSubcommand = literal("importstructure");
        // Only usable on singleplayer worlds and only by the local player (in case it is opened to LAN)
        importSubcommand.requires(source -> Minecraft.getInstance().hasSingleplayerServer());
        importSubcommand
                .then(Commands.argument("origin", BlockPosArgument.blockPos())
                        .executes(context -> {
                            var origin = BlockPosArgument.getBlockPos(context, "origin");
                            importStructure(context.getSource().getLevel(), origin);
                            return 0;
                        }));
        rootCommand.then(importSubcommand);
    }

    private static void importStructure(ServerLevel level, BlockPos origin) {
        var minecraft = Minecraft.getInstance();
        var server = minecraft.getSingleplayerServer();
        var player = minecraft.player;
        if (server == null || player == null) {
            return;
        }

        CompletableFuture
                .supplyAsync(GuidebookStructureCommands::pickFileForOpen, minecraft)
                .thenApplyAsync(selectedPath -> {
                    if (selectedPath == null) {
                        return null;
                    }

                    lastOpenedOrSavedPath = selectedPath; // remember for save dialog

                    var manager = server.getStructureManager();
                    try {
                        CompoundTag compound;
                        if (selectedPath.toLowerCase(Locale.ROOT).endsWith(".snbt")) {
                            var textInFile = Files.readString(Paths.get(selectedPath), StandardCharsets.UTF_8);
                            try {
                                compound = NbtUtils.snbtToStructure(textInFile);
                            } catch (CommandSyntaxException e) {
                                player.sendSystemMessage(Component.literal(e.toString()));
                                return null;
                            }
                        } else {
                            try (var is = new BufferedInputStream(new FileInputStream(selectedPath))) {
                                compound = NbtIo.readCompressed(is);
                            }
                        }
                        var structure = manager.readStructure(compound);
                        if (!structure.placeInWorld(
                                level,
                                origin,
                                origin,
                                new StructurePlaceSettings(),
                                new SingleThreadedRandomSource(0L),
                                Block.UPDATE_CLIENTS)) {
                            player.sendSystemMessage(Component.literal("Failed to place structure"));
                        } else {
                            player.sendSystemMessage(Component.literal("Placed structure"));
                        }
                    } catch (Exception e) {
                        player.sendSystemMessage(Component.literal(e.toString()));
                    }

                    return null;
                }, server)
                .thenRunAsync(() -> {
                    if (minecraft.screen instanceof PauseScreen) {
                        minecraft.setScreen(null);
                    }
                }, minecraft);
    }

    private static void registerExportCommand(LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        LiteralArgumentBuilder<CommandSourceStack> exportSubcommand = literal("exportstructure");
        // Only usable on singleplayer worlds and only by the local player (in case it is opened to LAN)
        exportSubcommand.requires(source -> Minecraft.getInstance().hasSingleplayerServer());
        exportSubcommand
                .then(Commands.argument("origin", BlockPosArgument.blockPos())
                        .then(Commands.argument("sizeX", IntegerArgumentType.integer(1))
                                .then(Commands.argument("sizeY", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("sizeZ", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    var origin = BlockPosArgument.getBlockPos(context, "origin");
                                                    var sizeX = IntegerArgumentType.getInteger(context, "sizeX");
                                                    var sizeY = IntegerArgumentType.getInteger(context, "sizeY");
                                                    var sizeZ = IntegerArgumentType.getInteger(context, "sizeZ");
                                                    var size = new Vec3i(sizeX, sizeY, sizeZ);
                                                    exportStructure(context.getSource().getLevel(), origin, size);
                                                    return 0;
                                                })))));
        rootCommand.then(exportSubcommand);
    }

    private static void exportStructure(ServerLevel level, BlockPos origin, Vec3i size) {
        var minecraft = Minecraft.getInstance();
        var server = minecraft.getSingleplayerServer();
        var player = minecraft.player;
        if (server == null || player == null) {
            return;
        }

        CompletableFuture
                .supplyAsync(GuidebookStructureCommands::pickFileForSave, minecraft)
                .thenApplyAsync(selectedPath -> {
                    if (selectedPath == null) {
                        return null;
                    }

                    try {
                        // Find the smallest box containing the placed blocks
                        var to = BlockPos
                                .betweenClosedStream(origin,
                                        origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1))
                                .filter(pos -> !level.getBlockState(pos).isAir())
                                .reduce(
                                        origin,
                                        (blockPos, blockPos2) -> new BlockPos(
                                                Math.max(blockPos.getX(), blockPos2.getX()),
                                                Math.max(blockPos.getY(), blockPos2.getY()),
                                                Math.max(blockPos.getZ(), blockPos2.getZ())));
                        var actualSize = new BlockPos(
                                1 + to.getX() - origin.getX(),
                                1 + to.getY() - origin.getY(),
                                1 + to.getZ() - origin.getZ());

                        var structureTemplate = new StructureTemplate();
                        structureTemplate.fillFromWorld(
                                level,
                                origin,
                                actualSize,
                                false,
                                Blocks.AIR);

                        var compound = structureTemplate.save(new CompoundTag());
                        if (selectedPath.toLowerCase(Locale.ROOT).endsWith(".snbt")) {
                            Files.writeString(
                                    Paths.get(selectedPath),
                                    NbtUtils.structureToSnbt(compound),
                                    StandardCharsets.UTF_8);
                        } else {
                            NbtIo.writeCompressed(compound, new File(selectedPath));
                        }

                        player.sendSystemMessage(Component.literal("Saved structure"));
                    } catch (IOException e) {
                        player.sendSystemMessage(Component.literal(e.toString()));
                    }

                    return null;
                }, server)
                .thenRunAsync(() -> {
                    if (minecraft.screen instanceof PauseScreen) {
                        minecraft.setScreen(null);
                    }
                }, minecraft);
    }

    private static String pickFileForOpen() {
        try (var stack = MemoryStack.stackPush()) {

            return TinyFileDialogs.tinyfd_openFileDialog(
                    "Load Structure",
                    lastOpenedOrSavedPath,
                    createFilterPatterns(stack),
                    FILE_PATTERN_DESC,
                    false);
        }
    }

    private static String pickFileForSave() {
        try (var stack = MemoryStack.stackPush()) {

            return TinyFileDialogs.tinyfd_saveFileDialog(
                    "Save Structure",
                    lastOpenedOrSavedPath,
                    createFilterPatterns(stack),
                    FILE_PATTERN_DESC);
        }
    }

    private static PointerBuffer createFilterPatterns(MemoryStack stack) {
        PointerBuffer filterPatternsBuffer = stack.mallocPointer(FILE_PATTERNS.length);
        for (var pattern : FILE_PATTERNS) {
            filterPatternsBuffer.put(stack.UTF8(pattern));
        }
        filterPatternsBuffer.flip();
        return filterPatternsBuffer;
    }
}
