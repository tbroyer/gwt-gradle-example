# Example GWT project with Gradle

This repository contains an example GWT project built with Gradle.
The goal is to show some good practices of organizing projects with shared libraries, exporting their sources to GWT consumers (transitively) but not to JVM ones (using Gradle variants), as well as GWT tests.
It is organized such that it shouldn't be much work to publish the internal Gradle plugins, but this is not something I'm intending to do, so if you're interested, feel free to use this project as a starting point.

The project is composed of 4 modules:

* `model` contains some _model_ classes
* `shared` is a shared library, declaring GWT-RPC interfaces; depends on `model`
* `server` is a JVM application using an embedded Jetty server;
  it contains a GWT-RPC servlet, and tests it using GWT. 
* `client` is a GWT (client-side) application

## Core idea

The core idea is that `model` sources (including generated ones) and GWT-specific dependencies are _exported_ down to the `client` project transitively through the `shared` project, without the `client` project having to do any special declaration, and the `server` project **not** having those sources and dependencies as it doesn't need them (the `gwtTest` task will however need them, and it _Just Works™_).
This is done through Gradle variants, so the `model` and `shared` projects, through the `gwt-library` internal plugin, export an additional variant with the sources directories,, the compiled classes and generated sources directories (output of the `compileJava` task), and the resources (output of the `processResources` task).

