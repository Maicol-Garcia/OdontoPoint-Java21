$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
$previousUser = $env:ODONTO_MYSQL_USER
$previousPassword = $env:ODONTO_MYSQL_PASSWORD
try {
    $jar = "$PSScriptRoot\bin\odontopoint-web.jar"
    if (-not (Test-Path $jar)) { $jar = "$PSScriptRoot\target\odontopoint-web-2.0.0.jar" }
    if (-not (Test-Path $jar)) {
        Write-Host "No existe el JAR compilado. Ejecute primero Compilar-Windows.bat." -ForegroundColor Yellow
        Read-Host "Enter para cerrar"
        exit 1
    }
    $dbUser = Read-Host "Usuario de MySQL (Enter = odontopoint)"
    if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = "odontopoint" }
    $securePassword = Read-Host "Contrasena de ese usuario de MySQL" -AsSecureString
    $env:ODONTO_MYSQL_USER = $dbUser
    $credential = New-Object System.Management.Automation.PSCredential($dbUser, $securePassword)
    $env:ODONTO_MYSQL_PASSWORD = $credential.GetNetworkCredential().Password
    Write-Host "Al completar el inicio, abra http://localhost:8080"
    & java -jar $jar --spring.profiles.active=mysql
    if ($LASTEXITCODE -ne 0) { Write-Host "Revise el error anterior y docs/MYSQL-WORKBENCH.md" }
} finally {
    $env:ODONTO_MYSQL_USER = $previousUser
    $env:ODONTO_MYSQL_PASSWORD = $previousPassword
    Remove-Variable credential, securePassword -ErrorAction SilentlyContinue
}
Read-Host "Enter para cerrar"
