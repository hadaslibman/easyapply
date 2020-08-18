package project;

import java.io.File;
import java.util.ArrayList;
//import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
import java.util.stream.Stream;
//import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
//import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FirstScript {
	public static void main(String[] args) throws InterruptedException {
	    //system Property for chrome Driver   
	    System.setProperty("webdriver.chrome.driver", "C:\\Selenium\\chromedriver_win32\\chromedriver.exe");  
	       
        //instantiate a ChromeDriver class.
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        //launch linkedin
        driver.navigate().to("https://www.linkedin.com");

        //maximize browser
        driver.manage().window().maximize();

        //action object
        Actions action = new Actions(driver);

        //locate "sign in" button on the sign up pop up and click it
        WebElement signIn = driver.findElement(By.xpath("/html/body/nav/a[3]"));
        Thread.sleep(2000);
        action.moveToElement(signIn).click(signIn).perform();
        Thread.sleep(2000);

        //locate "email or phone" box and enter email address
        WebElement emailField = driver.findElement(By.id("username"));
        emailField.sendKeys("hadaslibman@gmail.com");

        //locate password box and enter password
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(args[0]);
        Thread.sleep(2000);

        //locate and click sign in button
        WebElement signIn1 = driver.findElement(By.xpath("//main/div[2]/form/div[3]/button"));
        action.moveToElement(signIn1).click(signIn1).perform();
        Thread.sleep(2000);

        //locate and click the job search button
        WebElement jobs = driver.findElement(By.id("jobs-tab-icon"));
        action.moveToElement(jobs).click(jobs).perform();
        Thread.sleep(2000);

        //locate the job title box and enter title of job and search
        WebElement jobTitle = driver.findElement(By.className("jobs-search-box__text-input"));
        //jobTitle.sendKeys("Americas End User Support & Productivity Analyst Intern");
        //jobTitle.sendKeys("bd supply chain internship");
        //jobTitle.sendKeys("management rotation program intern - summer 2021");
        jobTitle.sendKeys("Accountant");
        Thread.sleep(2000);

        //locate and click the search button
        WebElement searchBtn = driver.findElement(By.className("jobs-search-box__submit-button"));
        action.moveToElement(searchBtn).click(searchBtn).perform();
        Thread.sleep(2000);

        //scroll down the list of positions for all elements to appear
        WebElement searchResults = driver.findElement(By.className("jobs-search-results"));
        for(int i = 0; i < 10; i++ ) {
            searchResults.sendKeys(Keys.PAGE_DOWN);
            Thread.sleep(500);
        }

		/**
		 * grab search results
		 * for each position, grab the link and open as separate tab (opens in linkedin page)
		 */
		searchResults.findElements(By.className("occludable-update"))
		    .parallelStream()
		    .filter(e -> match(e.getText())) //filters by predefined set of words defined by the user in the match method 
		    .limit(5) // number of tabs to open - TODO - extract to a variable
		    .forEach(we -> {
		        WebElement element = we.findElement(By.className("disabled"));
		        String link = element.getAttribute("href");
		        ((JavascriptExecutor) driver).executeScript("window.open('"+ link + "')");
		    });

   
        //saves the tabs as a list and goes over each tab
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        tabs.stream().skip(1).forEach(tab -> {
            driver.switchTo().window(tab);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            //saves the apply buttons as a list and goes over to check for cases 
            List<WebElement> applyButton = driver.findElements(By.className("jobs-s-apply"));
            if (applyButton.isEmpty()) { //in cases where position is still posted but cant longer apply
                ((JavascriptExecutor) driver).executeScript("window.close()");
            } else if (applyButton.get(0).getText().toLowerCase().contains("easy apply")) { //linkedin easy apply cases
                applyButton.get(0).click();
                try {
                    easyApply(driver);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else { //for any other case, click the 'apply' button and close the linkedin page
                applyButton.get(0).click();
                ((JavascriptExecutor) driver).executeScript("window.close()");
            }

        });
        
        /**
         * save the tabs left open as a list
         * for each tab, look for a 'apply'
         */
        List<String> apps = new ArrayList<>(driver.getWindowHandles());
        apps.stream().skip(1).forEach(app -> {
            try {
                driver.switchTo().window(app);
                Thread.sleep(3000);
                Stream.of(
                        driver.findElements(By.cssSelector("a[class*=button]")).stream(),
                        //driver.findElements(By.cssSelector("div[class*=button]")).stream(),
                        //driver.findElements(By.cssSelector("div[class*=btn]")).stream(),
                        driver.findElements(By.cssSelector("button")).stream()).flatMap(x->x)
                        .filter(x -> x.getText().toLowerCase().contains("apply") && !x.getText().toLowerCase().contains("easy apply"))
                        .limit(1) // should be always 1
                        .forEach(x -> x.click()         
                        );
                Thread.sleep(3000);
                postApply(driver, args[1]);
            } catch (Exception e) {}
        });
    }

    //method to be called after application case has been determined
    private static void postApply(WebDriver driver, String workdayPassword) throws InterruptedException {
        if(driver.getCurrentUrl().contains("myworkdayjobs.com")) {
            workDay(driver, workdayPassword);
        } else if (!driver.getCurrentUrl().contains("linkedin.com")) {
            manuallyContinue(driver);
        } else {
        	//do nothing
        }
    }

    //workday application filler
    private static void workDay(WebDriver driver, String workdayPassword) throws InterruptedException {
    	//check if there is an additional apply inside workday page
    	//clicks it if yes
    	Thread.sleep(3000);
    	Optional<WebElement> optionalApplyBtn = driver.findElements(By.cssSelector("button")).stream()
    			.filter(x -> x.getText().toLowerCase().equals("apply"))
    			.findFirst();
    	if(optionalApplyBtn.isPresent()) {
    		optionalApplyBtn.get().click();
    	}
    	
    	//checks if needs to create an account to continue
    	//creates if yes
    	Optional<WebElement> optionalCreateAccBtn = driver.findElements(By.cssSelector("a[data-automation-id*=changePassword")).stream()
    			.filter(x -> x.getText().toLowerCase().equals("create account"))
    			.findFirst();
    	if(optionalCreateAccBtn.isPresent()) {
    		optionalCreateAccBtn.get().click();
    		Thread.sleep(3000);
    		driver.findElements(By.cssSelector("input[aria-label='Email Address']")).stream().forEach(x -> x.sendKeys("hhygfhjtn@gmail.com"));
    		driver.findElements(By.cssSelector("input[aria-label='Password']")).stream().forEach(x -> x.sendKeys(workdayPassword));
    		driver.findElements(By.cssSelector("input[aria-label='Verify New Password']")).stream().forEach(x -> x.sendKeys(workdayPassword));
    		
    		driver.findElements(By.cssSelector("div[aria-label='Create Account']")).stream().forEach(x -> x.click());
    		
    		manuallyContinue(driver);
    	}
    	
    	// locate the drop area
        Thread.sleep(3000);
        WebElement droparea = driver.findElement(By.cssSelector("div[data-automation-id*=dragDropTarget"));

        // drop the file
        DropFile(new File("c:\\Users\\hadas\\Desktop\\Resume-PM.docx"), droparea, driver, 0, 0);

        WebElement application = driver.findElement(By.cssSelector("span[title='Next']"));
        application.click();
        Thread.sleep(5000);

        //WebElement inputs = driver.findElements(By.cssSelector("input[class*=gwt-TextBox]"));

        //PAGE 1///////////////////////////////////////////////////////////////////////////
        //first name
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(0).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(0).sendKeys("Hadas");
        //last name
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(1).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(1).sendKeys("Libman");
        //address line 1
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(4).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(4).sendKeys("123 Magnolia Dr");
        //city
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(5).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(5).sendKeys("Big City");
        //state
        driver.findElements(By.cssSelector("div[data-automation-id*=selectSelectedOption]")).get(1).click();
        driver.findElements(By.cssSelector("div[data-automation-id*=promptOption]")).stream().filter(x -> x.getAttribute("title").contains("Texas")).findAny().get().click();
        //postal code
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(6).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(6).sendKeys("12345");
        //email
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(7).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(7).sendKeys("email@gmail.com");
        //phone number
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(9).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(9).sendKeys("9413570012");
        Thread.sleep(3000);

        //former employee
        //driver.findElement(By.cssSelector("label[data-automation-label*=No]")).click();
        WebElement element = driver.findElement(By.cssSelector("label[data-automation-label*=No]"));
        JavascriptExecutor jse1 = (JavascriptExecutor)driver;
        jse1.executeScript("arguments[0].scrollIntoView()", element);
        element.click();

        //how did you hear about us?
        Thread.sleep(1000);
        driver.findElements(By.cssSelector("div[data-automation-id*=selectSelectedOption]")).get(2).click();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("div[data-automation-id*=promptOption]")).stream().filter(x -> x.getAttribute("title").contains("LinkedIn")).findAny().get().click();

        //next page
        driver.findElement(By.cssSelector("span[title='Next']")).click();
        Thread.sleep(3000);

        //PAGE 2///////////////////////////////////////////////////////////////////////////
        //job title
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(0).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(0).sendKeys("Quality Assurance Tester, Intern");
        //company
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(1).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(1).sendKeys("ClassCalc");
        //location
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(2).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(2).sendKeys("Los Angeles, CA");
        //dates
        //driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(3).clear();
        //Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(3).sendKeys("052020");
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(4).sendKeys("082020");
        // role description
//        driver.findElements(By.cssSelector("textarea[data-automation-id*=textAreaField]")).get(5).clear();
//        Thread.sleep(2000);
//        driver.findElements(By.cssSelector("textarea[data-automation-id*=textAreaField]")).get(0).sendKeys("•  Collaborated with QA engineers to develop and create effective strategies and test cases \r\n" + 
//        		"•	Tested new and existing features on different platforms and analyzed results\r\n" + 
//        		"•	Worked with cross-functional teams to ensure quality throughout the software\r\n" 
//         );

        //job title 2
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(5).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(5).sendKeys("Graduate Assistant for the Women’s Golf Team");
        //company 2
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(6).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(6).sendKeys("Lamar University Athletic Department");
        //location 2
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(7).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(7).sendKeys("Beaumont, TX");
        //dates 2
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(8).sendKeys("082019");
        //role description
        

        //job title 3
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(9).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(9).sendKeys("Operations Lead");
        //company 3
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(10).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(10).sendKeys("Tzomet Iron for Construction Ltd");
        //location 3
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(11).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(11).sendKeys("Or Akiva, Israel");
        //dates 3
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(12).sendKeys("102018");
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(13).sendKeys("062019");
        Thread.sleep(3000);
        //role description
        
        //job title 4
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(14).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(14).sendKeys("Non-Commissioned officer of Information Management");
        //company 4
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(15).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(15).sendKeys("Isreal Defense Forces");
        //location 4
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(16).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(16).sendKeys("Tel Aviv, Israel");
        //dates 4
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(17).sendKeys("102017");
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(18).sendKeys("102018");
        Thread.sleep(3000);
        //role description
        
        
//        //remove
//        WebElement element1 =  driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[1]/section/div[1]/div/div/div[1]/div/div/div[2]/div/div/div[2]/div[2]/div/div[3]/div[2]/div[3]/div/div[2]/div/div/div/div/div[2]/div/div[1]/div/div[2]/div/div/div/ul/li[3]/div/div/div[2]/button"));
//        JavascriptExecutor jse = (JavascriptExecutor)driver;
//        jse.executeScript("arguments[0].scrollIntoView()", element1);
//        element1.click();
//        Thread.sleep(3000);

        //education
        //
        //school
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(19).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(19).sendKeys("Lamar University");
        //gpa
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(21).clear();
        Thread.sleep(200);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(21).sendKeys("4.00");
        //dates
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(22).sendKeys("2019");
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(23).sendKeys("2021");
        //field of study
        driver.findElements(By.cssSelector("div[data-automation-id*=responsiveMonikerInput]")).get(0).click();
        Thread.sleep(3000);
        driver.findElements(By.cssSelector("div[data-automation-id*=promptOption]")).stream().filter(x -> x.getText().equals("Computer Science")).findAny().get().click();


        //school 2
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(24).clear();
        Thread.sleep(2000);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(24).sendKeys("University of California, Los Angeles");
        //gpa
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(26).clear();
        Thread.sleep(200);
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(26).sendKeys("3.25");
        //dates 2
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(27).sendKeys("2015");
        driver.findElements(By.cssSelector("input[class*=gwt-TextBox]")).get(28).sendKeys("2017");
        Thread.sleep(2000);
        //linkedin
        WebElement element2 = driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[1]/section/div[1]/div/div/div[1]/div/div/div[2]/div/div/div[2]/div[2]/div/div[3]/div[2]/div[3]/div/div[2]/div/div/div/div/div[2]/div/div[6]/div/div[2]/div/ul/li/div[2]/div/div/input"));
        JavascriptExecutor jse2 = (JavascriptExecutor)driver;
        jse2.executeScript("arguments[0].scrollIntoView()", element2);
        element2.clear();
        Thread.sleep(2000);
        element2.sendKeys("https://www.linkedin.com/in/hadaslibman/");

        //next page
        driver.findElement(By.cssSelector("span[title='Next']")).click();
        Thread.sleep(3000);


        //PAGE 3///////////////////////////////////////////////////////////////////////////
        //
        manuallyContinue(driver);

    }

    //method to upload resume to workday application first page
    public static void DropFile(File filePath, WebElement target, WebDriver driver, int offsetX, int offsetY) {
        if(!filePath.exists())
            throw new WebDriverException("File not found: " + filePath.toString());

        JavascriptExecutor jse = (JavascriptExecutor)driver;
        WebDriverWait wait = new WebDriverWait(driver, 30);

        String JS_DROP_FILE =
                "var target = arguments[0]," +
                        "    offsetX = arguments[1]," +
                        "    offsetY = arguments[2]," +
                        "    document = target.ownerDocument || document," +
                        "    window = document.defaultView || window;" +
                        "" +
                        "var input = document.createElement('INPUT');" +
                        "input.type = 'file';" +
                        "input.style.display = 'none';" +
                        "input.onchange = function () {" +
                        "  var rect = target.getBoundingClientRect()," +
                        "      x = rect.left + (offsetX || (rect.width >> 1))," +
                        "      y = rect.top + (offsetY || (rect.height >> 1))," +
                        "      dataTransfer = { files: this.files };" +
                        "" +
                        "  ['dragenter', 'dragover', 'drop'].forEach(function (name) {" +
                        "    var evt = document.createEvent('MouseEvent');" +
                        "    evt.initMouseEvent(name, !0, !0, window, 0, 0, 0, x, y, !1, !1, !1, !1, 0, null);" +
                        "    evt.dataTransfer = dataTransfer;" +
                        "    target.dispatchEvent(evt);" +
                        "  });" +
                        "" +
                        "  setTimeout(function () { document.body.removeChild(input); }, 25);" +
                        "};" +
                        "document.body.appendChild(input);" +
                        "return input;";

        WebElement input =  (WebElement)jse.executeScript(JS_DROP_FILE, target, offsetX, offsetY);
        input.sendKeys(filePath.getAbsoluteFile().toString());
        wait.until(ExpectedConditions.stalenessOf(input));
    }
    
    //method to use on easy apply aplication
    private static void easyApply(WebDriver driver) throws InterruptedException {
        Thread.sleep(3000);
        System.out.println();

        boolean isFinished = false;

        while (!isFinished) {

            if (isNeededManuallyCompletion(driver)) {
                manuallyContinue(driver);
                break;
            } else if (isHeaderLike(driver, "contact info")) {
                fillEasyApplyField(driver, "first name", "Hadas");
                fillEasyApplyField(driver, "last name", "Libman");
                fillEasyApplyField(driver, "phone", "123456789");
            } else if (isHeaderLike(driver, "resume")) {
            } else if (isHeaderLike(driver, "Home address")) {
                fillEasyApplyField(driver, "state", "NY");
                easyApplyAutoCompletionTextBox(driver, "city", "Waco, Texas");
            } else {
                manuallyContinue(driver);
                break;
            }


            Optional<WebElement> next = easyApplyClickButton(driver, "next");
            if (next.isPresent()) {
                next.get().click();
            } else {
                Optional<WebElement> review = easyApplyClickButton(driver, "review");
                if (review.isPresent()) {
                    review.get().click();
                } else {
                    Optional<WebElement> submitApplication = easyApplyClickButton(driver, "submit application");
                    // submitApplication.get().click();
                    isFinished = true;
                    String company = driver.findElement(By.cssSelector("h2[id*=jobs-apply-header]")).getText().replace("Apply to ", "");
                    System.out.println("Succesfully Submitted aplication to " + company);
                }
            }
        }

    }

    /**
	* method to tell the user to manually continue application
	* to be used on the 3rd page of workday, special cases of easy apply, and any other case
	*/
    private static void manuallyContinue(WebDriver driver) {
        String url = driver.getCurrentUrl();
        System.out.println("Go to browser and complete application: " + url);
        return;
    }

    
    /**
     * method to check if current page header contains "header"
     * @param driver
     * @param header the header string to search
     * @return
     */
    private static boolean isHeaderLike(WebDriver driver, String header) {
        return driver.findElements(By.cssSelector("h3")).stream().anyMatch(x -> x.getText().toLowerCase().contains(header.toLowerCase()));
    }

    //method to return true or false based on if header in easy apply contains 'additional', 'diversity', or 'allow access'
    private static boolean isNeededManuallyCompletion(WebDriver driver) {
        boolean cond1 = isHeaderLike(driver, "Additional") || isHeaderLike(driver, "Diversity");
        boolean cond2 = driver.findElements(By.cssSelector("button"))
                .stream()
                .anyMatch(x -> x.getText().contains("Allow access"));
        return cond2 || cond1;
    }
    
    //method to search for easy apply buttons (next, review, submit)
    private static Optional<WebElement> easyApplyClickButton(WebDriver driver, String buttonText) {
        return driver.findElements(By.cssSelector("button"))
                .stream()
                .filter(x -> x.getText().toLowerCase().contains(buttonText)).findAny();
    }

    /**
     * method to fill auto completion text box (city- special case)
     * @param driver
     * @param label text box label
     * @param value value to be completed
     */
    private static void easyApplyAutoCompletionTextBox(WebDriver driver, String label, String value) {
        try {
            driver.findElements(By.cssSelector("div[class*=jobs-easy-apply-form-section]")).stream()
                    .filter(section -> section.findElement(By.cssSelector("label")).getText().toLowerCase().contains(label.toLowerCase()))
                    .findAny()
                    .map(e -> {
                        WebElement element = e.findElement(By.cssSelector("input[class*=artdeco-typeahead]"));
                        element.clear();
                        try {
                            Thread.sleep(1000);
                            element.sendKeys(value);
                            Thread.sleep(2000);
                            e.findElement(By.cssSelector("div[aria-label*=Results]")).click();
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        return null;
                    });
        } catch (org.openqa.selenium.NoSuchElementException ignored){}
    }

    /**
     * method to auto fill text boxes
     * @param driver
     * @param label text box label
     * @param value to be filled
     */
    private static void fillEasyApplyField(WebDriver driver, String label, String value) {
        Stream<WebElement> easyApplyElements = driver.findElements(By.cssSelector("div[class*=jobs-easy-apply-form-section]")).stream();
        easyApplyElements
                .filter(section -> section.findElements(By.cssSelector("label")).stream().map(x -> x.getText().toLowerCase().contains(label.toLowerCase())).findFirst().orElse(false))
                .findAny()
                .map(e -> {
                    try {
                        WebElement element = e.findElement(By.cssSelector("input[class*=fb-single-line-text]"));
                        element.clear();
                        Thread.sleep(1000);
                        element.sendKeys(value);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    } catch (org.openqa.selenium.NoSuchElementException ignored) {}
                    return null;
                });
    }

    /**
     * method to check if position label contains specific arguments before opening in a different tab
     * if there is no match, skip position
     * only positions that contain 'product management intern' or 'product manager intern' will open
     */
    private static Boolean match(String title) {
	       // return title.toLowerCase().contains("product management intern") || title.toLowerCase().contains("product manager intern");
        return true;
    }

}