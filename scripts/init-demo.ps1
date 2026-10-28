$ErrorActionPreference = 'Stop'
$configurationPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../.env'))
if (Test-Path -LiteralPath $configurationPath) {
    throw '.env already exists. Keep the existing secrets or edit that file explicitly.'
}
function New-DemoSecret {
    $secretBytes = New-Object byte[] 32
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $generator.GetBytes($secretBytes)
        return [Convert]::ToBase64String($secretBytes)
    } finally {
        $generator.Dispose()
    }
}
$configurationLines = @(
    "DB_PASSWORD=$(New-DemoSecret)"
    "MYSQL_ROOT_PASSWORD=$(New-DemoSecret)"
    "JWT_SECRET=$(New-DemoSecret)"
    "DEMO_ADMIN_PASSWORD=$(New-DemoSecret)"
    'SPRING_PROFILES_ACTIVE=demo'
    'APP_PORT=8080'
)
$configurationLines | Set-Content -LiteralPath $configurationPath -Encoding ASCII
Write-Output 'Created .env with generated secrets. The demo admin password is in DEMO_ADMIN_PASSWORD.'
Write-Output 'Start the application with: docker compose up --build -d'
