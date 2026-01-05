---
navigation:
  parent: example-setups/example-setups-index.md
  title: Интерфейс автозаполнения
  icon: interface
---

# Интерфейс автозаполнения

Можно задать вопрос: «Как поддерживать определённое количество различных предметов в хранении, создавая их по мере необходимости?»

Одним из решений является использование <ItemLink id="interface" /> и <ItemLink id="crafting_card" /> для автоматического запроса новых предметов
из [автоматического создания](../ae2-mechanics/autocrafting.md) вашей сети. Такая установка больше подходит для хранения небольшого количества
самых разнообразных предметов.

Эта демонстрационная установка укорочена, чтобы не быть слишком широкой. Вероятно, оптимальным вариантом будет использование 4 <ItemLink id="interface" /> и 4 <ItemLink id="storage_bus" />,
чтобы использовать все 8 [каналов](../ae2-mechanics/channels.md) в обычном [кабеле](../items-blocks-machines/cables.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/interface_autostocking.snbt" />

<BoxAnnotation color="#dddddd" min="0 0 0" max="2 1 1">
        (1) Интерфейсы: установите так, чтобы нужные предметы хранились внутри. У них есть карты создания.
        <ItemImage id="crafting_card" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 0" max="2 1.3 1">
        (2) ME Шины хранения: Режим ввода/вывода установлен на «только извлечение».
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="interface" /> (1) настроен на сохранение нужных предметов, для этого нужно щёлкнуть по нужному предмету в
   верхнем слоте или перетащить его в верхний слот из JEI, а затем щёлкнуть по иконке гаечного ключа над слотом, чтобы установить количество. У него есть <ItemLink id="crafting_card" />.
* <ItemLink id="storage_bus" /> (2) настроена таким образом, что режим ввода/вывода установлен на «только извлечение».

## Как это работает

1. Если <ItemLink id="interface" /> не может извлечь достаточное количество настроенного предмета из [сетевого хранилища](../ae2-mechanics/import-export-storage.md),
   (и у него есть <ItemLink id="crafting_card" />), то он запросит у сети [автоматическое создание](../ae2-mechanics/autocrafting.md) большого количества этого предмета.
2. <ItemLink id="storage_bus" /> позволяет сети получать доступ к содержимому ME Интерфейса.