@echo off
cd "C:\Users\mazin\Desktop\OpenWar-Launcher\OpenWarLauncher\out\artifacts\ICI\OpenWarLauncher"

rem Exécute l'EXE et redirige la sortie dans output.txt
.\OpenWarLauncher.exe -Dprism.order=sw > output.txt 2>&1

rem Ouvre output.txt après l'exécution pour voir les logs
start notepad output.txt

pause
