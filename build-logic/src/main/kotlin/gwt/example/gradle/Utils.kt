package gwt.example.gradle

import gwt.example.gradle.GwtLibraryElementsAttributeCompatibilityRule.LIBRARY_ELEMENTS_CLASSES_AND_RESOURCES_AND_SOURCES
import gwt.example.gradle.GwtUsageAttributeCompatibilityRule.USAGE_GWT
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.kotlin.dsl.*

fun Configuration.configureForGwt(objects: ObjectFactory) {
    require(isCanBeResolved || isCanBeConsumed)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(USAGE_GWT))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LIBRARY_ELEMENTS_CLASSES_AND_RESOURCES_AND_SOURCES))
    }
}

fun Configuration.preferGwtUser() = prefer("gwt-user", "Prefer gwt-user to gwt-servlet")

fun Configuration.preferGwtServlet() = prefer("gwt-servlet", "Prefer gwt-servlet to gwt-user")

private fun Configuration.prefer(artifactId: String, reason: String) {
    require(isCanBeResolved)
    resolutionStrategy
        .capabilitiesResolution
        .withCapability("org.gwtproject:gwt-servlet") {
            candidates.firstOrNull {
                val id = it.id
                id is ModuleComponentIdentifier && id.group == "org.gwtproject" && id.module == artifactId
            }?.also {
                select(it).because(reason)
            }
        }
}

fun JvmTestSuite.extendsFromSuite(test: JvmTestSuite, configurations: ConfigurationContainer) {
    sources {
        configurations.named(implementationConfigurationName) {
            extendsFrom(configurations.named(test.sources.implementationConfigurationName).get())
        }
        configurations.named(runtimeOnlyConfigurationName) {
            extendsFrom(configurations.named(test.sources.runtimeOnlyConfigurationName).get())
        }
        compileClasspath += test.sources.output
        runtimeClasspath += test.sources.output + test.sources.java.sourceDirectories + test.sources.output.generatedSourcesDirs
    }
}
