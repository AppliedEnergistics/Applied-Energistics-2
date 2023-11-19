module fmljunit {
    requires org.junit.platform.launcher;
    requires cpw.mods.bootstraplauncher;
    requires net.neoforged.mergetool.api;
    requires fml_loader;
    requires cpw.mods.modlauncher;
    provides org.junit.platform.launcher.LauncherInterceptor with net.neoforged.fmljunit.FmlLauncherSessionListener;
}
