# Frontend - Sistema de Escalas

Interface web legada do sistema de escalas da Igreja Shekinah IAD.

O aplicativo principal do projeto e o mobile. Este frontend fica como apoio para comparar comportamento e testar pelo navegador.

## Requisitos

- Node.js compativel com o Vite.
- Backend rodando na porta `8081`.

## Portas

- Frontend web: `5173`
- Backend/API: `8081`

No proprio computador, a API fica em:

```text
http://localhost:8081/api
```

Em outro computador na mesma rede, use o IP da maquina que roda o backend:

```text
http://SEU-IP:8081/api
```

Ao iniciar com `.\scripts\start-frontend.ps1`, o frontend usa `/api` e o Vite encaminha as chamadas para `http://localhost:8081`. Assim, outro computador precisa acessar apenas o site em `http://SEU-IP:5173`.

## Configuracao

Crie um arquivo `.env` na pasta `frontend` somente se precisar forcar outra URL da API:

```env
VITE_API_BASE_URL=http://SEU-IP:8081/api
```

Ha um exemplo em `.env.example`.

## Desenvolvimento

Pela raiz do projeto:

```powershell
.\scripts\start-frontend.ps1
```

Ou manualmente:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\frontend
npm.cmd install
npm.cmd run dev -- --host 0.0.0.0 --port 5173
```

## Build

```powershell
npm.cmd run build
```

O build final fica em `frontend/dist`.

## XAMPP

Para publicar no Apache do XAMPP:

```powershell
npm.cmd run deploy:xampp
```

O script copia o build para:

```text
C:\xampp\htdocs\sistema-shekinah
```
