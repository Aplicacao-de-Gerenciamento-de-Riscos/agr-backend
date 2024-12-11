# 🚀 Aplicação de Gerenciamento de Riscos - Backend

Este repositório contém a aplicação backend para o gerenciamento de riscos, desenvolvida em **Java 19** com **Spring Boot** e utilizando o banco de dados **PostgreSQL**.

---

## 📋 Pré-requisitos

Antes de rodar o projeto, você precisa ter os seguintes itens instalados:

- ☕ **Java 19 (JDK 19)** - [Download aqui](https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html)
- 🛠️ **Apache Maven** - [Guia de instalação](https://maven.apache.org/install.html)
- 🐘 **PostgreSQL** - [Guia de instalação](https://www.postgresql.org/download/)

Além disso, o projeto exige duas variáveis de ambiente:

- 🔑 `JIRA_API_USER`: Usuário da API JIRA.
- 🔒 `JIRA_API_SECRET`: Token/segredo da API JIRA.

---

## ⚙️ Configuração das Variáveis de Ambiente

### Windows

```cmd
set JIRA_API_USER=seu_usuario_jira
set JIRA_API_SECRET=seu_secreto_jira
```

### Linux/Mac

```bash
export JIRA_API_USER=seu_usuario_jira
export JIRA_API_SECRET=seu_secreto_jira
```

---

## 🐘 Configuração do Banco de Dados PostgreSQL

1. **Crie o banco de dados e o usuário:**

   ```sql
   CREATE DATABASE agrbackend-dev;
   CREATE USER usuario_riscos WITH ENCRYPTED PASSWORD 'senha_riscos';
   GRANT ALL PRIVILEGES ON DATABASE agrbackend-dev TO usuario_riscos;
   ```

2. **Atualize o `application.properties` com as credenciais:**

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/agrbackend-dev
   spring.datasource.username=usuario_riscos
   spring.datasource.password=senha_riscos
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

---

## 🚀 Rodando a Aplicação

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/Aplicacao-de-Gerenciamento-de-Riscos/agr-backend.git
   cd agr-backend
   ```

2. **Instale as dependências:**

   ```bash
   mvn clean install
   ```

3. **Execute a aplicação:**

   ```bash
   mvn spring-boot:run
   ```

4. **Acesse a aplicação em:**

   ```
   http://localhost:8080
   ```

---

## 📦 Gerando o `.jar`

Para gerar um `.jar` executável, utilize:

```bash
mvn clean package
```

O arquivo `.jar` será criado em `target/`. Exemplo:

```
target/agr-backend-0.0.1-SNAPSHOT.jar
```

### ▶️ Executando o `.jar`

```bash
java -jar target/agr-backend-0.0.1-SNAPSHOT.jar
```
