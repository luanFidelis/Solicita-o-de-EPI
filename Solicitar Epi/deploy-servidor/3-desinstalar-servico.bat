@echo off
REM ---------------------------------------------------------------
REM  Remove a tarefa e para a API. Nao apaga nada do banco.
REM  RODE COMO ADMINISTRADOR.
REM ---------------------------------------------------------------
set "TAREFA=HhtecApi"

net session >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Rode como ADMINISTRADOR.
    pause
    exit /b 1
)

echo Parando...
schtasks /end    /tn "%TAREFA%" >nul 2>&1
echo Removendo a tarefa...
schtasks /delete /tn "%TAREFA%" /f

REM O java continua vivo depois de encerrar a tarefa; derruba quem esta na 8085.
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":8085" ^| findstr "LISTENING"') do (
    echo Encerrando processo %%P que ainda ocupava a porta 8085...
    taskkill /pid %%P /f >nul 2>&1
)

echo.
echo Removido. O sistema PHP continua funcionando normal; apenas a
echo tela de Solicitacao de EPI vai avisar que a API esta fora do ar.
echo.
pause
