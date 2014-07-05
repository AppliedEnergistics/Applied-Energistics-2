package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotNormal;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;

public class ContainerInterface extends ContainerUpgradeable
{

	DualityInterface myDuality;

	@GuiSync(3)
	public YesNo bMode = YesNo.NO;

	public ContainerInterface(InventoryPlayer ip, IInterfaceHost te) {
		super( ip, te.getInterfaceDuality().getHost() );

		myDuality = te.getInterfaceDuality();

		for (int x = 0; x < 9; x++)
			addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.ENCODED_PATTERN, myDuality.getPatterns(), x, 8 + 18 * x, 90 + 7 ) );

		for (int x = 0; x < 8; x++)
			addSlotToContainer( new SlotFake( myDuality.getConfig(), x, 17 + 18 * x, 35 ) );

		for (int x = 0; x < 8; x++)
			addSlotToContainer( new SlotNormal( myDuality.getStorage(), x, 17 + 18 * x, 35 + 18 ) );

	}

	@Override
	protected int getHeight()
	{
		return 211;
	}

	@Override
	protected void setupConfig()
	{
		setupUpgrades();
	}

	protected void loadSettingsFromHost(IConfigManager cm)
	{
		this.bMode = (YesNo) cm.getSetting( Settings.BLOCK );
	}

	public int availableUpgrades()
	{
		return 1;
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );
		super.detectAndSendChanges();
	}
}
