plugins {
    application
    id("gwt.example.java-conventions")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.gwt.servlet)
    implementation(platform(libs.jetty.bom))
    implementation(platform(libs.jetty.ee10.bom))
    implementation(libs.jetty.servlet)
    implementation(libs.jetty.servlets)
}

tasks {
    named<JavaExec>("run") {
        args(
            project.file("src/main/dist/www"),
            // XXX: use a variant in :client project?
            rootProject.file("client/build/gwtc/war")
        )
    }
}

// XXX: 'client' could be directly depended upon if it exposed the appropriate artifact
//val client by configurations.creating {
//    // TODO: add attributes to select the appropriate variant
//}
//distributions {
//    main {
//        contents {
//            from(client)
//            into("www")
//        }
//    }
//}
