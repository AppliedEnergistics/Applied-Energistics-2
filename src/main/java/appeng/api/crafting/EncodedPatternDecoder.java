package appeng.api.crafting;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;

@FunctionalInterface
public interface EncodedPatternDecoder<T extends IPatternDetails> {
    T decode(AEItemKey what, ServerLevel level);
}
