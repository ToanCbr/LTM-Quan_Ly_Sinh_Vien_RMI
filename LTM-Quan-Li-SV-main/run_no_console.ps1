# Launcher to start Server and Client without opening console windows
# Usage: double-click this file or run from PowerShell: .\run_no_console.ps1

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$bin = Join-Path $root 'bin'

if (-not (Test-Path $bin)) {
    Write-Output "Binary folder not found at $bin. Please compile first (javac -d .\\bin .\\src\\RMI\\*.java)"
    exit 1
}

# Start server (no console) using javaw
Start-Process -FilePath "javaw.exe" -ArgumentList "-cp", "$bin", "RMI.ServerMain" -WorkingDirectory $root
Start-Sleep -Milliseconds 300
# Start client (GUI) using javaw
Start-Process -FilePath "javaw.exe" -ArgumentList "-cp", "$bin", "RMI.ClientGUI" -WorkingDirectory $root

Write-Output "Launched server and client (no console windows)."