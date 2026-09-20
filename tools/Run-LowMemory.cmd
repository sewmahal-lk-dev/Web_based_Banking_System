@echo off
setlocal
cd /d "%~dp0.."
set "JAVA_HOME=C:\Users\User\.jdks\ms-17.0.20"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MAVEN_OPTS=-Xms16m -Xmx192m -XX:+UseSerialGC -XX:MaxMetaspaceSize=96m -XX:CompressedClassSpaceSize=32m -XX:ReservedCodeCacheSize=32m"
call mvn clean package
if errorlevel 1 exit /b 1
set "TEST_CP=target/maven-classes;target/WebBasedBankingSystem/WEB-INF/lib/*;C:/apache-tomcat-10/apache-tomcat-10.1.59/lib/*"
javac -J-Xmx128m -J-XX:+UseSerialGC -cp "%TEST_CP%" -d target/test-tools tools/EndToEndTest.java tools/FinancialSafetyTest.java tools/SmokeTest.java
if errorlevel 1 exit /b 1
set "TEST_CP=target/test-tools;%TEST_CP%;C:/apache-tomcat-10/apache-tomcat-10.1.59/bin/tomcat-juli.jar"
java -Xms16m -Xmx128m -XX:+UseSerialGC -cp "%TEST_CP%" FinancialSafetyTest
if errorlevel 1 exit /b 1
java -Xms16m -Xmx192m -XX:+UseSerialGC -XX:CompressedClassSpaceSize=32m -XX:ReservedCodeCacheSize=32m -cp "%TEST_CP%" EndToEndTest
if errorlevel 1 exit /b 1
echo Build and end-to-end tests passed.
