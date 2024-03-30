package gwt.example.gradle;

import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.LocalState;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.ExecOperations;

@CacheableTask
public abstract class GwtCompile extends DefaultTask {
  @Inject
  protected abstract FileSystemOperations getFileSystemOperations();

  @Inject
  protected abstract ExecOperations getExecOperations();

  @Nested
  public abstract Property<JavaLauncher> getJavaLauncher();

  @Classpath
  public abstract ConfigurableFileCollection getClasspath();

  @Internal
  public abstract ConfigurableFileCollection getSourceDirectories();

  @InputFiles
  @SkipWhenEmpty
  @PathSensitive(PathSensitivity.RELATIVE)
  FileCollection getSources() {
    return getSourceDirectories().getAsFileTree();
  }

  // TODO: more specific JVM arguments (incl. memory); see org.gradle.process.JavaForkOptions
  @Input
  public abstract ListProperty<Object> getJvmArgs();

  public void jvmArgs(Object... args) {
    getJvmArgs().appendAll(args);
  }

  public void jvmArgs(Iterable<?> args) {
    getJvmArgs().appendAll(args);
  }

  @Input
  public abstract ListProperty<Object> getArgs();

  public void args(Object... args) {
    getArgs().appendAll(args);
  }

  public void args(Iterable<?> args) {
    getArgs().appendAll(args);
  }

  @Input
  public abstract Property<String> getModuleName();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDir();

  @OutputDirectory @Optional
  public abstract DirectoryProperty getDeployDir();

  @OutputDirectory
  public abstract DirectoryProperty getExtraDir();

  @OutputDirectory @Optional
  public abstract DirectoryProperty getGeneratedSourcesDir();

  @LocalState
  public abstract DirectoryProperty getWorkDir();

  @LocalState
  public abstract DirectoryProperty getUnitCacheDir();

  // TODO: @Option (as @Input or @Console) for failOnError, logLevel, sourceLevel and style

  @TaskAction
  void run() {
    getFileSystemOperations().delete(spec -> {
      spec.delete(getOutputDir(), getDeployDir().getOrNull(), getExtraDir(), getGeneratedSourcesDir().getOrNull());
    });
    var result = getExecOperations().javaexec(spec -> {
      spec.setExecutable(getJavaLauncher().get().getExecutablePath());

      // disable --module-path
      spec.getModularity().getInferModulePath().set(false);

      spec.classpath(getSourceDirectories(), getClasspath());
      spec.getMainClass().set("com.google.gwt.dev.Compiler");

      spec.jvmArgs(getJvmArgs().get());
      spec.systemProperty("gwt.persistentunitcachedir", getUnitCacheDir().get());

      spec.args(getArgs().get());
      spec.args(
          "-war", getOutputDir().get(),
          "-extra", getExtraDir().get(),
          "-workDir", getWorkDir().get()
      );
      if (getDeployDir().isPresent()) {
        spec.args("-deploy", getDeployDir().get());
      }
      if (getGeneratedSourcesDir().isPresent()) {
        spec.args("-gen", getGeneratedSourcesDir().get());
      }
      spec.args(getModuleName().get());
    });
    result.assertNormalExitValue();
  }
}
