---
navigation:
  parent: example-setups/example-setups-index.md
  title: Advanced Certus Farm
  icon: certus_quartz_crystal
  position: 120
---

# Advanced Certus Farm

Unfortunately, the [simple certus farm](simple-certus-farm.md) requires a <ItemLink id="flawless_budding_quartz" /> to work fully
automatically. This requires either [Spatial IO](../ae2-mechanics/spatial-io.md) or building the farm at the [meteorite](../ae2-mechanics/meteorites.md).

However, AE2 can place and break blocks, and make [autocrafting](../ae2-mechanics/autocrafting.md) requests, so it might just
be possible to make your farm *replace the budding certus for you*.

This farm is a fair bit more complex than the [simple certus farm](simple-certus-farm.md), because it is actually
3 separate setups crammmed together.

It will require also constructing [Charger Automation](charger-automation.md) and [Throwing-In-Water Automation](throw-in-water-automation.md)
in order to refresh them.

**THIS IS A COMPLEX BUILD WITH STUFF HIDDEN BEHIND OTHER STUFF, PAN AROUND TO VIEW IT FROM ALL ANGLES**

<GameScene zoom="6">
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

<DiamondAnnotation pos="1.5 0.5 0" color="#00ff00">
        To Main Network, Charger Automation, and Throwing-In-Water Automation
        <Row>
        <GameScene zoom="3">
          <ImportStructure src="../assets/assemblies/charger_automation.snbt" />
          <IsometricCamera yaw="195" pitch="30" />
        </GameScene>
        <GameScene zoom="3">
          <ImportStructure src="../assets/assemblies/throw_in_water.snbt" />
          <IsometricCamera yaw="195" pitch="30" />
        </GameScene>
        </Row>
    </DiamondAnnotation>

  <IsometricCamera yaw="165" pitch="30" />
</GameScene>
