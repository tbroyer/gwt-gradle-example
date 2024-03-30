import gwt.example.gradle.GwtLibraryElementsAttributeCompatibilityRule
import gwt.example.gradle.GwtUsageAttributeCompatibilityRule
import gwt.example.gradle.preferGwtServlet

plugins {
    id("gwt.example.gwt-base")
    `java-library`
    id("gwt.example.gwt-test-suite")
}

val gwtOnlySrcs = objects.sourceDirectorySet("gwt", "GWT-specific sources").apply {
    srcDir("src/main/gwt")
}
sourceSets.main {
    extensions.add<SourceDirectorySet>("gwt", gwtOnlySrcs)
}

val gwtOnly by configurations.dependencyScope("gwtOnly")
// Similar to apiElements but adds gwtOnly and runtimeOnly dependencies,
// similar to runtimeElements but adds gwtOnly and compileOnly dependencies,
// and source dirs as outgoing artifacts.
val gwt by configurations.consumable("gwt") {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.compileOnly.get())
    extendsFrom(configurations.runtimeOnly.get())
    extendsFrom(gwtOnly)
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(GwtUsageAttributeCompatibilityRule.USAGE_GWT))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(GwtLibraryElementsAttributeCompatibilityRule.LIBRARY_ELEMENTS_CLASSES_AND_RESOURCES_AND_SOURCES))
        // TODO: add TargetJvmVersion attribute; maybe introduce a gwt.sourcelevel attribute?
        // XXX: use a specific "gwt" target JVM environment? (with compatibility rule)
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
    }
    fun addOutgoingArtifacts(sources: Provider<Iterable<*>>) {
        outgoing.artifacts(sources) { builtBy(sources) }
    }
    addOutgoingArtifacts(sourceSets.main.map { it.output })
    addOutgoingArtifacts(sourceSets.main.map { it.output.generatedSourcesDirs })
    addOutgoingArtifacts(sourceSets.main.map { it.java.sourceDirectories })
    addOutgoingArtifacts(provider { gwtOnlySrcs.sourceDirectories })
}

configurations {
    compileClasspath { preferGwtServlet() }
    runtimeClasspath { preferGwtServlet() }
    testCompileClasspath { preferGwtServlet() }
    testRuntimeClasspath { preferGwtServlet() }
}

testing {
    suites {
        named<JvmTestSuite>("gwtTest") {
            dependencies {
                implementation(project())
            }
        }
    }
}
