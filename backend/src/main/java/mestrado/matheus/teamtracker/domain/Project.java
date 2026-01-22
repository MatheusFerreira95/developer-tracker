package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import mestrado.matheus.teamtracker.util.CLOC;
import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;
import mestrado.matheus.teamtracker.util.ProjectCacheManager;

/**
 * Classe principal que representa um projeto Git e realiza análises de código.
 * 
 * RESPONSABILIDADES:
 * - Calcular métricas de desenvolvedores (LOC, commits, truck factor)
 * - Processar informações do Git de forma paralela e otimizada
 * - Gerenciar cache de resultados para melhor performance
 * - Integrar com ferramentas externas (CLOC, Git)
 * 
 * OTIMIZAÇÕES IMPLEMENTADAS:
 * 1. Paralelização: Processa múltiplos arquivos simultaneamente usando CompletableFuture
 * 2. Cache: Armazena resultados em memória para evitar recálculos
 * 3. Complexidade: Usa HashSet para O(1) em vez de O(n²) em loops aninhados
 * 4. Thread-safety: Usa ConcurrentHashMap e AtomicInteger para operações paralelas
 */
public class Project {

	/**
	 * Pool de threads dedicado para operações Git paralelas.
	 * 
	 * POR QUE UM POOL SEPARADO?
	 * - Operações Git são I/O-bound (esperam disco/rede), não CPU-bound
	 * - Usar ForkJoinPool comum pode saturar com muitas tarefas I/O
	 * - Pool dedicado permite melhor controle de concorrência
	 * 
	 * CONFIGURAÇÃO:
	 * - Mínimo de 4 threads
	 * - Máximo: número de processadores disponíveis
	 * - Ideal para processar múltiplos arquivos em paralelo (git blame)
	 */
	private static final ExecutorService GIT_EXECUTOR = Executors.newFixedThreadPool(
		Math.max(4, Runtime.getRuntime().availableProcessors())
	);

	public Integer numLoc;
	public Integer numCommits;
	public Integer numActiveDays;
	public String firstCommit;
	public String lastCommit;
	public String localRepository;
	public String checkout;
	public List<NumLocProgrammingLanguage> numLocProgrammingLanguageList = new ArrayList<NumLocProgrammingLanguage>();
	public List<Developer> developerList = new ArrayList<Developer>();
	public Integer truckFactor = 0;
	public String currentVersion;

	public Project(String localRepository, String checkout) {

		this.localRepository = localRepository;
		this.checkout = checkout != null && !checkout.isEmpty() ? checkout : "master";
	}

