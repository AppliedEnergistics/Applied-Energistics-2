package appeng.core.stats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;
import appeng.items.parts.ItemFacade;

public enum Achievements
{

	// done
	Compass(-2, -4, AEApi.instance().blocks().blockSkyCompass, AchievementType.Craft),

	// done
	Presses(-2, -2, AEApi.instance().materials().materialLogicProcessorPress, AchievementType.Custom),

	// done
	SpatialIO(-4, -4, AEApi.instance().blocks().blockSpatialIOPort, AchievementType.Craft),

	// done
	SpatialIOExplorer(-4, -2, AEApi.instance().items().itemSpatialCell128, AchievementType.Custom),

	// done
	StorageCell(-6, -4, AEApi.instance().items().itemCell64k, AchievementType.CraftItem),

	// done
	IOPort(-6, -2, AEApi.instance().blocks().blockIOPort, AchievementType.Craft),

	// done
	CraftingTerminal(-8, -4, AEApi.instance().parts().partCraftingTerminal, AchievementType.Craft),

	// done
	PatternTerminal(-8, -2, AEApi.instance().parts().partPatternTerminal, AchievementType.Craft),

	// done
	ChargedQuartz(0, -4, AEApi.instance().materials().materialCertusQuartzCrystalCharged, AchievementType.Pickup),

	// done
	Fluix(0, -2, AEApi.instance().materials().materialFluixCrystal, AchievementType.Pickup),

	// done
	Charger(0, 0, AEApi.instance().blocks().blockCharger, AchievementType.Craft),

	// done
	CrystalGrowthAccelerator(-2, 0, AEApi.instance().blocks().blockQuartzGrowthAccelerator, AchievementType.Craft),

	// done
	GlassCable(2, 0, AEApi.instance().parts().partCableGlass, AchievementType.Craft),

	// done
	Networking1(4, -6, AEApi.instance().parts().partCableCovered, AchievementType.Custom),

	// done
	Controller(4, -4, AEApi.instance().blocks().blockController, AchievementType.Craft),

	// done
	Networking2(4, 0, AEApi.instance().parts().partCableSmart, AchievementType.Custom),

	// done
	Networking3(4, 2, AEApi.instance().parts().partCableDense, AchievementType.Custom),

	// done
	P2P(2, -2, AEApi.instance().parts().partP2PTunnelME, AchievementType.Craft),

	// done
	Recursive(6, -2, AEApi.instance().blocks().blockInterface, AchievementType.Craft),

	// done
	CraftingCPU(6, 0, AEApi.instance().blocks().blockCraftingStorage64k, AchievementType.CraftItem),

	// done
	Facade(6, 2, ((ItemFacade) AEApi.instance().items().itemFacade.item()).createFacadeForItem( new ItemStack( Blocks.iron_block ), false ),
			AchievementType.CraftItem),

	// done
	NetworkTool(8, 0, AEApi.instance().items().itemNetworkTool, AchievementType.Craft),

	// done
	PortableCell(8, 2, AEApi.instance().items().itemPortableCell, AchievementType.Craft),

	// done
	StorageBus(10, 0, AEApi.instance().parts().partStorageBus, AchievementType.Craft),

	// done
	QNB(10, 2, AEApi.instance().blocks().blockQuantumLink, AchievementType.Craft);

	public final ItemStack stack;
	public final AchievementType type;
	private final int x, y;

	private Achievement parent;
	private Achievement stat;

	public void setParent(Achievements parent)
	{
		this.parent = parent.getAchievement();
	}

	public Achievement getAchievement()
	{
		if ( stat == null && stack != null )
		{
			stat = new Achievement( "achievement.ae2." + name(), "ae2." + name(), x, y, stack, parent );
			stat.registerStat();
		}

		return stat;
	}

	private Achievements(int x, int y, AEColoredItemDefinition which, AchievementType type)
	{
		stack = which.stack( AEColor.Transparent, 1 );
		this.type = type;
		this.x = x;
		this.y = y;
	}

	private Achievements(int x, int y, AEItemDefinition which, AchievementType type)
	{
		stack = which.stack( 1 );
		this.type = type;
		this.x = x;
		this.y = y;
	}

	private Achievements(int x, int y, ItemStack which, AchievementType type)
	{
		stack = which;
		this.type = type;
		this.x = x;
		this.y = y;
	}

	public void addToPlayer(EntityPlayer player)
	{
		player.addStat( getAchievement(), 1 );
	}

}
