# App Validation

Sistema de validação de transação de uma conta, baseado em microsserviços desenvolvido em Kotlin com Spring Boot, implementando arquitetura hexagonal e comunicação assíncrona.

## Índice

- [Visão Geral](#-visão-geral)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Execução do Projeto](#-execução-do-projeto)

## Visão Geral

O módulo `app-validation` é responsável pela validação de uma transação dentro do ecossistema de microsserviços. Implementa comunicação assíncrona via SQS e gRPC, armazenamento em DynamoDB e serialização eficiente com gRPC.

### Características Principais

- **Arquitetura Hexagonal** (Ports & Adapters)
- **Spring Boot 3**
- **Spring Cloud 2025**
- **gRPC** para comunicação síncrona de alta performance
- **Amazon DynamoDB** como banco de dados NoSQL
- **Observabilidade** com Prometheus e Micrometer
- **Cobertura de Testes** > 95% (Branch, Line, Instruction)
- **Containerização** com Docker
- **Health Checks** integrados

## Tecnologias Utilizadas

### Core Framework
- **Kotlin 2.2** - Linguagem principal
- **JDK 21** - Java Development Kit

### Comunicação e Messaging
- **Spring Cloud AWS** - Abstração para messaging
- **gRPC** - Comunicação RPC de alta performance

### Persistência
- **Amazon DynamoDB** - Banco de dados NoSQL
- **Spring Cloud AWS** - Integração com serviços AWS

### Observabilidade e Monitoramento
- **Micrometer** - Métricas de aplicação
- **Prometheus** - Sistema de monitoramento
- **Spring Boot Actuator** - Health checks e endpoints de gestão

### Qualidade e Testes
- **JUnit 5** - Framework de testes
- **MockK** - Mocking para Kotlin
- **Kover** - Cobertura de código para Kotlin

### Build e DevOps
- **Gradle Kotlin DSL** - Build automation
- **Docker** - Containerização
- **Amazon Corretto 21** - JVM otimizada para produção

## Arquitetura

O módulo segue os princípios da **Arquitetura Hexagonal (Ports & Adapters)**

## Pré Requisitos
- **JDK 21**
- **Docker**
- **Docker Compose**
- **Git**
- **Gradle**
- **LocalStack**
- **SQS**
- **DynamoDB**

## Execução do projeto
- **Para desenvolvimento**: 
  - Executar o arquivo [docker-compose.yml](../docker-compose.yml)
  - Executar o comando `./gradlew :app-validation:clean :app-validation:build`
  - Executar o comando `./gradlew :app-validation:bootRun` com o profile `localstack`
- **Projeto Containerizado**:
  - Executar o build do módulo `./gradlew :app-common:clean :app-common:build`
  - Executar o build do módulo `./gradlew :app-authorization:clean :app-authorization:build`
  - Executar o build do módulo `./gradlew :app-validation:clean :app-validation:build`
  - Executar o build do módulo `./gradlew :app-web:clean :app-web:build` 
  - Executar o docker `docker compose -f docker-compose-all.yml up --build`
- **Helper para execução**:
    - **Windows**: `.\run-project.ps1`
    - **Linux**: `./run-project.sh` 
