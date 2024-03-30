package gwt.example.gradle;

import java.util.Set;
import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;
import org.gradle.api.attributes.LibraryElements;

public class GwtLibraryElementsAttributeCompatibilityRule
    implements AttributeCompatibilityRule<LibraryElements> {
  public static final String LIBRARY_ELEMENTS_CLASSES_AND_RESOURCES_AND_SOURCES =
      "classes+resources+sources";

  private static final Set<String> COMPATIBLE_PRODUCERS =
      Set.of(LibraryElements.CLASSES_AND_RESOURCES, LibraryElements.JAR);

  @Override
  public void execute(CompatibilityCheckDetails<LibraryElements> details) {
    if (details.getConsumerValue() != null
        && LIBRARY_ELEMENTS_CLASSES_AND_RESOURCES_AND_SOURCES.equals(
            details.getConsumerValue().getName())
        && details.getProducerValue() != null
        && COMPATIBLE_PRODUCERS.contains(details.getProducerValue().getName())) {
      details.compatible();
    }
  }
}
