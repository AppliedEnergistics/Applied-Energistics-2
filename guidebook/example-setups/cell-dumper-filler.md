---
navigation:
  parent: example-setups/example-setups-index.md
  title: Cell Dumper or Filler
  icon: io_port
---

# Cell Dumper Or Filler

One might ask "How do I quickly empty a cell into a chest or drawer array or backpack, or, inversely, fill a cell from the same?"

The answer is use of an <ItemLink id="io_port" /> and some subnetting to restrict where it can put the items, or pull items from.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/cell_dumper_filler.snbt" />

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 2 1">
        (1) IO Port: Can be set to either "Transfer data to Network" or "Transfer data to Storage Cell" using the arrow button
        in the middle of the GUI. Has 3 Acceleration Cards.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 0.7 0" max="1 1 1">
        (2) Storage Bus: In its default configuration.
  </BoxAnnotation>

<BoxAnnotation color="#33dd33" min="0 1 0" max="1 2 1">
        Place whatever you want to fill or empty here.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0.35 0.35" max="2.3 0.65 0.65">
        Quartz Fiber: Only needed if the energy source is another network.
  </BoxAnnotation>

<DiamondAnnotation pos="3 0.5 0.5" color="#00ff00">
        To some energy source, like another network, or an energy acceptor.
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

* The <ItemLink id="io_port" /> (1) can be set to either "Transfer data to Network" or "Transfer data to Storage Cell" using the arrow button
  in the middle of the GUI. It has 3 acceleration cards for maximum speed.
* The <ItemLink id="storage_bus" /> (2) is in its default configuration.

## How It Works

### In "Transfer To Network" Mode

1. The <ItemLink id="io_port" /> attempts to dump the contents of the inserted [storage cell](../items-blocks-machines/storage_cells.md).
    into [network storage](../ae2-mechanics/import-export-storage.md).
2. The only storage on the subnet is the <ItemLink id="storage_bus" />, which stores the items or fluids or etc. in whatever
    you put in front of it.
* The <ItemLink id="energy_cell" /> provides a large enough buffer of [energy](../ae2-mechanics/energy.md) that the network does
    not run out from the power draw of transferring so many items per gametick.

### In "Transfer To Storage Cell" Mode

1. The <ItemLink id="io_port" /> attempts to dump the contents of the [network's storage](../ae2-mechanics/import-export-storage.md)
   into the inserted [storage cell](../items-blocks-machines/storage_cells.md).
2. The only storage on the subnet is the <ItemLink id="storage_bus" />, which pulls the items or fluids or etc. out of whatever
   you put in front of it.
* The <ItemLink id="energy_cell" /> provides a large enough buffer of [energy](../ae2-mechanics/energy.md) that the network does
  not run out from the power draw of transferring so many items per gametick.