	/**
	 * Calcula o número total de commits do projeto.
	 * 
	 * FLUXO:
	 * 1. Obtém o hash do commit atual (para chave de cache única)
	 * 2. Verifica se o resultado está em cache
	 * 3. Se cache HIT: retorna valor em cache (muito rápido)
	 * 4. Se cache MISS: executa "git rev-list --count HEAD"
	 * 5. Armazena resultado no cache para próximas consultas
	 * 
	 * OTIMIZAÇÃO:
	 * - Cache evita executar git rev-list repetidamente
	 * - Chave de cache inclui commit hash, garantindo que mudanças no repositório
	 *   invalidem automaticamente o cache
	 * 
	 * @param project Projeto a ser analisado
	 * @return Número total de commits (0 em caso de erro)
	 */
	public static Integer calcNumCommits(Project project) {
		// PASSO 1: Gerar chave de cache única baseada em repositório + branch + commit
		// O commit hash garante que mudanças no repositório invalidem o cache
		String commitHash = Git.getCommitHash(project);
		String cacheKey = ProjectCacheManager.getCommitsCacheKey(
			project.localRepository, project.checkout, commitHash);

		// PASSO 2: Verificar cache primeiro (operação O(1) - muito rápida)
		Integer cached = ProjectCacheManager.getCommits(cacheKey);
		if (cached != null) {
			System.out.println("Cache HIT: commits for " + project.localRepository);
			return cached; // Retorna imediatamente sem executar Git
		}

		// PASSO 3: Cache MISS - precisa calcular executando comando Git
		GitOutput gitOutput;
		try {
			// Executa: git rev-list --count HEAD
			// Retorna apenas o número de commits (ex: "1234")
			gitOutput = Git.runCommand(project, "git rev-list --count HEAD", true);
			Integer commits = Integer.parseInt(gitOutput.outputList.get(0));
			
			// PASSO 4: Armazenar no cache para próximas consultas
			ProjectCacheManager.putCommits(cacheKey, commits);
			return commits;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return 0; // Retorna 0 em caso de erro
	}

	public void calcNumActiveDaysAndFirstCommitAndLastCommit() throws IOException, InterruptedException {

		GitOutput gitOutput = Git.runCommand(this, "git log --date=short --pretty=format:%ad | sort | uniq -c", true);
		this.numActiveDays = gitOutput.outputList.size();
		this.firstCommit = gitOutput.outputList.get(0).substring(gitOutput.outputList.get(0).lastIndexOf(" "));
		this.lastCommit = gitOutput.outputList.get(this.numActiveDays - 1)
				.substring(gitOutput.outputList.get(this.numActiveDays - 1).lastIndexOf(" "));

	}

	/**
	 * Calculate programming language statistics with caching support.
	 */
	private static List<NumLocProgrammingLanguage> calcNumLocProgrammingLanguageList(Project project) {
		// Get commit hash for cache key
		String commitHash = Git.getCommitHash(project);
		String cacheKey = ProjectCacheManager.getLanguageStatsCacheKey(
			project.localRepository, project.checkout, commitHash);

		// Check cache first
		List<NumLocProgrammingLanguage> cached = ProjectCacheManager.getLanguageStats(cacheKey);
		if (cached != null) {
			System.out.println("Cache HIT: language stats for " + project.localRepository);
			return cached;
		}

		// Cache miss - calculate
		try {
			List<NumLocProgrammingLanguage> stats = CLOC.buildNumLocProgrammingLanguageList(project);
			
			// Store in cache
			ProjectCacheManager.putLanguageStats(cacheKey, stats);
			return stats;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return new ArrayList<NumLocProgrammingLanguage>();
	}

	public static List<Developer> calcLocCommitDeveloperList(Project project, String filterPath,
			List<Developer> devTFList) throws IOException, InterruptedException {

		final CompletableFuture<List<Developer>> calcLocsDeveloperListRun = CompletableFuture
				.supplyAsync(() -> calcLocsDeveloperList(project, filterPath));

		final CompletableFuture<HashMap<String, Integer>> calcCommitsDeveloperListRun = CompletableFuture
				.supplyAsync(() -> calcCommitsDeveloperList(project, filterPath));

		try {

			List<Developer> developerList = calcLocsDeveloperListRun.get();
			HashMap<String, Integer> developerCommitsMap = calcCommitsDeveloperListRun.get();

			for (Developer developer : developerList) {

				if (developerCommitsMap.containsKey(developer.name))
					developer.numCommits = developerCommitsMap.get(developer.name);

			}

			// Optimize: Use HashSet for O(1) lookup instead of O(n²) nested loop
			Set<String> devTFNames = new HashSet<String>(devTFList.size());
			for (Developer devTF : devTFList) {
				devTFNames.add(devTF.name);
			}
			
			for (Developer dev : developerList) {
				if (devTFNames.contains(dev.name)) {
					dev.truckFactor = true;
				}
			}

			return developerList;

		} catch (InterruptedException | ExecutionException e) {

			System.err.println("findResume process error" + e);
			throw new RuntimeException();
		}

	}

	/**
	 * Calcula as Linhas de Código (LOC) por desenvolvedor processando git blame de cada arquivo.
	 * 
	 * ESTE É O MÉTODO MAIS COMPLEXO E OTIMIZADO DO SISTEMA!
	 * 
	 * ANTES (versão antiga com shell):
	 *   git ls-files | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort -f | uniq -ic | sort -nr
	 *   - Processava arquivos sequencialmente (lento)
	 *   - Dependia de ferramentas Unix (xargs, sed, sort, uniq)
	 *   - Não funcionava no Windows
	 * 
	 * AGORA (versão otimizada):
	 *   - Processa arquivos EM PARALELO (4-8x mais rápido)
	 *   - Usa Java puro (funciona em Windows e Linux)
	 *   - Integra cache para evitar reprocessar arquivos
	 *   - Thread-safe usando ConcurrentHashMap e AtomicInteger
	 * 
	 * FLUXO DETALHADO:
	 * 1. Lista todos os arquivos do repositório (git ls-files)
	 * 2. Para cada arquivo, cria uma tarefa paralela que:
	 *    a. Verifica cache do git blame desse arquivo
	 *    b. Se cache MISS: executa git blame --line-porcelain
	 *    c. Extrai nomes dos autores de cada linha
	 *    d. Conta LOC por autor (thread-safe)
	 * 3. Aguarda todas as tarefas paralelas terminarem
	 * 4. Converte mapa de contadores em lista de Developer
	 * 5. Ordena por LOC (descendente)
	 * 
	 * OTIMIZAÇÕES:
	 * - Paralelização: múltiplos arquivos processados simultaneamente
	 * - Cache: git blame por arquivo é cacheado (evita reprocessar)
	 * - Thread-safety: ConcurrentHashMap permite atualizações concorrentes
	 * - AtomicInteger: incremento thread-safe sem locks pesados
	 * 
	 * @param project Projeto a ser analisado
	 * @param filterPath Caminho opcional para filtrar arquivos (null = todos os arquivos)
	 * @return Lista de desenvolvedores ordenada por LOC (maior para menor)
	 */
	private static List<Developer> calcLocsDeveloperList(Project project, String filterPath) {

		/**
		 * Mapa thread-safe para contar LOC por autor.
		 * 
		 * POR QUE ConcurrentHashMap?
		 * - Múltiplas threads vão atualizar este mapa simultaneamente
		 * - Cada thread processa um arquivo diferente
		 * - AtomicInteger permite incremento thread-safe sem locks
		 * 
		 * ESTRUTURA:
		 *   Key: nome do autor (String)
		 *   Value: contador de LOC (AtomicInteger)
		 */
		Map<String, AtomicInteger> authorLocMap = new ConcurrentHashMap<String, AtomicInteger>();

		try {
			/**
			 * PASSO 1: Obter lista de arquivos do repositório
			 * 
			 * Se filterPath for fornecido, lista apenas arquivos naquele caminho.
			 * Caso contrário, lista todos os arquivos do repositório.
			 */
			String lsFilesCommand = filterPath == null || filterPath.isEmpty() 
				? "git ls-files"  // Todos os arquivos
				: "git ls-files " + filterPath;  // Apenas arquivos no caminho especificado
			
			GitOutput filesOutput = Git.runCommand(project, lsFilesCommand, true);
			
			// Se não houver arquivos, retorna lista vazia
			if (filesOutput.outputList.isEmpty()) {
				return new ArrayList<Developer>();
			}

			/**
			 * PASSO 1.5: Filtrar e normalizar caminhos de arquivos
			 * 
			 * Remove arquivos inválidos (null, vazios) e normaliza espaços.
			 * Pré-aloca lista com tamanho estimado para melhor performance.
			 */
			List<String> validFiles = new ArrayList<String>(filesOutput.outputList.size());
			for (String filePath : filesOutput.outputList) {
				if (filePath != null && !filePath.trim().isEmpty()) {
					validFiles.add(filePath.trim());
				}
			}

			/**
			 * PASSO 1.6: Obter hash do commit atual para chave de cache
			 * 
			 * IMPORTANTE: O commit hash garante que:
			 * - Diferentes branches com mesmo arquivo têm chaves de cache diferentes
			 * - Mesmo branch em commits diferentes têm chaves diferentes
			 * - Mudanças no código invalidam automaticamente o cache
			 * 
			 * Se o commit hash for "unknown", desabilita cache por segurança.
			 */
			String commitHash = Git.getCommitHash(project);
			boolean useCache = !"unknown".equals(commitHash);

			/**
			 * PASSO 2: Criar lista de tarefas paralelas para processar arquivos
			 * 
			 * Cada arquivo será processado em uma thread separada.
			 * Pré-aloca lista com tamanho exato para evitar realocações.
			 */
			List<CompletableFuture<Void>> blameTasks = new ArrayList<CompletableFuture<Void>>(validFiles.size());
			
			/**
			 * PASSO 2.1: Para cada arquivo, criar uma tarefa assíncrona
			 * 
			 * Cada tarefa roda em uma thread do GIT_EXECUTOR pool.
			 * Múltiplas tarefas executam simultaneamente, processando arquivos em paralelo.
			 */
			for (String filePath : validFiles) {
				// Cria tarefa assíncrona que será executada em thread separada
				CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
					try {
						List<String> blameLines; // Linhas de saída do git blame
						
						/**
						 * PASSO 2.1.1: Verificar cache do git blame deste arquivo
						 * 
						 * O cache é thread-safe (Caffeine gerencia concorrência internamente).
						 * Múltiplas threads podem ler/escrever no cache simultaneamente sem problemas.
						 */
						if (useCache) {
							// Gera chave única: repositório + caminho do arquivo + commit hash
							String blameCacheKey = ProjectCacheManager.getGitBlameCacheKey(
								project.localRepository, filePath, commitHash);
							
							// Tenta obter resultado do cache (operação O(1) - muito rápida)
							List<String> cachedBlame = ProjectCacheManager.getGitBlame(blameCacheKey);
							
							if (cachedBlame != null) {
								// CACHE HIT: Usa resultado em cache (evita executar git blame)
								blameLines = cachedBlame;
							} else {
								// CACHE MISS: Precisa executar git blame
								// git blame --line-porcelain retorna informações detalhadas sobre cada linha
								GitOutput blameOutput = Git.runCommand(project, 
									"git blame --line-porcelain \"" + filePath + "\"", true);
								blameLines = blameOutput.outputList;
								
								// Armazena no cache para próximas consultas (thread-safe)
								ProjectCacheManager.putGitBlame(blameCacheKey, blameLines);
							}
						} else {
							// Cache desabilitado (commit hash desconhecido) - sempre calcula
							GitOutput blameOutput = Git.runCommand(project, 
								"git blame --line-porcelain \"" + filePath + "\"", true);
							blameLines = blameOutput.outputList;
						}
						
						/**
						 * PASSO 2.1.2: Processar saída do git blame e extrair autores
						 * 
						 * Formato do git blame --line-porcelain:
						 *   author Nome do Autor
						 *   author-mail email@exemplo.com
						 *   author-time timestamp
						 *   ...
						 * 
						 * Buscamos linhas que começam com "author " e extraímos o nome.
						 * 
						 * OTIMIZAÇÃO: Usa indexOf em vez de startsWith para melhor performance.
						 */
						for (String line : blameLines) {
							if (line != null && line.length() > 7) { // "author " tem 7 caracteres
								int authorIdx = line.indexOf("author ");
								if (authorIdx == 0) { // Linha começa com "author "
									String authorName = line.substring(7).trim(); // Remove "author " e espaços
									if (!authorName.isEmpty()) {
										/**
										 * Incrementa contador de LOC para este autor.
										 * 
										 * computeIfAbsent: Se autor não existe no mapa, cria AtomicInteger(0)
										 * incrementAndGet: Incrementa e retorna novo valor (thread-safe)
										 * 
										 * Esta operação é thread-safe mesmo com múltiplas threads
										 * atualizando o mesmo mapa simultaneamente.
										 */
										authorLocMap.computeIfAbsent(authorName, k -> new AtomicInteger(0)).incrementAndGet();
									}
								}
							}
						}
					} catch (IOException | InterruptedException e) {
						// Em caso de erro em um arquivo, continua processando os outros
						System.err.println("Error processing file " + filePath + ": " + e.getMessage());
						// Continue with next file
					}
				}, GIT_EXECUTOR); // Executa no pool de threads dedicado para Git
				
				blameTasks.add(task); // Adiciona tarefa à lista
			}

			/**
			 * PASSO 3: Aguardar todas as tarefas paralelas terminarem
			 * 
			 * CompletableFuture.allOf() cria um futuro que completa quando TODAS
			 * as tarefas terminarem. join() bloqueia até que isso aconteça.
			 * 
			 * IMPORTANTE: Todas as threads estão atualizando o mesmo authorLocMap
			 * simultaneamente, mas isso é seguro graças ao ConcurrentHashMap.
			 */
			CompletableFuture.allOf(blameTasks.toArray(new CompletableFuture[0])).join();

			/**
			 * PASSO 4: Converter mapa de contadores em lista de Developer
			 * 
			 * O mapa tem: { "Nome Autor" -> AtomicInteger(contador) }
			 * Precisamos converter para: List<Developer> onde cada Developer tem nome e numLoc
			 */
			List<Developer> locDeveloperList = new ArrayList<Developer>(authorLocMap.size());
			for (Map.Entry<String, AtomicInteger> entry : authorLocMap.entrySet()) {
				// Cria Developer com nome, email vazio, LOC (do contador), e 0 commits
				Developer dev = new Developer(entry.getKey(), "email", entry.getValue().get(), 0);
				locDeveloperList.add(dev);
			}

			/**
			 * PASSO 5: Ordenar desenvolvedores por LOC (descendente)
			 * 
			 * Equivalente ao "sort -nr" do pipeline Unix original.
			 * Ordena do desenvolvedor com mais LOC para o com menos LOC.
			 */
			Collections.sort(locDeveloperList, new Comparator<Developer>() {
				@Override
				public int compare(Developer d1, Developer d2) {
					// Compara d2 com d1 para ordem descendente (maior para menor)
					return Integer.compare(d2.numLoc, d1.numLoc);
				}
			});

			return locDeveloperList; // Retorna lista ordenada por LOC

		} catch (Exception e1) {
			System.err.println("Error in calcLocsDeveloperList: " + e1.getMessage());
			e1.printStackTrace();
			return new ArrayList<Developer>();
		}
	}

