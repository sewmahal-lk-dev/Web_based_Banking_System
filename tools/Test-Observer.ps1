param(
    [string]$JavaHome = 'C:/Users/User/.jdks/ms-17.0.20',
    [string]$TomcatHome = 'C:/apache-tomcat-10/apache-tomcat-10.1.59'
)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
$env:JAVA_HOME = $JavaHome
$env:MAVEN_OPTS = '-Xms16m -Xmx192m -XX:+UseSerialGC'
& mvn -q package
if ($LASTEXITCODE -ne 0) { throw 'Build failed.' }
$observerCompileClasspath = "target/maven-classes;target/WebBasedBankingSystem/WEB-INF/lib/*;$TomcatHome/lib/*"
& "$JavaHome/bin/javac.exe" -J-Xmx128m -cp $observerCompileClasspath -d target/observer-tests tools/ObserverDispatchTest.java tools/BankingNotificationTest.java
if ($LASTEXITCODE -ne 0) { throw 'Observer harness compilation failed.' }
$observerRunClasspath = "target/observer-tests;$observerCompileClasspath;$TomcatHome/bin/tomcat-juli.jar"
& "$JavaHome/bin/java.exe" -Xmx128m -cp $observerRunClasspath com.banking.dao.ObserverDispatchTest
if ($LASTEXITCODE -ne 0) { throw 'Observer dispatch checks failed.' }
# The existing banking harness reads configured schema definitions only, creates a new
# banking_test_notifications_<timestamp> database, and drops only that database in finally.
& "$JavaHome/bin/java.exe" -Xms16m -Xmx192m -XX:+UseSerialGC -cp $observerRunClasspath com.banking.dao.BankingNotificationTest --observer-report
if ($LASTEXITCODE -ne 0) { throw 'Observer banking workflow checks failed.' }
Write-Output 'Observer build, dispatch and disposable-database/HTTP checks passed. No deployment performed.'
