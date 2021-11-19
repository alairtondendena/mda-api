
# MDA PROJECT - API

API de geração de API's RESTful em Spring Framework.
## Environment

Para rodar esse projeto, é necessária a instalação do JAVA 8. 

Recomenda-se também o uso das ferramentas e aplicações:
`DOCKER`
`POSTGRESQL`

Base de dados exemplo: https://github.com/alairtondendena/mda-api/blob/master/dump.sql



## Run Locally

Clone o projeto

```bash
  git clone https://github.com/alairtondendena/mda-app.git
```

Vá para o diretório do projeto

```bash
  cd mda-api
```

Instale as dependências 

```bash
  mvn install
```

Start o servidor

```bash
  mvn spring-boot:run
```


## Documentation

Documentação local disponível em: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)


## Features

- Suporte ao banco de dados POSTGRESQL.
- Geração automática de @Controller, @Service, @Repository e @Entity.
- Geração automática de docoumentação do Swagger.
- Mapeamento automático de tipos e classes.


## Roadmap

- Suporte ao banco de dados MySQL.
- Ampliar mapeamento automático de tipos.
- Dynamic Querys na método principal de busca.
- Seleção de fields por meio de projeção.


