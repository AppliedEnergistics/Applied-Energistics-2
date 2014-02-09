package appeng.items.tools.powered;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.sync.packets.PacketLightning;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.server.ServerHelper;
import appeng.util.Platform;

public class ToolChargedStaff extends AEBasePoweredItem
{

	public ToolChargedStaff() {
		super( ToolChargedStaff.class, null );
		setfeature( EnumSet.of( AEFeature.ChargedStaff, AEFeature.PoweredTools ) );
		maxStoredPower = AEConfig.instance.staff_battery;
	}

	@Override
	public boolean hitEntity(ItemStack item, EntityLivingBase target, EntityLivingBase hitter)
	{
		if ( this.getAECurrentPower( item ) > 300 )
		{
			extractAEPower( item, 300 );
			if ( Platform.isServer() )
			{
				try
				{
					for (int x = 0; x < 2; x++)
					{
						float dx = (float) (Platform.getRandomFloat() * target.width + target.boundingBox.minX);
						float dy = (float) (Platform.getRandomFloat() * target.height + target.boundingBox.minY);
						float dz = (float) (Platform.getRandomFloat() * target.width + target.boundingBox.minZ);
						ServerHelper.proxy.sendToAllNearExcept( null, dx, dy, dz, 32.0, target.worldObj, new PacketLightning( dx, dy, dz ) );
					}
				}
				catch (IOException e)
				{

				}
			}
			target.attackEntityFrom( DamageSource.magic, 6 );
			return true;
		}

		return false;
	}
}
