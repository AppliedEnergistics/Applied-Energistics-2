package appeng.items.materials;

import java.util.EnumSet;
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

	private int currentMaterial = 0;
	private final MaterialType material[] = new MaterialType[MaterialType.values().length];

	public ItemMaterial() {
		super( ItemMaterial.class );
		setfeature( EnumSet.of( AEFeature.Core ) );
	}

	public ItemStack createMaterial(MaterialType mat)
	{
		if ( mat.damageValue == -1 )
		{
			boolean enabled = true;
			for (AEFeature f : mat.getFeature())
				enabled = enabled && Configuration.instance.isFeatureEnabled( f );

			if ( enabled )
			{
				material[currentMaterial] = mat;
				mat.damageValue = currentMaterial;
				ItemStack output = new ItemStack( this );
				output.setItemDamage( currentMaterial );
				currentMaterial++;

				if ( mat.getOreName() != null )
					OreDictionary.registerOre( mat.getOreName(), this );

				return output;
			}

			return null;
		}
		else
			throw new RuntimeException( "Cannot create the same material twice..." );
	}

	public MaterialType getTypeByStack(ItemStack is)
	{
		return material[is.getItemDamage()];
	}

	@Override
	public Icon getIconFromDamage(int dmg)
	{
		return material[dmg].icon;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		return AEFeatureHandler.getName( ItemMaterial.class, getTypeByStack( is ).name() );
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		for (int x = 0; x < currentMaterial; x++)
		{
			String tex = "appliedenergistics2:" + getUnlocalizedName( new ItemStack( this, 1, x ) );
			material[x].icon = par1IconRegister.registerIcon( tex );
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
		for (int x = 0; x < currentMaterial; x++)
			cList.add( new ItemStack( this, 1, x ) );
	}
}
