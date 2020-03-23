package com.hivemq.examples.testcontainer.junit5;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import javassist.ClassPool;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("rawtypes")
public class HiveMQTestContainerExtension implements BeforeEachCallback, AfterEachCallback {


    private static final @NotNull String validPluginXML =
            "<hivemq-extension>" + "<id>%s</id>" + "<name>%s</name>" + "<version>%s</version>" +
                    "<priority>%s</priority>" + "</hivemq-extension>";
    private static final @NotNull String HIVEMQ_CE_IMAGE = "hivemq/hivemq-ce";
    private static final @NotNull String HIVEMQ_CE_VERSION = "latest";
    public static final int MQTT_PORT = 1883;

    private final @NotNull GenericContainer container;

    public HiveMQTestContainerExtension() {
        container = new GenericContainer(HIVEMQ_CE_IMAGE + ":" + HIVEMQ_CE_VERSION);
        container.withExposedPorts(MQTT_PORT);

        final LogMessageWaitStrategy waitStrategy = new LogMessageWaitStrategy();
        waitStrategy.withRegEx(".*Started HiveMQ in.*");
        container.waitingFor(waitStrategy);
    }

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) {
        container.start();
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        container.stop();
    }

    public @NotNull CompletableFuture<Void> mountExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull String version,
            final @NotNull String priority,
            final @NotNull Class<? extends ExtensionMain> mainClazz) throws Exception {

        if (container.isRunning()) {
            final File extension = createExtension(id, name, version, priority, mainClazz, true);
            final MountableFile mountableExtension = MountableFile.forHostPath(extension.getAbsolutePath());
            container.copyFileToContainer(mountableExtension, "/opt/hivemq-ce-2020.2/extensions");
        }

        return CompletableFuture.completedFuture(null);
    }

    public static @NotNull File createExtension(
            final @NotNull String id,
            final @NotNull String name,
            final @NotNull String version,
            final @NotNull String priority,
            final @NotNull Class<? extends ExtensionMain> mainClazz,
            final boolean withSubClasses)
            throws Exception {

        final File tempDir = Files.createTempDir();

        final File pluginDir = new File(tempDir, id);
        FileUtils.writeStringToFile(new File(tempDir, "hivemq-extension.xml"),
                String.format(validPluginXML, id, name, version, priority), Charset.defaultCharset());

        final JavaArchive javaArchive =
                ShrinkWrap.create(JavaArchive.class)
                        .addAsServiceProviderAndClasses(ExtensionMain.class, mainClazz);


        if (withSubClasses) {
            //try to add all inner and anonymous classes of extension main to the extension jar
            try {
                //noinspection unchecked: always returns string set
                final Set<String> subClassNames =
                        ClassPool.getDefault().get(mainClazz.getName()).getClassFile().getConstPool().getClassNames();
                for (final String subClassName : subClassNames) {
                    if (subClassName.startsWith("single/") || subClassName.startsWith("cluster/") ||
                            subClassName.startsWith("longrunning/")) {
                        javaArchive.addClass(subClassName.replaceAll("/", "."));
                    }
                }
            } catch (final Exception e) {
                //ignore
            }
        }

        javaArchive.as(ZipExporter.class).exportTo(new File(tempDir, "extension.jar"));

        return pluginDir;
    }

    public int getMqttPort() {
        return container.getMappedPort(MQTT_PORT);
    }
}
