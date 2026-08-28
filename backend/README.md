# Backend - Sistema de Escalas

## Stack

- Java 17+
- Spring Boot
- Spring Security com JWT
- Spring Data JPA
- Flyway
- MySQL para desenvolvimento local
- H2 para testes automatizados

## Como subir o banco MySQL

Na pasta `backend`:

```powershell
docker compose up -d
```

Isso sobe um MySQL com:

- banco: `escala_shekinah`
- usuario: `escala_user`
- senha: `escala_pass`

## Como rodar a aplicacao em desenvolvimento

Defina um Java 17+ no `JAVA_HOME`, suba o MySQL e execute:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH\backend
docker compose up -d
.\gradlew.bat bootRun
```

O `bootRun` inicia com o perfil `local` por padrao, usando:

- MySQL local em `localhost:3310/escala_shekinah`
- porta `8081` por padrao
- endereco `0.0.0.0` no perfil local, permitindo acesso por celular/outros computadores da mesma rede

URL local da API:

```text
http://localhost:8081/api
```

URL da API para celular ou outro computador:

```text
http://SEU-IP:8081/api
```

Para descobrir o IP:

```powershell
cd C:\Users\Paulo55881126\Documents\SISTEMA-SHEKINAH
.\scripts\show-network.ps1
```

Se quiser rodar com a configuracao padrao do `application.properties`, execute:

```powershell
.\gradlew.bat bootRun -Dspring.profiles.active=default
```

Essa configuracao tambem usa a porta `8081`. Use `SERVER_PORT` apenas quando precisar trocar a porta manualmente.

## Variaveis de ambiente

Se quiser sobrescrever os padroes, use:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `SERVER_PORT`
- `SERVER_ADDRESS`

Sem essas variaveis, a aplicacao padrao tenta usar a mesma configuracao do `docker-compose.yml`.

## Credenciais iniciais

- admin: `admin / 1234`
- voluntarios:
  - `joao / 1234`
  - `maria / 1234`
  - `carlos / 1234`
  - `ana / 1234`
  - `pedro / 1234`

## Endpoints principais

### Autenticacao

- `POST /api/auth/login`
- `GET /api/auth/me`

### Ministerios

- `GET /api/ministries`
- `POST /api/ministries`
- `PUT /api/ministries/{id}`
- `DELETE /api/ministries/{id}`

### Voluntarios

- `GET /api/volunteers`
- `GET /api/volunteers/{id}`
- `GET /api/volunteers/me`
- `POST /api/volunteers`
- `PUT /api/volunteers/{id}`
- `DELETE /api/volunteers/{id}`

### Disponibilidades

- `GET /api/availabilities/volunteer/{volunteerId}`
- `GET /api/availabilities/me`
- `PUT /api/availabilities/volunteer/{volunteerId}`
- `PUT /api/availabilities/me`

### Escalas

- `GET /api/schedules`
- `GET /api/schedules/me`
- `GET /api/schedules/conflicts`
- `POST /api/schedules`
- `PUT /api/schedules/{id}`
- `DELETE /api/schedules/{id}`

## Regras implementadas

- autenticacao JWT
- perfis `ADMIN` e `VOLUNTARIO`
- CRUD de ministerios
- CRUD de voluntarios
- disponibilidade por dia da semana e turno
- criacao e edicao de escalas
- bloqueio de conflito por indisponibilidade
- bloqueio de dupla escala no mesmo horario
- seed inicial automatico

## Observacoes

- as migrations ficam em `src/main/resources/db/migration`
- os testes usam H2 e nao dependem de MySQL local
- o perfil `local` usa MySQL local para desenvolvimento
- o schema em producao e validado com `hibernate.ddl-auto=validate`

## Problemas comuns

### Backend nao conecta no MySQL

Confirme que o Docker Desktop esta aberto e que o container esta rodando:

```powershell
docker compose ps
```

Se nao estiver rodando:

```powershell
docker compose up -d
```

Se quiser apagar o banco e recriar tudo do zero:

```powershell
docker compose down -v
docker compose up -d
```

### Verificar se a API esta no ar

Este comando deve retornar `403 Proibido` quando nao houver login. Isso confirma que o backend esta respondendo:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8081/api/auth/me
```
