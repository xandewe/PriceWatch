# GitHub Labels Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar no repositório `xandewe/PriceWatch` somente as labels ausentes da taxonomia aprovada e verificar o estado remoto final.

**Architecture:** A alteração afeta exclusivamente a configuração de labels do GitHub. O fluxo consulta o estado remoto, reutiliza correspondências exatas, cria somente ausências pelas configurações do repositório e valida cada label por nome, descrição e cor após a escrita.

**Tech Stack:** GitHub Issues, GitHub Labels, navegador autenticado e conector GitHub para verificação.

## Global Constraints

- Usar exclusivamente o padrão `categoria: valor`.
- Usar somente as categorias `type`, `area` e `priority`.
- Não excluir nem renomear labels existentes.
- Não criar labels além das 18 definidas na especificação aprovada.
- Preservar exatamente os nomes, as descrições e as cores definidos em `docs/superpowers/specs/2026-07-15-github-labels-design.md`.
- Cada issue deverá receber exatamente uma label `type`, exatamente uma label `priority` e pelo menos uma label `area` quando houver uma área claramente relacionada.

---

### Task 1: Inventariar o estado remoto imediatamente antes da escrita

**Files:**
- Read: `docs/superpowers/specs/2026-07-15-github-labels-design.md`
- Remote inspect: `https://github.com/xandewe/PriceWatch/labels`

**Interfaces:**
- Consumes: lista das 18 labels aprovadas na especificação.
- Produces: conjuntos `existing_exact`, `existing_equivalent` e `missing`, indexados pelo nome da label.

- [ ] **Step 1: Abrir a administração de labels**

Abrir `https://github.com/xandewe/PriceWatch/labels` em uma sessão autenticada e confirmar que o repositório exibido é `xandewe/PriceWatch`.

- [ ] **Step 2: Comparar as labels existentes com a especificação**

Pesquisar cada um dos 18 nomes exatos e também os equivalentes comuns `bug`, `documentation` e `enhancement`. Classificar um nome exato existente em `existing_exact`, um equivalente semântico em `existing_equivalent` e os demais em `missing`.

- [ ] **Step 3: Confirmar a regra de criação**

Esperado: somente itens de `missing` seguem para criação. Nenhuma label de `existing_exact` ou `existing_equivalent` é excluída, renomeada ou alterada sem autorização adicional.

### Task 2: Criar as labels de tipo ausentes

**Files:**
- Remote modify: `https://github.com/xandewe/PriceWatch/labels`

**Interfaces:**
- Consumes: itens de categoria `type` presentes em `missing`.
- Produces: labels de tipo disponíveis no repositório com nome, descrição e cor aprovados.

- [ ] **Step 1: Criar cada tipo ausente**

Usar o botão de nova label e salvar exatamente estes valores quando o nome estiver em `missing`:

| Nome | Descrição | Cor |
| --- | --- | --- |
| `type: epic` | Objetivo amplo que será dividido em várias issues. | `5319E7` |
| `type: feature` | Implementação de uma nova funcionalidade. | `1D76DB` |
| `type: bug` | Correção de um comportamento incorreto. | `D73A4A` |
| `type: task` | Atividade técnica, operacional ou de configuração. | `0E8A16` |
| `type: refactor` | Melhoria interna sem alteração funcional. | `FBCA04` |
| `type: test` | Criação ou melhoria de testes. | `BFDADC` |
| `type: documentation` | Criação ou atualização de documentação. | `0075CA` |
| `type: research` | Investigação técnica, estudo ou prova de conceito. | `C5DEF5` |

- [ ] **Step 2: Confirmar o resultado da categoria**

Esperado: a página lista os oito nomes de tipo, somando labels já existentes e criadas, sem nomes adicionais nessa categoria.

### Task 3: Criar as labels de área ausentes

**Files:**
- Remote modify: `https://github.com/xandewe/PriceWatch/labels`

**Interfaces:**
- Consumes: itens de categoria `area` presentes em `missing`.
- Produces: labels de área disponíveis no repositório com nome, descrição e cor aprovados.

- [ ] **Step 1: Criar cada área ausente**

Usar o botão de nova label e salvar exatamente estes valores quando o nome estiver em `missing`:

| Nome | Descrição | Cor |
| --- | --- | --- |
| `area: api` | Contratos HTTP, endpoints, validações e comportamento da API REST. | `0052CC` |
| `area: infrastructure` | Ambiente, containers, configuração, entrega e execução da aplicação. | `006B75` |
| `area: documentation` | Documentação do projeto, da arquitetura e do uso da aplicação. | `0E8A16` |
| `area: automation` | Coleta automática de preços, integrações e tarefas agendadas. | `1F883D` |
| `area: notifications` | Alertas e notificações relacionados a alterações ou metas de preço. | `2DA44E` |
| `area: authentication` | Identidade, autenticação e controle de acesso dos usuários. | `57AB5A` |

- [ ] **Step 2: Confirmar o resultado da categoria**

Esperado: a página lista somente os seis nomes de área aprovados, somando labels já existentes e criadas.

### Task 4: Criar as labels de prioridade ausentes

**Files:**
- Remote modify: `https://github.com/xandewe/PriceWatch/labels`

**Interfaces:**
- Consumes: itens de categoria `priority` presentes em `missing`.
- Produces: labels de prioridade disponíveis no repositório com nome, descrição e cor aprovados.

- [ ] **Step 1: Criar cada prioridade ausente**

Usar o botão de nova label e salvar exatamente estes valores quando o nome estiver em `missing`:

| Nome | Descrição | Cor |
| --- | --- | --- |
| `priority: critical` | Falha grave, problema de segurança, incidente ou bloqueio completo. | `B60205` |
| `priority: high` | Entrega importante para o milestone atual ou que bloqueia outras issues. | `D93F0B` |
| `priority: medium` | Entrega relevante, mas que não impede o andamento principal. | `FBCA04` |
| `priority: low` | Melhoria opcional ou de baixo impacto imediato. | `C5DEF5` |

- [ ] **Step 2: Confirmar o resultado da categoria**

Esperado: a página lista as quatro prioridades aprovadas, somando labels já existentes e criadas, sem prioridades adicionais criadas pelo plano.

### Task 5: Verificar e relatar o estado final

**Files:**
- Read: `docs/superpowers/specs/2026-07-15-github-labels-design.md`
- Remote inspect: `https://github.com/xandewe/PriceWatch/labels`

**Interfaces:**
- Consumes: estado remoto após as Tasks 2 a 4 e conjuntos produzidos na Task 1.
- Produces: confirmação final das 18 labels e resumo `created` versus `already_existing`.

- [ ] **Step 1: Consultar cada label por nome**

Usar o conector GitHub para consultar os 18 nomes exatos. Para cada retorno, comparar `name`, `description` e `color` com a especificação.

- [ ] **Step 2: Validar as contagens**

Esperado: 8 labels `type`, 6 labels `area` e 4 labels `priority`, totalizando 18 labels da taxonomia. Todas devem ter descrição em português e cor igual à especificação.

- [ ] **Step 3: Preparar o relatório final**

Apresentar a tabela completa com nome, categoria, descrição, cor hexadecimal e exemplo de uso. Informar separadamente quais labels foram criadas, quais já existiam e quaisquer equivalentes encontrados. Declarar explicitamente que nenhuma label foi excluída ou renomeada.
