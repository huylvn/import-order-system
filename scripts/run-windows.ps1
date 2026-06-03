param(
    [switch] $SkipTests
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ToolsDir = Join-Path $ProjectRoot ".tools"
$JdkDir = Join-Path $ToolsDir "jdk-21"
$MavenDir = Join-Path $ToolsDir "apache-maven-3.9.9"
$DownloadsDir = Join-Path $ToolsDir "downloads"
$JdkZip = Join-Path $DownloadsDir "jdk-21-windows-x64.zip"
$MavenZip = Join-Path $DownloadsDir "apache-maven-3.9.9-bin.zip"
$JdkUrl = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"
$MavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

function Download-File($Url, $OutputPath) {
    if (Test-Path $OutputPath) {
        return
    }
    Write-Host "Downloading $Url"
    Invoke-WebRequest -Uri $Url -OutFile $OutputPath
}

function Expand-SingleRootArchive($ArchivePath, $DestinationPath) {
    $TempPath = Join-Path $ToolsDir ("extract-" + [System.Guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Force -Path $TempPath | Out-Null
    try {
        Expand-Archive -Path $ArchivePath -DestinationPath $TempPath -Force
        $Root = Get-ChildItem -Path $TempPath | Where-Object { $_.PSIsContainer } | Select-Object -First 1
        if ($null -eq $Root) {
            throw "Archive does not contain a root directory: $ArchivePath"
        }
        if (Test-Path $DestinationPath) {
            Remove-Item -Recurse -Force $DestinationPath
        }
        Move-Item -Path $Root.FullName -Destination $DestinationPath
    }
    finally {
        if (Test-Path $TempPath) {
            Remove-Item -Recurse -Force $TempPath
        }
    }
}

New-Item -ItemType Directory -Force -Path $DownloadsDir | Out-Null

if (-not (Test-Path $JdkDir)) {
    Download-File $JdkUrl $JdkZip
    Expand-SingleRootArchive $JdkZip $JdkDir
}

if (-not (Test-Path $MavenDir)) {
    Download-File $MavenUrl $MavenZip
    Expand-SingleRootArchive $MavenZip $MavenDir
}

$env:JAVA_HOME = $JdkDir
$env:PATH = (Join-Path $JdkDir "bin") + ";" + (Join-Path $MavenDir "bin") + ";" + $env:PATH

Push-Location $ProjectRoot
try {
    & java -version
    & mvn -version
    if (-not $SkipTests) {
        & mvn test
    }
    & mvn javafx:run
}
finally {
    Pop-Location
}
