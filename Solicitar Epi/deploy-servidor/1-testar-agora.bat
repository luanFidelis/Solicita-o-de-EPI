@echo off
REM ---------------------------------------------------------------
REM  PASSO 1 - Testar na mao, com a janela aberta.
REM  Use isto ANTES de instalar o servico: aqui voce ve os erros.
REM  Para parar: Ctrl+C ou feche a janela.
REM ---------------------------------------------------------------
cd /d "%~dp0"

where java >nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERRO] Java nao encontrado no PATH.
    echo        Instale o JDK 25 ou superior antes de continuar.
    echo.
    pause
    exit /b 1
)

echo.
echo Versao do Java instalada:
java -version
echo.
echo Subindo a API na porta 8085... aguarde a linha "Started HhtecApplication".
echo Depois teste no navegador do servidor:
echo    http://localhost:8085/solicitarEpi/listarRegiao?regiao=SBC-BOMBAS
echo.
java -jar hhtec-api.jar
pause
