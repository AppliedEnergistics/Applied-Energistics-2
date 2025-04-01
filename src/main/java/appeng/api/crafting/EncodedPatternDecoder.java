package appeng.api.crafting;

import net.minecraft.server.level.ServerLevel;

import appeng.api.stacks.AEItemKey;

@FunctionalInterface
public interface EncodedPatternDecoder<T extends IPatternDetails> {
    T decode(AEItemKey what, ServerLevel level);
}
