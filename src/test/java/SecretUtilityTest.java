import java.io.IOException;

import com.google.sps.utility.SecretUtility;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SecretUtilityTest {

  @Test
  public void getSecret() throws IOException {
    // Checks that the correct URI is built.
    String actual = SecretUtility.accessSecretVersion();
    Assert.assertEquals("FAIL", actual);
  }
  
}
