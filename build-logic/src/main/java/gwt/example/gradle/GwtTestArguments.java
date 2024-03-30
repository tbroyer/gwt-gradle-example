package gwt.example.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.LocalState;

public abstract class GwtTestArguments {

  @LocalState
  public abstract DirectoryProperty getOutputDir();

  @LocalState
  public abstract DirectoryProperty getWorkDir();

  @LocalState
  public abstract DirectoryProperty getUnitCacheDir();

  @Input
  public abstract ListProperty<String> getArgs();

  public void args(String... args) {
    getArgs().appendAll(args);
  }

  public void args(Iterable<? extends String> args) {
    getArgs().appendAll(args);
  }
}
