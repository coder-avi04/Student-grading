# ==============================================================================
# Local Tomcat Runner Script for Student Grading System
# ==============================================================================
# This script downloads Apache Tomcat, extracts it, deploys the compiled
# application, and starts the Tomcat server.
# ==============================================================================

$tomcatVersion = "10.1.20"
$tomcatZipName = "apache-tomcat-$tomcatVersion-windows-x64.zip"
$tomcatUrl = "https://archive.apache.org/dist/tomcat/tomcat-10/v$tomcatVersion/bin/$tomcatZipName"
$tomcatDir = Join-Path $PSScriptRoot "tomcat"
$zipPath = Join-Path $tomcatDir $tomcatZipName

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   SGS Local Runner (Tomcat $tomcatVersion)   " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Create tomcat directory if it doesn't exist
if (!(Test-Path $tomcatDir)) {
    New-Item -ItemType Directory -Path $tomcatDir | Out-Null
    Write-Host "Created tomcat container directory: $tomcatDir" -ForegroundColor Gray
}

# 2. Download Apache Tomcat Zip if not already cached
if (!(Test-Path $zipPath)) {
    Write-Host "Downloading Apache Tomcat $tomcatVersion from official archive..." -ForegroundColor Yellow
    Write-Host "Source URL: $tomcatUrl" -ForegroundColor Gray
    try {
        # Check if curl is available for faster, timeout-free download
        if (Get-Command "curl.exe" -ErrorAction SilentlyContinue) {
            Write-Host "Downloading via curl..." -ForegroundColor Gray
            curl.exe -L -o $zipPath $tomcatUrl
        } else {
            # Disable progress bar to speed up PowerShell downloads and prevent timeouts
            $oldProgressPreference = $ProgressPreference
            $ProgressPreference = 'SilentlyContinue'
            Invoke-WebRequest -Uri $tomcatUrl -OutFile $zipPath -UseBasicParsing -TimeoutSec 180
            $ProgressPreference = $oldProgressPreference
        }
        
        if (Test-Path $zipPath) {
            Write-Host "Successfully downloaded Tomcat zip." -ForegroundColor Green
        } else {
            throw "Download completed but ZIP file not found."
        }
    }
    catch {
        Write-Error "Failed to download Tomcat. Error: $_"
        exit 1
    }
} else {
    Write-Host "Apache Tomcat zip already cached." -ForegroundColor Green
}

# 3. Extract Apache Tomcat if not already extracted
$extractedDir = Join-Path $tomcatDir "apache-tomcat-$tomcatVersion"
if (!(Test-Path $extractedDir)) {
    Write-Host "Extracting Tomcat zip..." -ForegroundColor Yellow
    try {
        Expand-Archive -Path $zipPath -DestinationPath $tomcatDir
        Write-Host "Tomcat extracted successfully." -ForegroundColor Green
    }
    catch {
        Write-Error "Extraction failed. Error: $_"
        exit 1
    }
} else {
    Write-Host "Apache Tomcat already extracted." -ForegroundColor Green
}

# 4. Deploy web application files to webapps/ROOT/
$webappsRoot = Join-Path $extractedDir "webapps\ROOT"
Write-Host "Deploying compiled application files to webapps/ROOT..." -ForegroundColor Yellow

# Clean default ROOT contents first
if (Test-Path $webappsRoot) {
    Remove-Item -Path "$webappsRoot\*" -Recurse -Force -ErrorAction SilentlyContinue
} else {
    New-Item -ItemType Directory -Path $webappsRoot | Out-Null
}

# Copy files to deploy
try {
    Copy-Item -Path (Join-Path $PSScriptRoot "login.html") -Destination $webappsRoot
    Copy-Item -Path (Join-Path $PSScriptRoot "index.html") -Destination $webappsRoot
    Copy-Item -Path (Join-Path $PSScriptRoot "css") -Destination $webappsRoot -Recurse
    Copy-Item -Path (Join-Path $PSScriptRoot "js") -Destination $webappsRoot -Recurse
    Copy-Item -Path (Join-Path $PSScriptRoot "WEB-INF") -Destination $webappsRoot -Recurse
    Write-Host "Deployment completed successfully!" -ForegroundColor Green
}
catch {
    Write-Error "Failed to deploy application. Error: $_"
    exit 1
}

# 5. Start Apache Tomcat
$startupScript = Join-Path $extractedDir "bin\startup.bat"
Write-Host "----------------------------------------" -ForegroundColor Cyan
Write-Host "Starting Tomcat server..." -ForegroundColor Green
Write-Host "The application will be accessible at: http://localhost:8080/" -ForegroundColor Cyan
Write-Host "To shut down Tomcat, close the new cmd window or run 'shutdown.bat'." -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Cyan

try {
    Start-Process -FilePath $startupScript -WorkingDirectory (Join-Path $extractedDir "bin")
}
catch {
    Write-Error "Failed to start Tomcat process. Error: $_"
    exit 1
}
