# ADR-0001: Núcleo neutro em git, ferramentas de IA como adaptadores plugáveis

- **Status:** Aceita
- **Data:** 2026-08-07
- **Decisores:** Mantenedor do projeto
- **Relacionadas:** [[00-vision-and-architecture]], ADR-0002 (formato de spec — a criar), ADR-0003 (representação do knowledge graph — a criar)

---

## Contexto

Ao definir os próximos entregáveis, surgiu a pergunta certa:

> "Claude Code e Spec Kit já não fazem isso por mim? E se amanhã eu quiser usar opencode
> no lugar do Claude Code, essa estrutura ainda serve?"

Duas tensões:

1. **Risco de NIH (*not-invented-here*).** Reinventar um formato de spec (Spec Kit já dá)
   ou um mecanismo de Skills (Claude Code já dá) gastaria o orçamento de novidade no lugar
   errado e enfraqueceria o projeto como peça de portfólio Staff/Principal. Comprar > construir.

2. **Risco de lock-in.** Se a inteligência do projeto (specs, conhecimento, método) viver
   *dentro* de features proprietárias de uma ferramenta, trocar de agente (Claude Code →
   opencode, Cursor, Copilot, Gemini) exigiria reconstruir o projeto. Isso contradiz o
   não-objetivo de neutralidade de fornecedor definido na visão.

A pergunta "e se eu trocar a ferramenta amanhã?" é o teste de estresse do design inteiro.
Um projeto cuja resposta é "aí quebra tudo" não é uma referência de engenharia AI-native —
é um acoplamento disfarçado.

## Decisão

Adotamos duas regras, conjuntas e inseparáveis:

### 1. Compor, não reinventar
- **Specs:** adotamos **Spec Kit** como mecanismo de spec-driven development. Não criamos formato próprio.
- **Capacidades de IA (Skills):** adotamos o mecanismo de **Skills do Claude Code** como *um* adaptador. Não construímos um runtime de agentes próprio.
- **Gates/automação:** usamos os mecanismos nativos da ferramenta (permissões, hooks, CI) em vez de orquestrador próprio.

### 2. Núcleo neutro em git + ferramenta como adaptador fino
O conhecimento e o método vivem em **arquivos neutros no git**. A ferramenta de IA é uma
**camada de adaptador plugável** por cima. A fronteira é explícita e obrigatória:

```
┌───────────────────────────────────────────────────────────┐
│  NÚCLEO NEUTRO (git) — dono da inteligência do projeto      │
│  · specs/          (Spec Kit — formato agnóstico)          │
│  · decisions/      (ADRs em markdown)                      │
│  · knowledge/      (glossário, contratos de módulo, links) │
│  · methods/        (o "como" de cada skill, em prosa neutra)│
│  · app/            (código Android modularizado)           │
│  · tests + CI      (contrato executável, neutro)           │
└───────────────────────────┬───────────────────────────────┘
                            │ invocado por ↓ (fino, descartável)
┌───────────────────────────▼───────────────────────────────┐
│  CAMADA DE ADAPTADOR (específica da ferramenta)            │
│  · adapters/claude-code/   (SKILL.md, hooks, settings)     │
│  · adapters/opencode/      (equivalente — a criar quando   │
│                             /se houver necessidade)         │
└───────────────────────────────────────────────────────────┘
```

**Regra de ouro:** uma Skill não é uma capacidade que *vive* dentro do Claude Code. É um
**método documentado em `methods/`** (neutro) que um adaptador apenas *invoca*. O adaptador
contém só o empacotamento/invocação — zero lógica de domínio.

## Consequências

### Positivas
- **~85% da estrutura é imune à troca de ferramenta** (specs, knowledge, arquitetura, testes, CI). Trocar Claude Code → opencode = escrever um novo diretório `adapters/`, não reescrever o projeto.
- **Portabilidade verificável, não prometida.** A fronteira é uma regra de diretório que um reviewer (humano ou agente) consegue checar.
- **Entrega mais rápida:** não gastamos esforço reconstruindo Spec Kit / runtime de Skills.
- **Portfólio mais forte:** demonstra a decisão madura "comprar > construir" e "projetar para portabilidade", que é exatamente o que se avalia em Staff/Principal.

### Negativas / custos
- **Disciplina contínua exigida.** É fácil um agente acoplar features proprietárias sem perceber. Precisamos de um *guardrail* (ver Ações).
- **Alguma duplicação:** o método fica em `methods/` (neutro) e é referenciado pelo adaptador — um nível de indireção a mais.
- **Dependemos da evolução de terceiros** (Spec Kit, Claude Code). Aceitável: são substituíveis por design.

### Neutras
- Os ~15% acoplados (empacotamento de skill, hooks, settings) ficam isolados e explicitamente descartáveis em `adapters/`.

## Alternativas consideradas

1. **Construir sistema SDD próprio (formato de spec + runtime de skills).**
   Rejeitada: NIH, lento, gasta novidade no lugar errado, ainda assim precisaria de um agente para rodar.

2. **Acoplar tudo ao Claude Code (usar features proprietárias livremente).**
   Rejeitada: lock-in; quebra na primeira troca de ferramenta; contradiz a tese de neutralidade.

3. **Suportar múltiplas ferramentas desde já (Claude Code + opencode em paralelo na v1).**
   Rejeitada por ora: custo de manter dois adaptadores sem necessidade comprovada. A
   arquitetura *permite* isso; só criamos o segundo adaptador quando houver demanda real
   (provaria portabilidade — candidato natural para a v3).

## Ações decorrentes

- [ ] Criar diretórios `methods/` (neutro) e `adapters/claude-code/` (específico) com a fronteira documentada.
- [ ] Adicionar um **guardrail no CI**: verificar que nenhum arquivo fora de `adapters/` referencia mecanismos específicos de ferramenta (lint simples por grep).
- [ ] ADR-0002: formato de spec — confirmar adoção do Spec Kit e como ele se encaixa em `specs/`.
- [ ] ADR-0003: representação do knowledge graph (arquivos em git — já decidido na visão, formalizar).
- [ ] Atualizar `00-vision-and-architecture.md` para tornar a camada de adaptador explícita. ✅