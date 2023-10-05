---
navigation:
  parent: example-setups/example-setups-index.md
  title: Advanced Certus Farm
  icon: certus_quartz_crystal
  position: 120
---

# Advanced Certus Farm

This is basically just the [Semi-Auto Certus Farm](semiauto-certus-farm.md), except it has been fully integrated into your
ME system.

Instead of having a big stockpile of budding blocks and manually refreshing them every once in a while,
this setup uses [Charger Automation](charger-automation.md) and [Throwing-In-Water Automation](throw-in-water-automation.md)
to do it automatically.

**THIS IS A COMPLEX BUILD WITH STUFF HIDDEN BEHIND OTHER STUFF, PAN AROUND TO VIEW IT FROM ALL ANGLES**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/advanced_certus_farm.snbt" />

  <BoxAnnotation color="#ddaaaa" min="3.7 2 1" max="4 3 2">
        (1) Annihilation Plane #1: No GUI to configure, but can be enchanted with Fortune.
  </BoxAnnotation>

  <BoxAnnotation color="#ddaaaa" min="2 2 1.7" max="3 3 2">
        (2) Storage Bus #1: Filtered to Certus Quartz Crystal.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 2.5 1.5" color="#ff0000">
    Cluster Breaker Subnet
  </DiamondAnnotation>

  <BoxAnnotation color="#aaddaa" min="3.7 1 1" max="4 2 2">
        (3) Annihilation Plane #2: No GUI to configure, but enchanted with Silk Touch.
  </BoxAnnotation>

  <BoxAnnotation color="#aaddaa" min="2 1 1.7" max="3 2 2">
        (4) Storage Bus #2: Filtered to Certus Quartz Block.
        <BlockImage id="quartz_block" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 1.5 1.5" color="#00ff00">
    Certus Block Breaker Subnet
  </DiamondAnnotation>

  <BoxAnnotation color="#ffddaa" min="4 0.7 1" max="5 1 2">
        (5) Formation Plane: In its default configuration.
  </BoxAnnotation>

  <BoxAnnotation color="#ffddaa" min="2 0.7 2" max="3 1 3">
        (6) Import Bus: Filtered to Flawed Budding Certus Quartz.
        <BlockImage id="flawed_budding_quartz" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 0.5 1.5" color="#ddcc00">
    Budding Block Placer Subnet
  </DiamondAnnotation>

  <BoxAnnotation color="#aaaadd" min="1.7 2 2" max="2 3 3">
        (7) Storage Bus #3: Filtered to Certus Quartz Crystal. Has priority set higher than your main storage.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#aaaadd" min="2 1 2" max="3 2 3">
        (8) Interface: Set to keep 1 Flawed Budding Certus Quartz in itself, has a Crafting Card.
        <Row><BlockImage id="flawed_budding_quartz" scale="2" /> <ItemImage id="crafting_card" scale="2" /></Row>
  </BoxAnnotation>

<DiamondAnnotation pos="1.5 0.5 0" color="#00ff00">
        To Main Network, Charger Automation, and Throwing-In-Water Automation
        <Row>
        <GameScene zoom="3" background="transparent">
          <ImportStructure src="../assets/assemblies/charger_automation.snbt" />
          <IsometricCamera yaw="195" pitch="30" />
        </GameScene>
        <GameScene zoom="3" background="transparent">
          <ImportStructure src="../assets/assemblies/throw_in_water.snbt" />
          <IsometricCamera yaw="195" pitch="30" />
        </GameScene>
        </Row>
    </DiamondAnnotation>

  <IsometricCamera yaw="165" pitch="5" />
</GameScene>

## Configurations

### Cluster Breaker:

* The first <ItemLink id="annihilation_plane" /> (1) has no GUI and cannot be configured, but can be enchanted with Fortune.
* The first <ItemLink id="storage_bus" /> (2) is filtered to <ItemLink id="certus_quartz_crystal" />.

### Certus Block Breaker:

* The second <ItemLink id="annihilation_plane" /> (3) has no GUI and cannot be configured, but must be enchanted with Silk Touch.
* The second <ItemLink id="storage_bus" /> (4) is filtered to <ItemLink id="quartz_block" />.

### Budding Block Placer:

* The <ItemLink id="formation_plane" /> (5) is in its default configuration.
* The <ItemLink id="import_bus" /> (6) is filtered to <ItemLink id="flawed_budding_quartz" />.

### On Main Network:

* The third <ItemLink id="storage_bus" /> (7) is filtered to <ItemLink id="certus_quartz_crystal" />, and has its
  [priority](../ae2-mechanics/import-export-storage.md#storage-priority) set higher than your main storage.
* The <ItemLink id="interface" /> (8) is set to keep 1 Flawed Budding Certus Quartz in itself, and has a <ItemLink id="crafting_card" />.

## How It Works

### Cluster Breaker:

The cluster breaker subnet works very similarly to the subnet in the [simple certus farm](simple-certus-farm.md).

1. The <ItemLink id="annihilation_plane" /> attempts to break what is in front of it, but can only break <ItemLink id="quartz_cluster" />
   because the only storage on the subnet is the <ItemLink id="storage_bus" />, filtered to <ItemLink id="certus_quartz_crystal" />.
2. The <ItemLink id="storage_bus" /> stores the certus quartz crystals in the barrel.

### Certus Block Breaker

The certus block breaker subnet serves to break the depleted budding block once it turns into a plain <ItemLink id="quartz_block" />.
It works similarly to the cluster breaker.

1. The <ItemLink id="annihilation_plane" /> attempts to break what is in front of it, but can only break <ItemLink id="quartz_block" />
   because the only storage on the subnet is the <ItemLink id="storage_bus" />, filtered to <ItemLink id="quartz_block" />. 
   The plane needs to have silk touch, so the budding block won't degrade upon being broken, and thus the plane won't break it prematurely.
2. The <ItemLink id="storage_bus" /> stores the certus quartz block in the <ItemLink id="interface" />, allowing the
   [Throwing-In-Water Automation](throw-in-water-automation.md) to use it to make a new <ItemLink id="flawed_budding_quartz" />.

### Budding Block Placer

The budding block placer subnet serves to place a new <ItemLink id="flawed_budding_quartz" /> when the breaker subnet breaks the old depleted one.

1. The <ItemLink id="import_bus" /> Imports a budding block from the <ItemLink id="interface" /> into [network storage](../ae2-mechanics/import-export-storage.md)
2. The only storage on the subnet is the <ItemLink id="formation_plane" />, which places the budding block.

### On the Main Network

* The <ItemLink id="storage_bus" /> gives the main network (and also the [Charger Automation](charger-automation.md)) access to all of the certus quartz crystals in the barrel. It is set to
  high [priority](../ae2-mechanics/import-export-storage.md#storage-priority) so that certus quartz crystals are preferentially
  put back in the barrel instead of in your main storage.
* The <ItemLink id="interface" /> gives the budding block placer subnet access to a <ItemLink id="flawed_budding_quartz" />, and
    gives the certus block breaker subnet a way to get the depleted blocks back into the main network. The
    <ItemLink id="crafting_card" /> allows the interface to request new budding blocks from the main network's [autocrafting](../ae2-mechanics/autocrafting.md).