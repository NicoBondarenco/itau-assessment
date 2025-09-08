#!/bin/bash

# =============================================================================
# Script de Execução do Projeto - Authorizer Microservices
# =============================================================================
# Executa build completo de todos os módulos e inicialização via Docker Compose
#
# Uso: ./run-project.sh
#
# Pré-requisitos:
# - JDK 21+
# - Docker & Docker Compose
# - Git
# =============================================================================

set -e  # Parar execução em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funções utilitárias
print_header() {
    echo -e "\n${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}\n"
}

print_step() {
    echo -e "${YELLOW}➤ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Função para verificar se comando existe
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "Comando '$1' não encontrado. Por favor, instale $2"
        exit 1
    fi
}

# Função de cleanup em caso de erro
cleanup_on_error() {
    print_error "Erro detectado. Executando cleanup..."
    docker compose -f docker-compose-all.yml down --remove-orphans 2>/dev/null || true
    exit 1
}

# Configurar trap para cleanup em caso de erro
trap cleanup_on_error ERR

# =============================================================================
# VERIFICAÇÃO DE PRÉ-REQUISITOS
# =============================================================================
print_header "VERIFICAÇÃO DE PRÉ-REQUISITOS"

print_step "Verificando JDK 21..."
if java -version 2>&1 | grep -q "21\|2[2-9]\|[3-9][0-9]"; then
    print_success "JDK $(java -version 2>&1 | head -n1 | cut -d'"' -f2) detectado"
else
    print_error "JDK 21+ é obrigatório"
    exit 1
fi

print_step "Verificando Docker..."
check_command "docker" "Docker"
print_success "Docker $(docker --version | cut -d' ' -f3 | cut -d',' -f1) detectado"

print_step "Verificando Docker Compose..."
if docker compose version &> /dev/null; then
    print_success "Docker Compose $(docker compose version --short 2>/dev/null || echo 'v2+') detectado"
else
    check_command "docker-compose" "Docker Compose"
    print_success "Docker Compose $(docker-compose --version | cut -d' ' -f3 | cut -d',' -f1) detectado"
fi

print_step "Verificando Gradle Wrapper..."
if [[ -f "./gradlew" ]]; then
    chmod +x ./gradlew
    print_success "Gradle Wrapper configurado"
else
    print_error "gradlew não encontrado. Execute este script na raiz do projeto"
    exit 1
fi

# =============================================================================
# BUILD DOS MÓDULOS
# =============================================================================
print_header "BUILD DOS MÓDULOS DO PROJETO"

# Array com os módulos na ordem correta de build
modules=("app-common" "app-authorization" "app-validation" "app-web")

for module in "${modules[@]}"; do
    print_step "Building módulo: $module"

    start_time=$(date +%s)

    if ./gradlew :$module:clean :$module:build --no-daemon --console=plain; then
        end_time=$(date +%s)
        duration=$((end_time - start_time))
        print_success "Módulo $module compilado com sucesso (${duration}s)"
    else
        print_error "Falha no build do módulo $module"
        exit 1
    fi
done

print_success "Todos os módulos compilados com sucesso!"

# =============================================================================
# DOCKER COMPOSE SETUP
# =============================================================================
print_header "INICIALIZAÇÃO DOS SERVIÇOS DOCKER"

print_step "Parando containers existentes..."
docker compose -f docker-compose-all.yml down --remove-orphans 2>/dev/null || true
print_success "Containers anteriores removidos"

print_step "Iniciando serviços via Docker Compose..."
start_time=$(date +%s)

if docker compose -f docker-compose-all.yml up --build -d; then
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    print_success "Serviços iniciados com sucesso (${duration}s)"
else
    print_error "Falha na inicialização dos serviços Docker"
    exit 1
fi

# =============================================================================
# VERIFICAÇÃO DE SAÚDE DOS SERVIÇOS
# =============================================================================
print_header "VERIFICAÇÃO DE SAÚDE DOS SERVIÇOS"

print_step "Aguardando inicialização dos serviços..."
sleep 10

