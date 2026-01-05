---
navigation:
  parent: example-setups/example-setups-index.md
  title: Специализированное локальное хранилище
  icon: drive
---

# Специализированное локальное хранилище

Используя одно из [специальных поведений ME Интерфейса](../items-blocks-machines/interface.md#special-interactions),
[подсеть](../ae2-mechanics/subnetworks.md) может представить содержимое своего хранилища основной сети, не имея
возможности видеть хранилище основной сети и занимая только один [канал](../ae2-mechanics/channels.md).

Это полезно для локального хранения на какой-нибудь ферме, чтобы предметы не переполняли ваше основное хранилище.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/local_storage.snbt" />

<BoxAnnotation color="#dddddd" min="4 0 0" max="5 2 1">
        (1) Некоторый метод импорта предметов (в данном случае ME Интерфейс)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 0 0" max="4 1 1">
        (2) Дисковод: В нём есть несколько ячеек. Ячейки должны быть отфильтрованы по всему, что производит ферма.
        В ячейках могут быть карты равномерного распределения и карты уничтожения при переполнении.
        <Row><ItemImage id="item_storage_cell_4k" scale="2" /> <ItemImage id="equal_distribution_card" scale="2" /> <ItemImage id="void_card" scale="2" /></Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 1 0" max="4 2 0.3">
        (3) ME Терминал создания: может видеть содержимое ME Дисковода в подсети, но не содержимое хранилища вашей основной сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0 0" max="2.3 1 1">
        (4) ME Интерфейс #2: в конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1.7 0 0" max="2 1 1">
        (5) ME Шина хранения: имеет более высокий приоритет, чем у основного хранилища, может фильтроваться по всему, что выводит ферма.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 2 0.3">
        ME Терминал создания: может видеть как содержимое хранилища основной сети, так *и* подсети.
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* Первый <ItemLink id="interface" /> (1) просто принимает предметы из любой вашей фермы и отправляет их в подсеть.
* <ItemLink id="drive" /> (2) содержит несколько [ячеек](../items-blocks-machines/storage_cells.md). Ячейки должны быть
  [разделены](../items-blocks-machines/cell_workbench.md) в соответствии с выходами фермы.
  Ячейки могут содержать <ItemLink id="equal_distribution_card" /> и <ItemLink id="void_card" />.
* Второй <ItemLink id="interface" /> (4) имеет конфигурацию по умолчанию.
* <ItemLink id="storage_bus" /> имеет [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), установленный
  выше, чем у основного хранилища. Её можно фильтровать по выходам фермы.

## Как это работает

* <ItemLink id="interface" /> в подсети отображает содержимое <ItemLink id="drive" />а на <ItemLink id="storage_bus" /> в основной сети.
  Это означает, что ME Шина хранения может напрямую извлекать предметы из ячеек ME Дисковода и помещать их в ячейки ME Дисковода.
* ME Шина хранения имеет высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), поэтому предметы приоритетно
  загружаются обратно в подсеть, а не в основное хранилище.
* Важно отметить, что при заполнении ячеек в подсети предметы не переполнятся и не переместятся в основную сеть. Если ферма относится к типу,
  который выходит из строя при резервном копировании, для удаления лишних предметов можно использовать <ItemLink id="void_card" />. 
* Если ферма выводит несколько предметов, <ItemLink id="equal_distribution_card" /> могет помешать одному предмету заполнить все ячейки,
  и тем самым помешать сохранению других предметов.