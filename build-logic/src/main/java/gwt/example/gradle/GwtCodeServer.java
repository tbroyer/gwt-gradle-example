package gwt.example.gradle;

import java.io.IOException;
import java.nio.file.Files;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
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
import org.gradle.work.DisableCachingByDefault;
import org.gradle.work.NormalizeLineEndings;

@DisableCachingByDefault
public abstract class GwtCodeServer extends DefaultTask {

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

  @OutputDirectory @Optional
  public abstract DirectoryProperty getLauncherDir();

  @LocalState
  public abstract DirectoryProperty getWorkDir();

  @LocalState
  public abstract DirectoryProperty getUnitCacheDir();

  // TODO: @Option (as @Input or @Console) for failOnError, logLevel, sourceLevel and style

  {
    // Never up-to-date, we always want to run the task if invoked
    getOutputs().upToDateWhen(task -> false);
  }

  @TaskAction
  void run() throws IOException {
    Files.createDirectories(getWorkDir().get().getAsFile().toPath());
    var result = getExecOperations().javaexec(spec -> {
      spec.executable(getJavaLauncher().get().getExecutablePath());

      // disable --module-path
      spec.getModularity().getInferModulePath().set(false);

      // TODO: use -src for getSourceDirectories() ?
      //       ideally this would extract source dirs from project dependencies too
      spec.classpath(getSourceDirectories(), getClasspath());
      spec.getMainClass().set("com.google.gwt.dev.codeserver.CodeServer");

      spec.jvmArgs(getJvmArgs().get());
      spec.systemProperty("gwt.persistentunitcachedir", getUnitCacheDir().get());

      spec.args(getArgs().get());
      spec.args("-workDir", getWorkDir().get());
      if (getLauncherDir().isPresent()) {
        spec.args("-launcherDir", getLauncherDir().get());
      }
      spec.args(getModuleName().get());
    });
    result.assertNormalExitValue();
  }
}
