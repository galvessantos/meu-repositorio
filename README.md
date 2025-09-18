# Projeto MSIAV - Gestão de Garantias

## Descrição do Projeto

O sistema Msiav dá ao agente financeiro a possibilidade de aprovar todos os custos envolvidos no processo de recuperação extrajudicial e realizar a intervenção ou cancelamento do mesmo a qualquer momento, dando a este total controle sobre cada processo e seu andamento.

**Início Rápido - Quick Start**

Solicitar a recuperação extrajudicial agora é muito simples! O sistema Msiav dá ao agente financeiro a possibilidade de aprovar todos os custos envolvidos no processo e realizar a intervenção ou cancelamento do mesmo a qualquer momento, dando a este total controle sobre cada processo e seu andamento.

## Especificações Técnicas

- **Java**: 17
- **Spring Boot**: 3.2.2
- **Maven**: 3.8.3
- **Banco de Dados**: PostgreSQL
- **Documentação API**: Swagger/OpenAPI

## Dependências Principais

- Spring Boot Web
- Spring Boot Security
- Spring Boot Data JPA
- Spring Boot Validation
- Spring Boot Mail
- Spring Boot OAuth2 Resource Server
- Lombok
- MapStruct
- Swagger/OpenAPI
- PDF Box
- Thymeleaf

## Como Executar o Projeto

### Pré-requisitos

- Java 17
- Maven 3.8.3+
- PostgreSQL

### Usando Maven

1. Clone o repositório:
   ```
   git clone [URL_DO_REPOSITÓRIO]
   ```

2. Navegue até o diretório do projeto:
   ```
   cd MONTREAL-ACELERA-MAKER-GESTAO-GARANTIAS-BACKEND
   ```

3. Compile o projeto:
   ```
   mvn clean package
   ```

4. Execute a aplicação:
   ```
   java -jar target/garantias-api-0.0.1-SNAPSHOT.jar
   ```

### Usando Docker

1. Clone o repositório:
   ```
   git clone [URL_DO_REPOSITÓRIO]
   ```

2. Navegue até o diretório do projeto:
   ```
   cd MONTREAL-ACELERA-MAKER-GESTAO-GARANTIAS-BACKEND
   ```

3. Construa a imagem Docker:
   ```
   docker build -t garantias-api .
   ```

4. Execute o container:
   ```
   docker run -p 8080:8080 -p 8082:8082 garantias-api
   ```

## Perfis de Execução

O projeto suporta diferentes perfis de execução:

- **training**: Ambiente de treinamento
- **dev**: Ambiente de desenvolvimento
- **hml**: Ambiente de homologação
- **prod**: Ambiente de produção

Para executar com um perfil específico:

```
java -jar -Dspring.profiles.active=dev garantias-api.jar
```

Ou com Maven:

```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Documentação da API

A documentação da API está disponível através do Swagger UI, acessível em:

```
http://localhost:8080/swagger-ui/index.html
```

## Portas

A aplicação utiliza as seguintes portas:
- 8080: Porta principal da aplicação
- 8082: Porta secundária (monitoramento/atuação)