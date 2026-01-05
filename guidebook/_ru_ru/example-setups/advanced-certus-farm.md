---
navigation:
  parent: example-setups/example-setups-index.md
  title: Продвинутая ферма истинного кварца
  icon: certus_quartz_crystal
  position: 120
---

# Продвинутая ферма истинного кварца

По сути, это просто [полуавтоматическая ферма истинного кварца](semiauto-certus-farm.md), за исключением того, что она полностью интегрирована в вашу
ME Систему.

Вместо того, чтобы хранить большой запас цветущих блоков и вручную обновлять их время от времени,
эта установка использует [автоматизацию зарядки](charger-automation.md) и [автоматизацию бросания в воду](throw-in-water-automation.md),
чтобы делать это автоматически.

** ЭТО СЛОЖНАЯ КОНСТРУКЦИЯ, В КОТОРОЙ ЭЛЕМЕНТЫ СКРЫТЫ ЗА ДРУГИМИ ЭЛЕМЕНТАМИ, ПОВЕРНИТЕ ЕЁ, ЧТОБЫ РАССМОТРЕТЬ ИХ СО ВСЕХ СТОРОН.**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/advanced_certus_farm.snbt" />

  <BoxAnnotation color="#ddaaaa" min="3.7 2 1" max="4 3 2">
        (1) Плоскость уничтожения #1: без GUI для конфигурации, но может быть с зачарованием удачи.
  </BoxAnnotation>

  <BoxAnnotation color="#ddaaaa" min="2 2 1.7" max="3 3 2">
        (2) ME Шина хранения #1: фильтруется по кристаллу истинного кварца.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 2.5 1.5" color="#ff0000">
    Подсеть ломания друзы
  </DiamondAnnotation>

  <BoxAnnotation color="#aaddaa" min="3.7 1 1" max="4 2 2">
        (3) Плоскость уничтожения #2: без GUI для конфигурации, но с зачарованием шёлкового касания.
  </BoxAnnotation>

  <BoxAnnotation color="#aaddaa" min="2 1 1.7" max="3 2 2">
        (4) ME Шина хранения #2: фильтруется по блоку истинного кварца.
        <BlockImage id="quartz_block" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 1.5 1.5" color="#00ff00">
    Подсеть ломания блока истинного кварца
  </DiamondAnnotation>

  <BoxAnnotation color="#ffddaa" min="4 0.7 1" max="5 1 2">
        (5) Плоскость формирования: в конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#ffddaa" min="2 0.7 2" max="3 1 3">
        (6) ME Шина импорта: фильтруется по испорченному цветущему истинному кварцу.
        <BlockImage id="flawed_budding_quartz" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 0.5 1.5" color="#ddcc00">
    Подсеть размещения цветущего юлока
  </DiamondAnnotation>

  <BoxAnnotation color="#aaaadd" min="1.7 2 2" max="2 3 3">
        (7) ME Шина хранения #3: фильтруется по кристаллу истинного кварца. Имеет более высокий приоритет, чем у вашего основного хранилища.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#aaaadd" min="2 1 2" max="3 2 3">
        (8) Интерфейс: Настроен хранить в себе 1 цветущий блок истинного кварца, имеет карту создания.
        <Row><BlockImage id="flawed_budding_quartz" scale="2" /> <ItemImage id="crafting_card" scale="2" /></Row>
  </BoxAnnotation>

<DiamondAnnotation pos="1.5 0.5 0" color="#00ff00">
        К основной сети, автоматизации зарядки и автоматизации бросания в воду
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

## Конфигурации

### Ломание друзы:

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет GUI и не может быть настроена, но может быть с зачарованием удачи.
* Первая <ItemLink id="storage_bus" /> (2) фильтруется по <ItemLink id="certus_quartz_crystal" />.

### Ломание блока истинного кварца:

* Вторая <ItemLink id="annihilation_plane" /> (3) не имеет GUI и не может быть настроена, но должна быть с зачарованием шёлкового касания.
* Вторая <ItemLink id="storage_bus" /> (4) фильтруется по <ItemLink id="quartz_block" />.

### Размещение цветущего блока:

* <ItemLink id="formation_plane" /> (5) находится в конфигурации по умолчанию.
* <ItemLink id="import_bus" /> (6) фильтруется по <ItemLink id="flawed_budding_quartz" />.

### В основной сети:

* Третья <ItemLink id="storage_bus" /> (7) фильтруется по <ItemLink id="certus_quartz_crystal" />, и имеет
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) выше, чем у вашего основного хранилища.
* <ItemLink id="interface" /> (8) настроен на хранение 1 цветущего блока истинного кварца в себе и имеет <ItemLink id="crafting_card" />.

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
2. <ItemLink id="storage_bus" /> сохраняет блок истинного кварца в <ItemLink id="interface" />, позволяя
   [автоматизации бросания в воду](throw-in-water-automation.md) использовать его для создания нового <ItemLink id="flawed_budding_quartz" />.

### Размещение цветущего блока

Подсеть размещения цветущего блока служит для размещения нового <ItemLink id="flawed_budding_quartz" />, когда подсеть ломания ломает старый истощённый.

1. <ItemLink id="import_bus" /> импортирует цветущий блок из <ItemLink id="interface" /> в [сетевое хранилище](../ae2-mechanics/import-export-storage.md).
2. Единственным хранилищем в подсети является <ItemLink id="formation_plane" />, куда помещается цветущий блок.

### В основной сети

* <ItemLink id="storage_bus" /> предоставляет основной сети (а также [автоматизации зарядки](charger-automation.md)) доступ ко всем кристаллам истинного кварца в бочке. Ей присвоен
  высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы кристаллы истинного кварца приоритетно
  помещались обратно в бочку, а не в основное хранилище.
* <ItemLink id="interface" /> предоставляет подсети размещения цветущего блока доступ к <ItemLink id="flawed_budding_quartz" />, и
    предоставляет подсети ломания блока истинного кварца возможность вернуть использованные блоки в основную сеть.
    <ItemLink id="crafting_card" /> позволяет ME Интерфейсу запрашивать новые цветущие блоки из основной сети [автоматического создания](../ae2-mechanics/autocrafting.md).