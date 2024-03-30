import gwt.example.gradle.GwtApplicationExtension
import gwt.example.gradle.GwtCodeServer
import gwt.example.gradle.GwtCompile
import gwt.example.gradle.configureForGwt
import gwt.example.gradle.extendsFromSuite
import gwt.example.gradle.preferGwtUser

plugins {
    id("gwt.example.gwt-base")
    java
    id("gwt.example.gwt-test-suite")
}

//
// XXX: as is (applying the java plugin) assembling will create a JAR file
// with the classes bytecode; this is useless.
// This should probably apply java-base and jvm-test-suites only,
// re-create the "main" source set and "test" testing suite, and provide
// its own packaging.
//

java {
    modularity.inferModulePath = false
}

val gwtExtension = extensions.create<GwtApplicationExtension>("gwt")

val gwtConfiguration by configurations.dependencyScope("gwt")
val gwtClasspathConfiguration by configurations.resolvable("gwtClasspath") {
    extendsFrom(configurations.compileClasspath.get())
    extendsFrom(configurations.runtimeClasspath.get())
    extendsFrom(gwtConfiguration)
    configureForGwt(objects)
}

configurations.all {
    if (isCanBeResolved) {
        preferGwtUser()
    }
}

val gwtSources = project.files(
    sourceSets.main.map { it.java.sourceDirectories },
    sourceSets.main.map { it.output.generatedSourcesDirs },
)
val gwtClasspath = project.files(sourceSets.main.map { it.output }, gwtClasspathConfiguration)

tasks {
    val gwtCompile by registering(GwtCompile::class) {
        javaLauncher = project.javaToolchains.launcherFor(java.toolchain)

        outputDir.convention(layout.buildDirectory.dir("gwtc/war"))
        extraDir.convention(layout.buildDirectory.dir("gwtc/extra"))
        workDir.convention(layout.buildDirectory.dir("gwtc/work"))
        unitCacheDir.convention(layout.buildDirectory.dir("gwtc/unitcache"))
        // generatedSourcesDir.convention(layout.buildDirectory.dir("gwtc/gen"))

        classpath.from(gwtClasspath)
        sourceDirectories.from(gwtSources)
        moduleName.convention(gwtExtension.moduleName)
    }

    assemble {
        dependsOn(gwtCompile)
    }

    val run by registering(GwtCodeServer::class) {
        javaLauncher = project.javaToolchains.launcherFor(java.toolchain)

        // XXX: we can use the same output directory as gwtCompile,
        // as Gradle will correctly recompile if needed when assembling
        // But ideally this should use a separate directory (but this
        // requires possibly changing the configuration of the server
        // to pick the right directory depending on whether the developer
        // wants to use that "run" task or not.
        // Values are not "linked" though, if gwtCompile is reconfigured,
        // tasks will "diverge", or this "run" task needs to be
        // reconfigured as well.
        launcherDir.convention(layout.buildDirectory.dir("gwtc/war"))
        workDir.convention(layout.buildDirectory.dir("gwt/codeserver"))
        // Use the same persistent unit-cache as gwtCompile
        unitCacheDir.convention(layout.buildDirectory.dir("gwtc/unitcache"))

        classpath.from(gwtClasspath)
        sourceDirectories.from(gwtSources)
        moduleName.convention(gwtExtension.moduleName)
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class)
        named<JvmTestSuite>("gwtTest") {
            extendsFromSuite(test, configurations)
            dependencies {
                implementation(project())
            }
        }
    }
}
