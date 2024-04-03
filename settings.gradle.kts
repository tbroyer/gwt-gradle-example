pluginManagement {
    repositories {
        // gradlePluginPortal redirects to JCenter which isn't reliable,
        // prefer Central to JCenter (for the same dependencies)
        // cf. https://github.com/gradle/gradle/issues/15406
        mavenCentral()
        gradlePluginPortal()
    }

    includeBuild("build-logic")
}

rootProject.name = "gwt-gradle-example"
include("model", "shared", "client", "server")

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
