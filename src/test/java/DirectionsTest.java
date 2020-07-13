import com.google.maps.DirectionsApplication;
import com.google.maps.DirectionsConsumer;
import com.google.maps.DirectionsService;
import com.google.maps.DirectionsServiceInjector;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DirectionsTest {

  private DirectionsServiceInjector injector;
  private final String ORIGIN = "University of Waterloo";
  private final String DESTINATION = "Wilfrid Laurier University";
  // private final List<String> NO_WAYPOINTS = Arrays.asList();
  private final List<String> ONE_WAYPOINT = Arrays.asList("University of Waterloo Place");
  // private final List<String> MANY_WAYPOINTS =
  //    Arrays.asList("University of Waterloo Place", "Google Kitchener");
  private final String API_KEY = "SAMPLE_API_KEY";

  @Before
  public void init() {
    // Mock the injector with anonymous class
    injector =
        new DirectionsServiceInjector() {
          @Override
          public DirectionsConsumer getConsumer() {
            // Mock the Directions service
            return new DirectionsApplication(
                new DirectionsService() {
                  @Override
                  public String getDirections(String uri) {
                    System.out.println("Mock Directions Service Implementation");
                    return null;
                  }
                });
          }
        };
  }

  @After
  public void tear() {
    injector = null;
  }

  @Test
  public void test() {
    DirectionsConsumer consumer = injector.getConsumer();
    String result = consumer.processDirections(DESTINATION, ORIGIN, ONE_WAYPOINT, API_KEY);
    Assert.assertNull(result);
  }
}
