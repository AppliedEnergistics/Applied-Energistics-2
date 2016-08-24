package appeng.loot;


import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.api.AEApi;
import appeng.api.definitions.IMaterials;

public class ChestLoot
{

	@SubscribeEvent
	public void loadLootTable( LootTableLoadEvent event ){
		if( event.getName() == LootTableList.CHESTS_ABANDONED_MINESHAFT )
		{
			//TODO 1.9.4 aftermath - All these loot quality, pools and stuff. Figure it out and balance it.
			final IMaterials materials = AEApi.instance().definitions().materials();
			materials.certusQuartzCrystal().maybeStack( 1 ).ifPresent( is ->
			{
				event.getTable().addPool( new LootPool(
								new LootEntry[] {
										new LootEntryItem(
												is.getItem(),
												2,
												3,
												new LootFunction[] { new SetMetadata( null, new RandomValueRange( is.getItemDamage() ) ) },
												new LootCondition[] { new RandomChance( 1 ) },
												"AE2 Crystal_" + is.getItemDamage()
										)
								},
								new LootCondition[0],
								new RandomValueRange( 1, 4 ),
								new RandomValueRange( 0, 2 ), "AE2 Crystals"
						)
				);
			} );

			materials.certusQuartzDust().maybeStack( 1 ).ifPresent( is ->
			{
				event.getTable().addPool( new LootPool(
								new LootEntryItem[] {
										new LootEntryItem(
												is.getItem(),
												2,
												3,
												new LootFunction[] { new SetMetadata( null, new RandomValueRange( is.getItemDamage() ) ) },
												new LootCondition[] { new RandomChance( 1 ) },
												"AE2 Dust_" + is.getItemDamage()
										)
								},
								new LootCondition[0],
								new RandomValueRange( 1, 4 ),
								new RandomValueRange( 0, 2 ),
								"AE2 Dusts"
						)
				);
			} );

		}
	}
	
}