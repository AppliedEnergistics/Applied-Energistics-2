package appeng.core.stats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.ChatComponentTranslation;

public enum Stats
{

	// done
	ItemsInserted,

	// done
	ItemsExtracted,

	// done
	TurnedCranks;

	private StatBasic stat;

	public StatBasic getStat()
	{
		if ( stat == null )
		{
			stat = new StatBasic( "stat.ae2." + name(), new ChatComponentTranslation( "stat.ae2." + name() ) );
			stat.registerStat();
		}

		return stat;
	}

	private Stats() {
	}

	public void addToPlayer(EntityPlayer player, int howMany)
	{
		player.addStat( getStat(), howMany );
	}

}
