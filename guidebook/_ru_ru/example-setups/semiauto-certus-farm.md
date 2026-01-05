---
navigation:
  parent: example-setups/example-setups-index.md
  title: Полуавтоматическая ферма истинного кварца
  icon: certus_quartz_crystal
  position: 115
---

# Полуавтоматическая ферма истинного кварца

К сожалению, для [простой фермы истинного кварца](simple-certus-farm.md) для полностью автоматической работы требуется <ItemLink id="flawless_budding_quartz" />.
Для этого требуется либо [пространственный ввод/вывод](../ae2-mechanics/spatial-io.md), либо строительство фермы на [метеорите](../ae2-mechanics/meteorites.md).

Тем не менее «Прикладная энергетика 2» может размещать и ломать блоки, так что, возможно,
ваша ферма *заменит вам цветущий истинный кварц*. (Вам придётся периодически вставлять
<ItemLink id="flawed_budding_quartz" /> во входную бочку и извлекать <ItemLink id="quartz_block" /> из отработанной
бочки с цветущим истинным кварцем)

Чтобы сделать это полностью автоматически, см. [продвинутая ферма истинного кварца](advanced-certus-farm.md).

Эта ферма немного сложнее, чем [простая ферма истинного кварца](simple-certus-farm.md), потому что она 
фактически представляет собой три отдельные установки, объединённые вместе.

** ЭТО СЛОЖНАЯ КОНСТРУКЦИЯ, В КОТОРОЙ ЭЛЕМЕНТЫ СКРЫТЫ ЗА ДРУГИМИ ЭЛЕМЕНТАМИ, ПОВОРАЧИВАЙТЕ, ЧТОБЫ РАССМОТРЕТЬ ИХ СО ВСЕХ СТОРОН.**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/semiauto_certus_farm.snbt" />

  <BoxAnnotation color="#ddaaaa" min="3.7 2 1" max="4 3 2">
        (1) Плоскость уничтожения #1: не имеет GUI для конфигурации, но может быть с зачарованием удачи.
  </BoxAnnotation>

  <BoxAnnotation color="#ddaaaa" min="2 2 1" max="2.3 3 2">
        (2) ME Шина хранения #1: фильтруется по кристаллу истинного кварца.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 2.5 1.5" color="#ff0000">
    Подсеть ломания друзы
  </DiamondAnnotation>

  <BoxAnnotation color="#aaddaa" min="3.7 1 1" max="4 2 2">
        (3) Плоскость уничтожения #2: без GUI для конфигурации, но с зачарованием шёлкового касания.
  </BoxAnnotation>

  <BoxAnnotation color="#aaddaa" min="2 1 1" max="2.3 2 2">
        (4) ME Шина хранения #2: фильтруется по блоку истинного кварца.
        <BlockImage id="quartz_block" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 1.5 1.5" color="#00ff00">
    Подсеть ломания блока истинного кварца.
  </DiamondAnnotation>

  <BoxAnnotation color="#ffddaa" min="4 0.7 1" max="5 1 2">
        (5) Плоскость формирования: в конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#ffddaa" min="2 0 1" max="2.3 1 2">
        (6) ME Шина импорта: в конфигурации по умолчанию.
  </BoxAnnotation>

  <DiamondAnnotation pos="3 0.5 1.5" color="#ddcc00">
    Подсеть размещения цветущего блока истинного кварца.
  </DiamondAnnotation>

  <BoxAnnotation color="#aaaadd" min="0.7 2 1" max="1 3 2">
        (7) ME Шина хранения #3: фильтруется по кристаллу истинного кварца, имеет более высокий приоритет, чем у основного хранилища.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

    <DiamondAnnotation pos="1.5 0.5 1.5" color="#00ff00">
        Цветущий блок истинного кварца помещается вручную.
        <BlockImage id="flawed_budding_quartz" scale="2" />
    </DiamondAnnotation>

    <DiamondAnnotation pos="1.5 1.5 1.5" color="#00ff00">
        Блок истинного кварца извлекается вручную.
        <BlockImage id="quartz_block" scale="2" />
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="165" pitch="5" />
</GameScene>

## Конфигурации

### Ломание друзы:

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет GUI и не может быть настроена, но может быть с зачарованием удачи.
* Первая <ItemLink id="storage_bus" /> (2) фильтруется по <ItemLink id="certus_quartz_crystal" />.

### Ломание блока истинного кварца:

* Вторая <ItemLink id="annihilation_plane" /> (3) не имеет GUI и не может быть настроена, но должна быть с зачарованием шёлкового касания.
* Вторая <ItemLink id="storage_bus" /> (4) фильтруется по <ItemLink id="quartz_block" />.

### Размещение цветущего блока:

* <ItemLink id="formation_plane" /> (5) находится в конфигурации по умолчанию.
* <ItemLink id="import_bus" /> (6) находится в конфигурации по умолчанию.

### В основной сети:

* Третья <ItemLink id="storage_bus" /> (7) фильтруется по <ItemLink id="certus_quartz_crystal" />, и имеет
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) выше, чем у вашего основного хранилища.

## Как это работает

### Ломание друзы

Подсеть ломания друзы очень похожа на подсеть в [простой ферме истинного кварца](simple-certus-farm.md).

1. <ItemLink id="annihilation_plane" /> пытается сломать то, что находится перед ней, но может сломать только <ItemLink id="quartz_cluster" />,
   поскольку единственным хранилищем в подсети является <ItemLink id="storage_bus" />, отфильтрованная по <ItemLink id="certus_quartz_crystal" />.
2. <ItemLink id="storage_bus" /> сохраняет кристаллы истинного кварца в бочке.

### Ломание блока истинного кварца

Подсеть ломания блока истинного кварца служит для ломания истощённого цветущего блока, когда он превращается в обычный <ItemLink id="quartz_block" />.
Это работает аналогично ломанию друзы.

1. <ItemLink id="annihilation_plane" /> пытается сломать то, что находится перед ней, но может сломать только <ItemLink id="quartz_block" />,
   потому что единственное хранилище в подсети — это <ItemLink id="storage_bus" />, отфильтрованная по <ItemLink id="quartz_block" />.
   Плоскость должна иметь зачарование шёлкового касания, чтобы цветущий блок не деградировал при ломании, и, следовательно, плоскость не сломает его преждевременно.
2. <ItemLink id="storage_bus" /> сохраняет блок истинного кварца в отработанной
   бочке с цветущим кварцем, вам придётся вручную бросить его в воду с помощью <ItemLink id="charged_certus_quartz_crystal" />, чтобы обновить его.

### Размещение цветущего блока

Подсеть размещения цветущего блока служит для размещения нового <ItemLink id="flawed_budding_quartz" />, когда подсеть ломания ломает старый истощённый.

1. <ItemLink id="import_bus" /> импортирует цветущий блок из входной бочки.
2. Единственным хранилищем в подсети является <ItemLink id="formation_plane" />, куда помещается цветущий блок.

### В основной сети

* <ItemLink id="storage_bus" /> предоставляет основной сети (а также [автоматизации зарядки](charger-automation.md)) доступ ко всем кристаллам истинного кварца в бочке. Ей присвоен
  высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы кристаллы истинного кварца приоритетно
  помещались обратно в бочку, а не в основное хранилище.