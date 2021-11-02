package selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ParallelExecution {

    private static final int NUM_PARALLEL_EXECUTIONS = 5;
    private static final String DRIVER_PATH = "src/test/resources/geckodriver";
    private static final int EXP_ID = 38;
    private static final int[] U_IDS = {31, 32, 34, 39, 40};

    @Test
    public void run() {
        System.out.println("Running " + NUM_PARALLEL_EXECUTIONS + " test suites in parallel.");

        List<Thread> threads = new ArrayList<>();
        int numDigits = Math.max(1, (int) Math.floor(Math.log10(NUM_PARALLEL_EXECUTIONS - 1)) + 1);

        for (int i = 0; i < NUM_PARALLEL_EXECUTIONS; i++) {
            System.setProperty("webdriver.gecko.driver", DRIVER_PATH);
            final String testID = String.format("%0" + numDigits + "d", i);
            System.out.println(testID);
            int idPosition = i;
            Thread thread = new Thread(() -> {
                System.out.println("Running test suite with ID " + testID);
                WebDriver driver = new FirefoxDriver();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                String baseUrl = "http://localhost:8601/?uid=" + U_IDS[idPosition] + "&expid=" + EXP_ID;

                try {
                    ScratchTest scratchTest = new ScratchTest();
                    scratchTest.setTestID(testID);
                    scratchTest.setUp(driver, wait, baseUrl);
                    scratchTest.openScratchGUI();
                    Thread.sleep(2000 * (5 - idPosition));
                    scratchTest.dragFlackClickEvent();
                    scratchTest.dragGoToXY();
                    scratchTest.dragShow();
                    scratchTest.dragForever();
                    scratchTest.dragPointTowards();
                    scratchTest.dragGoTo();
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
