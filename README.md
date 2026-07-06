# Sistema Shekinah

Sistema de escalas da Igreja Shekinah IAD, com API Spring Boot e aplicativo principal em React Native/Expo.

| Pasta | Responsabilidade |
| --- | --- |
| `backend` | API Spring Boot, autenticacao JWT, regras de escalas e banco MySQL. |
| `mobile` | Aplicativo React Native/Expo para celular, com login, cadastro, agenda, disponibilidade e administracao. |
| `frontend` | Interface web legada mantida apenas como referencia. |

## Requisitos

- Java 17 ou superior.
- Node.js instalado.
- Expo Go instalado no celular, se for testar no aparelho fisico.
- Docker Desktop para subir o MySQL local.

## Portas usadas

- Backend: `http://localhost:8081/api`
- Expo/mobile: `http://localhost:8082`
- Frontend legado: porta exibida pelo Vite ao rodar `npm.cmd run dev`

## Rodar o projeto principal

Abra dois terminais no PowerShell.

### 1. Backend

No primeiro terminal:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\backend
docker compose up -d
.\gradlew.bat bootRun
```

Quando estiver funcionando, o backend fica em:

```text
http://localhost:8081/api
```

O backend inicia com o perfil `local` por padrao e usa MySQL local na porta `3310`.

### 2. Aplicativo mobile

No segundo terminal:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\mobile
npm.cmd install
npx expo start --lan --port 8082
```

Depois escaneie o QR Code pelo app Expo Go.

Se o QR Code nao abrir no celular, tente o modo tunnel:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\mobile
npx expo start --tunnel --port 8082
```

Se estiver usando emulador Android:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\mobile
npm.cmd run android
```

## Credenciais de teste

Admin:

```text
admin / 1234
```

Voluntarios:

```text
joao / 1234
maria / 1234
carlos / 1234
ana / 1234
pedro / 1234
```

## Banco MySQL

O projeto usa MySQL em desenvolvimento. Para subir apenas o banco:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\backend
docker compose up -d
```

Configuracao do MySQL local:

```text
banco: escala_shekinah
usuario: escala_user
senha: escala_pass
porta: 3310 no computador, 3306 dentro do container Docker
```

Se quiser parar o MySQL:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\backend
docker compose down
```

Se quiser apagar os dados do MySQL e recriar tudo do zero:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\backend
docker compose down -v
docker compose up -d
```

## Frontend web legado

O projeto principal e o mobile. O frontend web antigo pode ser usado apenas para comparar comportamento:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\frontend
npm.cmd install
npm.cmd run dev
```

## Problemas comuns

### Erro: package.json does not exist

Esse erro acontece quando o Expo e rodado na pasta errada. Rode os comandos dentro da pasta `mobile`:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\mobile
npx expo start --lan --port 8082
```

### QR Code nao funciona

- Verifique se o celular e o computador estao na mesma rede Wi-Fi.
- Escaneie o QR Code pelo app Expo Go.
- Evite iniciar o Expo com `--localhost` para celular fisico.
- Se o modo LAN falhar, use:

```powershell
npx expo start --tunnel --port 8082
```

- Se o Windows Firewall perguntar, permita acesso para `Node.js` e `Java`.

### Backend nao conecta no banco MySQL

Confirme que o Docker Desktop esta aberto e que o MySQL esta rodando:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\backend
docker compose ps
```

Se nao estiver rodando:

```powershell
docker compose up -d
```

O projeto usa a porta `3310` no computador para evitar conflito com outro MySQL instalado na porta `3306`.

### Porta 8081 ja esta em uso

Isso normalmente acontece quando o backend ja esta aberto em outro terminal. Feche o terminal antigo com `Ctrl + C`.

Se nao encontrar o terminal, veja qual processo esta usando a porta:

```powershell
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
```

### Testar se o backend esta aberto

Sem login, este endpoint deve responder `403 Proibido`, o que confirma que a API esta no ar:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8081/api/auth/me
```

## Arquivos gerados

Pastas como `node_modules`, `dist`, `.expo`, `backend/build`, `backend/.gradle` e logs `bootrun*.log` nao fazem parte do codigo fonte. Elas podem ser recriadas com os comandos acima.
