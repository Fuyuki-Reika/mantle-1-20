$Env:JAVA_HOME = "H:\mcp-platform\tools\scoop\apps\temurin21-jdk\current"
$Env:Path = $Env:JAVA_HOME + "\bin;" + $Env:Path

# Find the remapped/joined Minecraft jar
$jars = Get-ChildItem "$env:USERPROFILE\.gradle\caches\transforms-3" -Recurse -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Length -gt 5MB -and $_.Name -notlike "*sources*" }

foreach ($jar in $jars) {
    $result = jar tf $jar.FullName 2>&1 | Select-String "BlockElementFace.class" | Select-Object -First 1
    if ($result) {
        Write-Host "Found in: $($jar.FullName)"
        break
    }
}
