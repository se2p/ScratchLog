package selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The tests in this class can only be run successfully when using the {@link ParallelExecution} class to execute them.
 */
public class ScratchTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseURL;

    private String testID;

    public ScratchTest() {
        this.testID = "";
    }

    @BeforeEach
    public void setUp(WebDriver driver, WebDriverWait wait, String baseURL) {
        this.driver = driver;
        this.wait = wait;
        this.baseURL = baseURL;
    }

    @Test
    public void openScratchGUI() throws InterruptedException {
        driver.get(baseURL);
        driver.manage().window().setSize(new Dimension(924, 1016));
        Thread.sleep(5000);
        wait.until(driver -> driver.findElement(By.cssSelector(".sprite-selector_sprite-wrapper_1C5Mq:nth-child(1)"))).click();
        {
            WebElement element = wait.until(driver -> driver.findElement(By.cssSelector(".blocklyMainBackground")));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).click();
        }
        driver.findElement(By.cssSelector(".injectionDiv")).click();
    }

    @Test
    public void dragFlackClickEvent() {
        wait.until(driver -> driver.findElement(By.cssSelector(".scratchCategoryId-events > .scratchCategoryItemBubble"))).click();
        {
            WebElement element = wait.until(driver -> driver.findElement(By.cssSelector(".injectionDiv > "
                    + ".blocklyFlyout > .blocklyWorkspace > .blocklyBlockCanvas > "
                    + ".blocklyDraggable[data-id='event_whenflagclicked']")));
            Actions builder = new Actions(driver);
            builder.moveToElement(wait.until(ExpectedConditions.elementToBeClickable(element))).perform();
            builder.dragAndDropBy(element, 300, 50).perform();
        }
    }

    @Test
    public void dragGoToXY() {
        wait.until(driver -> driver.findElement(By.cssSelector(".scratchCategoryId-motion > .scratchCategoryMenuItemLabel"))).click();
        {
            WebElement element = wait.until(driver -> driver.findElement(By.cssSelector(".injectionDiv > "
                    + ".blocklyFlyout > .blocklyWorkspace > .blocklyBlockCanvas > "
                    + ".blocklyDraggable[data-id='motion_gotoxy']")));
            Actions builder = new Actions(driver);
            builder.moveToElement(wait.until(ExpectedConditions.elementToBeClickable(element))).perform();
            builder.dragAndDropBy(element, 300, -100).perform();
        }
    }

    @Test
    public void dragShow() {
        wait.until(driver -> driver.findElement(By.cssSelector(".scratchCategoryId-looks > .scratchCategoryMenuItemLabel"))).click();
        {
            WebElement element = wait.until(driver -> driver.findElement(By.cssSelector(".injectionDiv > "
                    + ".blocklyFlyout > .blocklyWorkspace > .blocklyBlockCanvas > "
                    + ".blocklyDraggable[data-id='looks_hide']")));
            Actions builder = new Actions(driver);
            builder.moveToElement(wait.until(ExpectedConditions.elementToBeClickable(element))).perform();
            builder.dragAndDropBy(element, 300, -600).perform();
        }
    }

    @Test
    public void dragForever() {
        wait.until(driver -> driver.findElement(By.cssSelector(".scratchCategoryId-control > .scratchCategoryMenuItemLabel"))).click();
        {
            WebElement element = wait.until(driver ->driver.findElement(By.cssSelector(".injectionDiv > "
                    + ".blocklyFlyout > .blocklyWorkspace > .blocklyBlockCanvas > "
                    + ".blocklyDraggable[data-id='forever']")));
            Actions builder = new Actions(driver);
            builder.moveToElement(wait.until(ExpectedConditions.elementToBeClickable(element))).perform();
            builder.dragAndDropBy(element, 300, 0).perform();
        }
    }

    @Test
    public void dragPointTowards() {
        wait.until(driver -> driver.findElement(By.cssSelector(".scratchCategoryId-motion > .scratchCategoryMenuItemLabel"))).click();
        {
            WebElement element = wait.until(driver ->driver.findElement(By.cssSelector(".injectionDiv > "
                    + ".blocklyFlyout > .blocklyWorkspace > .blocklyBlockCanvas > "
                    + ".blocklyDraggable[data-id='motion_pointtowards']")));
            Actions builder = new Actions(driver);
            builder.moveToElement(wait.until(ExpectedConditions.elementToBeClickable(element))).perform();
            builder.dragAndDropBy(element, 300, -200).perform();
        }
    }

    @Test
    public void dragGoTo() {
        wait.until(driver -> driver.findElement(By.cssSelector(".scratchCategoryId-motion > .scratchCategoryMenuItemLabel"))).click();
        {
            WebElement element = wait.until(driver ->driver.findElement(By.cssSelector(".injectionDiv > "
                    + ".blocklyFlyout > .blocklyWorkspace > .blocklyBlockCanvas > "
                    + ".blocklyDraggable[data-id='motion_goto']")));
            Actions builder = new Actions(driver);
            builder.moveToElement(wait.until(ExpectedConditions.elementToBeClickable(element))).perform();
            builder.dragAndDropBy(element, 300, 70).perform();
        }
    }

    public void setTestID(String testID) {
        this.testID = testID;
    }

}
