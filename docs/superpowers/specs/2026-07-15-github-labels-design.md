# Taxonomia de labels do GitHub

## Contexto do projeto

O PriceWatch é uma API REST para cadastrar produtos e lojas, associar produtos às lojas, registrar preços manualmente, consultar o histórico e comparar o menor preço atual com o preço desejado pelo usuário.

O repositório está na fase inicial e contém somente a documentação do produto. Docker e Docker Compose estão previstos para o ambiente local, mas ainda não há linguagem, framework ou banco de dados definidos. A coleta automática de preços, as notificações e a autenticação estão explicitamente previstas como evoluções futuras.

## Decisão de taxonomia

As labels usarão exclusivamente o padrão `categoria: valor` e as categorias `type`, `area` e `priority`.

As áreas serão limitadas a responsabilidades amplas e confirmadas no escopo: API, infraestrutura, documentação, automação, notificações e autenticação. Não serão criadas labels para frontend, banco de dados, mensageria, cache, observabilidade, segurança, tecnologias específicas ou funcionalidades particulares.

## Labels

| Nome | Categoria | Descrição | Cor | Exemplo de uso |
| --- | --- | --- | --- | --- |
| `type: epic` | type | Objetivo amplo que será dividido em várias issues. | `#5319E7` | Implementar o MVP do acompanhamento de preços. |
| `type: feature` | type | Implementação de uma nova funcionalidade. | `#1D76DB` | Criar endpoint para registrar um preço. |
| `type: bug` | type | Correção de um comportamento incorreto. | `#D73A4A` | Corrigir cálculo do menor preço atual. |
| `type: task` | type | Atividade técnica, operacional ou de configuração. | `#0E8A16` | Configurar o ambiente com Docker Compose. |
| `type: refactor` | type | Melhoria interna sem alteração funcional. | `#FBCA04` | Simplificar a organização das rotas da API. |
| `type: test` | type | Criação ou melhoria de testes. | `#BFDADC` | Adicionar testes para a comparação de preços. |
| `type: documentation` | type | Criação ou atualização de documentação. | `#0075CA` | Documentar como executar a aplicação localmente. |
| `type: research` | type | Investigação técnica, estudo ou prova de conceito. | `#C5DEF5` | Avaliar abordagens para coleta automática de preços. |
| `area: api` | area | Contratos HTTP, endpoints, validações e comportamento da API REST. | `#0052CC` | Alterar o endpoint de consulta do histórico de preços. |
| `area: infrastructure` | area | Ambiente, containers, configuração, entrega e execução da aplicação. | `#006B75` | Criar a configuração do Docker Compose. |
| `area: documentation` | area | Documentação do projeto, da arquitetura e do uso da aplicação. | `#0E8A16` | Atualizar as instruções de instalação. |
| `area: automation` | area | Coleta automática de preços, integrações e tarefas agendadas. | `#1F883D` | Criar tarefa agendada para atualizar preços. |
| `area: notifications` | area | Alertas e notificações relacionados a alterações ou metas de preço. | `#2DA44E` | Notificar quando o preço desejado for atingido. |
| `area: authentication` | area | Identidade, autenticação e controle de acesso dos usuários. | `#57AB5A` | Implementar autenticação de usuários. |
| `priority: critical` | priority | Falha grave, problema de segurança, incidente ou bloqueio completo. | `#B60205` | API indisponível em todos os ambientes. |
| `priority: high` | priority | Entrega importante para o milestone atual ou que bloqueia outras issues. | `#D93F0B` | Preparar a infraestrutura necessária para iniciar o MVP. |
| `priority: medium` | priority | Entrega relevante, mas que não impede o andamento principal. | `#FBCA04` | Adicionar um novo filtro de consulta. |
| `priority: low` | priority | Melhoria opcional ou de baixo impacto imediato. | `#C5DEF5` | Melhorar exemplos da documentação. |

## Regras de utilização

Cada issue deve possuir exatamente uma label `type`, exatamente uma label `priority` e pelo menos uma label `area` quando houver uma área claramente relacionada. Uma issue pode receber mais de uma área quando afetar responsabilidades distintas.

Labels de área não representam o tipo da atividade. Status, estimativa, milestone, tecnologia específica, funcionalidade muito particular e estados já controlados pelo GitHub não fazem parte desta taxonomia.

## Aplicação no repositório

Antes da criação, as labels existentes serão consultadas novamente. Labels iguais serão reutilizadas e labels equivalentes serão relatadas para evitar duplicidade. Somente as 18 labels definidas neste documento que estiverem ausentes serão criadas. Nenhuma label será excluída ou renomeada.

Após a criação, uma nova consulta confirmará nome, descrição e cor de cada label, e o resultado será apresentado em uma tabela acompanhado do resumo das labels criadas e das que já existiam.
