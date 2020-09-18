[![Build master](https://img.shields.io/github/workflow/status/AppliedEnergistics/Applied-Energistics-2/Build%20master?style=flat-square)](https://github.com/AppliedEnergistics/Applied-Energistics-2/actions?query=workflow%3A%22Build+master%22)
[![Latest Release](https://img.shields.io/github/v/release/AppliedEnergistics/Applied-Energistics-2?style=flat-square&label=Release)](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases)
[![Latest PreRelease](https://img.shields.io/github/v/release/AppliedEnergistics/Applied-Energistics-2?include_prereleases&style=flat-square&label=Pre)](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases)

# Applied Energistics 2

## Table of Contents

* [About](#about)
* [Contacts](#contacts)
* [License](#license)
* [Downloads](#downloads)
* [Installation](#installation)
* [Issues](#issues)
* [API](#applied-energistics-2-api)
* [Building](#building)
* [Contribution](#contribution)
* [Localization](#applied-energistics-2-localization)
* [Credits](#credits)

## About

A Mod about Matter, Energy and using them to conquer the world..

## Contacts

* [Website](http://ae-mod.info/)
* [Discord](https://discord.gg/GygKjjm)
* [GitHub](https://github.com/AppliedEnergistics/Applied-Energistics-2)
* [IRC #appliedenergistics on esper.net](http://webchat.esper.net/?channels=appliedenergistics&prompt=1)

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

## Downloads

Downloads can be found on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2) or on the [official website](http://ae-mod.info/Downloads/).

## Installation

You install this mod by putting it into the `minecraft/mods/` folder. It has no additional hard dependencies.

## Issues

Applied Energistics 2 crashing, have a suggestion, found a bug?  Create an issue now!

1. Make sure your issue has not already been answered or fixed and you are using the latest version. Also think about whether your issue is a valid one before submitting it.
    * If it is already possible with vanilla and AE2 itself, the suggestion will be considered invalid.
    * Asking for a smaller version, more compact version, or more efficient version of something will also be considered invalid.
2. Go to [the issues page](https://github.com/AppliedEnergistics/Applied-Energistics-2/issues) and click [new issue](https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/new)
3. If applicable, use on of the provided templates. It will also contain further details about required or useful information to add.
4. Click `Submit New Issue`, and wait for feedback!

Providing as many details as possible does help us to find and resolve the issue faster and also you getting a fixed version as fast as possible.

Please note that we might close any issue not matching these requirements. 

## Applied Energistics 2 API

The API for Applied Energistics 2. It is open source to discuss changes, improve documentation, and provide better add-on support in general.

### Maven

Our authoritative Maven repository is Github Packages, which you can also use in your builds. Use of Github Packages 
[requires special setup](https://docs.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-gradle-for-use-with-github-packages#authenticating-to-github-packages) 
to authenticate with your personal access token.

AE2 is also available without authentication from Modmaven. You can use the following snippet as example on how to add a repository to your gradle build file.

    repositories {
        maven {
            name "Modmaven"
            url "https://modmaven.dev/"
            // For Gradle 5.1 and above, limit it to just AE2
            content {
                includeGroup 'appeng'
            }
        }
    }

When compiling against the AE2 API you can use gradle dependencies, just add

    dependencies {
        compileOnly "appeng:appliedenergistics2:VERSION:api"
    }

or add the `compileOnly` line to your existing dependencies task to your build.gradle.

Replace `VERSION` with the desired one. With 1.15+ we switched to using [semver](https://semver.org/). 
It is highly recommended following its specification and further considering an upper bound for the dependency version.
A change of the `MAJOR` version will be an API break and can lead to various crashes. Better to inform a player about the addon not supporting the new version until it could be tested or updated.

An example string would be `appeng:appliedenergistics2:7.0.0-alpha.6:api` for the API only or `appeng:appliedenergistics2:7.0.0-alpha.6` for the whole mod.

Builds obtained [here](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases) or via [maven](https://github.com/AppliedEnergistics/Applied-Energistics-2/packages) should work in a development environment.
However the dependency might need to be wrapped in `fg.deobf()` like `runtimeOnly fg.deobf("appeng:appliedenergistics2:VERSION:api")`

## Building

1. Clone this repository via 
  - SSH `git clone git@github.com:AppliedEnergistics/Applied-Energistics-2.git` or 
  - HTTPS `git clone https://github.com/AppliedEnergistics/Applied-Energistics-2.git`
2. Build using the `gradlew runData build` command. Jar will be in `build/libs`
3. For core developer: Setup IDE
  - IntelliJ: Import as gradle project
  - Eclipse: Import as gradle project or execute gradle task `eclipse` and potentially `genEclipseRuns`

## Contribution

Before you want to add major changes, you might want to discuss them with us first, before wasting your time.
If you are still willing to contribute to this project, you can contribute via [Pull-Request](https://help.github.com/articles/creating-a-pull-request).

The [guidelines for contributing](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/master/.github/CONTRIBUTING.md) contain more detailed information about topics like the used code style and should also be considered.

Here are a few things to keep in mind that will help get your PR approved.

* A PR should be focused on content. Any PRs where the changes are only syntax will be rejected.
* Use the file you are editing as a style guide.
* Consider your feature. [Suggestion Guidelines](http://ae-mod.info/Suggestion-Guidelines/)
  - Is your suggestion already possible using Vanilla + AE2?
  - Make sure your feature isn't already in the works, or hasn't been rejected previously.
  - Does your feature simplify another feature of AE2? These changes will not be accepted.
  - If your feature can be done by any popular mod, discuss with us first.

**Getting Started**

1. Fork this repository
2. Clone the fork via
  * SSH `git clone git@github.com:<your username>/Applied-Energistics-2.git` or 
  * HTTPS `git clone https://github.com/<your username>/Applied-Energistics-2.git`
3. Change code base
4. Run `gradlew spotlessApply` to apply automatic code formatting
5. Add changes to git `git add -A`
6. Commit changes to your clone `git commit -m "<summary of made changes>"`
7. Push to your fork `git push`
8. Create a Pull-Request on GitHub
9. Wait for review
10. Squash commits for cleaner history

If you are only doing single file pull requests, GitHub supports using a quick way without the need of cloning your fork. Also read up about [synching](https://help.github.com/articles/syncing-a-fork) if you plan to contribute on regular basis.

## Applied Energistics 2 Localization

### English Text

`en_US` is included in this repository, fixes to typos are welcome.

### Encoding

Files must be encoded as UTF-8.

### New or updated Translations

The language files are located in `/src/main/resources/assets/appliedenergistics2/lang/` and use the [appropriate locale code](http://minecraft.gamepedia.com/Language) as name and `.lang` as extension.

To update an translation edit the corresponding file and improve/correct the existing entry. Or copy any entries from `en_US.lang` for missing translation.

To create a new translation, copy the contents of `en_US.lang`, create a new file with appropriate filename, and translate it to your language.

Please keep in mind that we use [String format](https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html) to pass additional data to the text for displaying.
Therefore you should preserve parts like `%s` or `%1$d%%`, which allows us to replace them with the correct values while you still have the option to change their order for match the rules of grammar.
This might not be possible for some languages. Should this be the case, please contact us.

### Final Note

If you have have issues localizing something feel free to contact us on IRC, at #AppliedEnergistics on Esper.net

Thanks to everyone helping out to improve localization of AE2.

## Credits

Thanks to
 
* Notch et al for Minecraft
* Lex et al for MinecraftForge
* AlgorithmX2 for AppliedEnergistics2
* [Ridanisaurus Rid](https://github.com/Ridanisaurus/) for the new 2020 textures
* all [contributors](https://github.com/AppliedEnergistics/Applied-Energistics-2/graphs/contributors)
