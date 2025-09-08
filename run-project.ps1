# =============================================================================
# Script de Execução do Projeto - Authorizer Microservices (PowerShell)
# =============================================================================
# Executa build completo de todos os módulos e inicialização via Docker Compose
#
# Uso: .\run-project.ps1
#
# Pré-requisitos:
# - JDK 21+
# - Docker Desktop
# - Git
# - PowerShell 5.1+
# =============================================================================

# Configurar política de execução e tratamento de erros
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Função para imprimir cabeçalhos coloridos
function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════════════════" -ForegroundColor Blue
    Write-Host "  $Message" -ForegroundColor Blue
    Write-Host "═══════════════════════════════════════════════════════════════════" -ForegroundColor Blue
    Write-Host ""
}

function Write-Step {
    param([string]$Message)
    Write-Host "➤ $Message" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

# Função para verificar se comando existe
function Test-Command {
    param([string]$Command, [string]$Description)

    if (-not (Get-Command $Command -ErrorAction SilentlyContinue)) {
        Write-Error-Custom "Comando '$Command' não encontrado. Por favor, instale $Description"
        exit 1
    }
}

# Função de cleanup em caso de erro
function Invoke-Cleanup {
    Write-Error-Custom "Erro detectado. Executando cleanup..."
    try {
        docker compose -f docker-compose-all.yml down --remove-orphans 2>$null
    }
    catch {
        # Ignora erros de cleanup
    }
    exit 1
}

# Configurar trap para cleanup em caso de erro
trap { Invoke-Cleanup }

# =============================================================================
# VERIFICAÇÃO DE PRÉ-REQUISITOS
# =============================================================================
Write-Header "VERIFICAÇÃO DE PRÉ-REQUISITOS"

Write-Step "Verificando PowerShell..."
$psVersion = $PSVersionTable.PSVersion
if ($psVersion.Major -ge 5) {
    Write-Success "PowerShell $($psVersion.Major).$($psVersion.Minor) detectado"
} else {
    Write-Error-Custom "PowerShell 5.1+ é obrigatório"
    exit 1
}

Write-Step "Verificando JDK 21..."
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.Line }
    if ($javaVersion -match '"(2[1-9]|[3-9]\d|\d{3,})\.' -or $javaVersion -match '"21\.') {
        Write-Success "JDK detectado: $($javaVersion -replace '.*"([^"]*)".*', '$1')"
    } else {
        Write-Error-Custom "JDK 21+ é obrigatório"
        exit 1
    }
}
catch {
    Write-Error-Custom "Java não encontrado. Por favor, instale JDK 21+"
    exit 1
}

Write-Step "Verificando Docker..."
Test-Command "docker" "Docker Desktop"
$dockerVersion = (docker --version).Split(' ')[2].TrimEnd(',')
Write-Success "Docker $dockerVersion detectado"

Write-Step "Verificando Docker Compose..."
try {
    $composeVersion = docker compose version --short 2>$null
    if ($composeVersion) {
        Write-Success "Docker Compose $composeVersion detectado"
    } else {
        Test-Command "docker-compose" "Docker Compose"
        $composeVersion = (docker-compose --version).Split(' ')[2].TrimEnd(',')
        Write-Success "Docker Compose $composeVersion detectado"
    }
}
catch {
    Write-Error-Custom "Docker Compose não encontrado"
    exit 1
}

Write-Step "Verificando Gradle Wrapper..."
if (Test-Path ".\gradlew.bat") {
    Write-Success "Gradle Wrapper configurado"
} else {
    Write-Error-Custom "gradlew.bat não encontrado. Execute este script na raiz do projeto"
    exit 1
}

# =============================================================================
# BUILD DOS MÓDULOS
# =============================================================================
Write-Header "BUILD DOS MÓDULOS DO PROJETO"

# Array com os módulos na ordem correta de build
$modules = @("app-common", "app-authorization", "app-validation", "app-web")

foreach ($module in $modules) {
    Write-Step "Building módulo: $module"

    $startTime = Get-Date

    try {
        $result = & .\gradlew.bat ":$module:clean" ":$module:build" --no-daemon --console=plain
        if ($LASTEXITCODE -eq 0) {
            $duration = [math]::Round(((Get-Date) - $startTime).TotalSeconds)
            Write-Success "Módulo $module compilado com sucesso (${duration}s)"
        } else {
            throw "Build falhou"
        }
    }
    catch {
        Write-Error-Custom "Falha no build do módulo $module"
        exit 1
    }
}

Write-Success "Todos os módulos compilados com sucesso!"

# =============================================================================
# DOCKER COMPOSE SETUP
# =============================================================================
Write-Header "INICIALIZAÇÃO DOS SERVIÇOS DOCKER"

Write-Step "Parando containers existentes..."
try {
    docker compose -f docker-compose-all.yml down --remove-orphans 2>$null | Out-Null
}
catch {
    # Ignora erros se não houver containers
}
Write-Success "Containers anteriores removidos"

Write-Step "Iniciando serviços via Docker Compose..."
$startTime = Get-Date

try {
    $result = docker compose -f docker-compose-all.yml up --build -d
    if ($LASTEXITCODE -eq 0) {
        $duration = [math]::Round(((Get-Date) - $startTime).TotalSeconds)
        Write-Success "Serviços iniciados com sucesso (${duration}s)"
    } else {
        throw "Docker Compose falhou"
    }
}
catch {
    Write-Error-Custom "Falha na inicialização dos serviços Docker"
    exit 1
}

# =============================================================================
# VERIFICAÇÃO DE SAÚDE DOS SERVIÇOS
# =============================================================================
Write-Header "VERIFICAÇÃO DE SAÚDE DOS SERVIÇOS"

Write-Step "Aguardando inicialização dos serviços..."
Start-Sleep -Seconds 10

# Verificar serviços básicos
$servicesToCheck = @(
    @{Name="kafka"; Port=9092},
    @{Name="zookeeper"; Port=2181},
    @{Name="schema-registry"; Port=8081},
    @{Name="localstack"; Port=4566}
)

foreach ($service in $servicesToCheck) {
    Write-Step "Verificando $($service.Name) na porta $($service.Port)..."

    $timeout = 30
    $timer = 0
    $connected = $false

    do {
        try {
            $connection = New-Object System.Net.Sockets.TcpClient
            $connection.ConnectAsync("localhost", $service.Port).Wait(1000)
            if ($connection.Connected) {
                $connected = $true
                $connection.Close()
            }
        }
        catch {
            Start-Sleep -Seconds 1
            $timer++
        }
    } while (-not $connected -and $timer -lt $timeout)

    if ($connected) {
        Write-Success "Serviço $($service.Name) está ativo"
    } else {
        Write-Error-Custom "Serviço $($service.Name) não está respondendo na porta $($service.Port)"
    }
}

# Verificar aplicações (com retry)
$appsToCheck = @(
    @{Name="app-authorization"; Port=8200; Endpoint="/actuator/health"},
    @{Name="app-validation"; Port=8201; Endpoint="/actuator/health"},
    @{Name="app-web"; Port=8080; Endpoint="/actuator/health"}
)

foreach ($app in $appsToCheck) {
    Write-Step "Verificando health check do $($app.Name)..."

    $retryCount = 6
    $success = $false

    for ($i = 1; $i -le $retryCount; $i++) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$($app.Port)$($app.Endpoint)" -TimeoutSec 5 -UseBasicParsing
            if ($response.StatusCode -eq 200) {
                Write-Success "$($app.Name) está saudável"
                $success = $true
                break
            }
        }
        catch {
            if ($i -eq $retryCount) {
                Write-Error-Custom "$($app.Name) não está respondendo ao health check"
            } else {
                Write-Host "." -NoNewline
                Start-Sleep -Seconds 10
            }
        }
    }

    if (-not $success) {
        Write-Host ""  # Nova linha após os pontos
    }
}

