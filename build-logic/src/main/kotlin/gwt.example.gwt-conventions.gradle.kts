plugins {
    `java-base`
    id("gwt.example.java-conventions")
}

java {
    // This is the maximum sourceLevel supported by GWT
    // Note that this will also set targetCompatibility, and it would
    // probably be better to use --release in the compilation tasks
    // I don't really care at that point, this is just an safeguard
    // in addition to, say, running GWT compilation (possibly through
    // GWT tests).
    sourceCompatibility = JavaVersion.VERSION_11
}
