# ADR-0002: Adotar GitHub Spec Kit como motor de Spec-Driven Development

- **Status:** Aceita
- **Data:** 2026-08-07
- **Decisores:** Mantenedor do projeto
- **Relacionadas:** [[ADR-0001-build-on-existing-tools-neutral-core]], [[00-vision-and-architecture]], constituição do projeto (`.specify/memory/constitution.md`)

---

## Contexto

O [ADR-0001](ADR-0001-build-on-existing-tools-neutral-core.md) definiu a postura "compor, não
reinventar" e "núcleo neutro + ferramenta como adaptador". Faltava registrar a decisão concreta:
**qual** ferramenta de spec adotar e **como** ela se encaixa.

O [GitHub Spec Kit](https://github.com/github/spec-kit) foi avaliado e verificado na máquina de
desenvolvimento. Fatos apurados (não de memória — checados via CLI instalada):

- Versão instalada: **`specify 0.12.2`**.
- Suporta **30+ agentes** via `specify init --integration <agent>`. `specify check` confirmou que
  **Claude Code** e **opencode** estão ambos *available* nesta máquina.
- Tem **skills mode**: para Claude, `specify init` instala *agent skills* por padrão (não prompts
  de slash-command). Isso realiza o mapeamento "método neutro → skill da ferramenta" do ADR-0001
  **de fábrica** — o próprio Spec Kit gera a camada de adaptador por agente.
- Init in-place: `specify init . --integration claude` (usado `--force` por o repo não estar vazio).

Essa decisão é *distinta* do ADR-0001: aquele estabeleceu a **estratégia** (neutro + adaptador);
este registra a **escolha concreta de ferramenta** e sua adoção.

## Decisão

Adotar o **GitHub Spec Kit** como motor de SDD do projeto, em **skills mode** para o Claude Code.

Fluxo canônico (Princípio "The Loop" da constituição):
`/speckit-specify` → `/speckit-plan` → `/speckit-tasks` → `/speckit-implement`,
com `/speckit-constitution` para princípios e `/speckit-converge` para reconciliar o codebase.
Skills opcionais de qualidade: `/speckit-clarify`, `/speckit-analyze`, `/speckit-checklist`.

Estrutura instalada (executado em 2026-08-07):
- `.specify/` — templates, scripts, workflows, memory (constituição), integrations. **Núcleo neutro** → versionado.
- `.claude/skills/speckit-*` — **camada de adaptador** do Claude Code → versionada (reproduzível por clone).
- `.claude/settings.local.json` — settings locais/possíveis credenciais → **git-ignorado** (aviso de segurança do próprio Spec Kit).

## Relação com a constituição (por que os dois existem)

- A **constituição** *afirma a regra* (Princípio V: núcleo neutro, ferramenta como adaptador; e
  o Loop de desenvolvimento). É governança voltada pra frente.
- Este **ADR** *registra e justifica a decisão* que produziu a regra: qual ferramenta, versão,
  alternativas, consequências. É proveniência voltada pra trás.
- A própria constituição exige que emendas venham com um ADR e que "why does this exist?" tenha
  resposta linkável (Princípio IV). Este ADR é essa resposta para a adoção do Spec Kit.

## Consequências

### Positivas
- **Zero esforço** reconstruindo formato de spec ou runtime de skills.
- **Portabilidade comprovada, não prometida:** `opencode` já aparece como integração disponível;
  trocar de ferramenta = `specify init . --integration opencode --force`, com o núcleo neutro intacto.
- O modo skills já produz a fronteira núcleo/adaptador do ADR-0001 automaticamente.

### Negativas / custos
- **Acoplamento à evolução do Spec Kit** (v0.12.2, pré-1.0 — API pode mudar). Mitigado: templates
  são versionados no repo (`--force` regenera); Spec Kit é substituível por design.
- `.claude/` pode acumular arquivos de ferramenta; exige disciplina de `.gitignore` (feito).
- Comandos slash específicos do Spec Kit não são, por si, neutros — mas são regeneráveis por agente.

### Neutras
- Templates default do Spec Kit podem precisar de ajuste para refletir a constituição; divergências
  vão para ADRs futuros.

## Alternativas consideradas

1. **Formato de spec próprio + runtime próprio.** Rejeitada no ADR-0001 (NIH, lento, ainda precisaria de agente).
2. **Outro toolkit SDD.** Não avaliado a fundo: Spec Kit é neutro quanto a agente, open-source, ativo
   e já instalado/funcionando aqui — atende o requisito com o menor custo.
3. **Prompts de slash-command em vez de skills.** Rejeitada: skills mode é o default do Claude e
   materializa melhor a separação método/adaptador.

## Ações decorrentes

- [x] `specify init . --integration claude --force` executado (2026-08-07).
- [x] Constituição ratificada em `.specify/memory/constitution.md` (v1.0.0) via `/speckit-constitution`.
- [x] `.gitignore` protege `.claude/settings.local.json`; rastreia `.claude/skills/`.
- [ ] Primeira spec real via `/speckit-specify` para exercer o loop ponta-a-ponta (critério de saída da v1).
- [ ] Guardrail de CI (ADR-0001, item aberto #7) garantindo fronteira núcleo-neutro / adaptador.
- [ ] Reavaliar quando Spec Kit chegar a 1.0 (possível emenda deste ADR).
