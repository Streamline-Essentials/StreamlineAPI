package net.streamline.api.modules.java;

import com.google.common.base.Preconditions;
import com.google.re2j.Pattern;
import net.streamline.api.BasePlugin;
import net.streamline.api.events.server.ModuleDisableEvent;
import net.streamline.api.events.server.ModuleEnableEvent;
import net.streamline.api.modules.SimpleModuleManager;
import net.streamline.api.modules.*;
import net.streamline.api.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Represents a Java module loader, allowing modules in the form of .jar
 */
public final class JavaModuleLoader implements ModuleLoader {
    final BasePlugin server;
    private final Pattern[] fileFilters = new Pattern[]{Pattern.compile("\\.jar$")};
    private final List<ModuleClassLoader> loaders = new CopyOnWriteArrayList<ModuleClassLoader>();
    private final LibraryLoader libraryLoader;

    /**
     * This class was not meant to be constructed explicitly
     *
     * @param instance the server instance
     */
    @Deprecated
    public JavaModuleLoader(@NotNull BasePlugin instance) {
        Preconditions.checkArgument(instance != null, "Server cannot be null");
        server = instance;

        LibraryLoader libraryLoader = null;
        try {
            libraryLoader = new LibraryLoader(server.getLogger());
        } catch (NoClassDefFoundError ex) {
            // Provided depends were not added back
            server.getLogger().warning("Could not initialize LibraryLoader (missing dependencies?)");
        }
        this.libraryLoader = libraryLoader;
    }

    @Override
    public Module loadModule(@NotNull final File file) throws InvalidModuleException {
        Preconditions.checkArgument(file != null, "File cannot be null");

        if (!file.exists()) {
            throw new InvalidModuleException(new FileNotFoundException(file.getPath() + " does not exist"));
        }

        final ModuleDescriptionFile description;
        try {
            description = getModuleDescription(file);
        } catch (InvalidDescriptionException ex) {
            throw new InvalidModuleException(ex);
        }

        final File parentFile = file.getParentFile();
        final File dataFolder = new File(parentFile, description.getName());
        @SuppressWarnings("deprecation")
        final File oldDataFolder = new File(parentFile, description.getRawName());

        // Found old data folder
        if (dataFolder.equals(oldDataFolder)) {
            // They are equal -- nothing needs to be done!
        } else if (dataFolder.isDirectory() && oldDataFolder.isDirectory()) {
            server.getLogger().warning(String.format(
                    "While loading %s (%s) found old-data folder: `%s' next to the new one `%s'",
                    description.getFullName(),
                    file,
                    oldDataFolder,
                    dataFolder
            ));
        } else if (oldDataFolder.isDirectory() && !dataFolder.exists()) {
            if (!oldDataFolder.renameTo(dataFolder)) {
                throw new InvalidModuleException("Unable to rename old data folder: `" + oldDataFolder + "' to: `" + dataFolder + "'");
            }
            server.getLogger().log(Level.INFO, String.format(
                    "While loading %s (%s) renamed data folder: `%s' to `%s'",
                    description.getFullName(),
                    file,
                    oldDataFolder,
                    dataFolder
            ));
        }

        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidModuleException(String.format(
                    "Projected datafolder: `%s' for %s (%s) exists and is not a directory",
                    dataFolder,
                    description.getFullName(),
                    file
            ));
        }

        for (final String moduleName : description.getDepend()) {
            Module current = server.getModuleManager().getModule(moduleName);

            if (current == null) {
                throw new UnknownDependencyException("Unknown dependency " + moduleName + ". Please download and install " + moduleName + " to run this module.");
            }
        }

        server.getUnsafe().checkSupported(description);

        final ModuleClassLoader loader;
        try {
            loader = new ModuleClassLoader(this, getClass().getClassLoader(), description, dataFolder, file, (libraryLoader != null) ? libraryLoader.createLoader(description) : null);
        } catch (InvalidModuleException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new InvalidModuleException(ex);
        }

        loaders.add(loader);

        return loader.module;
    }

    @Override
    @NotNull
    public ModuleDescriptionFile getModuleDescription(@NotNull File file) throws InvalidDescriptionException {
        Preconditions.checkArgument(file != null, "File cannot be null");

        JarFile jar = null;
        InputStream stream = null;

        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("module.yml");

            if (entry == null) {
                throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain module.yml"));
            }

            stream = jar.getInputStream(entry);

            return new ModuleDescriptionFile(stream);

        } catch (IOException ex) {
            throw new InvalidDescriptionException(ex);
        } catch (YAMLException ex) {
            throw new InvalidDescriptionException(ex);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    @NotNull
    public Pattern[] getModuleFileFilters() {
        return fileFilters.clone();
    }

    @Nullable
    Class<?> getClassByName(final String name, boolean resolve, ModuleDescriptionFile description) {
        for (ModuleClassLoader loader : loaders) {
            try {
                return loader.loadClass0(name, resolve, false, ((SimpleModuleManager) server.getModuleManager()).isTransitiveDepend(description, loader.module.getDescription()));
            } catch (ClassNotFoundException cnfe) {
            }
        }
        return null;
    }

    void setClass(@NotNull final String name, @NotNull final Class<?> clazz) {
//        if (ConfigurationSerializable.class.isAssignableFrom(clazz)) {
//            Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass(ConfigurationSerializable.class);
//            ConfigurationSerialization.registerClass(serializable);
//        }
    }

    private void removeClass(@NotNull Class<?> clazz) {
//        if (ConfigurationSerializable.class.isAssignableFrom(clazz)) {
//            Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass(ConfigurationSerializable.class);
//            ConfigurationSerialization.unregisterClass(serializable);
//        }
    }

    @Override
    public void enableModule(@NotNull final Module module) {
        Preconditions.checkArgument(module instanceof JavaModule, "Module is not associated with this ModuleLoader");

        if (!module.isEnabled()) {
            module.getLogger().info("Enabling " + module.getDescription().getFullName());

            JavaModule jModule = (JavaModule) module;

            ModuleClassLoader moduleLoader = (ModuleClassLoader) jModule.getClassLoader();

            if (!loaders.contains(moduleLoader)) {
                loaders.add(moduleLoader);
                server.getLogger().log(Level.WARNING, "Enabled module with unregistered ModuleClassLoader " + module.getDescription().getFullName());
            }

            try {
                jModule.setEnabled(true);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred while enabling " + module.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            // Perhaps abort here, rather than continue going, but as it stands,
            // an abort is not possible the way it's currently written
            server.getProxy().getPluginManager().callEvent(new ModuleEnableEvent(module));
        }
    }

    @Override
    public void disableModule(@NotNull Module module) {
        Preconditions.checkArgument(module instanceof JavaModule, "Module is not associated with this ModuleLoader");

        if (module.isEnabled()) {
            String message = String.format("Disabling %s", module.getDescription().getFullName());
            module.getLogger().info(message);

            server.getProxy().getPluginManager().callEvent(new ModuleDisableEvent(module));

            JavaModule jModule = (JavaModule) module;
            ClassLoader cloader = jModule.getClassLoader();

            try {
                jModule.setEnabled(false);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred while disabling " + module.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            if (cloader instanceof ModuleClassLoader) {
                ModuleClassLoader loader = (ModuleClassLoader) cloader;
                loaders.remove(loader);

                Collection<Class<?>> classes = loader.getClasses();

                for (Class<?> clazz : classes) {
                    removeClass(clazz);
                }

                try {
                    loader.close();
                } catch (IOException ex) {
                    //
                }
            }
        }
    }
}
