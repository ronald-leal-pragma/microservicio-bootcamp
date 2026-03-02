# Try to run the microservicio-bootcamp using the wrapper if exists, otherwise use global gradle
if (Test-Path .\gradlew.bat) {
  Write-Host "Using gradlew.bat"
  .\gradlew.bat bootRun
} else {
  Write-Host "gradlew.bat not found, trying global gradle"
  gradle bootRun
}
