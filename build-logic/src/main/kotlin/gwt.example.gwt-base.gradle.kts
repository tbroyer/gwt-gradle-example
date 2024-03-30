import gwt.example.gradle.GwtLibraryElementsAttributeCompatibilityRule
import gwt.example.gradle.GwtUsageAttributeCompatibilityRule

plugins {
    `java-base`
}

dependencies {
    attributesSchema {
        attribute(Usage.USAGE_ATTRIBUTE) {
            compatibilityRules.add(GwtUsageAttributeCompatibilityRule::class)
        }
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE) {
            compatibilityRules.add(GwtLibraryElementsAttributeCompatibilityRule::class)
        }
    }
    components {
        // Create conflict between gwt-user and gwt-servlet
        // XXX: Declare gwt-user and gwt-servlet as variants of each other instead?
        withModule("org.gwtproject:gwt-user") {
            val version = id.version
            allVariants {
                withCapabilities {
                    addCapability("org.gwtproject", "gwt-servlet", version)
                }
            }
        }
        // Workaround for https://github.com/google/guava/issues/7134
        withModule("com.google.guava:guava-gwt") {
            allVariants {
                withDependencies {
                    // This would be inherited from guava's POM, but guava's Gradle module metadata
                    // only exposes it in its jreApiElements, not jreRuntimeElements
                    // we need to add it here as GWT needs it (with -failOnError)
                    add("com.google.j2objc:j2objc-annotations:3.0.0")
                }
            }
        }
        // Handle guava-gwt as a variant of guava
//        withModule("com.google.guava:guava") {
//            addVariant("gwt", "jreRuntimeElements") {
//                attributes {
//                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(GwtUsageAttributeCompatibilityRule.USAGE_GWT))
//                }
//                withFiles {
//                    addFile("guava-gwt-${id.version}.jar", "../../guava-gwt/${id.version}/guava-gwt-${id.version}.jar")
//                }
//            }
//        }
    }
}
