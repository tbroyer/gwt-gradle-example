plugins {
    id("gwt.example.gwt-application")
    id("gwt.example.gwt-conventions")
}

gwt  {
    moduleName = "gwt.example.App"
}

dependencies {
    implementation(platform(libs.gwt.bom))
    implementation(libs.gwt.user)
    gwt(libs.gwt.dev)

    implementation(projects.shared)
    compileOnly(libs.autoValue.annotations)
    annotationProcessor(libs.autoValue.processor)
}

tasks {
    gwtCompile {
        args("-failOnError")
    }
    (run) {
        args("-failOnError")
    }
}
