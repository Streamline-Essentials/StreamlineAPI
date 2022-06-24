package net.streamline.api.base.modules.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.streamline.api.base.modules.ModuleDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class LibraryLoader {
    private final Logger logger;
//    private final RepositorySystem repository;
//    private final DefaultRepositorySystemSession session;
//    private final List<RemoteRepository> repositories;

    public LibraryLoader(@NotNull Logger logger) {
        this.logger = logger;

//        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
//        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
//        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
//
//        this.repository = locator.getService(RepositorySystem.class);
//        this.session = MavenRepositorySystemUtils.newSession();
//
//        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
//        session.setLocalRepositoryManager(repository.newLocalRepositoryManager(session, new LocalRepository("libraries")));
//        session.setTransferListener(new AbstractTransferListener() {
//            @Override
//            public void transferStarted(@NotNull TransferEvent event) throws TransferCancelledException {
//                logger.log(Level.INFO, "Downloading {0}", event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
//            }
//        });
//        session.setReadOnly();
//
//        this.repositories = repository.newResolutionRepositories(session, Arrays.asList(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build()));
    }

    @Nullable
    public ClassLoader createLoader(@NotNull ModuleDescriptionFile desc) {
        if (desc.getLibraries().isEmpty()) {
            return null;
        }
        logger.log(Level.INFO, "[{0}] Loading {1} libraries... please wait", new Object[]
                {
                        desc.getName(), desc.getLibraries().size()
                });

//        List<Dependency> dependencies = new ArrayList<>();
//        for (String library : desc.getLibraries()) {
//            Artifact artifact = new DefaultArtifact(library);
//            Dependency dependency = new Dependency(artifact, null);
//
//            dependencies.add(dependency);
//        }
//
//        DependencyResult result;
//        try {
//            result = repository.resolveDependencies(session, new DependencyRequest(new CollectRequest((Dependency) null, dependencies, repositories), null));
//        } catch (DependencyResolutionException ex) {
//            throw new RuntimeException("Error resolving libraries", ex);
//        }

        List<URL> jarFiles = new ArrayList<>();
//        for (ArtifactResult artifact : result.getArtifactResults()) {
//            File file = artifact.getArtifact().getFile();
//
//            URL url;
//            try {
//                url = file.toURI().toURL();
//            } catch (MalformedURLException ex) {
//                throw new AssertionError(ex);
//            }
//
//            jarFiles.add(url);
//            logger.log(Level.INFO, "[{0}] Loaded library {1}", new Object[]
//                    {
//                            desc.getName(), file
//                    });
//        }

        URLClassLoader loader = new URLClassLoader(jarFiles.toArray(new URL[jarFiles.size()]), getClass().getClassLoader());

        return loader;
    }
}
