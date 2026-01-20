package mestrado.matheus.teamtracker.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import mestrado.matheus.teamtracker.domain.Developer;
import mestrado.matheus.teamtracker.domain.NumLocProgrammingLanguage;
import mestrado.matheus.teamtracker.domain.Project;

/**
 * Cache manager for project analysis results.
 * Uses Caffeine cache for high-performance in-memory caching.
 * 
 * Cache Strategy:
 * - Project overview results: cached by repository + checkout + commit hash
 * - Git blame results: cached by repository + file path + commit hash
 * - Truck factor results: cached by repository + checkout + commit hash
 * 
 * Cache invalidation:
 * - TTL: 1 hour (configurable)
 * - Max size: 1000 entries per cache
 * - Automatic eviction based on size and time
 */
public class ProjectCacheManager {

	// Cache for complete project overview results
	private static final Cache<String, Project> PROJECT_CACHE = Caffeine.newBuilder()
			.maximumSize(100) // Cache up to 100 project analyses
			.expireAfterWrite(1, TimeUnit.HOURS) // Expire after 1 hour
			.recordStats() // Enable statistics
			.build();

	// Cache for git blame results per file (most frequently accessed)
	private static final Cache<String, List<String>> GIT_BLAME_CACHE = Caffeine.newBuilder()
			.maximumSize(1000) // Cache up to 1000 file blame results
			.expireAfterWrite(30, TimeUnit.MINUTES) // Expire after 30 minutes
			.recordStats()
			.build();

	// Cache for developer lists (LOC calculations)
	private static final Cache<String, List<Developer>> DEVELOPER_LIST_CACHE = Caffeine.newBuilder()
			.maximumSize(200) // Cache up to 200 developer lists
			.expireAfterWrite(1, TimeUnit.HOURS)
			.recordStats()
			.build();

	// Cache for commits count
	private static final Cache<String, Integer> COMMITS_CACHE = Caffeine.newBuilder()
			.maximumSize(200)
			.expireAfterWrite(1, TimeUnit.HOURS)
			.recordStats()
			.build();

	// Cache for programming language statistics
	private static final Cache<String, List<NumLocProgrammingLanguage>> LANGUAGE_STATS_CACHE = Caffeine.newBuilder()
			.maximumSize(200)
			.expireAfterWrite(1, TimeUnit.HOURS)
			.recordStats()
			.build();

	// Cache for truck factor results (most expensive operation)
	private static final Cache<String, List<Developer>> TRUCK_FACTOR_CACHE = Caffeine.newBuilder()
			.maximumSize(100)
			.expireAfterWrite(2, TimeUnit.HOURS) // Longer TTL for expensive operations
			.recordStats()
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
