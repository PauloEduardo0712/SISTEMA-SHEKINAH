# Mobile - Sistema Shekinah

Aplicativo React Native com Expo para consultar escalas, atualizar disponibilidade, criar conta e administrar ministerios, voluntarios e escalas.

## Requisitos

- Node.js instalado.
- Backend rodando em `http://localhost:8081/api`.
- Expo Go no celular, ou emulador Android/iOS configurado.

## Configuracao da API

Por padrao, o app usa:

- Expo Go em celular fisico: o IP exibido pelo Expo na rede local
- Android emulator: `http://10.0.2.2:8081/api`
- iOS simulator e web: `http://localhost:8081/api`

Se precisar forcar a URL da API:

```powershell
$env:EXPO_PUBLIC_API_BASE_URL="http://SEU-IP:8081/api"
npm.cmd start
```

## Desenvolvimento

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\mobile
npm.cmd install
npx expo start --lan --port 8082
```

Depois escaneie o QR Code com o Expo Go ou rode:

```powershell
npm.cmd run android
```

O Expo usa a porta `8082` neste projeto para deixar a `8081` livre para o backend.

## QR Code no celular fisico

- O celular e o computador precisam estar na mesma rede Wi-Fi.
- Escaneie o QR Code pelo app Expo Go.
- Para celular fisico, prefira `--lan`:

```powershell
npx expo start --lan --port 8082
```

- Se a rede ou o firewall bloquear, use tunnel:

```powershell
npx expo start --tunnel --port 8082
```

- Evite `--localhost` quando estiver usando celular fisico.

Se aparecer erro dizendo que `package.json` nao existe na raiz do projeto, voce esta na pasta errada. Entre em `mobile` antes de rodar o Expo.

## Credenciais de teste

- admin: `admin / 1234`
- voluntarios: `joao / 1234`, `maria / 1234`, `carlos / 1234`, `ana / 1234`, `pedro / 1234`
