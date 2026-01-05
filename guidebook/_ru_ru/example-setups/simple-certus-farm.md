---
navigation:
  parent: example-setups/example-setups-index.md
  title: Простая ферма истинного кварца
  icon: certus_quartz_crystal
  position: 110
---

# Простая ферма истинного кварца

Как упоминалось в [выращивании истинного кварца](../ae2-mechanics/certus-growth.md), автоматизация сбора урожая <ItemLink id="certus_quartz_crystal" />
использует <ItemLink id="annihilation_plane" /> и <ItemLink id="storage_bus" />. 
<ItemLink id="growth_accelerator" /> используется для значительного ускорения роста бутонов истинного кварца, а затем плоскость
полностью ломает выросшие <ItemLink id="quartz_cluster" />. Они фильтруются, используя подозрительно удачное свойство: из незрелых
бутонов истинного кварца выпадает <ItemLink id="certus_quartz_dust" /> вместо того, чтобы ничего не выпадало.

Эта ферма работает полностью автоматически с <ItemLink id="flawless_budding_quartz" />, но с испорченным, потрескавшимся, и повреждённым
цветущим блоком истинного кварца вам придётся заменить цветущий блок вручную. Или, как описано в [полуавтоматической ферме истинного кварца](semiauto-certus-farm.md)
и [продвинутой ферме истинного кварца](advanced-certus-farm.md), автоматически.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/simple_certus_farm.snbt" />

  <BoxAnnotation color="#dddddd" min="3.7 1 1" max="4 2 2">
        (1) Плоскость уничтожения: не имеет GUI для конфигурации, но может быть с зачарованием удачи.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 1" max="3.3 2 2">
        (2) ME Шина хранения #1: фильтруется по кристаллу истинного кварца.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 .7" max="2 2 1">
        (3) ME Шина хранения #2: фильтруется по кристаллу истинного кварца, имеет более высокий приоритет, чем у основного хранилища.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="1 0.5 0.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="annihilation_plane" /> (1) не имеет GUI для конфигурации, но может быть с зачарованием удачи.
* Первая <ItemLink id="storage_bus" /> (2) фильтруется по <ItemLink id="certus_quartz_crystal" />.
* Вторая <ItemLink id="storage_bus" /> (3) фильтруется по <ItemLink id="certus_quartz_crystal" />, и имеет
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) выше, чем у основного хранилища.

## Как это работает

1. <ItemLink id="annihilation_plane" /> пытается сломать то, что находится перед ней, но может сломать только <ItemLink id="quartz_cluster" />
   потому что единственное хранилище в подсети — это <ItemLink id="storage_bus" />, отфильтрованное по <ItemLink id="certus_quartz_crystal" />.
2. Первая <ItemLink id="storage_bus" /> сохраняет кристаллы истинного кварца в бочке.
3. Вторая <ItemLink id="storage_bus" /> предоставляет основной доступ сети ко всем кристаллам истинного кварца в бочке. Ей присвоен
   высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы кристаллы истинного кварца приоритетно
   помещались обратно в бочку, а не в основное хранилище.
