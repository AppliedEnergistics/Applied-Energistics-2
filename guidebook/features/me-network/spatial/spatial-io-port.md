---
categories:
  - ME Network/Spatial
item_ids:
  - ae2:spatial_io_port
navigation:
  title: Spatial IO Port
---

![A picture of a Spatial IO
Port](../../../assets/large/spatial_io_port.png)<ItemLink
id="spatial_io_port"/> are used to capture and
deploy regions of space that are defiend by <ItemLink
id="spatial_pylon"/>.

To Capture/Deploy a region of space you must first construct a [Spatial
Containtment Structure](spatial-containment-structure.md), once
constructed and ready your <ItemLink
id="spatial_io_port"/> will show your required
power, and current power, the next step would be to adjust your
[SCS](spatial-containment-structure.md) design, or to build and
power your required <ItemLink id="energy_cell"/>
or <ItemLink id="dense_energy_cell"/> to meet
the demands of the <ItemLink
id="spatial_io_port"/>.

Once power is available and your [SCS](spatial-containment-structure.md) is valid, you need to insert a <ItemLink
id="spatial_storage_cell_2"/>, <ItemLink
id="spatial_storage_cell_16"/>, or

<ItemLink id="spatial_storage_cell_128" />
depending on the required size you may need a larger or small storage cell.

When everything is ready, and the storage cell is placed inside the <ItemLink
id="spatial_io_port"/> applying a redstone
signal to the <ItemLink id="spatial_io_port"/>
will trigger the capture/deployment of the cell into the [SCS](spatial-containment-structure.md).

Requires a [channel](../channels.md) to function.

<RecipeFor id="spatial_io_port" />
