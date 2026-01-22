package mestrado.matheus.teamtracker.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import mestrado.matheus.teamtracker.domain.Developer;
import mestrado.matheus.teamtracker.domain.NumLocProgrammingLanguage;
import mestrado.matheus.teamtracker.domain.Project;

/**
 * Gerenciador de Cache para resultados de análise de projetos.
 * 
 * RESPONSABILIDADES:
 * - Armazenar resultados de análises em memória para evitar recálculos
 * - Gerar chaves de cache únicas baseadas em repositório + branch + commit
 * - Gerenciar múltiplos caches com diferentes TTLs e tamanhos
 * - Fornecer estatísticas de uso do cache
 * 
 * TECNOLOGIA:
 * Usa a biblioteca Caffeine, que é uma implementação de cache em memória
 * de alta performance, thread-safe e com políticas de eviction inteligentes.
 * 
 * ESTRATÉGIA DE CACHE:
 * - Resultados de projeto completo: cacheado por repositório + checkout + commit hash
 * - Resultados de git blame: cacheado por repositório + caminho do arquivo + commit hash
 * - Resultados de truck factor: cacheado por repositório + checkout + commit hash
 * 
 * INVALIDAÇÃO DE CACHE:
 * - TTL (Time To Live): Entradas expiram após tempo configurado
 * - Tamanho máximo: Quando limite é atingido, remove entradas menos usadas (LRU)
 * - Commit hash: Mudanças no commit invalidam cache automaticamente (chave diferente)
 * 
 * THREAD-SAFETY:
 * Todos os caches são thread-safe. Múltiplas threads podem ler/escrever
 * simultaneamente sem problemas de concorrência.
 */
public class ProjectCacheManager {

	/**
	 * Cache para resultados completos de análise de projeto.
	 * 
	 * CONTEÚDO: Objeto Project completo com todas as métricas calculadas
	 * TAMANHO: Até 100 projetos
	 * TTL: 1 hora
	 * 
	 * USO: Quando usuário solicita análise completa, verifica este cache primeiro.
	 * Se encontrado, retorna instantaneamente sem recalcular nada.
	 */
	private static final Cache<String, Project> PROJECT_CACHE = Caffeine.newBuilder()
			.maximumSize(100) // Armazena até 100 análises de projeto
			.expireAfterWrite(1, TimeUnit.HOURS) // Expira após 1 hora sem escrita
			.recordStats() // Habilita estatísticas (hit rate, miss rate, etc.)
			.build();

	/**
	 * Cache para resultados de git blame por arquivo.
	 * 
	 * CONTEÚDO: Lista de linhas da saída do git blame --line-porcelain
	 * TAMANHO: Até 1000 arquivos
	 * TTL: 30 minutos
	 * 
	 * USO: Este é o cache mais acessado durante cálculo de LOC por desenvolvedor.
	 * Cada arquivo processado verifica este cache antes de executar git blame.
	 * 
	 * POR QUE TTL MENOR?
	 * - Arquivos podem mudar com mais frequência que análises completas
	 * - 30 minutos é suficiente para múltiplas requisições
	 * - Libera memória mais rapidamente se não usado
	 */
	private static final Cache<String, List<String>> GIT_BLAME_CACHE = Caffeine.newBuilder()
			.maximumSize(1000) // Armazena até 1000 resultados de git blame
			.expireAfterWrite(30, TimeUnit.MINUTES) // Expira após 30 minutos
			.recordStats() // Habilita estatísticas
			.build();

	/**
	 * Cache para listas de desenvolvedores com estatísticas de LOC.
	 * 
	 * CONTEÚDO: Lista de objetos Developer com LOC calculado
	 * TAMANHO: Até 200 listas
	 * TTL: 1 hora
	 * 
	 * USO: Armazena resultado final do cálculo de LOC por desenvolvedor.
	 * Evita reprocessar todos os arquivos quando mesma análise é solicitada.
	 */
	private static final Cache<String, List<Developer>> DEVELOPER_LIST_CACHE = Caffeine.newBuilder()
			.maximumSize(200) // Armazena até 200 listas de desenvolvedores
			.expireAfterWrite(1, TimeUnit.HOURS) // Expira após 1 hora
			.recordStats() // Habilita estatísticas
			.build();

	/**
	 * Cache para contagem de commits.
	 * 
	 * CONTEÚDO: Número inteiro representando total de commits
	 * TAMANHO: Até 200 entradas
	 * TTL: 1 hora
	 * 
	 * USO: Armazena resultado de "git rev-list --count HEAD".
	 * Operação rápida, mas cache evita executar Git repetidamente.
	 */
	private static final Cache<String, Integer> COMMITS_CACHE = Caffeine.newBuilder()
			.maximumSize(200) // Armazena até 200 contagens de commits
			.expireAfterWrite(1, TimeUnit.HOURS) // Expira após 1 hora
			.recordStats() // Habilita estatísticas
			.build();

	/**
	 * Cache para estatísticas de linguagens de programação.
	 * 
	 * CONTEÚDO: Lista de NumLocProgrammingLanguage com LOC por linguagem
	 * TAMANHO: Até 200 entradas
	 * TTL: 1 hora
	 * 
	 * USO: Armazena resultado da execução do CLOC.
	 * CLOC pode ser lento em projetos grandes, então cache é importante.
	 */
	private static final Cache<String, List<NumLocProgrammingLanguage>> LANGUAGE_STATS_CACHE = Caffeine.newBuilder()
			.maximumSize(200) // Armazena até 200 estatísticas de linguagens
			.expireAfterWrite(1, TimeUnit.HOURS) // Expira após 1 hora
			.recordStats() // Habilita estatísticas
			.build();

