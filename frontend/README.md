# Frontend - Sistema de Escalas

Interface web do sistema de escalas da Igreja Shekinah IAD, feita com HTML, CSS e JavaScript.

## Estrutura

```text
frontend/
|-- index.html
|-- css/
|   |-- global.css
|   |-- login.css
|   |-- admin.css
|   |-- voluntario.css
|   `-- responsivo.css
|-- js/
|   |-- dados.js
|   |-- utils.js
|   |-- validacoes.js
|   |-- conflitos.js
|   |-- login.js
|   |-- admin.js
|   |-- escalas.js
|   `-- voluntario.js
`-- img/
    `-- logo-shekinah.svg
```

## Como usar

1. Inicie o backend com `backend\\gradlew.bat bootRun`.
2. Abra `frontend/index.html` no navegador.
3. Faca login com uma das credenciais cadastradas no backend.

## Observacoes

- A URL da API e lida de `localStorage["shekinah-api-url"]` quando existir.
- Na ausencia dessa chave, o frontend usa `http://localhost:8081/api`.
- A sessao salva no navegador e descartada automaticamente quando o token nao tem formato JWT valido.
- O backend local sobe em H2 por padrao, entao o frontend pode ser usado mesmo sem MySQL ou Docker.
