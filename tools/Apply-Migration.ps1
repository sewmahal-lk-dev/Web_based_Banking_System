param([ValidateSet(3,4,5,6,7)][int]$Version = 3)

$ErrorActionPreference = 'Stop'

Set-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)

$migrationClasspath = 'target/maven-classes;target/WebBasedBankingSystem/WEB-INF/lib/*'

if (-not (Test-Path -LiteralPath 'target/WebBasedBankingSystem.war')) {
    throw 'Build with mvn clean package first.'
}

$migrationFile = if ($Version -eq 7) { 'database/migrations/V7__notifications.sql' } elseif ($Version -eq 6) { 'database/migrations/V6__ticket_department_routing.sql' } elseif ($Version -eq 5) { 'database/migrations/V5__ticket_conversations.sql' } elseif ($Version -eq 4) { 'database/migrations/V4__bill_reference.sql' } else { 'database/migrations/V3__cash_transactions.sql' }
if (-not (Test-Path -LiteralPath $migrationFile)) {
    throw "Migration file was not found: $migrationFile"
}

$snapshotDirectory =
    'database/backups/migration-' +
    (Get-Date -Format 'yyyyMMdd-HHmmss')

Write-Output "Creating database backup before V$Version migration..."

& java `
    -Xmx256m `
    -cp $migrationClasspath `
    tools/DatabaseSnapshot.java `
    $snapshotDirectory

if ($LASTEXITCODE -ne 0) {
    throw "Backup failed; V$Version migration was not started."
}

Write-Output "Backup complete: $snapshotDirectory"
Write-Output "Applying V$Version migration..."

& java `
    -Xmx256m `
    -cp $migrationClasspath `
    tools/ApplyMigration.java `
    "--apply-v$Version"

if ($LASTEXITCODE -ne 0) {
    throw "V$Version migration failed. Backup is in $snapshotDirectory. No destructive recovery was attempted."
}

Write-Output "V$Version migration completed successfully."

Write-Output "Backup: $snapshotDirectory"