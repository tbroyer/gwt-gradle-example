plugins {
    id("gwt.example.gwt-library")
    id("gwt.example.gwt-conventions")
}

dependencies {
    api(projects.model)
    api(libs.gwt.servlet)
}
