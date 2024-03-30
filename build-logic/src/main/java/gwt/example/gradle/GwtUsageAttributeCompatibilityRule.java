package gwt.example.gradle;

import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;
import org.gradle.api.attributes.Usage;

public class GwtUsageAttributeCompatibilityRule implements AttributeCompatibilityRule<Usage> {
  public static final String USAGE_GWT = "gwt";

  @Override
  public void execute(CompatibilityCheckDetails<Usage> details) {
    if (details.getConsumerValue() != null
        && USAGE_GWT.equals(details.getConsumerValue().getName())
        && details.getProducerValue() != null
        && Usage.JAVA_RUNTIME.equals(details.getProducerValue().getName())) {
      details.compatible();
    }
  }
}
