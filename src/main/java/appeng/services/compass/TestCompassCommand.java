package appeng.services.compass;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import appeng.server.ISubCommand;

public class TestCompassCommand implements ISubCommand {
    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        var level = sender.getLevel();
        var chunkPos = new ChunkPos(new BlockPos(sender.getPosition()));
        var compassRegion = CompassRegion.get(level, chunkPos);

        for (var i = 0; i <= level.getSectionsCount(); i++) {
            var hasSkyStone = compassRegion.hasSkyStone(chunkPos.x, chunkPos.z, i);
            var yMin = i * SectionPos.SECTION_SIZE;
            var yMax = (i + 1) * SectionPos.SECTION_SIZE - 1;
            sender.sendSuccess(
                    new TextComponent("Section [y=" + yMin + "-" + yMax + "] " + i + ": " + hasSkyStone),
                    false);
        }
    }
}
