---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация удачи руды
  icon: minecraft:raw_iron
---

# Автоматизация удачи руды

<ItemLink id="annihilation_plane" /> можно зачаровать любыми зачарованиями для кирки, включая удачу, поэтому очевидный вариант использования —
применить удачу к нескольким из них, и <ItemLink id="formation_plane" /> и <ItemLink id="annihilation_plane" /> будут быстро размещать и
ломать руду.

Обратите внимание, что поскольку <ItemLink id="import_bus" /> «набирает скорость», установка начнётся медленно, а затем через несколько секунд достигнет полной скорости.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/ore_fortuner.snbt" />

  <BoxAnnotation color="#dddddd" min="2.7 0 2" max="3 1 3">
        (1) ME Шина импорта: имеет несколько карт ускорения.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 2" max="2 1 2.3">
        (2) Плоскость формирования: в конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 0.7" max="2 1 1">
        (3) Плоскость уничтожения: без GUI для конфигурации, но может быть с зачарованием удачи.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 0 0" max="3 1 1">
        (4) ME Шина хранения: в конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="3.5 0.5 2.5" color="#00ff00">
        Вход
    </DiamondAnnotation>

<DiamondAnnotation pos="3.5 0.5 0.5" color="#00ff00">
        Выход
    </DiamondAnnotation>

<DiamondAnnotation pos="4 0.5 1.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

*   <ItemLink id="import_bus" /> (1) содержит несколько <ItemLink id="speed_card" />. Чем больше плоскостей формирования
    в массиве, тем больше их требуется, поскольку они заставляют ME Шину импорта загружать больше предметов одновременно.
*   <ItemLink id="formation_plane" /> (2) находится в своей конфигурациии по умолчанию.
*   <ItemLink id="annihilation_plane" /> (3) не имеет GUI для конфигурации, но может быть с зачарованием удачи.
*   <ItemLink id="storage_bus" /> (4) находится в конфигурации по умолчанию.

## Как это работает

1.  <ItemLink id="import_bus" /> в зелёной подсети импортирует блоки из первой бочки в [сетевое хранилище](../ae2-mechanics/import-export-storage.md).
2.  Единственное хранилище в зелёной подсети — это <ItemLink id="formation_plane" />, которая размещает блоки.
3.  <ItemLink id="annihilation_plane" /> в оранжевой подсети ломает блоки, применяя к ним удачу.
4.  <ItemLink id="storage_bus" /> в оранжевой подсети сохраняет результаты ломания во второй бочке.
