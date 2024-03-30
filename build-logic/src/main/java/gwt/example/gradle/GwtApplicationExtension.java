package gwt.example.gradle;

import org.gradle.api.provider.Property;

public interface GwtApplicationExtension {
  Property<String> getModuleName();
}
