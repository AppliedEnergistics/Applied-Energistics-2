package appeng.api.networking.events;

/**
 * Posted by storage devices to inform AE to refresh its storage structure.
 * 
 * This is done in cases such as a storage cell being removed or added to a
 * drive.
 * 
 * you do not need to send this event when your node is added / removed from the
 * grid.
 */
public class MENetworkCellArrayUpdate extends MENetworkEvent
{

}