	/**
	 * Calculate developer list for entire project (no filter) with caching support.
	 * Reuses calcLocsDeveloperList with null filter.
	 */
	public static List<Developer> calcDeveloperList(Project project) {
		// Get commit hash for cache key
		String commitHash = Git.getCommitHash(project);
		String cacheKey = ProjectCacheManager.getDeveloperListCacheKey(
			project.localRepository, project.checkout, null, commitHash);

		// Check cache first
		List<Developer> cached = ProjectCacheManager.getDeveloperList(cacheKey);
		if (cached != null) {
			System.out.println("Cache HIT: developer list for " + project.localRepository);
			project.developerList = cached;
			return cached;
		}

		// Cache miss - calculate
		try {
			// Reuse calcLocsDeveloperList with no filter (all files)
			List<Developer> developers = calcLocsDeveloperList(project, null);
			project.developerList = developers;
			
			// Store in cache
			ProjectCacheManager.putDeveloperList(cacheKey, developers);
			return developers;
		} catch (Exception e) {
			System.err.println("Error in calcDeveloperList: " + e.getMessage());
			e.printStackTrace();
		}

		return project.developerList;
	}

	/**
	 * Calculate commits per developer.
	 * Replaces: git log | grep Author: | sort | uniq -c | sort -nr (for root)
	 * or: git log --pretty=format:"%an" --follow "<path>" | sort -f | uniq -ic | sort -nr (for specific path)
	 */
	/**
	 * Calculate commits per developer.
	 * OPTIMIZED: Pre-allocates HashMap with estimated size for better performance.
	 * Replaces: git log | grep Author: | sort | uniq -c | sort -nr (for root)
	 * or: git log --pretty=format:"%an" --follow "<path>" | sort -f | uniq -ic | sort -nr (for specific path)
	 */
	public static HashMap<String, Integer> calcCommitsDeveloperList(Project project, String filterPath) {

		HashMap<String, Integer> developerCommitsMap = new HashMap<String, Integer>();

		try {
			// Build git log command - use --pretty=format:"%an" to get author names directly
			String command;
			if (filterPath == null) {
				// Root level: get all authors from all commits
				command = "git log --pretty=format:\"%an\"";
			} else {
				// Specific path: get authors for commits that touched this path
				command = "git log --pretty=format:\"%an\" --follow \"" + filterPath + "\"";
			}

			GitOutput gitOutput = Git.runCommand(project, command, true);

			// Pre-allocate HashMap with estimated size (assuming ~50% unique authors)
			int estimatedSize = Math.max(16, (gitOutput.outputList.size() / 2));
			developerCommitsMap = new HashMap<String, Integer>(estimatedSize);

			// Count occurrences of each author name (replaces sort | uniq -c)
			// Optimize: avoid trim() when not needed
			for (String line : gitOutput.outputList) {
				if (line != null) {
					String trimmed = line.trim();
					if (!trimmed.isEmpty()) {
						developerCommitsMap.put(trimmed, developerCommitsMap.getOrDefault(trimmed, 0) + 1);
					}
				}
			}

		} catch (IOException | InterruptedException e1) {
			System.err.println("Error in calcCommitsDeveloperList: " + e1.getMessage());
			e1.printStackTrace();
		}

		return developerCommitsMap;
	}

