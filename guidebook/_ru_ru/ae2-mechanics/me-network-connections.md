---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Сетевые подключения
  icon: fluix_glass_cable
---

# Сетевые подключения

## Что означает «Сеть»?

«Сеть» — это группа [устройств](../ae2-mechanics/devices.md)соединённых блоками, которые могут передавать [каналы](../ae2-mechanics/channels.md),
например, [кабели](../items-blocks-machines/cables.md) или полноблочные машины и [устройства](../ae2-mechanics/devices.md). 
(<ItemLink id="charger" />, <ItemLink id="interface" />, <ItemLink id="drive" />, и т. д.)
Технически отдельный кабель — это, по сути, сеть.

## Доп. замечание о позиционировании устройства

Для [устройств](../ae2-mechanics/devices.md), выполняющих определённую сетевую функцию (например, <ItemLink id="interface" />
для помещения и извлечения предметов из [сетевого хранилища](../ae2-mechanics/import-export-storage.md), <ItemLink id="level_emitter" />
для чтения содержимого сетевого хранилища, <ItemLink id="drive" /> для использования в качестве сетевого хранилища и т. д.),
физическое положение устройства не имеет значения.

Опять же, **физическое положение устройства не имеет значения**. Важно лишь, что оно подключено к сети
(и, конечно же, к какой именно сети оно подключено).

## Сетевые подключения

Простой способ определить, что подключено к сети, — использовать <ItemLink id="network_tool" />. Он покажет все
компоненты сети, поэтому, если вы увидите то, чего не должно быть, или не увидите то, что должно быть, у вас проблема.

Например, это 2 отдельные сети.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/2_networks_1.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1 2 2">
        1 Сеть
  </BoxAnnotation>

<BoxAnnotation color="#915dcd" min="2 0 0" max="3 2 2">
        2 Сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Это также две отдельные сети, поскольку <ItemLink id="quartz_fiber" /> делится [энергией](../ae2-mechanics/energy.md)
без предоставления сетевого соединения.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/2_networks_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1 2 2">
        1 Сеть
  </BoxAnnotation>

  <BoxAnnotation color="#915dcd" min="1.3 0 0" max="3 2 2">
        2 Сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Однако это всего лишь одна сеть, а не две отдельные. [Квантовый мост](../items-blocks-machines/quantum_bridge.md) действует как
беспроводной [покрытый кабель](../items-blocks-machines/cables.md#dense-cable), поэтому оба конца находятся в одной сети.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/actually_1_network.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="7 3 3">
        Всё 1 сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Это также всего лишь одна сеть, поскольку цвет [кабеля](../items-blocks-machines/cables.md) не имеет никакого отношения к сетевым соединениям, за исключением того, что кабели разных цветов не
соединяются друг с другом. Все цвета подключаются к флюисовым (или «неокрашенным») кабелям.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/actually_1_network_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="4 2 2">
        Всё 1 сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Менее интуитивные подключения

В данном случае это всего лишь одна сеть, поскольку <ItemLink id="pattern_provider" />, будучи полноблочным устройством, действует как
кабель, а <ItemLink id="inscriber" /> выполняет аналогичные функции. Таким образом, сетевое соединение проходит через
поставщик и высекатель.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_network_connection_1.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="4 2 2">
        Всё 1 сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Чтобы предотвратить это (полезно для многих настроек автоматического создания, включающих [подсети](../ae2-mechanics/subnetworks.md)),
вы можете щёлкнуть [ПКМ] по поставщику с помощью <ItemLink id="certus_quartz_wrench" />, чтобы сделать его направленным. В этом случае он
не будет пропускать каналы через одну сторону.

<Row gap="40">
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_network_connection_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="2 2 2">
        1 Сеть
  </BoxAnnotation>

  <BoxAnnotation color="#915dcd" min="2 0 0" max="4 2 2">
        2 Сеть
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_directional_connection.snbt" />

  <BoxAnnotation color="#ee3333" min="1 .3 .3" max="1.3 .7 .7">
        Обратите внимание, что кабель не подключается
  </BoxAnnotation>

  <IsometricCamera yaw="255" pitch="30" />
</GameScene>
</Row>

Другие части, не обеспечивающие направленные сетевые соединения, — это большинство [подчастей](../ae2-mechanics/cable-subparts.md)
[устройств](../ae2-mechanics/devices.md), таких как <ItemLink id="import_bus" />, <ItemLink id="storage_bus" />, и
<ItemLink id="cable_interface" />.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/subpart_no_connection.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>