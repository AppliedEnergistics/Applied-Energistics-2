---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация печи
  icon: minecraft:furnace
---

# Автоматизация печи

Обратите внимание, что поскольку здесь используется <ItemLink id="pattern_provider" />, он предназначен для интеграции в вашу установку [автоматического создания](../ae2-mechanics/autocrafting.md).
Если вы просто хотите автоматизировать автономную печь, используйте воронки, сундуки и прочее.

Автоматизация <ItemLink id="minecraft:furnace" /> немного сложнее, чем автоматизация более простой машины, таких как [зарядник](../example-setups/charger-automation.md).
Печь требует подачи материала с двух отдельных сторон и извлечения с третьей. Плавящийся предмет необходимо поместить в верхнюю часть,
топливо — в боковую часть, а готовый результат — извлечь снизу.

Это можно сделать с помощью <ItemLink id="pattern_provider" />
сверху, <ItemLink id="export_bus" /> сбоку для постоянной подачи топлива и <ItemLink id="import_bus" />
снизу для импорта результатов в сеть. Однако для этого используются 3 [канала](../ae2-mechanics/channels.md).

Вот как это можно сделать, используя всего один канал:

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/furnace_automation.snbt" />

<BoxAnnotation color="#dddddd" min="1 0 0" max="2 1 1">
        (1) Поставщик шаблонов: направленный вариант с использованием гаечного ключа из истинного кварца с соответствующими шаблонами обработки.

        ![Iron Pattern](../assets/diagrams/furnace_pattern_small.png)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 1.3 1">
        (2) Интерфейс: в конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="1.3 2 1">
        (3) ME Шина хранения #1: фильтруется по углю.
        <ItemImage id="minecraft:coal" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 2 0" max="1 2.3 1">
        (4) ME Шина хранения #2: фильтруется по углю в чёрном списке, с помощью карты-инвертера.
        <Row><ItemImage id="minecraft:coal" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) находится в конфигурации по умолчанию с соответствующим <ItemLink id="processing_pattern" />.
    Направление задаётся с помощью <ItemLink id="certus_quartz_wrench" />.

  ![Iron Pattern](../assets/diagrams/furnace_pattern.png)

* <ItemLink id="interface" /> (2) находится в конфигурации по умолчанию.
* Первая <ItemLink id="storage_bus" /> (3) фильтруется до угля или любого другого топлива, которое вы хотите использовать.
* Вторая <ItemLink id="storage_bus" /> (4) фильтруется до используемого вами топлива в чёрном списке с помощью <ItemLink id="inverter_card" />.

## Как это работает

1. <ItemLink id="pattern_provider" /> помещает ингредиенты в <ItemLink id="interface" />.
   (На самом деле, в целях оптимизации, ингредиенты передаются напрямую через ME Шину хранения, как если бы они были расширениями лицевых панелей поставщика. Предметы фактически не попадают в ME Интерфейс.)
2. Интерфейс настроен на отсутствие хранения, поэтому он пытается поместить ингредиенты в [сетевое хранилище](../ae2-mechanics/import-export-storage.md).
3. Единственное хранилище в зелёной подсети — это <ItemLink id="storage_bus" />. ME Шина, фильтрующая уголь, помещает уголь в топливный слот печи через боковую часть.
   ME Шина, фильтрующая НЕ уголь, помещает предметы для плавки в верхний слот через верхнюю часть.
4. Печь выполняет свою функцию.
5. Воронка извлекает результаты из нижней части печи и помещает их в возвратные слоты поставщика, возвращая их в основную сеть.
