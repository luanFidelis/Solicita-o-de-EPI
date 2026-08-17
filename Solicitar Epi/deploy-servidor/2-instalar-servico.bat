@echo off
REM ---------------------------------------------------------------
REM  PASSO 2 - Deixar a API rodando sozinha.
REM
REM  Usa o Agendador de Tarefas do proprio Windows, entao nao precisa
REM  baixar nada no servidor. A tarefa roda como SYSTEM e no boot, ou
REM  seja: sobe com a maquina e nao cai quando voce desloga.
REM
REM  RODE COMO ADMINISTRADOR (botao direito > Executar como admin).
REM ---------------------------------------------------------------
cd /d "%~dp0"

net session >nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERRO] Rode este arquivo como ADMINISTRADOR.
    echo.
    pause
    exit /b 1
)

set "TAREFA=HhtecApi"

echo Removendo tarefa anterior, se existir...
schtasks /delete /tn "%TAREFA%" /f >nul 2>&1

echo Criando a tarefa "%TAREFA%"...
schtasks /create /tn "%TAREFA%" /tr "\"%~dp0iniciar-api.bat\"" /sc onstart /ru SYSTEM /rl HIGHEST /f
if errorlevel 1 (
    echo.
    echo [ERRO] Nao consegui criar a tarefa.
    pause
    exit /b 1
)

echo.
echo Iniciando agora...
schtasks /run /tn "%TAREFA%"

echo.
echo ---------------------------------------------------------------
echo  Pronto. A API sobe junto com o servidor.
echo.
echo  Ver situacao:  schtasks /query /tn "%TAREFA%"
echo  Parar:         schtasks /end   /tn "%TAREFA%"
echo  Iniciar:       schtasks /run   /tn "%TAREFA%"
echo  Log:           %~dp0logs\
echo ---------------------------------------------------------------
echo.
echo Aguarde uns 30 segundos e teste:
echo    http://localhost:8085/solicitarEpi/listarRegiao?regiao=SBC-BOMBAS
echo.
pause
