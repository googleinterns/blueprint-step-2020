import com.google.api.services.tasks.model.Task;
import com.google.common.collect.ImmutableList;
import com.google.sps.servlets.GoServlet;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GoServletTest extends AuthenticatedServletTestBase {

  private GoServlet servlet;

  private static final Task TASK_WITH_NO_LOCATION = new Task().setNotes("sample notes");
  private static final Task TASK_WITH_ONE_LOCATION =
      new Task().setNotes("sample notes [Location: Google Kitchener] more sample notes");
  private static final Task TASK_WITH_TWO_LOCATIONS =
      new Task().setNotes("sample notes [Location: Google Kitchener] [Location: Google Montreal]");
  private static final Task TASK_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY =
      new Task().setNotes("(Location: Google Kitchener)");
  private static final Task TASK_WITH_EMPTY_LOCATION = new Task().setNotes("[Location: ]");

  private static final List<Task> NO_TASKS = ImmutableList.of();
  private static final List<Task> TASKS_WITH_NO_LOCATION = ImmutableList.of(TASK_WITH_NO_LOCATION);
  private static final List<Task> TASKS_WITH_ONE_LOCATION =
      ImmutableList.of(TASK_WITH_ONE_LOCATION);
  private static final List<Task> TASKS_WITH_TWO_LOCATIONS =
      ImmutableList.of(TASK_WITH_TWO_LOCATIONS);
  private static final List<Task> TASKS_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY =
      ImmutableList.of(TASK_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY);
  private static final List<Task> TASKS_WITH_EMPTY_LOCATION =
      ImmutableList.of(TASK_WITH_EMPTY_LOCATION);
  private static final List<Task> ALL_TASKS =
      ImmutableList.of(
          TASK_WITH_NO_LOCATION,
          TASK_WITH_ONE_LOCATION,
          TASK_WITH_TWO_LOCATIONS,
          TASK_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY,
          TASK_WITH_EMPTY_LOCATION);

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    servlet = new GoServlet();
  }

  @Test
  public void getLocationNoTasks() {
    // Obtain locations in the the task notes of no tasks.
    Assert.assertEquals(servlet.getLocations(NO_TASKS), ImmutableList.of());
  }

  @Test
  public void getNoLocation() {
    // Obtain location in the task notes of one task with no location.
    Assert.assertEquals(
        servlet.getLocations(TASKS_WITH_NO_LOCATION), ImmutableList.of("No location found"));
  }

  @Test
  public void getOneLocation() {
    // Obtain location in the task notes of one task with one location.
    Assert.assertEquals(
        servlet.getLocations(TASKS_WITH_ONE_LOCATION), ImmutableList.of("Google Kitchener"));
  }

  @Test
  public void getTwoLocations() {
    // Obtain location in the task notes of one task with two locations. Second location, Google
    // Montreal, is ignored.
    Assert.assertEquals(
        servlet.getLocations(TASKS_WITH_TWO_LOCATIONS), ImmutableList.of("Google Kitchener"));
  }

  @Test
  public void getIncorrectlyEnclosedLocation() {
    // Obtain location in the task notes of one task with one location enclosed with () instead of
    // [].
    Assert.assertEquals(
        servlet.getLocations(TASKS_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY),
        ImmutableList.of("No location found"));
  }

  @Test
  public void getEmptyLocation() {
    // Obtain location in the task notes of one task with one location with [Location: ] tag but
    // nothing inside of it.
    Assert.assertEquals(servlet.getLocations(TASKS_WITH_EMPTY_LOCATION), ImmutableList.of(""));
  }

  @Test
  public void getLocationAllTasks() {
    // Obtain location in the task notes of five tasks each with either one, empty or no location as
    // noted in the individual tests above.
    Assert.assertEquals(
        servlet.getLocations(ALL_TASKS),
        ImmutableList.of(
            "No location found", "Google Kitchener", "Google Kitchener", "No location found", ""));
  }
}
