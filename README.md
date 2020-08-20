# easyapply

A system using Selenium WebDriver that automatically finds and submits multiple job applications through LinkedIn at once.

The system allows the user to enter number of applications to submit and specific keywords by which it further filters positions.

Covers most cases of LinkedIn easy apply, and Workday applications. In special cases, program prompts user to continue application manually.

**Cases covered** 
1. Linkedin Easy Apply applications 

2. Workday applications when user does not need to create an account (fills pages one and two of application)

3. Workday applications when user does need to create an account

4. In unfalimiar cases, the program promtes the user to continue application manually


### Set up

1. **Lines 30 and 33:** set personal driver's property and driver's type

2. **Lines 50 and 57:** set user's Linkedin log in information and password

3. **Line 73:** set position to be searched and in the method 'match' set specific keywords to which the system will further filter

4. **Line 95:** set number of tabs to be opened at a time (as of now, system works only with first page of Linkedin so max value can be 25)

5. **Lines 185-187:** set email and password (to be used in cases of Workday applications that require creating an account)

6. **Line 199:** set perosnal directory to resume (to be uploaded to Workday)

7. **Lines 209-383:** fill personal information to be used in pages one and two of Workday applications (some additional changes may be needed according to user's resume)

8. **Lines 450-458:** set personal information to be used for Linkedin Easy Apply applications

### Additional

* For Linkedin Easy Apply cases --> resume needs to have been previously uploaded before running the program initially

* If user wants Linkedin Easy Apply to submit applications, uncomment line 575 in the code (it is commented out for testing purposes) 

### Example runs

[Workday Example](https://youtu.be/VJ9W9tRXhDY)

[LinkedIn Easy Apply examples](https://youtu.be/IffIbOj79gE)
