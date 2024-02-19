# _teste-software-api_
This project is based on [test-software](https://github.com/leonidesfernando/teste-software) other project, 
which was created to explorer tests with: TestNG, Mockito, REST Assured, Cucumber, Selenium Webdriver and JMeter.

## Docker
- JDK 17
- port _8080_
- to build the image: `docker build -t test-software/api .` (or put whatever name you want instead of _teste-software_)
- to run: `docker run --name test-software-api -p 8080:8080 test-software-api/api` (or the name of the image you gave on **build**)
  - _--name will be the container name_

## Requirements
- Maven
- JDK 17+
- Spring Boot 2.6.15+

## Structure
This project uses some frameworks such as Spring Boot(thymeleaf, bootstrap) and Lombok, in case you come to use an IDE that has plugins to them, it's recommended that you install it. 
   
### For IntelijIDEA:
1. Enable annotation processing: 
   1. Settings->Compiler->Annotation Processors: "Enable annotation processing"
2. Install plugins to Spring Boot and Lombok(via Marketplace)
3. Restart IDEA and enjoy it.

### Coverage
It's used [_Jacoco_](https://www.jacoco.org/jacoco/trunk/index.html) to apply the expected coverage in the code.
To apply the expected coverage ratio just execute: `mvn clean verify`
and check the report on `/target/jacoco-report`.
In case the coverage ratio is below expectations, it'll get a failure, 
and you will see where the violation happened.
You can adjust the coverage changing the value of this property
`<coverage_ratio_percentage>` on **pom.xml**.

![Coverage violations](/src/test/resources/readme_assets/coverage_violation.png)


### Login:
* The credentias are inside ``security.acesss.properties`` change as you wish.

---
[Português](README.pt_br.md)