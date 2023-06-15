---
navigation:
  parent: example-setups/example-setups-index.md
  title: Specialized Local Storage
  icon: drive
---

# Specialized Local Storage

Utilizing one of the [special behaviors of the Interface](../items-blocks-machines/interface.md#special-interactions), a
[subnetwork](../ae2-mechanics/subnetworks.md) can present the contents of its storage to the main network, without being able
to see the main network's storage, and taking up only 1 [channel](../ae2-mechanics/channels.md).

This is useful for local storage at some farm, so that the items will not overflow into your main storage.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/local_storage.snbt" />

<BoxAnnotation color="#dddddd" min="4 0 0" max="5 2 1">
        (1) Some method of importing items (in this case an interface)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 0 0" max="4 1 1">
        (2) Drive: Has some cells in it. The cells should be filtered to whatever the farm outputs.
        The cells can have Equal Distribution Cards and Overflow Destruction Cards.
        <Row><ItemImage id="item_storage_cell_4k" scale="2" /> <ItemImage id="equal_distribution_card" scale="2" /> <ItemImage id="void_card" scale="2" /></Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 1 0" max="4 2 0.3">
        (3) Crafting Terminal: This can see the contents of the Drive on the subnet, but not the contents of your main network's storage.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0 0" max="2.3 1 1">
        (4) Interface #2: In its default configuration.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1.7 0 0" max="2 1 1">
        (5) Storage Bus: Has priority set higher than the main storage, can be filtered to whatever the farm outputs.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 2 0.3">
        Crafting Terminal: This can see both the contents of the main network's storage *and* the subnetwork.
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

* The first <ItemLink id="interface" /> (1) simply accepts items from whatever farm you have and pushes them into the subnet.
* The <ItemLink id="drive" /> (2) has some [cells](../items-blocks-machines/storage_cells.md) in it. The cells should be
  [partitioned](../items-blocks-machines/cell_workbench.md) to whatever the farm outputs.
  The cells can have <ItemLink id="equal_distribution_card" />s and <ItemLink id="void_card" />s.
* The second <ItemLink id="interface" /> (4) is in its default configuration.
* The <ItemLink id="storage_bus" /> has its [priority](../ae2-mechanics/import-export-storage.md#storage-priority) set
  higher than the main storage. It can be filtered to whatever the farm outputs.

## How It Works

* The <ItemLink id="interface" /> on the subnet shows the <ItemLink id="storage_bus" /> on the main network the contents of
the <ItemLink id="drive" />. This means the storage bus can directly pull items from and push items to the cells in the drive.
* The storage bus is set to high [priority](../ae2-mechanics/import-export-storage.md#storage-priority) so that items are preferentially
  put back in the subnet instead of in your main storage.
* Importantly, if the cells in the subnet fill up, the items will not overflow into the main network. If the farm is of a type
that breaks if it backs up, <ItemLink id="void_card" />s can be used to delete the excess items instead. 
* If the farm outputs multiple items, <ItemLink id="equal_distribution_card" />s can stop one item from filling all the cells
and not letting the other items be stored.