	/**
	 * Cache para resultados de truck factor.
	 * 
	 * CONTEÚDO: Lista de desenvolvedores que são "truck factor"
	 * TAMANHO: Até 100 entradas
	 * TTL: 2 horas
	 * 
	 * USO: Truck factor é a operação mais cara do sistema.
	 * TTL maior (2 horas) porque:
	 * - Operação muito lenta (pode levar minutos)
	 * - Resultado não muda frequentemente
	 * - Vale a pena manter em cache por mais tempo
	 */
	private static final Cache<String, List<Developer>> TRUCK_FACTOR_CACHE = Caffeine.newBuilder()
			.maximumSize(100) // Armazena até 100 resultados de truck factor
			.expireAfterWrite(2, TimeUnit.HOURS) // TTL maior para operação cara
			.recordStats() // Habilita estatísticas
			.build();

	/**
	 * Generate cache key for project overview
	 */
	public static String getProjectCacheKey(String repository, String checkout, String commitHash) {
		return String.format("project:%s:%s:%s", repository, checkout, commitHash);
	}

	/**
	 * Generate cache key for git blame of a specific file.
	 * 
	 * IMPORTANT: The commit hash ensures that:
	 * - Different branches with same file path have different cache keys
	 * - Same branch at different commits have different cache keys
	 * - File content changes are automatically detected via commit hash
	 */
	public static String getGitBlameCacheKey(String repository, String filePath, String commitHash) {
		// Normalize file path to handle Windows/Linux path differences
		String normalizedPath = filePath.replace("\\", "/");
		return String.format("blame:%s:%s:%s", repository, normalizedPath, commitHash);
	}

	/**
	 * Generate cache key for developer list
	 */
	public static String getDeveloperListCacheKey(String repository, String checkout, String filterPath, String commitHash) {
		return String.format("devlist:%s:%s:%s:%s", repository, checkout, 
			filterPath != null ? filterPath : "root", commitHash);
	}

	/**
	 * Generate cache key for commits count
	 */
	public static String getCommitsCacheKey(String repository, String checkout, String commitHash) {
		return String.format("commits:%s:%s:%s", repository, checkout, commitHash);
	}

	/**
	 * Generate cache key for language statistics
	 */
	public static String getLanguageStatsCacheKey(String repository, String checkout, String commitHash) {
		return String.format("langstats:%s:%s:%s", repository, checkout, commitHash);
	}

	/**
	 * Generate cache key for truck factor
	 */
	public static String getTruckFactorCacheKey(String repository, String checkout, String commitHash) {
		return String.format("truckfactor:%s:%s:%s", repository, checkout, commitHash);
	}

	// Project cache operations
	public static Project getProject(String key) {
		return PROJECT_CACHE.getIfPresent(key);
	}

	public static void putProject(String key, Project project) {
		PROJECT_CACHE.put(key, project);
	}

	// Git blame cache operations
	public static List<String> getGitBlame(String key) {
		return GIT_BLAME_CACHE.getIfPresent(key);
	}

	public static void putGitBlame(String key, List<String> blameOutput) {
		GIT_BLAME_CACHE.put(key, blameOutput);
	}

	// Developer list cache operations
	public static List<Developer> getDeveloperList(String key) {
		return DEVELOPER_LIST_CACHE.getIfPresent(key);
	}

	public static void putDeveloperList(String key, List<Developer> developers) {
		DEVELOPER_LIST_CACHE.put(key, developers);
	}

	// Commits cache operations
	public static Integer getCommits(String key) {
		return COMMITS_CACHE.getIfPresent(key);
	}

	public static void putCommits(String key, Integer commits) {
		COMMITS_CACHE.put(key, commits);
	}

	// Language stats cache operations
	public static List<NumLocProgrammingLanguage> getLanguageStats(String key) {
		return LANGUAGE_STATS_CACHE.getIfPresent(key);
	}

	public static void putLanguageStats(String key, List<NumLocProgrammingLanguage> stats) {
		LANGUAGE_STATS_CACHE.put(key, stats);
	}

	// Truck factor cache operations
	public static List<Developer> getTruckFactor(String key) {
		return TRUCK_FACTOR_CACHE.getIfPresent(key);
	}

	public static void putTruckFactor(String key, List<Developer> developers) {
		TRUCK_FACTOR_CACHE.put(key, developers);
	}

	/**
	 * Clear all caches (useful for testing or manual invalidation)
	 */
	public static void clearAll() {
		PROJECT_CACHE.invalidateAll();
		GIT_BLAME_CACHE.invalidateAll();
		DEVELOPER_LIST_CACHE.invalidateAll();
		COMMITS_CACHE.invalidateAll();
		LANGUAGE_STATS_CACHE.invalidateAll();
		TRUCK_FACTOR_CACHE.invalidateAll();
	}

	/**
	 * Get cache statistics for monitoring
	 */
	public static String getCacheStats() {
		StringBuilder stats = new StringBuilder();
		stats.append("Project Cache: ").append(PROJECT_CACHE.stats()).append("\n");
		stats.append("Git Blame Cache: ").append(GIT_BLAME_CACHE.stats()).append("\n");
		stats.append("Developer List Cache: ").append(DEVELOPER_LIST_CACHE.stats()).append("\n");
		stats.append("Commits Cache: ").append(COMMITS_CACHE.stats()).append("\n");
		stats.append("Language Stats Cache: ").append(LANGUAGE_STATS_CACHE.stats()).append("\n");
		stats.append("Truck Factor Cache: ").append(TRUCK_FACTOR_CACHE.stats()).append("\n");
		return stats.toString();
	}
}