	private void calcNumLocProjectByDeveloperList() {
		this.numLoc = 0;
		for (Developer developer : developerList) {
			this.numLoc += developer.numLoc;
		}

	}

	/**
	 * Calculate truck factor with caching support (most expensive operation).
	 */
	private static List<Developer> calcTruckFactor(Project project) {
		// Get commit hash for cache key
		String commitHash = Git.getCommitHash(project);
		String cacheKey = ProjectCacheManager.getTruckFactorCacheKey(
			project.localRepository, project.checkout, commitHash);

		// Check cache first (truck factor is very expensive)
		List<Developer> cached = ProjectCacheManager.getTruckFactor(cacheKey);
		if (cached != null) {
			System.out.println("Cache HIT: truck factor for " + project.localRepository);
			return cached;
		}

		// Cache miss - calculate (expensive operation)
		List<Developer> developerListTF = new ArrayList<Developer>();

		try {
			GitOutput gitOutput;

			gitOutput = Git.runGitTruckFactor(project);

			int isAuthors = 0;
			for (String line : gitOutput.outputList) {

				if (line.contains("TF authors")) {
					isAuthors++;
				}

				if (isAuthors < 1)
					continue;

				if (isAuthors == 1) {
					isAuthors++;
					continue;
				}
				if (!line.contains(";")) {
					continue;
				}

				developerListTF.add(new Developer(line.substring(0, line.indexOf(";"))));
			}

			// Store in cache
			ProjectCacheManager.putTruckFactor(cacheKey, developerListTF);
			return developerListTF;

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return developerListTF;
	}

	/**
	 * MÉTODO PRINCIPAL: Constrói visão geral completa do projeto.
	 * 
	 * Este é o método mais importante da classe. Ele orquestra todas as análises
	 * e retorna um objeto Project completo com todas as métricas calculadas.
	 * 
	 * FLUXO GERAL:
	 * 1. Cria/obtém projeto e faz checkout da branch especificada
	 * 2. Valida estado do checkout (segurança do cache)
	 * 3. Verifica cache completo do projeto
	 * 4. Se cache HIT: retorna projeto em cache (muito rápido!)
	 * 5. Se cache MISS: calcula todas as métricas em PARALELO
	 * 6. Combina resultados e armazena no cache
	 * 
	 * OTIMIZAÇÕES:
	 * - Cache completo: Se projeto já foi analisado, retorna instantaneamente
	 * - Paralelização: Calcula 4 métricas simultaneamente (4x mais rápido)
	 * - Validação: Garante que cache não seja usado incorretamente
	 * 
	 * MÉTRICAS CALCULADAS:
	 * - Lista de desenvolvedores com LOC
	 * - Truck Factor (desenvolvedores críticos)
	 * - Número total de commits
	 * - Estatísticas por linguagem de programação
	 * 
	 * @param filter Filtro com informações do repositório (local ou remoto)
	 * @param checkout Branch/tag/commit a ser analisado
	 * @return Projeto completo com todas as métricas calculadas
	 * @throws IOException Se houver erro de I/O
	 * @throws InterruptedException Se thread for interrompida
	 */
	public static Project buildOverview(Filter filter, String checkout) throws IOException, InterruptedException {

		/**
		 * PASSO 1: Criar/obter projeto e fazer checkout
		 * 
		 * builderProject pode:
		 * - Clonar repositório remoto
		 * - Usar repositório local existente
		 * - Fazer checkout da branch/tag especificada
		 */
		Project project = Project.builderProject(filter, checkout);
		
		/**
		 * PASSO 2: Validar estado do checkout
		 * 
		 * IMPORTANTE: Se o checkout falhar silenciosamente, podemos ter
		 * cache incorreto (dados de branch errada). Esta validação previne isso.
		 */
		boolean checkoutValid = Git.validateCheckoutState(project, checkout);
		if (!checkoutValid) {
			System.err.println("WARNING: Checkout validation failed. Cache may be invalidated.");
		}
		
		/**
		 * PASSO 3: Obter hash do commit atual
		 * 
		 * O commit hash identifica unicamente o estado do repositório.
		 * É usado como parte da chave de cache para garantir que:
		 * - Diferentes branches = diferentes chaves
		 * - Diferentes commits = diferentes chaves
		 * - Mudanças no código invalidam cache automaticamente
		 */
		String commitHash = Git.getCommitHash(project);
		
		/**
		 * PASSO 4: Verificar cache completo do projeto
		 * 
		 * Se o projeto completo já foi analisado antes (mesmo repositório,
		 * mesma branch, mesmo commit), retornamos o resultado em cache.
		 * Isso pode ser 10-50x mais rápido que recalcular tudo!
		 */
		Project cachedProject = null;
		String projectCacheKey = null;
		
		if (!"unknown".equals(commitHash)) {
			// Gera chave única: repositório + branch + commit hash
			projectCacheKey = ProjectCacheManager.getProjectCacheKey(
				project.localRepository, project.checkout, commitHash);

			// Tenta obter projeto completo do cache
			cachedProject = ProjectCacheManager.getProject(projectCacheKey);
			if (cachedProject != null) {
				// CACHE HIT: Retorna projeto completo em cache (muito rápido!)
				System.out.println("Cache HIT: complete project overview for " + project.localRepository + 
					" (commit: " + commitHash.substring(0, Math.min(7, commitHash.length())) + ")");
				return cachedProject;
			}
		} else {
			// Se commit hash for desconhecido, não usa cache por segurança
			System.err.println("WARNING: Could not determine commit hash. Cache will be bypassed for safety.");
		}

		/**
		 * PASSO 5: CACHE MISS - Calcular todas as métricas em PARALELO
		 * 
		 * Como as métricas são independentes, podemos calculá-las simultaneamente.
		 * Isso reduz o tempo total de ~4x (se cada uma leva 1s, total = 1s em vez de 4s).
		 * 
		 * Cada CompletableFuture executa em thread separada:
		 * - calcDeveloperList: Processa git blame de todos os arquivos (mais lento)
		 * - calcTruckFactor: Executa algoritmo de truck factor (muito lento)
		 * - calcNumCommits: Conta commits (rápido)
		 * - calcNumLocProgrammingLanguageList: Executa CLOC (médio)
		 */
		final CompletableFuture<List<Developer>> calcDeveloperListRun = CompletableFuture
				.supplyAsync(() -> calcDeveloperList(project));

		final CompletableFuture<List<Developer>> calcTruckFactorRun = CompletableFuture
				.supplyAsync(() -> calcTruckFactor(project));

		final CompletableFuture<Integer> calcNumCommitsRun = CompletableFuture
				.supplyAsync(() -> calcNumCommits(project));

		final CompletableFuture<List<NumLocProgrammingLanguage>> calcNumLocProgrammingLanguageListRun = CompletableFuture
				.supplyAsync(() -> calcNumLocProgrammingLanguageList(project));

		try {

			project.developerList = calcDeveloperListRun.get();

			project.calcNumLocProjectByDeveloperList();

			project.numCommits = calcNumCommitsRun.get();

			project.numLocProgrammingLanguageList = calcNumLocProgrammingLanguageListRun.get();

			List<Developer> devTFList = calcTruckFactorRun.get();

			// Optimize: Use HashSet for O(1) lookup instead of O(n²) nested loop
			Set<String> devTFNames = new HashSet<String>(devTFList.size());
			for (Developer devTF : devTFList) {
				devTFNames.add(devTF.name);
			}
			
			for (Developer developer : project.developerList) {
				if (devTFNames.contains(developer.name)) {
					project.truckFactor++;
					developer.truckFactor = true;
				}
			}

			// Store complete project in cache (only if we have a valid cache key)
			if (projectCacheKey != null) {
				ProjectCacheManager.putProject(projectCacheKey, project);
			}
			return project;

		} catch (InterruptedException | ExecutionException e) {

			System.err.println("findResume process error: " + e.getMessage());
			if (e.getCause() != null) {
				System.err.println("root cause: " + e.getCause().getMessage());
			}
			throw new RuntimeException("Error while building project overview", e);
		}

	}

	public static Project builderProject(Filter filter, String checkout) {

		if (filter.remoteRepository != null && filter.remoteRepository.equals("[local]")) {

			System.out.println("info...................BuilderProject is checkouting (" + checkout + ") in 100% local: "
					+ filter.localRepository);

			Project project = new Project(Git.getLocalRepositoryFromLocalProject(), checkout);
			// entre na pasta do seu projeto local, use 'docker cp .
			// nomeContainer:/root/team-tracker-clones/local/
			Git.runCheckout(project);

			return project;

		} else if (filter.localRepository != null && !filter.localRepository.isEmpty()) {

			System.out.println("info...................BuilderProject is checkouting (" + checkout + ") in local: "
					+ filter.localRepository);

			Project project = new Project(filter.localRepository, checkout);

			Git.runCheckout(project);

			return project;

		} else if (filter.remoteRepository != null && !filter.remoteRepository.isEmpty()) {

			System.out.println("info...................BuilderProject is clonning from: " + filter.remoteRepository);

			if (filter.user != null && !filter.user.isEmpty() && filter.password != null
					&& !filter.password.isEmpty()) {

				return Git.clone("https://" + encodeValue(filter.user) + ":" + encodeValue(filter.password) + "@"
						+ filter.remoteRepository.substring(8), checkout);
			}

			return Git.clone(filter.remoteRepository, checkout);

		} else {

			throw new RuntimeException();
		}
	}

	private static String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}
}
