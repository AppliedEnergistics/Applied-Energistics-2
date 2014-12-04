Applied-Energistics-2-API
=========================

The API for Applied Energistics 2. It is open source to discuss changes, improve documentation, and provide better add-on support in general.

Development and standard builds can be obtained [Here](http://ae2.ae-mod.info/Downloads/).

Maven
=========================

When compiling against the AE2 API you can use gradle dependencies, just add

dependencies {
	compile "appeng:appliedenergistics2:rv_-_____-__:dev"
}

or add the compile line to your existing dependencies task to your build.gradle

Where the __ are filled in with the correct version criteria; AE2 is available from the default forge maven so no additional repositories are necessary.
