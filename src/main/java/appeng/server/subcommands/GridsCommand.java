package appeng.server.subcommands;

import appeng.hooks.ticking.TickHandler;
import appeng.server.ISubCommand;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

public class GridsCommand implements ISubCommand {
    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("dump").executes(ctx -> {
            dumpGrids();
            return 1;
        }));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> data,
                     CommandSourceStack sender) {
    }

    private void dumpGrids() {
        var gridsPath = Paths.get("grids.json.gz");
        try (var out = Files.newOutputStream(gridsPath);
             var gzOut = new GZIPOutputStream(out);
             var writer = new BufferedWriter(new OutputStreamWriter(gzOut, StandardCharsets.UTF_8));
             var jsonWriter = new JsonWriter(writer)) {

            jsonWriter.beginObject();
            jsonWriter.name("grids");
            jsonWriter.beginArray();

            for (var grid : TickHandler.instance().getGridList()) {
                grid.debugDump(jsonWriter);
            }

            jsonWriter.endArray();
            jsonWriter.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
