param([string]$TomcatHome = 'C:/apache-tomcat-10/apache-tomcat-10.1.59')
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
$env:MAVEN_OPTS = '-Xms16m -Xmx192m -XX:+UseSerialGC -XX:MaxMetaspaceSize=96m -XX:CompressedClassSpaceSize=32m -XX:ReservedCodeCacheSize=32m'
& mvn clean package
if ($LASTEXITCODE -ne 0) { throw 'Maven failed.' }
$testCompileClasspath = "target/maven-classes;target/WebBasedBankingSystem/WEB-INF/lib/*;$TomcatHome/lib/*"
& javac -J-Xmx128m -J-XX:+UseSerialGC -cp $testCompileClasspath -d target/test-tools tools/EndToEndTest.java tools/FinancialSafetyTest.java tools/SmokeTest.java
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
if (-not (Test-Path -LiteralPath 'database/test-database.txt')) {
    $newTestDatabase = 'banking_test_' + (Get-Date -Format 'yyyyMMddHHmmss')
    & java -Xmx256m -cp $testCompileClasspath tools/PrepareTestDatabase.java $newTestDatabase
    if ($LASTEXITCODE -ne 0) { throw 'Test database creation failed.' }
}
$testRunClasspath = "target/test-tools;$testCompileClasspath;$TomcatHome/bin/tomcat-juli.jar"
& java -Xms16m -Xmx128m -XX:+UseSerialGC -cp $testRunClasspath FinancialSafetyTest
if ($LASTEXITCODE -ne 0) { throw 'Isolated safety checks failed.' }
& java -Xms16m -Xmx192m -XX:+UseSerialGC -XX:CompressedClassSpaceSize=32m -XX:ReservedCodeCacheSize=32m -cp $testRunClasspath EndToEndTest
if ($LASTEXITCODE -ne 0) { throw 'End-to-end checks failed.' }
$testDatabaseName = (Get-Content -LiteralPath 'database/test-database.txt' -Raw).Trim()
if ($testDatabaseName -notmatch '^banking_test_[0-9]+$') { throw 'Unexpected test database name.' }
& java -Xms16m -Xmx192m -XX:+UseSerialGC -XX:CompressedClassSpaceSize=32m -XX:ReservedCodeCacheSize=32m "-Dbank.db.url=jdbc:mysql://localhost:3306/$($testDatabaseName)?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo" -cp $testRunClasspath SmokeTest
if ($LASTEXITCODE -ne 0) { throw 'Read-only smoke checks failed.' }
Write-Output 'Build and all checks passed. No deployment performed.'
