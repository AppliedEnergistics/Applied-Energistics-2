
package appeng.decorative.stair;


import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;


/**
 * @author thatsIch
 * @version rv3 - 30.06.2015
 * @since rv3 30.06.2015
 */
public class FluixStairSupplier implements Supplier<BlockStairs>
{
	private final Supplier<Block> fluixBlockSupplier;

	public FluixStairSupplier( final Supplier<Block> fluixBlockSupplier )
	{
		Preconditions.checkNotNull( fluixBlockSupplier );

		this.fluixBlockSupplier = fluixBlockSupplier;
	}

	@Override
	public FluixStairBlock get()
	{
		return new FluixStairBlock( this.fluixBlockSupplier.get(), "fluix" );
	}
}
