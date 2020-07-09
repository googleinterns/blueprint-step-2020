import java.io.IOException;

import com.google.sps.utility.ApiKeyProvider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ApiKeyProviderTest {

  @Test
  public void testGet() throws IOException {
    String expected = "SHOULD FAIL";
    String apiKey = ApiKeyProvider.get("API_KEY");
    Assert.assertEquals(expected, apiKey);
  }
  
}