# Verificar serviços básicos
services_to_check=(
    "kafka:9092"
    "zookeeper:2181"
    "schema-registry:8081"
    "localstack:4566"
)

for service in "${services_to_check[@]}"; do
    service_name=$(echo $service | cut -d':' -f1)
    port=$(echo $service | cut -d':' -f2)

    print_step "Verificando $service_name na porta $port..."

    if timeout 30 bash -c "until nc -z localhost $port; do sleep 1; done" 2>/dev/null; then
        print_success "Serviço $service_name está ativo"
    else
        print_error "Serviço $service_name não está respondendo na porta $port"
    fi
done

# Verificar aplicações (com retry)
apps_to_check=(
    "app-authorization:8200:/actuator/health"
    "app-validation:8201:/actuator/health"
    "app-web:8080:/actuator/health"
)

for app in "${apps_to_check[@]}"; do
    app_name=$(echo $app | cut -d':' -f1)
    port=$(echo $app | cut -d':' -f2)
    endpoint=$(echo $app | cut -d':' -f3)

    print_step "Verificando health check do $app_name..."

    # Retry logic para health checks
    for i in {1..6}; do
        if curl -sf "http://localhost:$port$endpoint" > /dev/null 2>&1; then
            print_success "$app_name está saudável"
            break
        elif [[ $i -eq 6 ]]; then
            print_error "$app_name não está respondendo ao health check"
        else
            echo -n "."
            sleep 10
        fi
    done
done

# =============================================================================
# INFORMAÇÕES FINAIS
# =============================================================================
print_header "PROJETO INICIADO COM SUCESSO!"

echo -e "${GREEN}🚀 Todos os serviços estão executando!${NC}\n"

echo -e "${BLUE}📋 ENDPOINTS DISPONÍVEIS:${NC}"
echo "┌─────────────────────────────────────────────────────────────────┐"
echo "│ APLICAÇÕES                                                      │"
echo "├─────────────────────────────────────────────────────────────────┤"
echo "│ • App Web:          http://localhost:8080                      │"
echo "│ • App Authorization: http://localhost:8200                     │"
echo "│ • App Validation:   http://localhost:8201                      │"
echo "├─────────────────────────────────────────────────────────────────┤"
echo "│ INFRAESTRUTURA                                                  │"
echo "├─────────────────────────────────────────────────────────────────┤"
echo "│ • Kafka Broker:     localhost:9092                             │"
echo "│ • Schema Registry:  http://localhost:8081                      │"
echo "│ • LocalStack:       http://localhost:4566                      │"
echo "│ • DynamoDB Local:   http://localhost:4566                      │"
echo "├─────────────────────────────────────────────────────────────────┤"
echo "│ MONITORAMENTO                                                   │"
echo "├─────────────────────────────────────────────────────────────────┤"
echo "│ • Authorization Health: http://localhost:8200/actuator/health  │"
echo "│ • Validation Health:    http://localhost:8201/actuator/health  │"
echo "│ • Web Health:          http://localhost:8080/actuator/health   │"
echo "└─────────────────────────────────────────────────────────────────┘"

echo -e "\n${YELLOW}📝 COMANDOS ÚTEIS:${NC}"
echo "• Ver logs:           docker compose -f docker-compose-all.yml logs -f"
echo "• Parar serviços:     docker compose -f docker-compose-all.yml down"
echo "• Status containers:  docker compose -f docker-compose-all.yml ps"
echo "• Rebuild aplicação:  docker compose -f docker-compose-all.yml up --build -d"

echo -e "\n${GREEN}✨ Projeto pronto para desenvolvimento!${NC}"
echo -e "${BLUE}Para parar todos os serviços: docker compose -f docker-compose-all.yml down${NC}\n"

# Opcional: Abrir browser automaticamente (descomente se desejar)
# if command -v xdg-open &> /dev/null; then
#     xdg-open http://localhost:8080
# elif command -v open &> /dev/null; then
#     open http://localhost:8080
# fi
