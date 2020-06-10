# General
### Renames
`IBlockAccess` renamed to `IBlockReader`

`EntityPlayer` renamed to `PlayerEntity`

`NBTTagCompound` renamed to `CompoundNBT`

`EnumFacing` renamed to `Direction`

`InventoryCrafting` renamed to `CraftingInventory`

`@SideOnly` renamed to `@OnlyIn`

`Side` renamed to `Dist`

`EnumHand` renamed to `Hand`

`EntityLivingBase` renamed to `LivingEntity`

`EnumActionResult` renamed to `ActionResultType`

`EnumDyeColor` renamed to `DyeColor`

`I18n.translateToLocal` renamed to `I18n.format`

`EnumHand` renamed to `Hand`

### Replaced stuff

WorldProvider removed - probably replaced by Dimension

## Other

TileEntity needs default constructor

Container wants an ContainerType and an id (bad for ContainerNull)