package appeng.items.materials;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IStorageComponent;
import appeng.api.implementations.IUpgradeModule;
import appeng.core.Configuration;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.items.AEBaseItem;

public class ItemMaterial extends AEBaseItem implements IStorageComponent, IUpgradeModule
{

	HashMap<Integer, MaterialType> dmgToMaterial = new HashMap();

	public ItemMaterial() {
		super( ItemMaterial.class );
		setfeature( EnumSet.of( AEFeature.Core ) );
	}

	public ItemStack createMaterial(MaterialType mat)
	{
		String name = mat.name();

		if ( mat.damageValue == -1 )
		{
			boolean enabled = true;
			for (AEFeature f : mat.getFeature())
				enabled = enabled && Configuration.instance.isFeatureEnabled( f );

			if ( enabled )
			{
				int newMaterialNum = Configuration.instance.get( "materials", name, Configuration.instance.getFreeMaterial() ).getInt();
				mat.damageValue = newMaterialNum;
				ItemStack output = new ItemStack( this, 1, newMaterialNum );
				output.setItemDamage( newMaterialNum );

				dmgToMaterial.put( newMaterialNum, mat );

				if ( mat.getOreName() != null )
					OreDictionary.registerOre( mat.getOreName(), output );

				return output;
			}

			return null;
		}
		else
			throw new RuntimeException( "Cannot create the same material twice..." );
	}

	public MaterialType getTypeByStack(ItemStack is)
	{
		return dmgToMaterial.get( is.getItemDamage() );
	}

	@Override
	public Icon getIconFromDamage(int dmg)
	{
		return dmgToMaterial.get( dmg ).icon;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		return AEFeatureHandler.getName( ItemMaterial.class, getTypeByStack( is ).name() );
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		for (MaterialType mat : MaterialType.values())
		{
			if ( mat.damageValue != -1 )
			{
				String tex = "appliedenergistics2:" + getUnlocalizedName( new ItemStack( this, 1, mat.damageValue ) );
				mat.icon = par1IconRegister.registerIcon( tex );
			}
		}
	}

	@Override
	public boolean hasCustomEntity(ItemStack is)
	{
		return getTypeByStack( is ).hasCustomEntity();
	}

	@Override
	public Entity createEntity(World w, Entity location, ItemStack itemstack)
	{
		Class<? extends Entity> droppedEntity = getTypeByStack( itemstack ).getCustomEntityClass();
		Entity eqi;

		try
		{
			eqi = droppedEntity.getConstructor( World.class, double.class, double.class, double.class, ItemStack.class ).newInstance( w, location.posX,
					location.posY, location.posZ, itemstack );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}

		eqi.motionX = location.motionX;
		eqi.motionY = location.motionY;
		eqi.motionZ = location.motionZ;

		if ( location instanceof EntityItem && eqi instanceof EntityItem )
			((EntityItem) eqi).delayBeforeCanPickup = ((EntityItem) location).delayBeforeCanPickup;

		return eqi;
	}

	@Override
	public int getBytes(ItemStack is)
	{
		switch (getTypeByStack( is ))
		{
		case Cell1kPart:
			return 1024;
		case Cell4kPart:
			return 1024 * 4;
		case Cell16kPart:
			return 1024 * 16;
		case Cell64kPart:
			return 1024 * 64;
		default:
		}
		return 0;
	}

	@Override
	public boolean isStorageComponent(ItemStack is)
	{
		switch (getTypeByStack( is ))
		{
		case Cell1kPart:
		case Cell4kPart:
		case Cell16kPart:
		case Cell64kPart:
			return true;
		default:
		}
		return false;
	}

	@Override
	public Upgrades getType(ItemStack itemstack)
	{
		switch (getTypeByStack( itemstack ))
		{
		case CardCapacity:
			return Upgrades.CAPACITY;
		case CardFuzzy:
			return Upgrades.FUZZY;
		case CardRedstone:
			return Upgrades.REDSTONE;
		case CardSpeed:
			return Upgrades.SPEED;
		default:
			return null;
		}
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List cList)
	{
		for (MaterialType mat : MaterialType.values())
		{
			if ( mat.damageValue >= 0 )
				cList.add( new ItemStack( this, 1, mat.damageValue ) );
		}
	}
}
