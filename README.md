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
* Docker;
* Docker Compose.

### Configuração

Copie o arquivo de exemplo para criar as variáveis do ambiente local:

```powershell
Copy-Item .env.example .env
```

Inicie os serviços:

```powershell
docker compose up -d
```

O PostgreSQL ficará disponível para ferramentas instaladas na máquina em `localhost:5432`. Entre containers da mesma rede do Compose, a conexão deverá usar `postgres:5432`.

O Adminer ficará disponível em [http://localhost:8081](http://localhost:8081). Para acessar o banco, selecione PostgreSQL e use:

* servidor: `postgres`;
* usuário, senha e banco: valores definidos no arquivo `.env`.

Para encerrar os serviços sem apagar os dados:

```powershell
docker compose down
```

O comando abaixo também remove o volume do PostgreSQL e apaga permanentemente os dados locais:

```powershell
docker compose down -v
```

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
