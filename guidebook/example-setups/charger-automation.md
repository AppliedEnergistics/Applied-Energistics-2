---
navigation:
  parent: example-setups/example-setups-index.md
  title: Charger Automation
  icon: charger
---

# Charger Automation

Automation of a <ItemLink id="charger" /> is fairly simple. A pattern provider pushes the ingredient into the charger, then a [pipe subnet](pipe-subnet.md)
or other item pipe pushes the result back into the provider.

<GameScene zoom="6">
  <ImportStructure src="../assets/assemblies/charger_automation.snbt" />

<BoxAnnotation color="#dddddd" x1="1" x2="2" y1="0" y2="1" z1="0" z2="1">
        (1) Pattern Provider: In its default configuration, with the relevant processing patterns. Also provides the charger with power.
        ![Charger Pattern](../assets/diagrams/charger_pattern_small.png)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" x1="0" x2="1" y1="1" y2="1.3" z1="0" z2="1">
        (2) Import Bus: In its default configuration.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" x1="1" x2="2" y1="1" y2="1.3" z1="0" z2="1">
        (3) Storage Bus: In its default configuration.
  </BoxAnnotation>

<DiamondAnnotation x="4" y="0.5" z="0.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

* The <ItemLink id="pattern_provider" /> (1) is in its default configuration, with the relevant <ItemLink id="processing_pattern" />s.
  It also provides the <ItemLink id="charger" /> with [energy](../ae2-mechanics/energy.md) because it acts like a [cable](../items-blocks-machines/cables.md).
  
    ![Charger Pattern](../assets/diagrams/charger_pattern.png)

* The <ItemLink id="import_bus" /> (2) is in its default configuration.
* The <ItemLink id="storage_bus" /> (3) is in its default configuration.

## How It Works

1. The <ItemLink id="pattern_provider" /> pushes the ingredients into the <ItemLink id="charger" />.
2. The charger does its charging thing.
3. The <ItemLink id="import_bus" /> on the green subnet pulls the result out of the charger and attempts to store it in
   [network storage](../ae2-mechanics/import-export-storage.md).
4. The only storage on the green subnet is the <ItemLink id="storage_bus" />, which stores the resulting items in the pattern provider, returning them to the main network.
