import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

val pluginYml = file("Verite/src/main/resources/plugin.yml").readText()
val pluginVersion = Regex("""(?m)^version:\s*([0-9]+\.[0-9]+\.[0-9]+)\s*$""")
    .find(pluginYml)?.groupValues?.get(1)
    ?: throw GradleException("no 'version: X.Y.Z' line in plugin.yml")

group = "teacommontea"
version = pluginVersion

val srcRoot = file("Verite/src/main/java")
val resRoot = file("Verite/src/main/resources")

val skript264 = files(".buildlibs/.papercache/skript/2.6.4/Skript.jar")
val skript2102 = files(".buildlibs/.papercache/skript/2.10.2/Skript.jar")
val skript2160 = files(".buildlibs/.papercache/skript/2.16.0/Skript.jar")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

val proxyCompile: Configuration by configurations.creating
val velocityAp: Configuration by configurations.creating

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.maxmind.geoip2:geoip2:4.2.0")

    compileOnly("io.netty:netty-transport:4.1.87.Final")
    compileOnly("io.netty:netty-buffer:4.1.87.Final")
    compileOnly("io.netty:netty-codec:4.1.87.Final")
    compileOnly("io.netty:netty-common:4.1.87.Final")
    compileOnly("io.netty:netty-handler:4.1.87.Final")
    compileOnly("io.netty:netty-resolver:4.1.87.Final")

    compileOnly("org.tukaani:xz:1.9")
    compileOnly("com.maxmind.db:maxmind-db:3.1.0")
    compileOnly("org.ow2.asm:asm:9.9")
    compileOnly("org.ow2.asm:asm-tree:9.9")
    compileOnly("org.ow2.asm:asm-commons:9.9")
    compileOnly("org.ow2.asm:asm-analysis:9.9")

    proxyCompile("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    proxyCompile("com.google.inject:guice:7.0.0")
    proxyCompile("com.mojang:brigadier:1.0.18")
    proxyCompile("net.md-5:bungeecord-api:1.21-R0.3")
    proxyCompile("net.md-5:bungeecord-chat:1.20-R0.2")
    proxyCompile("net.md-5:bungeecord-event:1.21-R0.3")
    proxyCompile("net.kyori:adventure-api:4.15.0")
    proxyCompile("net.kyori:adventure-key:4.15.0")
    proxyCompile("net.kyori:adventure-text-serializer-legacy:4.15.0")
    proxyCompile("net.kyori:adventure-text-serializer-plain:4.15.0")
    proxyCompile("net.kyori:examination-api:1.3.0")

    velocityAp("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

val jdk17 = javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(17)) }
val jdk21 = javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(21)) }

val skriptShimNames = listOf("GetterEventValues.java", "ConverterEventValues.java", "RegistryEventValues.java")
val proxyPkgDir = file("Verite/src/main/java/teacommontea/veriteproxy")

val mainOut = layout.buildDirectory.dir("verite-classes/main")

val compileMain by tasks.registering(JavaCompile::class) {
    javaCompiler.set(jdk17)
    source = fileTree(srcRoot) {
        exclude(skriptShimNames.map { "**/$it" })
        exclude("teacommontea/veriteproxy/**")
    }
    classpath = configurations.compileClasspath.get() + skript2102
    destinationDirectory.set(mainOut)
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all")
    options.isFork = true
}

fun skriptShimTask(name: String, shim: String, skriptJar: FileCollection, useJdk21: Boolean) =
    tasks.register<JavaCompile>(name) {
        dependsOn(compileMain)
        javaCompiler.set(if (useJdk21) jdk21 else jdk17)
        source = fileTree(srcRoot) { include("**/$shim") }
        classpath = files(mainOut) + configurations.compileClasspath.get() + skriptJar
        destinationDirectory.set(layout.buildDirectory.dir("verite-classes/$name"))
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:all")
        options.isFork = true
    }

val compileGetter = skriptShimTask("compileGetter", "GetterEventValues.java", skript264, false)
val compileConverter = skriptShimTask("compileConverter", "ConverterEventValues.java", skript2102, false)
val compileRegistry = skriptShimTask("compileRegistry", "RegistryEventValues.java", skript2160, true)

val proxyOut = layout.buildDirectory.dir("verite-classes/proxy")

val compileProxy by tasks.registering(JavaCompile::class) {
    dependsOn(compileMain)
    javaCompiler.set(jdk17)
    source = fileTree(proxyPkgDir)
    classpath = files(mainOut) + configurations.compileClasspath.get() + proxyCompile
    destinationDirectory.set(proxyOut)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-Werror"))
    options.annotationProcessorPath = velocityAp
    options.compilerArgs.addAll(listOf("-processor", "com.velocitypowered.api.plugin.ap.PluginAnnotationProcessor"))
    options.isFork = true
}

val assembleClasses by tasks.registering {
    dependsOn(compileMain, compileGetter, compileConverter, compileRegistry, compileProxy)
}

tasks.named<Jar>("jar") {
    enabled = false
}

val shadedJars = listOf(
    "org.tukaani:xz:1.9",
    "com.maxmind.db:maxmind-db:3.1.0",
    "org.ow2.asm:asm:9.9",
    "org.ow2.asm:asm-tree:9.9",
    "org.ow2.asm:asm-commons:9.9",
    "org.ow2.asm:asm-analysis:9.9"
)
val shadeConfig: Configuration by configurations.creating
dependencies {
    shadedJars.forEach { shadeConfig(it) }
}

tasks.register<org.gradle.jvm.tasks.Jar>("veriteJar") {
    dependsOn(assembleClasses)
    archiveFileName.set("Verite-$version.jar")
    destinationDirectory.set(layout.projectDirectory)

    from(compileMain.map { it.destinationDirectory })
    from(compileGetter.map { it.destinationDirectory })
    from(compileConverter.map { it.destinationDirectory })
    from(compileRegistry.map { it.destinationDirectory })
    from(compileProxy.map { it.destinationDirectory })
    from(resRoot)
    shadeConfig.forEach { dep ->
        from(zipTree(dep)) {
            exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA", "META-INF/MANIFEST.MF")
            exclude("META-INF/versions/**")
        }
    }
    exclude("META-INF/versions/**")

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named("build") {
    dependsOn("veriteJar")
}
