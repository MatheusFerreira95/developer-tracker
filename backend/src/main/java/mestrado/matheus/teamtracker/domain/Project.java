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

public class Project {

	// Thread pool for parallel git operations (I/O bound tasks)
	// Using a fixed pool size to avoid overwhelming the system
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
	 * Calculate number of commits with caching support.
	 */
	public static Integer calcNumCommits(Project project) {
		// Get commit hash for cache key
		String commitHash = Git.getCommitHash(project);
		String cacheKey = ProjectCacheManager.getCommitsCacheKey(
			project.localRepository, project.checkout, commitHash);

		// Check cache first
		Integer cached = ProjectCacheManager.getCommits(cacheKey);
		if (cached != null) {
			System.out.println("Cache HIT: commits for " + project.localRepository);
			return cached;
		}

		// Cache miss - calculate
		GitOutput gitOutput;
		try {
			gitOutput = Git.runCommand(project, "git rev-list --count HEAD", true);
			Integer commits = Integer.parseInt(gitOutput.outputList.get(0));
			
			// Store in cache
			ProjectCacheManager.putCommits(cacheKey, commits);
			return commits;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return 0;
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
	 * Calculate LOC per developer by processing git blame for each file.
	 * OPTIMIZED: Parallelizes git blame operations for multiple files.
	 * Replaces the shell pipeline: git ls-files | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort -f | uniq -ic | sort -nr
	 */
	private static List<Developer> calcLocsDeveloperList(Project project, String filterPath) {

		// Use ConcurrentHashMap for thread-safe parallel updates
		Map<String, AtomicInteger> authorLocMap = new ConcurrentHashMap<String, AtomicInteger>();

		try {
			// Step 1: Get list of files
			String lsFilesCommand = filterPath == null || filterPath.isEmpty() 
				? "git ls-files" 
				: "git ls-files " + filterPath;
			
			GitOutput filesOutput = Git.runCommand(project, lsFilesCommand, true);
			
			if (filesOutput.outputList.isEmpty()) {
				return new ArrayList<Developer>();
			}

			// Pre-allocate with estimated size
			List<String> validFiles = new ArrayList<String>(filesOutput.outputList.size());
			for (String filePath : filesOutput.outputList) {
				if (filePath != null && !filePath.trim().isEmpty()) {
					validFiles.add(filePath.trim());
				}
			}

			// Get commit hash for cache key (AFTER checkout is complete)
			// This ensures cache keys are unique per commit, preventing cross-branch contamination
			String commitHash = Git.getCommitHash(project);
			boolean useCache = !"unknown".equals(commitHash);

			// Step 2: Process files in parallel using CompletableFuture (with caching support)
			List<CompletableFuture<Void>> blameTasks = new ArrayList<CompletableFuture<Void>>(validFiles.size());
			
			for (String filePath : validFiles) {
				CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
					try {
						List<String> blameLines;
						
						// Check cache first (thread-safe - Caffeine handles concurrency)
						if (useCache) {
							String blameCacheKey = ProjectCacheManager.getGitBlameCacheKey(
								project.localRepository, filePath, commitHash);
							List<String> cachedBlame = ProjectCacheManager.getGitBlame(blameCacheKey);
							
							if (cachedBlame != null) {
								// Cache hit - use cached output
								blameLines = cachedBlame;
							} else {
								// Cache miss - run git blame
								GitOutput blameOutput = Git.runCommand(project, 
									"git blame --line-porcelain \"" + filePath + "\"", true);
								blameLines = blameOutput.outputList;
								
								// Store in cache (thread-safe)
								ProjectCacheManager.putGitBlame(blameCacheKey, blameLines);
							}
						} else {
							// Cache disabled (unknown commit hash) - always calculate
							GitOutput blameOutput = Git.runCommand(project, 
								"git blame --line-porcelain \"" + filePath + "\"", true);
							blameLines = blameOutput.outputList;
						}
						
						// Parse blame output: look for lines starting with "author "
						// Optimize: use indexOf instead of startsWith for better performance
						for (String line : blameLines) {
							if (line != null && line.length() > 7) {
								int authorIdx = line.indexOf("author ");
								if (authorIdx == 0) {
									String authorName = line.substring(7).trim();
									if (!authorName.isEmpty()) {
										// Thread-safe increment using AtomicInteger
										authorLocMap.computeIfAbsent(authorName, k -> new AtomicInteger(0)).incrementAndGet();
									}
								}
							}
						}
					} catch (IOException | InterruptedException e) {
						System.err.println("Error processing file " + filePath + ": " + e.getMessage());
						// Continue with next file
					}
				}, GIT_EXECUTOR);
				
				blameTasks.add(task);
			}

			// Wait for all parallel tasks to complete
			CompletableFuture.allOf(blameTasks.toArray(new CompletableFuture[0])).join();

			// Step 3: Convert map to List<Developer> and sort by LOC (descending)
			List<Developer> locDeveloperList = new ArrayList<Developer>(authorLocMap.size());
			for (Map.Entry<String, AtomicInteger> entry : authorLocMap.entrySet()) {
				Developer dev = new Developer(entry.getKey(), "email", entry.getValue().get(), 0);
				locDeveloperList.add(dev);
			}

			// Sort by LOC descending (same as "sort -nr" in the original pipeline)
			Collections.sort(locDeveloperList, new Comparator<Developer>() {
				@Override
				public int compare(Developer d1, Developer d2) {
					return Integer.compare(d2.numLoc, d1.numLoc); // Descending order
				}
			});

			return locDeveloperList;

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
	 * Build project overview with caching support.
	 * Checks cache first before performing expensive calculations.
	 */
	public static Project buildOverview(Filter filter, String checkout) throws IOException, InterruptedException {

		Project project = Project.builderProject(filter, checkout);
		
		// Validate checkout state to ensure cache safety
		// This prevents cache issues if checkout failed silently
		boolean checkoutValid = Git.validateCheckoutState(project, checkout);
		if (!checkoutValid) {
			System.err.println("WARNING: Checkout validation failed. Cache may be invalidated.");
		}
		
		// Get commit hash for cache key (AFTER checkout is complete)
		// The commit hash uniquely identifies the repository state
		String commitHash = Git.getCommitHash(project);
		
		// Safety check: if commit hash is unknown, don't use cache
		Project cachedProject = null;
		String projectCacheKey = null;
		
		if (!"unknown".equals(commitHash)) {
			projectCacheKey = ProjectCacheManager.getProjectCacheKey(
				project.localRepository, project.checkout, commitHash);

			// Check cache for complete project overview first
			cachedProject = ProjectCacheManager.getProject(projectCacheKey);
			if (cachedProject != null) {
				// Additional validation: verify the cached project's commit hash matches
				// This is a safety check for edge cases
				System.out.println("Cache HIT: complete project overview for " + project.localRepository + 
					" (commit: " + commitHash.substring(0, Math.min(7, commitHash.length())) + ")");
				return cachedProject;
			}
		} else {
			System.err.println("WARNING: Could not determine commit hash. Cache will be bypassed for safety.");
		}

		// Cache miss - calculate all metrics in parallel
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
