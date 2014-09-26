package gregtechmod.api.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * This is a bunch of Functions my TileEntities provide, to make life much easier, and to get rid of internal TileEntity stuff.
 */
public interface IHasWorldObjectAndCoords {
	public World getWorld();
	public int getXCoord();
	public int getYCoord();
	public int getZCoord();
	
	public boolean isServerSide();
    public boolean isClientSide();
    
    public int getRandomNumber(int aRange);
    
	public TileEntity getTileEntity(int aX, int aY, int aZ);
    public TileEntity getTileEntityOffset(int aX, int aY, int aZ);
	public TileEntity getTileEntityAtSide(byte aSide);
	public TileEntity getTileEntityAtSideAndDistance(byte aSide, int aDistance);
	
	public IInventory getIInventory(int aX, int aY, int aZ);
    public IInventory getIInventoryOffset(int aX, int aY, int aZ);
	public IInventory getIInventoryAtSide(byte aSide);
	public IInventory getIInventoryAtSideAndDistance(byte aSide, int aDistance);
	
    public short getBlockID(int aX, int aY, int aZ);
    public short getBlockIDOffset(int aX, int aY, int aZ);
    public short getBlockIDAtSide(byte aSide);
    public short getBlockIDAtSideAndDistance(byte aSide, int aDistance);
    
	public byte getMetaID(int aX, int aY, int aZ);
    public byte getMetaIDOffset(int aX, int aY, int aZ);
    public byte getMetaIDAtSide(byte aSide);
    public byte getMetaIDAtSideAndDistance(byte aSide, int aDistance);
    
	public byte getLightLevel(int aX, int aY, int aZ);
    public byte getLightLevelOffset(int aX, int aY, int aZ);
    public byte getLightLevelAtSide(byte aSide);
    public byte getLightLevelAtSideAndDistance(byte aSide, int aDistance);
    
	public boolean getSky(int aX, int aY, int aZ);
    public boolean getSkyOffset(int aX, int aY, int aZ);
    public boolean getSkyAtSide(byte aSide);
    public boolean getSkyAtSideAndDistance(byte aSide, int aDistance);
    
    public BiomeGenBase getBiome(int aX, int aZ);

    /**
     * Function of the regular TileEntity
     */
    public void writeToNBT(NBTTagCompound aNBT);
    
    /**
     * Function of the regular TileEntity
     */
    public void readFromNBT(NBTTagCompound aNBT);
    
    /**
     * Function of the regular TileEntity
     */
    public boolean isInvalid();
    
	/**
	 * Opens GUI-ID of any Mod
	 */
	public void openGUI(EntityPlayer aPlayer, int aID, Object aMod);
}