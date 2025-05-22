---
navigation:
  parent: example-setups/example-setups-index.md
  title: Item/Fluid "Pipe" Subnet
  icon: storage_bus
---

# Item/Fluid "Pipe" Subnet

A simple method of emulating an item and/or fluid pipe with AE2 [devices](../ae2-mechanics/devices.md), useful for, well, anything you'd use an item or fluid pipe for.
This includes returning the result of a craft to a <ItemLink id="pattern_provider" />.

There are generally two different methods of achieving this:

## Import Bus -> Storage Bus

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_storage_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        (1) Import Bus: Can be filtered.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        (2) Storage Bus: Can be filtered. This (and other storage busses you want to be a destination)
        must be the only storage on the network.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#00ff00">
        Source
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
        Destination
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

The <ItemLink id="import_bus" /> (1) on the source inventory imports the items or fluid, and attempts to store them in [network storage](../ae2-mechanics/import-export-storage.md).
Since the only storage on the network is the <ItemLink id="storage_bus" /> (2) (which is why this is a subnet and not on your main network), the items or fluid
are placed in the destination inventory, thus being transferred. Energy is provided through a <ItemLink id="quartz_fiber" />.
Both the import bus and storage bus can be filtered, but the setup will transfer everything it can access if no filters are applied.
This setup also works with multiple import busses and multiple storage busses.

## Storage Bus -> Export Bus

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/storage_export_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        (1) Storage Bus: Can be filtered. This (and other storage busses you want to be a source)
        must be the only storage on the network.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        (2) Export Bus: Must be filtered.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#00ff00">
        Source
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
        Destination
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

The <ItemLink id="export_bus" /> on the destination inventory attempts to pull items in its filter from [network storage](../ae2-mechanics/import-export-storage.md).
Since the only storage on the network is the <ItemLink id="storage_bus" /> (which is why this is a subnet and not on your main network), the items or fluid
are pulled from the source inventory, thus being transferred. Energy is provided through a <ItemLink id="quartz_fiber" />.
Because export busses must be filtered to function, this setup only operates if you filter the export bus.
This setup also works with multiple storage busses and multiple export busses.

## A Setup That Does Not Work (Import Bus -> Export Bus)

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_export_pipe.snbt" />

<BoxAnnotation color="#dd3333" min="3.7 0 0" max="4 1 1">
        Import Bus: Since the network has no storage, there is nowhere for it to import to.
  </BoxAnnotation>

<BoxAnnotation color="#dd3333" min="1 0 0" max="1.3 1 1">
        (2) Export Bus: Since the network has no storage, there is nothing for it to export.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#ff0000">
        Source
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#ff0000">
        Destination
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

A setup with just an import and export bus will not work. The import bus will attempt to pull from the source inventory
and store the items or fluid in network storage. The export bus will attempt to pull from network storage and put the
items or fluid in the destination inventory. However since this network **has no storage**, the import bus can't import
and the export bus can't export, so nothing happens.

## Inputting And Outputting Through 1 Face

Say you have some machine that can receive input and have its output pulled through 1 face. (Like a <ItemLink id="charger" />)
You can both push in the ingredients and pull out the result, by combining the 2 pipe subnet methods:

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_storage_export_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="4 1 1" max="5 1.3 2">
        (1) Import Bus: Can be filtered.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 1 1" max="3 1.3 2">
        (2) Storage Bus: Can be filtered. This (and other storage busses you want to push and pull items)
        must be the only storage on the network.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0 1" max="3 1 2">
        (3) Thing You Want To Push To And Pull From: In this case a Charger.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 1" max="1 1.3 2">
        (4) Export Bus: Must be filtered.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 1.5" color="#00ff00">
        Source
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 1.5" color="#00ff00">
        Destination
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Interfaces

It turns out there are [devices](../ae2-mechanics/devices.md) besides import busses and export busses that push items into
and pull items out of [network storage](../ae2-mechanics/import-export-storage.md)!
Of relevance here is the <ItemLink id="interface" />. If an item is inserted that the interface is not set to stock, the interface will
push it to network storage, which we can exploit similarly to the import bus -> storage bus pipe. Setting an interface to
stock some item will pull it from network storage, similar to the storage bus -> export bus pipe. Interfaces can be set to
stock some things and not stock others, allowing you to remotely push and pull through storage busses, if you for some reason want to do that.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/interface_pipes.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        Interface
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        Storage Bus
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.7 0 2" max="4 1 3">
        Storage Bus
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 2" max="1 1.3 3">
        Storage Bus
  </BoxAnnotation>

<IsometricCamera yaw="195" pitch="30" />
</GameScene>

## One-To-Many and Many-To One (and many-to-many)

Of course, you don't have to use just one <ItemLink id="import_bus" /> or <ItemLink id="export_bus" /> or <ItemLink id="storage_bus" />

<GameScene zoom="3" background="transparent">
<ImportStructure src="../assets/assemblies/many_to_many_pipe.snbt" />

<IsometricCamera yaw="185" pitch="30" />
</GameScene>

## Providing To Multiple Places

From all this, we can derive a method to send ingredients from one <ItemLink id="pattern_provider" /> face to many different
locations, like an array of machines, or several different faces of one machine.

We don't want an import -> storage pipe or a storage -> export pipe because the <ItemLink id="pattern_provider" /> never
actually contains the ingredients. Instead, providers *push* the ingredients to adjacent inventories, so we need some 
adjacent inventory that can also import items.

This sounds like... an <ItemLink id="interface" />!
Make sure the provider is in directional or flat subpart mode and/or the interface is in flat subpart mode, so the two don't form a network
connection.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/provider_interface_storage.snbt" />

<BoxAnnotation color="#dddddd" min="2.7 0 1" max="3 1 2">
        Interface (must be flat, not fullblock)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 4">
        Storage Busses
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 0 0" max="1 1 4">
        Places you want to pattern-provide to (multiple machines, or multiple faces of 1 machine)
  </BoxAnnotation>

<IsometricCamera yaw="185" pitch="30" />
</GameScene>