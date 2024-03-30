package gwt.example.model;

import com.google.gwt.junit.client.GWTTestCase;
import gwt.example.model.FieldVerifier;

public class FieldVerifierTest extends GWTTestCase {
  public String getModuleName() {
    // Run in the JVM by default (see subclass for actually running with GWT)
    return null;
  }

  public void testIsValidName() {
    assertFalse(FieldVerifier.isValidName(null));
    assertFalse(FieldVerifier.isValidName(""));
    assertFalse(FieldVerifier.isValidName("a"));
    assertFalse(FieldVerifier.isValidName("ab"));
    assertFalse(FieldVerifier.isValidName("abc"));
    assertTrue(FieldVerifier.isValidName("abcd"));
  }
}
