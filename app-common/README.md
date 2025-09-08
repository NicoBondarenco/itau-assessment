# App Common

Biblioteca de código centralizado e compartilhado entre todos os módulos do projeto, contém modelos, configurações e mappers, implementando arquitetura hexagonal.

## Índice

- [Visão Geral](#-visão-geral)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Execução do Projeto](#-execução-do-projeto)

## Visão Geral

O módulo `app-common` é responsável pela centralização de código compartilhado entre os módulos do projeto.

### Características Principais

- **Arquitetura Hexagonal** (Ports & Adapters)
- **Spring Boot 3**
- **gRPC** para geração dos modelos
- **Amazon DynamoDB** modelos das entidades do banco
- **Cobertura de Testes** > 95% (Branch, Line, Instruction)

## Tecnologias Utilizadas

### Core Framework
- **Kotlin 2.2** - Linguagem principal
- **JDK 21** - Java Development Kit

### Qualidade e Testes
- **JUnit 5** - Framework de testes
- **MockK** - Mocking para Kotlin
- **Kover** - Cobertura de código para Kotlin

### Build e DevOps
- **Gradle Kotlin DSL** - Build automation
- **Amazon Corretto 21** - JVM otimizada para produção

## Arquitetura

O módulo segue os princípios da **Arquitetura Hexagonal (Ports & Adapters)**

## Pré Requisitos
- **JDK 21**
- **Git**
- **Gradle**
- **gRPC**
- **DynamoDB**

## Execução do projeto
- **Para desenvolvimento**: 
  - Este módulo é uma biblioteca não executável
- **Projeto Containerizado**:
  - Executar o build do módulo `./gradlew :app-common:clean :app-common:build`
  - Executar o build do módulo `./gradlew :app-authorization:clean :app-authorization:build`
  - Executar o build do módulo `./gradlew :app-validation:clean :app-validation:build`
  - Executar o build do módulo `./gradlew :app-web:clean :app-web:build` 
  - Executar o docker `docker compose -f docker-compose-all.yml up --build`
