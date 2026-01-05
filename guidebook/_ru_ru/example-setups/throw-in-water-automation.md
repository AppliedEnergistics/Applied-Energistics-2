---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация бросания в воду
  icon: fluix_crystal
---

# Автоматизация рецептов бросания в воду

Обратите внимание: поскольку здесь используется <ItemLink id="pattern_provider" />, он предназначен для интеграции в вашу систему [автоматического создания](../ae2-mechanics/autocrafting.md).

Некоторые рецепты требуют бросания предметов в воду (хотя похожую установку можно использовать и для бросания предметов в другие места).
Это можно автоматизировать с помощью <ItemLink id="formation_plane" />, <ItemLink id="annihilation_plane" />, и некоторой вспомогательной
инфраструктуры (по сути, это две модифицированные [подсети кабеля](pipe-subnet.md)).

Данная установка предназначена для использования в сочетании с [автоматизацией зарядки](charger-automation.md) для обеспечения <ItemLink id="charged_certus_quartz_crystal" />.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/throw_in_water.snbt" />

<BoxAnnotation color="#dddddd" min="2 0 1" max="3 1 2">
        (1) Поставщик шаблонов: в конфигурации по умолчанию, с соответствующими шаблонами обработки.

        ![Fluix Pattern](../assets/diagrams/fluix_pattern_small.png) ![Flawed Budding Pattern](../assets/diagrams/flawed_budding_pattern_small.png)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1.7 0 1" max="2 1 2">
        (2) Интерфейс: в конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 .7 1" max="2 1 2">
        (3) Плоскость формирования: настраивает входное выкидывание в качестве предметов.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 2 1" max="2 2.3 2">
        (4) Плоскость уничтожения: без GUI для конфигурации.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 1 1" max="3 1.3 2">
        (5) ME Шина хранения: фильтрует по выходам шаблонов.
        <Row><ItemImage id="fluix_crystal" scale="2" /><BlockImage id="flawless_budding_quartz" scale="2" /></Row>
  </BoxAnnotation>

<DiamondAnnotation pos="3.9 0.5 1.5" color="#00ff00">
        К основной сети и автоматизации зарядки
        <GameScene zoom="3" background="transparent">
          <ImportStructure src="../assets/assemblies/charger_automation.snbt" />
          <IsometricCamera yaw="195" pitch="30" />
        </GameScene>
    </DiamondAnnotation>

  <IsometricCamera yaw="180" pitch="0" />
</GameScene>

## Конфигурации и шаблоны

* <ItemLink id="pattern_provider" /> (1) находится в конфигурации по умолчанию с соответствующим <ItemLink id="processing_pattern" />.
  * Для <ItemLink id="fluix_crystal" /> рецепт по умолчанию из JEI/REI работает нормально:

    ![Fluix Pattern](../assets/diagrams/fluix_pattern.png)

  * Для <ItemLink id="flawed_budding_quartz" />, вероятно, лучше всего создавать его непосредственно из <ItemLink id="quartz_block" />,
    что позволяет избежать проблем, связанных с тем, что ввод одного рецепта является выводом другого, что приводит к невозможности фильтрации по ME Шине хранения:

    ![Flawed Budding Pattern](../assets/diagrams/flawed_budding_pattern.png)


* <ItemLink id="interface" /> (2) находится в конфигурации по умолчанию.
* <ItemLink id="formation_plane" /> (3) настроена на входное выкидывание в качестве предметов.
* <ItemLink id="annihilation_plane" /> (4) не имеет GUI и не может быть настроена.
* <ItemLink id="storage_bus" /> (5) фильтруется по выходам шаблонов.

## Как это работает

1.  <ItemLink id="pattern_provider" /> помещает ингредиенты в <ItemLink id="interface" /> на своей стороне, в зелёной подсети.
2.  Интерфейс (настроенный по умолчанию без сохранения) пытается отправить своё содержимое в [сетевое хранилище](../ae2-mechanics/import-export-storage.md).
3.  Единственное хранилище в зелёной подсети — это <ItemLink id="formation_plane" />, которое выкидывает полученные предметы в воду.
4.  <ItemLink id="annihilation_plane" /> в оранжевой подсети пытается подобрать только что выкинутые предметы, но не может, потому что
    <ItemLink id="storage_bus" /> на верхней части поставщика шаблонов (единственное хранилище в оранжевой подсети) фильтруется и принимает только результаты возможных созданий.
5.  Предметы преобразуются в мире.
6.  Плоскость уничтожения теперь может собирать предметы, находящиеся перед ней, поскольку ME Шине хранения разрешено их хранить.
7.  ME Шина хранения сохраняет полученные предметы в поставщик шаблонов, возвращая их в сеть.
