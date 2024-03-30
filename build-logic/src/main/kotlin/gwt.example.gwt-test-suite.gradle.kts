import gwt.example.gradle.GwtTestArgumentProvider
import gwt.example.gradle.GwtTestArguments
import gwt.example.gradle.configureForGwt
import gwt.example.gradle.extendsFromSuite
import gwt.example.gradle.preferGwtServlet
import gwt.example.gradle.preferGwtUser

plugins {
    id("gwt.example.gwt-base")
    `jvm-test-suite`
}

testing {
    suites {
        val test by getting(JvmTestSuite::class)
        // XXX: make a GwtTestSuite?
        register<JvmTestSuite>("gwtTest") {
            testType = "gwt-test"
            useJUnit()
            sources {
                runtimeClasspath += sources.java.sourceDirectories + sources.output.generatedSourcesDirs
            }
            configurations.named(sources.compileClasspathConfigurationName) {
                preferGwtUser()
            }
            configurations.named(sources.runtimeClasspathConfigurationName) {
                configureForGwt(objects)
                preferGwtUser()
            }
            targets.configureEach {
                testTask {
                    shouldRunAfter(test)

                    isScanForTestClasses = false
                    include("**/*Suite.class")

                    val gwtTestArgs = objects.newInstance<GwtTestArguments>().apply {
                        outputDir.convention(layout.buildDirectory.dir("gwt/www-test"))
                        workDir.convention(layout.buildDirectory.dir("gwt/work"))
                        unitCacheDir.convention(layout.buildDirectory.dir("gwt/unitCache"))
                    }
                    extensions.add<GwtTestArguments>("gwt", gwtTestArgs)
                    jvmArgumentProviders.add(GwtTestArgumentProvider(gwtTestArgs))
                }
            }
        }
    }
}

tasks {
    check { dependsOn(testing.suites.named("gwtTest")) }
}
