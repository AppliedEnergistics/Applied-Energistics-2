---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Spatial IO
  icon: spatial_storage_cell_2
---

# Spatial IO

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/spatial_storage_1x1x1.snbt" />

  <BoxAnnotation color="#33dd33" min="1 1 1" max="2 2 2">
        The volume to be moved
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />

</GameScene>

Spatial IO is a way to cut-and-paste physical volumes of space in your world. It can be used to move <ItemLink id="flawless_budding_quartz" />,
have a room in your base where you can swap out various interiors to use it for different purposes, or even move
the end portal!

It works by *swapping* the defined volume with an identically-sized volume in the spatial storage dimension, sending whatever's
in the pylon array to the spatial storage dimension, and whatever's in the dimension to the pylon array.

This means that if you have a way of travelling between dimensions (spatial IO *can* be used to make a teleporter,
but doing it is very complex, a bit janky, and beyond the scope of the guide), you can use them like custom-sized compact machines or pocket
dimensions.

# The Multiblock Setup

Spatial IO requires a specific arrangement of its components in order to function, and define the volume to be cut-and-pasted.

All of the components must be on the same [network](me-network-connections.md) in order to function, and you can have only one
spatial IO setup on a network. Thus, a [subnetwork](subnetworks.md) is recommended.

## The Spatial IO Port

<BlockImage id="spatial_io_port" p:powered="true" scale="4" />

The <ItemLink id="spatial_io_port" /> controls the spatial IO operation. It shows stats on the multiblock setup, and holds
the [spatial cells](../items-blocks-machines/spatial_cells.md)

It shows
- Stored and max [energy](energy.md) in the network
- Required energy to perform the operation. This can be quite large and is used instantaneously, so make sure you have enough
  [energy cells](../items-blocks-machines/energy_cells.md) to hold it all.
- Efficiency of the pylon array
- Size of the defined volume

To perform a spatial IO operation, place a spatial storage cell in the input slot and give the spatial IO port a redstone pulse.
It will then *swap* the volume in the pylons with the volume in the spatial storage dimension. This means that if you send some
set of blocks to the spatial storage dimension, *then put another set of blocks in the pylons*, put the cell back in the input slot,
and trigger the IO port again, the 2nd set of blocks will disappear and the 1st set of blcks will reappear.

**BE CAREFUL, Any entity in the defined volume, including you, will be carried along, and if you have no way of getting out, you will be trapped
in the spatial storage dimension, in a dark, featureless box.** Use this to prank your friends!

## Pylons

<BlockImage id="spatial_pylon" p:powered_on="true" scale="4" />

<ItemLink id="spatial_pylon" />s are the main part of a spatial IO setup, and define the volume to be affected.

The volume is defined by the bounding box of the outside of the pylons, contracted in by 1 block in all directions.

The rules are:
- Minimum size of 3x3x3 (which defines a 1x1x1 volume)
- All spatial pylons must be in the outside bounding box
- All spatial pylons must be on the same network
- All pylons must be at least 2 blocks long

For example, say you want to define a 3x3x3 volume. Following rule 2, all of the pylons must be within a 5x5x5 shell around
the volume you want to define. They can be in almost any configuration, as long as they're contained within that 1-block-thick
5x5x5 shell.

<GameScene zoom="4" interactive={true}>
<ImportStructure src="../assets/assemblies/spatial_storage_3x3x3_pylon_demonstration.snbt" />

<BoxAnnotation color="#33dd33" min="1 1 1" max="4 4 4">
        The volume to be moved
  </BoxAnnotation>

<BoxAnnotation color="#3333ff" min="5 5 0" max="0 0 5">
  </BoxAnnotation>

<IsometricCamera yaw="195" pitch="30" />
</GameScene>

A more reasonable setup is this:

<GameScene zoom="4" interactive={true}>
<ImportStructure src="../assets/assemblies/better_spatial_storage_3x3x3.snbt" />

<BoxAnnotation color="#33dd33" min="1 1 1" max="4 4 4">
        The volume to be moved
  </BoxAnnotation>

<BoxAnnotation color="#3333ff" min="5 5 0" max="0 0 5">
  </BoxAnnotation>

<IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Efficiency

The efficiency of the pylon array depends on the amount of the shell that you fill. Minimal setups around large volumes
will be very inefficient and possibly require *billions* of AE.

## Cell Dimensions

Once a [spatial cell](../items-blocks-machines/spatial_cells.md) has been used, it gains a permanently defined set of XYZ dimensions (eg, 3x4x2)
and is linked to a volume of space in the spatial storage dimension. **YOU CANNOT RESET, REFORMAT, OR RESIZE A SPATIAL CELL AFTER
IT HAS BEEN USED.** Make a new cell if you want to use different dimensions. 

These are not the same dimensions in the name of a cell, a 16^3 cell can have any dimensions *up to* 16x16x16

Note that this volume is directional and cannot be rotated. A 2x2x3 volume is not the same as a 3x2x2 volume, even though they're the
same size.

if the XYZ dimensions of a cell do not match the defined volume (seen in the IO port), the IO port will not operate.