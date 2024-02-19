# _teste-software-api_
Este projeto é baseado no projeto [test-software](https://github.com/leonidesfernando/teste-software),
o qual foi criado para explorar testes com: TestNG, Mockito, REST Assured, Cucumber, Selenium Webdriver e JMeter.

## Docker
- JDK 17
- porta _8080_
- para fazer o build da imagem: `docker build -t teste-software .` (ou coloque o nome que quiser no lugar de _teste-software_)
- para executar: `docker run -p 8080:8080 teste-software` (ou o nome da imagem que você deu no **build**)

## Requisitos
- Maven
- JDK 17+
- Spring Boot 2.6.15+


## Estrutura
Este projeto alguns frameworks como SpringBoot e Lombok. Caso venha usar uma IDE que possua plugins para esses frameworks, recomenda-se que faça a instalação.

### No IntelijIDEA:
1. Habilitar annotaion processing:
   Settings->Compiler->Annotation Processors: "Enable annotation processing"
2. Instalar o plugin do Lombok, Spring e Spring Boot(via Marketplace)
3. Reiniciar o IDEA


### Cobertura
É usado o [_Jacoco_](https://www.jacoco.org/jacoco/trunk/index.html) para aplicar a cobertura esperada no código.
Para aplicar a taxa de cobertura esperada basta executar: `mnv clean verify`
e verificar o relatório em `/target/jacoco-report`.
Caso a taxa de cobertura esteja abaixo do esperado, o comando falhará e você poderá ver onde ocorreu a violação.
Você pode ajustar a cobertura mudando o valor desta propriedade 
`<coverage_ratio_percentage>` no **pom.xml**.

![Coverage violations](/src/test/resources/readme_assets/coverage_violation.png)


### Login:
* As credenciais estão no arquivo ``security.acesss.properties`` altere como desejar.  

---
[English](README.md)