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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import mestrado.matheus.teamtracker.util.CLOC;
import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;

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

	public static Integer calcNumCommits(Project project) {

		GitOutput gitOutput;

		try {

			gitOutput = Git.runCommand(project, "git rev-list --count HEAD", true);
			return Integer.parseInt(gitOutput.outputList.get(0));

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

	private static List<NumLocProgrammingLanguage> calcNumLocProgrammingLanguageList(Project project) {

		try {

			return CLOC.buildNumLocProgrammingLanguageList(project);

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

			// Step 2: Process files in parallel using CompletableFuture
			List<CompletableFuture<Void>> blameTasks = new ArrayList<CompletableFuture<Void>>(validFiles.size());
			
			for (String filePath : validFiles) {
				CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
					try {
						GitOutput blameOutput = Git.runCommand(project, "git blame --line-porcelain \"" + filePath + "\"", true);
						
						// Parse blame output: look for lines starting with "author "
						// Optimize: use indexOf instead of startsWith for better performance
						for (String line : blameOutput.outputList) {
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
	 * Calculate developer list for entire project (no filter).
	 * Reuses calcLocsDeveloperList with null filter.
	 */
	public static List<Developer> calcDeveloperList(Project project) {

		try {
			// Reuse calcLocsDeveloperList with no filter (all files)
			List<Developer> developers = calcLocsDeveloperList(project, null);
			project.developerList = developers;
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

	private static List<Developer> calcTruckFactor(Project project) {

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

			return developerListTF;

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return developerListTF;
	}

	public static Project buildOverview(Filter filter, String checkout) throws IOException, InterruptedException {

		Project project = Project.builderProject(filter, checkout);

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
