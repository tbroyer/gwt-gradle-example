import gwt.example.gradle.GwtTestArguments
import gwt.example.gradle.extendsFromSuite

plugins {
    id("gwt.example.gwt-library")
    id("gwt.example.gwt-conventions")
}

dependencies {
    implementation(platform(libs.guava.bom))
    implementation(libs.guava) {
        because("""For the GwtCompatible annotation mainly,
            AutoValue can as a result use the Guava classes in generated code.
            This helps examplify transitive dependencies with guava-gwt""")
    }
    // XXX: this could probably be handled as a variant of Guava as well
    gwtOnly(libs.guava.gwt)
    compileOnly(libs.gwt.servlet) {
        because("For AutoValue to generate CustomFieldSerializers")
    }
    compileOnlyApi(libs.autoValue.annotations)
    annotationProcessor(libs.autoValue.processor)
}

testing {
    suites {
        named<JvmTestSuite>("gwtTest") {
            dependencies {
                implementation(platform(libs.gwt.bom))
                implementation(libs.gwt.user)
                implementation(libs.gwt.dev)
            }
            targets.configureEach {
                testTask {
                    configure<GwtTestArguments> {
                        args("-ea", "-batch", "module", "-draftCompile")
                    }
                }
            }
        }
    }
}
