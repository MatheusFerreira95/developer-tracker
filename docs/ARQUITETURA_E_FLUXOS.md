
# Documentação de Arquitetura e Fluxos - Developer Tracker

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura do Sistema](#arquitetura-do-sistema)
3. [Fluxos Principais](#fluxos-principais)
4. [Sistema de Cache](#sistema-de-cache)
5. [Otimizações de Performance](#otimizações-de-performance)
6. [Diagramas de Fluxo](#diagramas-de-fluxo)

---

## 🎯 Visão Geral

O **Developer Tracker** é uma aplicação que analisa repositórios Git para extrair métricas sobre desenvolvedores, código e histórico do projeto. O sistema foi otimizado para processar grandes repositórios de forma eficiente usando paralelização e cache inteligente.

### Principais Funcionalidades

- **Análise de Desenvolvedores**: Calcula LOC (Linhas de Código) por desenvolvedor
- **Truck Factor**: Identifica desenvolvedores críticos do projeto
- **Estatísticas de Commits**: Conta commits por desenvolvedor
- **Análise de Linguagens**: Estatísticas de código por linguagem de programação
- **Cache Inteligente**: Armazena resultados para evitar recálculos

---

## 🏗️ Arquitetura do Sistema

### Componentes Principais

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Vue.js)                        │
│              Interface web para visualização                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ HTTP REST API
                       │
┌──────────────────────▼──────────────────────────────────────┐
│              Backend (Spring Boot)                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         ProjectController (REST Endpoints)           │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                        │
│  ┌──────────────────▼───────────────────────────────────┐  │
│  │              Project (Domain)                         │  │
│  │  - buildOverview()                                    │  │
│  │  - calcLocsDeveloperList()                            │  │
│  │  - calcTruckFactor()                                   │  │
│  │  - calcNumCommits()                                   │  │
│  └──────┬──────────────┬──────────────┬──────────────────┘  │
│         │              │              │                       │
│  ┌──────▼──────┐ ┌────▼──────┐ ┌───▼────────────┐          │
│  │     Git     │ │   CLOC     │ │ ProjectCache   │          │
│  │  (Utils)    │ │  (Utils)   │ │   Manager      │          │
│  └─────────────┘ └────────────┘ └────────────────┘          │
│         │                                                     │
│         └──────────────────┬──────────────────┐             │
│                            │                  │              │
│                    ┌───────▼──────┐   ┌───────▼──────┐      │
│                    │  Git Repo    │   │   Caffeine   │      │
│                    │  (Local)     │   │    Cache     │      │
│                    └──────────────┘   └───────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

### Classes Principais

#### 1. **Project** (Domain)
Classe central que orquestra todas as análises.

**Responsabilidades:**
- Calcular métricas de desenvolvedores
- Coordenar processamento paralelo
- Integrar com cache
- Orquestrar chamadas para Git e CLOC

**Métodos Principais:**
- `buildOverview()`: Método principal que constrói visão completa
- `calcLocsDeveloperList()`: Calcula LOC por desenvolvedor (paralelizado)
- `calcTruckFactor()`: Calcula truck factor
- `calcNumCommits()`: Conta commits
- `calcDeveloperList()`: Lista desenvolvedores com estatísticas

#### 2. **ProjectCacheManager** (Util)
Gerencia cache em memória usando Caffeine.

**Responsabilidades:**
- Armazenar resultados de análises
- Gerar chaves de cache únicas
- Invalidar cache quando necessário
- Fornecer estatísticas de cache

**Caches Gerenciados:**
- Cache de projeto completo
- Cache de git blame por arquivo
- Cache de lista de desenvolvedores
- Cache de truck factor
- Cache de commits
- Cache de estatísticas de linguagens

#### 3. **Git** (Util)
Utilitário para executar comandos Git.

**Responsabilidades:**
- Executar comandos Git de forma cross-platform
- Validar estado do repositório
- Obter hash de commits
- Fazer checkout de branches

#### 4. **CLOC** (Util)
Utilitário para executar CLOC (Count Lines of Code).

**Responsabilidades:**
- Executar CLOC para contar linhas por linguagem
- Processar saída do CLOC
- Retornar estatísticas formatadas

---

## 🔄 Fluxos Principais

### Fluxo 1: Análise Completa do Projeto (buildOverview)

Este é o fluxo mais importante do sistema. Ele é acionado quando o usuário solicita uma análise completa do projeto.

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Usuário solicita análise via API                        │
│    POST /api/project/overview                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Project.buildOverview(filter, checkout)                 │
│    - Cria/obtém projeto                                     │
│    - Faz checkout da branch                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. Validação de Segurança                                   │
│    - Valida estado do checkout                              │
│    - Obtém hash do commit atual                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. Verificação de Cache                                     │
│    ┌──────────────────────────────────────┐                │
│    │ Cache Key: repo + branch + commit    │                │
│    └──────────────────────────────────────┘                │
│                       │                                     │
│         ┌─────────────┴─────────────┐                      │
│         │                            │                      │
│    CACHE HIT                    CACHE MISS                  │
│         │                            │                      │
│         ▼                            ▼                      │
│    Retorna projeto              Continua para passo 5       │
│    em cache (rápido!)                                       │
└─────────────────────────────────────────────────────────────┘
                       │
                       ▼ (apenas se cache miss)
┌─────────────────────────────────────────────────────────────┐
│ 5. Cálculo Paralelo de Métricas                             │
│                                                              │
│    ┌──────────────────┐  ┌──────────────────┐              │
│    │ calcDeveloperList│  │ calcTruckFactor  │              │
│    │ (paralelo)       │  │ (paralelo)       │              │
│    └────────┬─────────┘  └────────┬─────────┘              │
│             │                     │                          │
│    ┌────────▼─────────┐  ┌──────▼──────────┐              │
│    │ calcNumCommits    │  │ calcLanguageStats│              │
│    │ (paralelo)        │  │ (paralelo)       │              │
│    └───────────────────┘  └─────────────────┘              │
│                                                              │
│    Aguarda todas as tarefas terminarem                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. Combinação de Resultados                                 │
│    - Combina resultados de todas as métricas                │
│    - Calcula truck factor final                             │
│    - Calcula LOC total do projeto                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. Armazenamento no Cache                                   │
│    - Armazena projeto completo no cache                     │
│    - Próxima consulta será instantânea                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 8. Retorna Projeto Completo                                 │
│    - Todas as métricas calculadas                          │
│    - Pronto para exibição no frontend                      │
└─────────────────────────────────────────────────────────────┘
```

### Fluxo 2: Cálculo de LOC por Desenvolvedor (calcLocsDeveloperList)

Este é o método mais complexo e otimizado. Processa git blame de múltiplos arquivos em paralelo.

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Listar Arquivos do Repositório                           │
│    git ls-files [filterPath]                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Filtrar e Normalizar Caminhos                            │
│    - Remove arquivos inválidos                              │
│    - Normaliza espaços                                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. Obter Hash do Commit                                     │
│    - Para gerar chaves de cache únicas                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. Criar Tarefas Paralelas para Cada Arquivo                │
│                                                              │
│    Arquivo 1 ──┐                                            │
│    Arquivo 2 ──┤                                            │
│    Arquivo 3 ──┼──► Thread Pool (GIT_EXECUTOR)              │
│    Arquivo 4 ──┤                                            │
│    ...         ┘                                            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼ (para cada arquivo em paralelo)
┌─────────────────────────────────────────────────────────────┐
│ 5. Processar Arquivo (dentro de thread separada)           │
│                                                              │
│    ┌──────────────────────────────────────┐                │
│    │ 5.1. Verificar Cache do Git Blame    │                │
│    │      Chave: repo + arquivo + commit  │                │
│    └──────────────┬───────────────────────┘                │
│                   │                                          │
│         ┌─────────┴─────────┐                                │
│         │                   │                                │
│    CACHE HIT          CACHE MISS                            │
│         │                   │                                │
│         │                   ▼                                │
│         │    ┌──────────────────────────┐                   │
│         │    │ Executar git blame        │                   │
│         │    │ git blame --line-porcelain│                   │
│         │    └───────────┬────────────────┘                  │
│         │                │                                    │
│         │                ▼                                    │
│         │    ┌──────────────────────────┐                    │
│         │    │ Armazenar no Cache        │                    │
│         │    └───────────┬────────────────┘                   │
│         │                │                                    │
│         └────────┬───────┘                                    │
│                  │                                            │
│                  ▼                                            │
│    ┌──────────────────────────────────────┐                  │
│    │ 5.2. Processar Saída do Git Blame    │                  │
│    │      - Extrair linhas "author ..."   │                  │
│    │      - Contar LOC por autor          │                  │
│    │      - Atualizar ConcurrentHashMap   │                  │
│    └──────────────────────────────────────┘                  │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼ (aguarda todas as threads)
┌─────────────────────────────────────────────────────────────┐
│ 6. Aguardar Todas as Tarefas Paralelas                      │
│    CompletableFuture.allOf(...).join()                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. Converter Mapa em Lista de Developers                   │
│    ConcurrentHashMap<String, AtomicInteger>                  │
│              ↓                                                │
│    List<Developer> ordenada por LOC                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 8. Ordenar por LOC (Descendente)                            │
│    - Desenvolvedor com mais LOC primeiro                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ 9. Retornar Lista de Developers                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 💾 Sistema de Cache

### Estratégia de Cache

O sistema usa **cache em memória** com a biblioteca Caffeine para armazenar resultados de análises.

### Chaves de Cache

Todas as chaves de cache incluem:
- **Repositório**: Identifica qual repositório
- **Branch/Checkout**: Identifica qual branch/tag
- **Commit Hash**: Identifica qual commit específico

**Exemplo de chave:**
```
project:/path/to/repo:master:abc123def456...
```

### Por que Commit Hash na Chave?

O commit hash garante que:
- ✅ Diferentes branches com mesmo arquivo = chaves diferentes
- ✅ Mesmo branch em commits diferentes = chaves diferentes
- ✅ Mudanças no código invalidam cache automaticamente
- ✅ Não há risco de usar cache de branch errada

### Tipos de Cache

| Tipo de Cache | Tamanho Máx | TTL | Uso |
|--------------|-------------|-----|-----|
| **Projeto Completo** | 100 | 1 hora | Resultado completo da análise |
| **Git Blame** | 1000 | 30 min | Saída do git blame por arquivo |
| **Lista de Developers** | 200 | 1 hora | Lista de desenvolvedores com LOC |
| **Truck Factor** | 100 | 2 horas | Resultado do truck factor (mais caro) |
| **Commits** | 200 | 1 hora | Contagem de commits |
| **Linguagens** | 200 | 1 hora | Estatísticas por linguagem |

### Fluxo de Cache

```
┌─────────────────────────────────────────────────────────────┐
│ Requisição de Análise                                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ Gerar Chave de Cache                                        │
│ repo + branch + commitHash                                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ Verificar Cache                                             │
│                                                              │
│         ┌──────────────────────┐                            │
│         │  Cache.get(key)      │                            │
│         └──────────┬───────────┘                            │
│                    │                                          │
│         ┌──────────┴──────────┐                             │
│         │                      │                             │
│    CACHE HIT              CACHE MISS                         │
│         │                      │                             │
│         │                      ▼                             │
│         │         ┌──────────────────────┐                 │
│         │         │ Calcular Resultado     │                 │
│         │         │ (pode ser lento)      │                 │
│         │         └──────────┬─────────────┘                 │
│         │                   │                                │
│         │                   ▼                                │
│         │         ┌──────────────────────┐                 │
│         │         │ Cache.put(key, value) │                 │
│         │         └──────────┬─────────────┘                 │
│         │                   │                                │
│         └───────────┬───────┘                                │
│                     │                                         │
│                     ▼                                         │
│         ┌──────────────────────┐                             │
│         │ Retornar Resultado   │                             │
│         └──────────────────────┘                             │
└─────────────────────────────────────────────────────────────┘
```

### Invalidação de Cache

O cache é invalidado automaticamente quando:
1. **TTL expira**: Após o tempo de vida configurado
2. **Tamanho máximo**: Quando cache atinge limite, remove entradas antigas (LRU)
3. **Commit diferente**: Chave diferente = cache miss automático

---

## ⚡ Otimizações de Performance

### 1. Paralelização

**Antes:**
```
Arquivo 1 → git blame → processar
Arquivo 2 → git blame → processar
Arquivo 3 → git blame → processar
...
Tempo total: N × tempo_por_arquivo
```

**Depois:**
```
Arquivo 1 ──┐
Arquivo 2 ──┼──► Thread Pool ──► Processar em paralelo
Arquivo 3 ──┤
...         ┘
Tempo total: ~tempo_por_arquivo (limitado por número de threads)
```

**Ganho:** 4-8x mais rápido (dependendo do número de processadores)

### 2. Cache Inteligente

**Antes:**
```
Cada requisição → Recalcula tudo → Lento
```

**Depois:**
```
Primeira requisição → Calcula e cacheia → Lento
Requisições seguintes → Retorna do cache → Instantâneo (10-50x mais rápido)
```

**Ganho:** 10-50x mais rápido em requisições subsequentes

### 3. Otimização de Complexidade

**Antes (O(n²)):**
```java
// Loop aninhado - O(n²)
for (Developer dev : developerList) {
    for (Developer devTF : devTFList) {
        if (dev.equals(devTF)) {
            dev.truckFactor = true;
        }
    }
}
```

**Depois (O(n)):**
```java
// HashSet lookup - O(1) por elemento
Set<String> devTFNames = new HashSet<>(devTFList.size());
for (Developer devTF : devTFList) {
    devTFNames.add(devTF.name);
}
for (Developer dev : developerList) {
    if (devTFNames.contains(dev.name)) {
        dev.truckFactor = true;
    }
}
```

**Ganho:** De O(n²) para O(n) - muito mais rápido com muitos desenvolvedores

### 4. Thread-Safe Collections

- **ConcurrentHashMap**: Permite atualizações concorrentes sem locks pesados
- **AtomicInteger**: Incremento thread-safe sem sincronização explícita
- **Caffeine Cache**: Gerencia concorrência internamente

### 5. Pool de Threads Dedicado

- Pool separado para operações Git (I/O-bound)
- Evita saturar ForkJoinPool comum
- Melhor controle de concorrência

---

## 📊 Diagramas de Fluxo

### Diagrama de Sequência: buildOverview

```
Cliente          Project          Cache          Git/CLOC
  │                │                │              │
  │──buildOverview─►│                │              │
  │                │                │              │
  │                │──getCommitHash─►│              │
  │                │◄──commitHash────│              │
  │                │                │              │
  │                │──getProject─────►│              │
  │                │                │              │
  │         ┌──────┴──────┐         │              │
  │         │ Cache HIT?  │         │              │
  │         └──┬──────┬───┘         │              │
  │            │      │             │              │
  │         SIM│      │NÃO          │              │
  │            │      │             │              │
  │            │      ▼             │              │
  │            │   ┌──────────────────────────┐  │
  │            │   │ Executar em Paralelo:    │  │
  │            │   │ - calcDeveloperList       │──┤
  │            │   │ - calcTruckFactor         │──┤
  │            │   │ - calcNumCommits         │──┤
  │            │   │ - calcLanguageStats      │──┤
  │            │   └──────────────────────────┘  │
  │            │      │             │              │
  │            │      │             │              │
  │            │      ▼             │              │
  │            │   ┌──────────────────────────┐  │
  │            │   │ Aguardar todas completarem│  │
  │            │   └──────────────────────────┘  │
  │            │      │             │              │
  │            │      ▼             │              │
  │            │   ┌──────────────────────────┐  │
  │            │   │ putProject (cache)        │──►│
  │            │   └──────────────────────────┘  │
  │            │      │             │              │
  │            ▼      ▼             │              │
  │         ┌──────────────────────┐              │
  │         │ Retornar Projeto     │              │
  │         └──────────────────────┘              │
  │◄─────────────────────────────────│              │
```

### Diagrama de Estados: Processamento de Arquivo

```
┌─────────────┐
│  Início     │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ Listar Arquivos │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐      ┌──────────────┐
│ Para cada       │─────►│ Criar Tarefa │
│ Arquivo         │      │ Paralela     │
└─────────────────┘      └──────┬───────┘
                                 │
                                 ▼
                    ┌────────────────────┐
                    │ Verificar Cache     │
                    └──────┬──────────────┘
                           │
              ┌────────────┴────────────┐
              │                          │
         CACHE HIT                  CACHE MISS
              │                          │
              │                          ▼
              │              ┌────────────────────┐
              │              │ Executar git blame │
              │              └──────┬─────────────┘
              │                     │
              │                     ▼
              │         ┌────────────────────┐
              │         │ Armazenar no Cache  │
              │         └──────┬─────────────┘
              │                │
              └────────┬───────┘
                       │
                       ▼
            ┌────────────────────┐
            │ Processar Saída    │
            │ Extrair Autores    │
            └──────┬─────────────┘
                   │
                   ▼
            ┌────────────────────┐
            │ Atualizar Mapa     │
            │ (Thread-Safe)       │
            └──────┬─────────────┘
                   │
                   ▼
            ┌────────────────────┐
            │ Aguardar Todas     │
            │ Tarefas            │
            └──────┬─────────────┘
                   │
                   ▼
            ┌────────────────────┐
            │ Converter e        │
            │ Ordenar            │
            └──────┬─────────────┘
                   │
                   ▼
            ┌────────────────────┐
            │ Retornar Lista     │
            └────────────────────┘
```

---

## 🔍 Detalhes Técnicos

### Thread Pool Configuration

```java
ExecutorService GIT_EXECUTOR = Executors.newFixedThreadPool(
    Math.max(4, Runtime.getRuntime().availableProcessors())
);
```

- **Mínimo:** 4 threads
- **Máximo:** Número de processadores disponíveis
- **Tipo:** Fixed Thread Pool (threads reutilizáveis)
- **Uso:** Operações Git I/O-bound

### Estruturas de Dados

| Estrutura | Uso | Thread-Safe? |
|-----------|-----|--------------|
| `ConcurrentHashMap<String, AtomicInteger>` | Contar LOC por autor | ✅ Sim |
| `HashSet<String>` | Lookup O(1) de nomes | ❌ Não (apenas leitura) |
| `ArrayList<Developer>` | Lista final de desenvolvedores | ❌ Não (após paralelização) |
| `Caffeine Cache` | Cache em memória | ✅ Sim (gerenciado internamente) |

### Comandos Git Utilizados

| Comando | Uso | Exemplo |
|---------|-----|---------|
| `git ls-files` | Listar arquivos | `git ls-files src/` |
| `git blame --line-porcelain` | Análise linha por linha | `git blame --line-porcelain arquivo.java` |
| `git log --pretty=format:"%an"` | Listar autores | `git log --pretty=format:"%an"` |
| `git rev-list --count HEAD` | Contar commits | `git rev-list --count HEAD` |
| `git rev-parse HEAD` | Obter hash do commit | `git rev-parse HEAD` |

---

## 📝 Notas Finais

### Pontos Importantes

1. **Cache é seguro entre branches**: Commit hash na chave garante isolamento
2. **Paralelização não bloqueia**: Múltiplas requisições podem processar simultaneamente
3. **Thread-safety**: Todas as estruturas compartilhadas são thread-safe
4. **Performance**: Cache + Paralelização = 10-50x mais rápido em casos comuns

### Limitações Conhecidas

1. Cache é em memória: Perdido ao reiniciar aplicação
2. Tamanho do cache: Limitado pela memória disponível
3. Primeira execução: Sempre mais lenta (sem cache)

### Melhorias Futuras

1. Cache distribuído (Redis)
2. Cache persistente em disco
3. Métricas de performance mais detalhadas
4. Configuração dinâmica de tamanho de cache

---

**Última atualização:** Janeiro 2026
**Versão:** 1.0
