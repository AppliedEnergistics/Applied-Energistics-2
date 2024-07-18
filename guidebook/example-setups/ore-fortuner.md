---
navigation:
  parent: example-setups/example-setups-index.md
  title: Automatic Ore Fortuner
  icon: minecraft:raw_iron
---

# Automation of Ore Fortuning

The <ItemLink id="annihilation_plane" /> can be enchanted with any pickaxe enchantment, including fortune, so an obvious use case is to
apply fortune to a few, and have <ItemLink id="formation_plane" />s and <ItemLink id="annihilation_plane" />s rapidly place and
break ores.

Note that since <ItemLink id="import_bus" />ses "spin up to speed", the setup will start slow then reach full speed in a few seconds.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/ore_fortuner.snbt" />

  <BoxAnnotation color="#dddddd" min="2.7 0 2" max="3 1 3">
        (1) Import Bus: Has a few Acceleration Cards in it.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 2" max="2 1 2.3">
        (2) Formation Planes: In their default configuration.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 0.7" max="2 1 1">
        (3) Annihilation Planes: No GUI to configure, but enchanted with Fortune.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 0 0" max="3 1 1">
        (4) Storage Bus: In its default configuration.
  </BoxAnnotation>

<DiamondAnnotation pos="3.5 0.5 2.5" color="#00ff00">
        Input
    </DiamondAnnotation>

<DiamondAnnotation pos="3.5 0.5 0.5" color="#00ff00">
        Output
    </DiamondAnnotation>

<DiamondAnnotation pos="4 0.5 1.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

*   The <ItemLink id="import_bus" /> (1) has a few <ItemLink id="speed_card" />s in it. More are required the more formation planes
    are in the array, as they make the import bus pull more items at once.
*   The <ItemLink id="formation_plane" />s (2) are in their default configurations.
*   The <ItemLink id="annihilation_plane" />s (3) have no GUI and cannot be configured, but are enchanted with fortune.
*   The <ItemLink id="storage_bus" /> (4) is in its default configuration.

## How It Works

1.  The <ItemLink id="import_bus" /> on the green subnet imports blocks from the first barrel into [network storage](../ae2-mechanics/import-export-storage.md)
2.  The only storage on the green subnet is the <ItemLink id="formation_plane" />, which places the blocks.
3.  The <ItemLink id="annihilation_plane" /> on the orange subnet breaks the blocks, applying fortune to them.
4.  The <ItemLink id="storage_bus" /> on the orange subnet stores the results of the breaking in the second barrel.
