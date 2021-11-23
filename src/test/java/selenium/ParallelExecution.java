package selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to execute multiple selenium test instances in parallel using the geckodriver or chromedriver to test
 * parallel execution of the instrumented Scratch GUI and VM. Please refer to the comments below for information on how
 * to adapt this test to execute it successfully on your system.
 */
public class ParallelExecution {

    private static final int NUM_PARALLEL_EXECUTIONS = 2;
    private static final String DRIVER_PATH = "src/test/resources/";
    // The experiment ID needs to be adapted to an existing experiment.
    private static final int EXP_ID = 38; //1
    // The user IDs need to be adapted to existing users that have started, but not yet finished the experiment.
    private static final int[] U_IDS = {34, 39, 40}; //48, 49, 50

    @Test
    public void run() {
        System.out.println("Running " + NUM_PARALLEL_EXECUTIONS + " test suites in parallel.");

        List<Thread> threads = new ArrayList<>();
        int numDigits = Math.max(1, (int) Math.floor(Math.log10(NUM_PARALLEL_EXECUTIONS - 1)) + 1);

        for (int i = 0; i < NUM_PARALLEL_EXECUTIONS; i++) {
            System.setProperty("webdriver.gecko.driver", DRIVER_PATH + "geckodriver");
            // The chromedriver path needs to be adapted to the used system in order to use the driver.
            System.setProperty("webdriver.chrome.driver", "/usr/bin/" + "chromedriver");
            final String testID = String.format("%0" + numDigits + "d", i);
            System.out.println(testID);
            int idPosition = i;

            Thread thread = new Thread(() -> {
                System.out.println("Running test suite with ID " + testID);
                WebDriver driver = idPosition > 0 ? new FirefoxDriver() : new ChromeDriver();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                // The baseUrl needs to be adapted to the location under which the Scratch GUI is available.
                //https://scratch.fim.uni-passau.de/scratch/?uid=
                String baseUrl = "http://localhost:8601/?uid=" + U_IDS[idPosition] + "&expid=" + EXP_ID;

                try {
                    ScratchTest scratchTest = new ScratchTest();
                    scratchTest.setTestID(testID);
                    scratchTest.setUp(driver, wait, baseUrl);
                    scratchTest.openScratchGUI();
                    Thread.sleep(2000 * (5 - idPosition));
                    for (int j = 0; j < 1; j++) {
                        scratchTest.dragFlackClickEvent();
                        scratchTest.dragGoToXY();
                        scratchTest.dragShow();
                        scratchTest.dragForever();
                        scratchTest.dragPointTowards();
                        scratchTest.dragGoTo();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    driver.quit();
                }

                System.out.println("Finished test suite with ID " + testID);
            });
            threads.add(thread);
            thread.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
