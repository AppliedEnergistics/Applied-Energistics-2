package appeng.core.stats;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.util.FakePlayer;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class PlayerStatsRegistration
{

	public final static PlayerStatsRegistration instance = new PlayerStatsRegistration();

	AchievementPage ae2AchievementPage;

	@SubscribeEvent
	public void onCrafting(PlayerEvent.ItemCraftedEvent event)
	{
		if ( notPlayer( event.player ) || event.crafting == null )
			return;

		for (Achievements a : Achievements.values())
		{
			switch (a.type)
			{
			case Craft:
				if ( Platform.isSameItemPrecise( a.stack, event.crafting ) )
				{
					a.addToPlayer( event.player );
					return;
				}
				break;
			case CraftItem:
				if ( a.stack.getItem().getClass() == event.crafting.getItem().getClass() )
				{
					a.addToPlayer( event.player );
					return;
				}
			default:
			}
		}
	}

	@SubscribeEvent
	public void onCrafting(PlayerEvent.ItemPickupEvent event)
	{
		if ( notPlayer( event.player ) || event.pickedUp == null || event.pickedUp.getEntityItem() == null )
			return;

		ItemStack is = event.pickedUp.getEntityItem();

		for (Achievements a : Achievements.values())
		{
			switch (a.type)
			{
			case Pickup:
				if ( Platform.isSameItemPrecise( a.stack, is ) )
				{
					a.addToPlayer( event.player );
					return;
				}
			default:
			}
		}
	}

	private boolean notPlayer(EntityPlayer player)
	{
		if ( player == null || player.isDead || player instanceof FakePlayer )
			return true;
		return false;
	}

	/**
	 * Assign Parents and hierarchy.
	 */
	private void initHierarchy()
	{
		Achievements.Presses.setParent( Achievements.Compass );

		Achievements.Fluix.setParent( Achievements.ChargedQuartz );

		Achievements.Charger.setParent( Achievements.Fluix );

		Achievements.CrystalGrowthAccelerator.setParent( Achievements.Charger );

		Achievements.GlassCable.setParent( Achievements.Charger );

		Achievements.SpatialIOExplorer.setParent( Achievements.SpatialIO );

		Achievements.IOPort.setParent( Achievements.StorageCell );

		Achievements.PatternTerminal.setParent( Achievements.CraftingTerminal );

		Achievements.Controller.setParent( Achievements.Networking1 );

		Achievements.Networking2.setParent( Achievements.Controller );

		Achievements.Networking3.setParent( Achievements.Networking2 );

		Achievements.P2P.setParent( Achievements.Controller );

		Achievements.Recursive.setParent( Achievements.Controller );
	}

	public void init()
	{
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.Achievements ) )
		{
			FMLCommonHandler.instance().bus().register( this );
			initHierarchy();

			for (Stats s : Stats.values())
				s.getStat();

			/**
			 * register
			 */
			ArrayList<Achievement> list = new ArrayList();

			for (Achievements a : Achievements.values())
			{
				Achievement ach = a.getAchivement();
				if ( ach != null )
					list.add( ach );
			}

			ae2AchievementPage = new AchievementPage( "Applied Energistics 2", list.toArray( new Achievement[list.size()] ) );
			AchievementPage.registerAchievementPage( ae2AchievementPage );
		}
	}
}
