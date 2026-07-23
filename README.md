# PriceWatch

API REST para acompanhamento e análise de preços de produtos em diferentes lojas.

## Sobre o projeto

O PriceWatch permite cadastrar produtos, associá-los a lojas confiáveis e registrar os preços encontrados ao longo do tempo.

Com base nesse histórico, a aplicação identifica o menor preço atual, o menor preço histórico, a data em que esse preço foi encontrado e a loja responsável pela oferta.

O usuário também pode informar um preço desejado para avaliar se o valor atual de um produto já está dentro da sua expectativa de compra.

Nesta primeira versão, os preços são registrados manualmente. A coleta automática de preços poderá ser adicionada em evoluções futuras do projeto.

## O que o projeto faz

O PriceWatch centraliza o histórico de preços de produtos para facilitar o acompanhamento e a comparação entre diferentes lojas.

A aplicação permite:

* cadastrar produtos;
* cadastrar lojas;
* associar produtos às lojas onde são vendidos;
* registrar preços encontrados;
* consultar o histórico de preços;
* identificar o menor preço disponível atualmente;
* identificar o menor preço já registrado;
* visualizar a loja e a data do menor preço;
* definir um preço desejado para um produto;
* verificar se o preço atual atingiu o valor desejado.

## Por que o projeto é útil

Os preços de um mesmo produto podem variar significativamente entre lojas e ao longo do tempo.

Sem um histórico centralizado, fica difícil saber se uma oferta realmente está barata ou se o produto já foi vendido por um valor menor.

O PriceWatch ajuda o usuário a tomar decisões de compra mais conscientes, oferecendo informações objetivas sobre:

* evolução dos preços;
* comparação entre lojas;
* melhores ofertas disponíveis;
* menor preço histórico;
* proximidade do preço desejado.

## Funcionalidades

### Gestão de produtos

* Cadastro de produtos.
* Consulta de produtos cadastrados.
* Atualização das informações de um produto.
* Remoção de produtos.
* Definição de preço desejado.

### Gestão de lojas

* Cadastro de lojas.
* Consulta de lojas cadastradas.
* Atualização dos dados de uma loja.
* Remoção de lojas.

### Produtos por loja

* Associação de um produto a uma ou mais lojas.
* Registro do endereço do produto em cada loja.
* Consulta das lojas que vendem determinado produto.

### Histórico de preços

* Registro manual de preços.
* Consulta do histórico de preços de um produto.
* Identificação do preço mais recente por loja.
* Identificação do menor preço atual.
* Identificação do menor preço histórico.
* Consulta da data e da loja onde o menor preço foi registrado.

### Comparação com preço desejado

* Configuração de um preço desejado para o produto.
* Comparação entre o menor preço atual e o preço desejado.
* Indicação de que o produto atingiu ou não o valor esperado.

## Como começar a usar

O projeto ainda está em fase inicial de desenvolvimento. Nesta etapa, o ambiente local disponibiliza PostgreSQL e Adminer por meio do Docker Compose.

### Requisitos

* Git;
* Java 21;
* Docker com Docker Compose.

Não é necessário instalar o Maven globalmente. O repositório inclui o Maven Wrapper, que baixa e executa a versão adequada do Maven:

```powershell
.\mvnw.cmd --version
```

Em Linux ou macOS, use `./mvnw` no lugar de `.\mvnw.cmd` nos comandos deste documento.

### Configuração

Copie o arquivo de exemplo para criar as variáveis do ambiente local:

```powershell
Copy-Item .env.example .env
```

A aplicação usa as mesmas credenciais configuradas para o PostgreSQL no Docker Compose:

* `DB_HOST`, com valor padrão `localhost`;
* `DB_PORT`, com valor padrão `5432`;
* `POSTGRES_DB`;
* `POSTGRES_USER`;
* `POSTGRES_PASSWORD`.

Ao executar a aplicação fora do Docker Compose, disponibilize `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` como variáveis de ambiente.

Inicie os serviços:

```powershell
docker compose up -d
```

Confirme que os containers estão em execução e que o PostgreSQL está saudável:

```powershell
docker compose ps
```

O PostgreSQL ficará disponível para ferramentas instaladas na máquina em `localhost:5432`. Entre containers da mesma rede do Compose, a conexão deverá usar `postgres:5432`.