You can check the results with commands such as:
* `./gradlew :client:dependencies --configuration compileClasspath` and `./gradlew :client:dependencies --configuration gwtClasspath`, also `./gradlew :shared:dependencies --configuration testRuntimeClasspath` and `./gradlew :shared:dependencies --configuration gwtTestRuntimeClasspath`
* `./gradlew :client:dependencyInsight --dependency guava-gwt --configuration gwtClasspath` (also try it with `--configuration compileClasspath` and/or `:server:dependencyInsight` and notice it's not there)
* `./gradlew :model:outgoingVariants`

I've been successfully using this approach since mid-2019 on a project at work with ~45 subprojects, 10 of which are shared between the client and server like the `model` and `shared` projects here; so this is not just a proof of concept.

## Getting started

The project can be built and tested with the common Gradle lifecycle tasks: `./gradlew assemble`, `./gradlew check`, and/or `./gradlew build`.
The GWT application is output to `client/build/gwtc/war`; packaging is done with Docker outside Gradle (see below).

To run it for development:

1. start the GWT code server with `./gradlew :client:run`; alternatively you can compile the client app with `./gradlew :client:assemble` (or `./gradlew :client:gwtCompile`)
2. start the application with `./gradlew :server:run`; there's no hot-reload of bytecode, so everytime you make a change to server code you'll need to stop and restart that task.
3. Go to http://localhost:8000

Note that there's no continuous build, so if you change a resource in a shared project, you'll have to re-run the `processResources` task; same for the _shape_ of annotations (there aren't in this example project) or generated code (by annotation processors), you'd have to re-run the `compileJava` task. You could run a third Gradle command to automatically re-run those tasks when needed, through Gradle's continuous-build feature: `./gradlew :client:classes --continuous` (this would work in this project, but any project that would add another project as a `gwtOnly` dependency wouldn't grab that gwt-only project; in this case you'd have to use `./gradlew classes --continuous` to include all projects, or create a new task in the `client` project that would resolve the `gwtClasspath` configuration)

To package the project, Docker is used:
1. run `./gradlew assemble :server:installDist` to build the project and prepare the artifacts
2. run `docker build -t gwt-example .` to create the Docker image
3. run it with `docker run --rm -p 8000:8000 gwt-example` then go to http://localhost:8000

## Implementation details

### Internal plugins

The `gwt-base` internal plugin sets up variant attributes and their _attribute compatibility rules_ (necessary for third-party dependencies: whenever you ask for the GWT variant, it should fallback to the JVM runtime variant).
Technically there are two attributes, one that says you want to use the dependency in a GWT context (so a variant based on that attribute could add GWT-specific dependencies such as to third-parties' sources JARs), and another that says you prefer it in the form of classes + resources + sources directories (rather than, say, a JAR file).  
The plugin will also make sure that you cannot have both `gwt-user` and `gwt-servlet` in the classpath at the same time, forcing you to make a decision (there are Kotlin extension methods to setup configurations accordingly, using either `preferGwtServlet()` or `preferGwtUser()`). This is not technically required, but will help making sure `gwt-user` doesn't make it to the server for instance.  
As far as I can tell, these attributes can't be used in published libraries (Gradle module metadata) as that would require any project consuming them to declare them (apply a plugin similar to this `gwt-base` internal plugin), so they're only suitable as part of a multi-project build like this one.

The `gwt-library` internal plugin applies the `java-library` plugin and adds:
* a `gwt` source directory set to the `main` source set (i.e. a `src/main/gwt` folder) to put GWT-specific code and resources (in this case the GWT module file for shared libraries, but could also include super-sources)
* a `gwtOnly` configuration to declare GWT-specific dependencies (here used to add a dependency to `guava-gwt`, but could also be sources JARs of those third-party dependencies declared in `api` or `implementation`)
* a GWT variant exposing all the sources, generated sources, and compiled classes, and all the compile-time, runtime, and `gwtOnly` dependencies
* a `gwtTest` test suite (see below) that depends on the project's `main` code

Also, the `compileClasspath`, `runtimeClasspath`, `testCompileClasspath` and `testRuntimeClasspath` will prefer `gwt-servlet` over `gwt-user` to help make sure the code is compatible with running in a JVM (server-side) environment. This might constrain how shared libraries are implemented though, and similarly there aren't _server-only_ dependencies (for cases where you'd use `@GwtIncompatible` to exclude some code that'll only run on the server). Setting up such things is left as a exercise (beware there are a lot of edge cases).

The `gwt-application` internal plugin applies the `java` plugin and adds:
* a `gwt` project extension to set the GWT module name (`gwt.moduleName = "…"`), to configure the GWT tasks
* a `gwt` configuration to declare GWT-specific dependencies (those that are not needed by the `compileJava` or `test` tasks, but needed for the GWT tasks; this is generally where you'd declare the dependency to `gwt-dev`), along with a `gwtClasspath` configuration to resolve all dependencies (in their GWT variants) needed for GWT tasks (including compile-time and runtime dependencies, declared in `implementation`, `compileOnly`, or `runtimeOnly`)
* a `gwtCompile` task to run the GWT compiler, the result of which being by default output to `build/gwtc/war`; that task is also wired up to be executed when running the `assemble` task
* a `run` task to run the GWT code server for development. It reuses the same output directory (`build/gwtc/war`) for its `launcherDir` such that HTTP servers don't need to be configured differently depending on whether you want to use the code server or the output of the GWT compilation (and thanks to Gradle incremental build and build cache, you can safely run the `gwtCompile` task before packaging without always incurring the cost of a GWT compilation, and with the assurance that it will still run when needed so you never risk shipping the code server's `nocache.js` stub)
* a `gwtTest` test suite (see below) that depends on the project's `main` code and _extends_ the `test` suite (i.e. classes from `src/test/java` can be used from `src/gwtTest/java`, among other things)

The `gwt-test-suite` internal plugin applies the `jvm-test-suite` plugin and adds a `gwtTest` testing suite whose configurations will resolve the GWT variants of the dependencies, and the task is configured to use JUnit 4 and only look for `*Suite` classes.
The task also has a `gwt` extension of type `GwtTestArguments` that can be used to pass additional GWT arguments (that will end up in the `gwt.args` system dependency). This is used in the `model` project to pass `-ea`, `-batch module`, and `-draftCompile` for instance.
An `extendsFromSuite` Kotlin extension function can be used to make the `gwtTest` testing _extend_ from another testing suite, e.g. the `test` suite, such that classes from `src/test/java` can be referenced from `src/gwtTest/java` (that extension method makes sure that the `src/test` sources are made available to GWT when running the tests)

### Project-specific details

In the `model` project, the tests run both in GWT and in the JVM, by using a `GWTTestCase` whose `getModuleName()` returns `null`. This means you cannot run the JVM tests without the much slower GWT tests. One way to do that would be to move the JVM tests to `src/test/java` but without using `GWTTestCase`, and calling them from the `GWTTestCase` of `src/gwtTest/java` (e.g. as a static method). The goal of this project is mainly to show that GWT tests work, not really to tell you how you **should** organize your tests.

As-is, the project requires JDK 17 to build, due to Jetty 12, but sources for shared libs are required to be Java 11-compatible as GWT doesn't support things added to Java since then (records, pattern matching, etc.) This is enforced in this project through project-specific conventions that configure the source compatibility for the Java compilation, but there are many other ways this could be done. 

Packaging is done with Docker in a way that there's no need to depend on the output from the GWT compilation of the `client` project from the `server` project; this means the `client` project is never depended upon and currently exposes a JAR of the class bytecode which is useless, and doesn't expose the output of the GWT compilation as an artifact. Ideally, the JAR wouldn't even be built (or possibly as a secondary artifact), and the output of the GWT compilation would be exposed as a directory artifact (possibly with a ZIP or JAR variant, e.g. using the _webjar_ file layout) so it could be consumed by another project to assemble the final artifacts (e.g. directly included in the `server` distribution)
