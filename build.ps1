# ==============================================================================
# Enterprise Build Script - Student Grading System v2.0
# Compiled following IBM Development Standards
# ==============================================================================
# Compiles Java source files with enterprise-grade quality checks
# Requires: Java 11+ and Windows PowerShell 5.0+
# ==============================================================================

$ErrorActionPreference = "Stop"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "   ENTERPRISE BUILD TOOL - Student Grading System v2.0   " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# ========== Configuration ==========
$projectName = "Student Grading System"
$version = "2.0.0"
$environment = "production"
$sourceDir = Join-Path $PSScriptRoot "src"
$classesDir = Join-Path $PSScriptRoot "WEB-INF\classes"
$libDir = Join-Path $PSScriptRoot "lib"
$logsDir = Join-Path $PSScriptRoot "logs"

# ========== Setup Phase ==========
Write-Host "`n[PHASE 1/4] Environment Setup" -ForegroundColor Yellow
Write-Host "Project: $projectName" -ForegroundColor Gray
Write-Host "Version: $version" -ForegroundColor Gray
Write-Host "Environment: $environment" -ForegroundColor Gray

# Create required directories
@($classesDir, $libDir, $logsDir) | ForEach-Object {
    if (!(Test-Path $_)) {
        New-Item -ItemType Directory -Path $_ | Out-Null
        Write-Host "[OK] Created directory: $_" -ForegroundColor Green
    }
}

# ========== Validation Phase ==========
Write-Host "`n[PHASE 2/4] Prerequisites Validation" -ForegroundColor Yellow

# Check Java installation
$javaCheck = $true
try {
    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $javaVersionOutput = & java -version 2>&1
    $ErrorActionPreference = $previousPreference

    $javaVersion = $javaVersionOutput | Select-Object -First 1
    if ($LASTEXITCODE -ne 0) {
        throw "java -version failed with exit code $LASTEXITCODE"
    }
    Write-Host "[OK] Java detected: $javaVersion" -ForegroundColor Green
}
catch {
    Write-Host "[ERROR] Java not found. Install JDK 11+ and add to PATH" -ForegroundColor Red
    $javaCheck = $false
}

if (-not $javaCheck) {
    exit 1
}

# Check source files
$sourceFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
if ($sourceFiles.Count -eq 0) {
    Write-Host "[ERROR] No Java source files found in $sourceDir" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Found $($sourceFiles.Count) source files to compile:" -ForegroundColor Green
$sourceFiles | ForEach-Object { Write-Host "  - $($_.FullName.Substring($sourceDir.Length+1))" -ForegroundColor Gray }

# ========== Dependency Download ==========
Write-Host "`n[PHASE 3/4] Dependency Resolution" -ForegroundColor Yellow
$jarPath = Join-Path $libDir "jakarta.servlet-api-6.0.0.jar"
$downloadUrl = "https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar"

if (!(Test-Path $jarPath)) {
    Write-Host "Downloading Jakarta Servlet API 6.0.0 from Maven Central..." -ForegroundColor Cyan
    Write-Host "URL: $downloadUrl" -ForegroundColor Gray
    $downloadSuccess = $true
    try {
        Invoke-WebRequest -Uri $downloadUrl -OutFile $jarPath -UseBasicParsing
        Write-Host "[OK] Successfully downloaded jakarta.servlet-api-6.0.0.jar" -ForegroundColor Green
    }
    catch {
        Write-Host "[ERROR] Failed to download dependency. Verify internet access." -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        $downloadSuccess = $false
    }
    if (-not $downloadSuccess) {
        exit 1
    }
}
else {
    Write-Host "[OK] Jakarta Servlet API dependency already cached" -ForegroundColor Green
}

# ========== Compilation Phase ==========
Write-Host "`n[PHASE 4/4] Source Code Compilation" -ForegroundColor Yellow
Write-Host "Target Java Version: 11" -ForegroundColor Gray
Write-Host "Output Directory: $classesDir" -ForegroundColor Gray

$sourceFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

Write-Host "Compiling $($sourceFiles.Count) source files..." -ForegroundColor Cyan
$classpath = $jarPath

$compilationSuccess = $true
try {
    # Execute compilation
    & javac -source 11 -target 11 -encoding UTF-8 -cp "$classpath" -d "$classesDir" $sourceFiles
    
    if ($LASTEXITCODE -eq 0) {
        # Count compiled classes
        $classFiles = Get-ChildItem -Path $classesDir -Filter "*.class" -Recurse
        Write-Host "[OK] Compilation completed successfully" -ForegroundColor Green
        Write-Host "  Generated $($classFiles.Count) class files" -ForegroundColor Gray
        Write-Host "  Output: $classesDir" -ForegroundColor Gray
        
        # Display summary
        Write-Host "`n============================================================" -ForegroundColor Green
        Write-Host "  BUILD SUCCESSFUL - Enterprise Edition v$version" -ForegroundColor Green
        Write-Host "============================================================" -ForegroundColor Green
        Write-Host "Summary:" -ForegroundColor Cyan
        Write-Host "  Project: $projectName" -ForegroundColor Gray
        Write-Host "  Version: $version" -ForegroundColor Gray
        Write-Host "  Source Files: $($sourceFiles.Count)" -ForegroundColor Gray
        Write-Host "  Class Files: $($classFiles.Count)" -ForegroundColor Gray
        Write-Host "  Target JDK: 11+" -ForegroundColor Gray
        Write-Host "  Status: Ready for deployment" -ForegroundColor Gray
        Write-Host "`nNext steps:" -ForegroundColor Cyan
        Write-Host "  1. Run: .\run.ps1" -ForegroundColor Gray
        Write-Host "  2. Access: http://localhost:8080/" -ForegroundColor Gray
        Write-Host "  3. Login: admin / admin123" -ForegroundColor Gray
        Write-Host "============================================================" -ForegroundColor Green
    }
    else {
        Write-Host "[ERROR] Compilation failed" -ForegroundColor Red
        Write-Host "BUILD FAILED: javac returned exit code $LASTEXITCODE" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        $compilationSuccess = $false
    }
}
catch {
    Write-Host "`n============================================================" -ForegroundColor Red
    Write-Host "  BUILD ERROR" -ForegroundColor Red
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host "`nTroubleshooting:" -ForegroundColor Yellow
    Write-Host "  - Verify JDK is installed and javac is in PATH" -ForegroundColor Gray
    Write-Host "  - Run: java -version" -ForegroundColor Gray
    Write-Host "  - Run: javac -version" -ForegroundColor Gray
    $compilationSuccess = $false
}

if (-not $compilationSuccess) {
    exit 1
}

Write-Host "`nBuild completed successfully!" -ForegroundColor Green
