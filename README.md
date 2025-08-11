[![Latest Release](https://img.shields.io/github/v/release/Frontiers-PackForge/Applied-Energistics-2-cosmolite?style=flat-square&label=Release)](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases)

# Applied Energistics 2 - Cosmic Frontiers
## A Version of AE2 for the Cosmic Frontiers modpack, unlimiting, combining, and upgrading many features required for the modpack to fully function. Akin to GTNH-AE2

## Table of Contents


* [License](#license)
* [Credits](#credits)

## About

A Mod about Matter, Energy and using them to conquer the world..

## Contacts

* [Website](https://appliedenergistics.org/)
* [Players Guide](https://guide.appliedenergistics.org/)
* [GitHub] You are here.

## Applied Energistics 2 API

The API for Applied Energistics 2. It is open source to discuss changes, improve documentation, and provide better add-on support in general.

### Working with the mod

Thanks to [GTCEu](https://github.com/GregTechCEu) for graciously providing us with a Maven repository.

First of all, you need to add the following repository to your build.gradle:

```groovy
repositories {
    maven {
        name "GTCEu Maven"
        url "https://maven.gtceu.com/"
        content {
            includeGroup 'appeng'
        }
    }
}
```

Then you can add the dependency to your build.gradle:

```groovy
dependencies {
    // For ModDevGradle Users
    modImplementation "appeng:appliedenergistics2-forge:$ae2_version"
    // For ForgeGradle Users
    implementation fg.deobf("appeng:appliedenergistics2-forge:$ae2_version")
}
```
<br>

[![Latest version](https://img.shields.io/github/v/release/Frontiers-PackForge/Applied-Energistics-2-cosmolite?style=flat-square&label=Release)](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases)

For versioning, add an entry to your `gradle.properties` file, example:

```properties
ae2_version=15.4.7-cosmolite.8
```

An example string would be `appeng:appliedenergistics2-forge:15.4.7-cosmolite.8`.

## Contributing

Contributions are always welcome and appreciated!

Before contributing major changes, you should probably discuss them with us first,
to waste noone's time.
You can either open an issue or ping `kolja_` or `ghostipedia` in the [Cosmic Frontiers Discord](https://discord.gg/cQ2hNbsNMd)

Still want to contribute? Great!

### Getting started:

1. [Fork the repository](https://github.com/Frontiers-PackForge/Applied-Energistics-2-cosmolite/fork)
2. Open a new [pull request](https://github.com/Frontiers-PackForge/Applied-Energistics-2-cosmolite/pulls) targeting the `forge/1.20.1` branch 
  * Build check failing? You might have forgotten to run the `spotlessApply` task
3. Changes requested by maintainers? Do your best to solve them
4. Pull request merged? Congrats and thank you for your contribution!

## License

* Applied Energistics 2 API
  - (c) 2013 - 2020 AlgorithmX2 et al
  - [![License](https://img.shields.io/badge/License-MIT-red.svg?style=flat-square)](http://opensource.org/licenses/MIT)
* Applied Energistics 2
  - (c) 2013 - 2020 AlgorithmX2 et al
  - [![License](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv2/LICENSE)
* Textures and Models
  - (c) 2020, [Ridanisaurus Rid](https://github.com/Ridanisaurus/), (c) 2013 - 2020 AlgorithmX2 et al
  - [![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%203.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-sa/3.0/)
* Text and Translations
  - [![License](https://img.shields.io/badge/License-No%20Restriction-green.svg?style=flat-square)](https://creativecommons.org/publicdomain/zero/1.0/)
* Additional Sound Licenses
  - Guidebook Click Sound
    - [EminYILDIRIM](https://freesound.org/people/EminYILDIRIM/sounds/536108/) 
    - [![License](https://img.shields.io/badge/License-CC%20BY%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by/4.0/)

## Downloads

Downloads can be found on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2) or on the [official website](https://appliedenergistics.github.io/download).

## Installation

You install this mod by putting it into the `minecraft/mods/` folder. It has no additional hard dependencies.

## Credits

Thanks to
 
* Notch et al for Minecraft
* Lex et al for MinecraftForge
* AlgorithmX2 for AppliedEnergistics2
* [Ridanisaurus Rid](https://github.com/Ridanisaurus/) for the new 2020 textures
* all [contributors](https://github.com/AppliedEnergistics/Applied-Energistics-2/graphs/contributors)
