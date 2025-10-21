package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class UITestingPlaygroundTests {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE = "https://uitestingplayground.com";

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // По ТЗ: не headless
        // options.addArguments("--headless=new");
        options.setExperimentalOption("detach", false);
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // используем только explicit
        driver.manage().window().setSize(new Dimension(1280, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(8));   // 5–10 секунд
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    /* 1) Sample App: login -> "Welcome, <name>!" */
    @Test @DisplayName("01 Sample App: login greets user")
    void sampleApp_login() {
        driver.get(BASE + "/sampleapp");
        driver.findElement(By.cssSelector("input[name='UserName']")).sendKeys("Anton");
        driver.findElement(By.cssSelector("input[name='Password']")).sendKeys("pwd");
        driver.findElement(By.cssSelector("#login")).click();
        WebElement status = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#loginstatus")));
        Assertions.assertEquals("Welcome, Anton!", status.getText());
    }

    /* 2) Sample App: logout -> "User logged out." */
    @Test @DisplayName("02 Sample App: logout shows message")
    void sampleApp_logout() {
        driver.get(BASE + "/sampleapp");
        driver.findElement(By.cssSelector("input[name='UserName']")).sendKeys("Anton");
        driver.findElement(By.cssSelector("input[name='Password']")).sendKeys("pwd");
        driver.findElement(By.cssSelector("#login")).click();
        wait.until(ExpectedConditions.textToBe(By.cssSelector("#loginstatus"), "Welcome, Anton!"));
        driver.findElement(By.cssSelector("#logout")).click();
        WebElement status = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#loginstatus")));
        Assertions.assertEquals("User logged out.", status.getText());
    }

    /* 3) Dynamic ID: click succeeds (no exception) */
    @Test @DisplayName("03 Dynamic ID: clickable by text/class")
    void dynamicId_click() {
        driver.get(BASE + "/dynamicid");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-primary")));
        btn.click();
        Assertions.assertTrue(true); // клик без исключения
    }

    /* 4) Class Attribute: click green -> accept alert, text contains "primary" */
    @Test @DisplayName("04 Class Attribute: alert contains 'primary'")
    void classAttr_alert() {
        driver.get(BASE + "/classattr");
        driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String txt = alert.getText();
        alert.accept();
        Assertions.assertTrue(txt.toLowerCase().contains("primary"));
    }

    /* 5) Hidden Layers: second click on Green is blocked by overlay */
    @Test @DisplayName("05 Hidden Layers: second click blocked")
    void hiddenLayers_blockedSecondClick() {
        driver.get(BASE + "/hiddenlayers");
        WebElement green = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#greenButton")));
        green.click();
        boolean blocked = false;
        try {
            // попытка повторного клика по тому же месту
            green.click();
        } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
            blocked = true;
        }
        Assertions.assertTrue(blocked);
    }

    /* 6) Load Delay: wait for button to appear -> isDisplayed() */
    @Test @DisplayName("06 Load Delay: button becomes visible")
    void loadDelay_buttonVisible() {
        driver.get(BASE + "/loaddelay");
        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.btn.btn-primary")));
        Assertions.assertTrue(btn.isDisplayed());
    }

    /* 7) AJAX Data: click -> wait content -> exact text */
    @Test @DisplayName("07 AJAX: content text appears")
    void ajax_contentLoaded() {
        driver.get(BASE + "/ajax");
        driver.findElement(By.cssSelector("#ajaxButton")).click();
        WebElement p = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content p")));
        Assertions.assertEquals("Data loaded with AJAX get request.", p.getText());
    }

    /* 8) Text Input: type "Hello" -> Update -> button text becomes "Hello" */
    @Test @DisplayName("08 Text Input: button shows typed text")
    void textInput_updatesButton() {
        driver.get(BASE + "/textinput");
        driver.findElement(By.cssSelector("#newButtonName")).sendKeys("Hello");
        driver.findElement(By.cssSelector("#updatingButton")).click();
        String btnText = driver.findElement(By.cssSelector("#updatingButton")).getText();
        Assertions.assertEquals("Hello", btnText);
    }

    /* 9) Scrollbars: scroll to hidden button and click -> no intercept */
    @Test @DisplayName("09 Scrollbars: click succeeds after scroll")
    void scrollbars_clickHidden() {
        driver.get(BASE + "/scrollbars");
        WebElement btn = driver.findElement(By.cssSelector("#hidingButton"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        btn.click();
        Assertions.assertTrue(true); // клик выполнен без исключения
    }

    /* 10) Overlapped Element: scroll and type "abc" -> value == "abc" */
    @Test @DisplayName("10 Overlapped: can type into field")
    void overlappedElement_type() {
        driver.get(BASE + "/overlapped");
        WebElement input = driver.findElement(By.cssSelector("#name"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", input);
        input.clear();
        input.sendKeys("abc");
        Assertions.assertEquals("abc", input.getAttribute("value"));
    }

    /* 11) Visibility: distinguish hidden vs removed (assert removed = not in DOM) */
    @Test @DisplayName("11 Visibility: removed element not in DOM")
    void visibility_removed() {
        driver.get(BASE + "/visibility");
        // Кнопка Remove удаляет элемент с id=removedButton (по странице тренажёра)
        driver.findElement(By.cssSelector("#hideButton")).click();   // триггеры на странице
        driver.findElement(By.cssSelector("#removeButton")).click();
        // ждём, пока элемент исчезнет из DOM:
        boolean gone = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#removedButton")));
        Assertions.assertTrue(gone);
    }

    /* 12) Click: blue button becomes btn-success */
    @Test @DisplayName("12 Click: class changes to btn-success")
    void click_buttonSuccess() {
        driver.get(BASE + "/click");
        WebElement bad = driver.findElement(By.cssSelector("#badButton"));
        bad.click();
        String cls = bad.getAttribute("class");
        Assertions.assertTrue(cls.contains("btn-success"));
    }

    /* 13) Progress Bar: stop at >=75 */
    @Test @DisplayName("13 Progress Bar: stop at >= 75")
    void progressBar_stopAt75() {
        driver.get(BASE + "/progressbar");
        driver.findElement(By.cssSelector("#startButton")).click();

        WebElement bar = driver.findElement(By.cssSelector("#progressBar"));
        wait.until(d -> {
            try {
                int v = Integer.parseInt(bar.getAttribute("aria-valuenow"));
                return v >= 75;
            } catch (Exception e) {
                return false;
            }
        });

        driver.findElement(By.cssSelector("#stopButton")).click();
        int val = Integer.parseInt(bar.getAttribute("aria-valuenow"));
        Assertions.assertTrue(val >= 75);
    }

    /* 14) Mouse Over: hover "Click me" and click 2x -> counter "2" */
    @Test @DisplayName("14 Mouse Over: counter is 2")
    void mouseOver_counter2() {
        driver.get(BASE + "/mouseover");
        WebElement target = driver.findElement(By.cssSelector("a#clickMe"));
        Actions actions = new Actions(driver);
        actions.moveToElement(target).click().click().perform();
        String cnt = driver.findElement(By.cssSelector("#clickCount")).getText().trim();
        Assertions.assertEquals("2", cnt);
    }

    /* 15) Shadow DOM: find text inside shadow root via getShadowRoot() */
    @Test @DisplayName("15 Shadow DOM: text exists inside shadow root")
    void shadowDom_textExists() {
        driver.get(BASE + "/shadowdom");
        // На странице есть веб-компонент; возьмём пример с кастомным элементом
        WebElement host = driver.findElement(By.cssSelector("my-paragraph, #shadowHost, .shadow-root-host"));
        SearchContext shadow = host.getShadowRoot();
        // Ищем любой текстовый узел внутри, напр. span/p
        WebElement inside = shadow.findElement(By.cssSelector("span, p, div"));
        Assertions.assertTrue(inside.getText().trim().length() > 0);
    }
}
