module fmljunit {
    requires org.junit.platform.launcher;
    requires cpw.mods.securejarhandler;
    requires net.neoforged.mergetool.api;
    requires fml_loader;
    requires cpw.mods.modlauncher;
    provides org.junit.platform.launcher.LauncherInterceptor with net.neoforged.fmljunit.FmlLauncherInterceptor;
    provides org.junit.platform.launcher.LauncherSessionListener with net.neoforged.fmljunit.FmlLauncherSessionListener;
    provides cpw.mods.modlauncher.api.ILaunchHandlerService with net.neoforged.fmljunit.JunitLaunchHandlerService;
    uses java.util.function.Consumer;
}
