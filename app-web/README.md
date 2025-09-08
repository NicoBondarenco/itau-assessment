# App Web

Sistema helper para execução das transações, criação de contas e demais funções para testar o projeto, baseado em microsserviços desenvolvido em Kotlin com Spring Boot, implementando arquitetura hexagonal e comunicação assíncrona.

## Índice

- [Visão Geral](#-visão-geral)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Execução do Projeto](#-execução-do-projeto)

## Visão Geral

O módulo `app-web` é responsável pelas funções helper, para execução das transações, criação de contas e demais funções para testar o projeto, dentro do ecossistema de microsserviços. Implementa comunicação assíncrona via SQS e armazenamento em DynamoDB.
Este módulo é somente um helper, não possui testes, métricas ou monitoramento.

### Características Principais

- **Arquitetura Hexagonal** (Ports & Adapters)
- **Spring Boot 3**
- **Spring Cloud 2025**
- **SQS** para comunicação assíncrona de alta performance
- **Amazon DynamoDB** como banco de dados NoSQL
- **Containerização** com Docker
- **Health Checks** integrados

## Tecnologias Utilizadas

### Core Framework
- **Kotlin 2.2** - Linguagem principal
- **JDK 21** - Java Development Kit

### Comunicação e Messaging
- **Spring Cloud AWS** - Abstração para messaging
- **SQS** - Comunicação por mensagens de alta performance

### Persistência
- **Amazon DynamoDB** - Banco de dados NoSQL
- **Spring Cloud AWS** - Integração com serviços AWS

### Observabilidade
- **Spring Boot Actuator** - Health checks e endpoints de gestão

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
  - Executar o comando `./gradlew :app-web:clean :app-web:build`
  - Executar o comando `./gradlew :app-web:bootRun` com o profile `localstack`
- **Projeto Containerizado**:
  - Executar o build do módulo `./gradlew :app-common:clean :app-common:build`
  - Executar o build do módulo `./gradlew :app-authorization:clean :app-authorization:build`
  - Executar o build do módulo `./gradlew :app-validation:clean :app-validation:build`
  - Executar o build do módulo `./gradlew :app-web:clean :app-web:build` 
  - Executar o docker `docker compose -f docker-compose-all.yml up --build`
- **Helper para execução**:
    - **Windows**: `.\run-project.ps1`
    - **Linux**: `./run-project.sh` 
