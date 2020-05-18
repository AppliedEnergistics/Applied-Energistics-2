
package appeng.fluids.container;


import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.container.guisync.GuiSync;
import appeng.fluids.parts.PartFluidLevelEmitter;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;


public class ContainerFluidLevelEmitter extends ContainerFluidConfigurable
{
	private final PartFluidLevelEmitter lvlEmitter;

	@OnlyIn( Dist.CLIENT )
	private GuiTextField textField;
	@GuiSync( 3 )
	public long EmitterValue = -1;

	public ContainerFluidLevelEmitter( final PlayerInventory ip, final PartFluidLevelEmitter te )
	{
		super( ip, te );
		this.lvlEmitter = te;
	}

	@OnlyIn( Dist.CLIENT )
	public void setTextField( final GuiTextField level )
	{
		this.textField = level;
		this.textField.setText( String.valueOf( this.EmitterValue ) );
	}

	public void setLevel( final long l, final PlayerEntity player )
	{
		this.lvlEmitter.setReportingValue( l );
		this.EmitterValue = l;
	}

	@Override
	protected void setupConfig()
	{
	}

	@Override
	protected boolean supportCapacity()
	{
		return false;
	}

	@Override
	public int availableUpgrades()
	{

		return 0;
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			this.EmitterValue = this.lvlEmitter.getReportingValue();
			this.setRedStoneMode( (RedstoneMode) this.getUpgradeable().getConfigManager().getSetting( Settings.REDSTONE_EMITTER ) );
		}

		this.standardDetectAndSendChanges();
	}

	@Override
	public void onUpdate( final String field, final Object oldValue, final Object newValue )
	{
		if( field.equals( "EmitterValue" ) )
		{
			if( this.textField != null )
			{
				this.textField.setText( String.valueOf( this.EmitterValue ) );
			}
		}
	}

	@Override
	public IAEFluidTank getFluidConfigInventory()
	{
		return this.lvlEmitter.getConfig();
	}
}
