package gwt.example.gradle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.Named;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.process.CommandLineArgumentProvider;

public class GwtTestArgumentProvider implements CommandLineArgumentProvider, Named {
  private final GwtTestArguments args;

  public GwtTestArgumentProvider(GwtTestArguments args) {
    this.args = args;
  }

  @Internal
  @Override
  public String getName() {
    return "gwt";
  }

  @Override
  public Iterable<String> asArguments() {
    List<String> args = new ArrayList<>();
    args.addAll(getArgs().getArgs().get());
    args.add("-war");
    args.add(getArgs().getOutputDir().get().toString());
    args.add("-workDir");
    args.add(getArgs().getWorkDir().get().toString());
    return Arrays.asList(
        "-Dgwt.args=" + args.stream().map(s -> '"' + s + '"').collect(Collectors.joining(" ")),
        "-Dgwt.persistentunitcachedir=" + getArgs().getUnitCacheDir().get()
    );
  }

  @Nested
  public GwtTestArguments getArgs() {
    return args;
  }
}
