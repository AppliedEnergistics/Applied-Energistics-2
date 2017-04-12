package appeng.core.features.registries.entries;


import appeng.api.features.IInscriberRecipe;
import appeng.api.features.InscriberProcessType;
import com.google.common.base.Optional;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Basic inscriber recipe
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public class InscriberRecipe implements IInscriberRecipe
{
	@Nonnull
	private final List<ItemStack> inputs;

	@Nonnull
	private final ItemStack output;

	@Nonnull
	private final Optional<ItemStack> maybeTop;

	@Nonnull
	private final Optional<ItemStack> maybeBot;

	@Nonnull
	private final InscriberProcessType type;

	public InscriberRecipe( @Nonnull final Collection<ItemStack> inputs, @Nonnull final ItemStack output, @Nullable final ItemStack top, @Nullable final ItemStack bot, @Nonnull final InscriberProcessType type )
	{
		this.inputs = new ArrayList<ItemStack>( inputs.size() );
		this.inputs.addAll( inputs );

		this.output = output;
		this.maybeTop = Optional.fromNullable( top );
		this.maybeBot = Optional.fromNullable( bot );

		this.type = type;
	}

	@Nonnull
	@Override
	public final List<ItemStack> getInputs()
	{
		return this.inputs;
	}

	@Nonnull
	@Override
	public final ItemStack getOutput()
	{
		return this.output;
	}

	@Nonnull
	@Override
	public final Optional<ItemStack> getTopOptional()
	{
		return this.maybeTop;
	}

	@Nonnull
	@Override
	public final Optional<ItemStack> getBottomOptional()
	{
		return this.maybeBot;
	}

	@Nonnull
	@Override
	public final InscriberProcessType getProcessType()
	{
		return this.type;
	}

	@Override
	public boolean equals( final Object o )
	{
		if( this == o )
		{
			return true;
		}
		if( !( o instanceof IInscriberRecipe ) )
		{
			return false;
		}

		final IInscriberRecipe that = (IInscriberRecipe) o;

		if( !this.inputs.equals( that.getInputs() ) )
		{
			return false;
		}
		if( !this.output.equals( that.getOutput() ) )
		{
			return false;
		}
		if( !this.maybeTop.equals( that.getTopOptional() ) )
		{
			return false;
		}
		if( !this.maybeBot.equals( that.getBottomOptional() ) )
		{
			return false;
		}
		return this.type == that.getProcessType();
	}

	@Override
	public int hashCode()
	{
		int result = this.inputs.hashCode();
		result = 31 * result + this.output.hashCode();
		result = 31 * result + this.maybeTop.hashCode();
		result = 31 * result + this.maybeBot.hashCode();
		result = 31 * result + this.type.hashCode();
		return result;
	}
}