# =============================================================================
# INFORMAÇÕES FINAIS
# =============================================================================
Write-Header "PROJETO INICIADO COM SUCESSO!"

Write-Host "🚀 Todos os serviços estão executando!" -ForegroundColor Green
Write-Host ""

Write-Host "📋 ENDPOINTS DISPONÍVEIS:" -ForegroundColor Blue
Write-Host "┌─────────────────────────────────────────────────────────────────┐"
Write-Host "│ APLICAÇÕES                                                      │"
Write-Host "├─────────────────────────────────────────────────────────────────┤"
Write-Host "│ • App Web:          http://localhost:8080                      │"
Write-Host "│ • App Authorization: http://localhost:8200                     │"
Write-Host "│ • App Validation:   http://localhost:8201                      │"
Write-Host "├─────────────────────────────────────────────────────────────────┤"
Write-Host "│ INFRAESTRUTURA                                                  │"
Write-Host "├─────────────────────────────────────────────────────────────────┤"
Write-Host "│ • Kafka Broker:     localhost:9092                             │"
Write-Host "│ • Schema Registry:  http://localhost:8081                      │"
Write-Host "│ • LocalStack:       http://localhost:4566                      │"
Write-Host "│ • DynamoDB Local:   http://localhost:4566                      │"
Write-Host "├─────────────────────────────────────────────────────────────────┤"
Write-Host "│ MONITORAMENTO                                                   │"
Write-Host "├─────────────────────────────────────────────────────────────────┤"
Write-Host "│ • Authorization Health: http://localhost:8200/actuator/health  │"
Write-Host "│ • Validation Health:    http://localhost:8201/actuator/health  │"
Write-Host "│ • Web Health:          http://localhost:8080/actuator/health   │"
Write-Host "└─────────────────────────────────────────────────────────────────┘"

Write-Host ""
Write-Host "📝 COMANDOS ÚTEIS:" -ForegroundColor Yellow
Write-Host "• Ver logs:           docker compose -f docker-compose-all.yml logs -f"
Write-Host "• Parar serviços:     docker compose -f docker-compose-all.yml down"
Write-Host "• Status containers:  docker compose -f docker-compose-all.yml ps"
Write-Host "• Rebuild aplicação:  docker compose -f docker-compose-all.yml up --build -d"

Write-Host ""
Write-Host "✨ Projeto pronto para desenvolvimento!" -ForegroundColor Green
Write-Host "Para parar todos os serviços: docker compose -f docker-compose-all.yml down" -ForegroundColor Blue
Write-Host ""

# Opcional: Abrir browser automaticamente (descomente se desejar)
# Start-Process "http://localhost:8080"

# Pausa para manter a janela aberta se executado diretamente
if ($Host.Name -eq "ConsoleHost") {
    Write-Host "Pressione qualquer tecla para continuar..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}
