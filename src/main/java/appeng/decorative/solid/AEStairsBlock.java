package appeng.decorative.solid;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;

public class AEStairsBlock extends StairsBlock {

    public AEStairsBlock(Supplier<BlockState> baseBlockState, Properties settings) {
        super(baseBlockState.get(), settings);
    }

}
