package gwt.example.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  FieldVerifierTest.class,
  FieldVerifierGwtTest.class,
})
public class ModelSuite {}