O Adminer ficará disponível em [http://localhost:8081](http://localhost:8081). Para acessar o banco, selecione PostgreSQL e use:

* servidor: `postgres`;
* usuário, senha e banco: valores definidos no arquivo `.env`.

### Execução da API

Disponibilize no terminal as mesmas credenciais definidas no `.env`:

```powershell
$env:POSTGRES_DB = "pricewatch"
$env:POSTGRES_USER = "pricewatch"
$env:POSTGRES_PASSWORD = "pricewatch"
```

Se você alterou o `.env`, use os valores correspondentes nos comandos acima. Em seguida, inicie a aplicação com o Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Com a API e o PostgreSQL em execução, consulte o health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

A resposta deve informar o status `UP`. O Actuator expõe somente os endpoints `health` e `info`.

### Testes e build

Execute os testes automatizados:

```powershell
.\mvnw.cmd test
```

Execute a validação completa, incluindo limpeza, testes e empacotamento da aplicação:

```powershell
.\mvnw.cmd clean verify
```

Os testes de integração usam Testcontainers e, por isso, exigem que o Docker esteja em execução.

Para encerrar os serviços sem apagar os dados:

```powershell
docker compose down
```

O comando abaixo também remove o volume do PostgreSQL e apaga permanentemente os dados locais:

```powershell
docker compose down -v
```

## Módulos da aplicação

O monólito modular está organizado em três módulos de domínio:

* `product`: concentra o catálogo de produtos e seus dados, incluindo o preço desejado quando essa funcionalidade for implementada;
* `store`: concentra as lojas confiáveis e as associações entre produtos e lojas;
* `pricing`: concentra o registro histórico e a análise dos preços observados.

Os módulos já contêm as entidades persistidas de produto, loja, anúncio e histórico de preços. A API de gestão de lojas está disponível; as APIs de produto, anúncio e preço ainda não foram implementadas.

## API de lojas

A gestão de lojas está disponível nos seguintes endpoints:

```text
POST   /api/v1/stores
GET    /api/v1/stores
GET    /api/v1/stores/{storeId}
PUT    /api/v1/stores/{storeId}
DELETE /api/v1/stores/{storeId}
PATCH  /api/v1/stores/{storeId}/restore
```

O cadastro e a atualização recebem `name` e o campo opcional `websiteUrl`. O nome é limpo antes de ser armazenado e sua comparação para duplicidade ignora caixa, espaços nas extremidades e múltiplos espaços internos. A URL, quando informada, deve usar HTTP ou HTTPS.

A listagem aceita os parâmetros `page`, `size`, `sort`, `direction`, `search` e `status`. A página padrão é `0`, o tamanho padrão é `20` e o tamanho permitido varia de `1` a `100`. O status aceita `ACTIVE`, `INACTIVE` ou `ALL` e, quando omitido, retorna somente lojas ativas. Os campos de ordenação aceitos são `name`, `websiteUrl`, `active`, `createdAt` e `updatedAt`.

O `DELETE` realiza exclusão lógica, desativando a loja e preenchendo `deletedAt`. O `PATCH` de restauração reativa a loja e limpa esse timestamp. Anúncios e preços relacionados não são removidos.

## Migrations do banco de dados

O schema é gerenciado exclusivamente pelo Liquibase. O arquivo `src/main/resources/db/changelog/db.changelog-master.yaml` é o ponto de entrada das migrations, enquanto o Hibernate apenas valida o schema existente.

A migration inicial do domínio cria as tabelas `products`, `stores`, `product_stores` e `prices`, incluindo seus relacionamentos, constraints e índices. Os timestamps do domínio são representados por `Instant` na aplicação e armazenados como `timestamp with time zone` no PostgreSQL para preservar o instante em UTC.

As migrations futuras devem ser criadas em SQL formatado pelo Liquibase e incluídas no arquivo YAML mestre em ordem de execução. Cada arquivo SQL deve iniciar com `--liquibase formatted sql` e declarar seus changesets com `--changeset autor:identificador`.

Migrations que já tenham sido executadas não devem ser alteradas. Uma nova migration deve ser criada para cada mudança posterior no schema.

## Status do projeto

Em desenvolvimento.

A primeira versão será focada no cadastro, gerenciamento e análise manual dos preços.

## Evoluções futuras

Algumas funcionalidades poderão ser adicionadas após a conclusão do MVP:

* coleta automática de preços;
* alertas de redução de preço;
* notificações quando o preço desejado for atingido;
* autenticação de usuários;
* listas de produtos favoritos;
* gráficos de evolução de preços;
* integração com lojas e marketplaces;
* tarefas agendadas para atualização dos preços.

## Licença

Este projeto está licenciado sob a licença MIT.
