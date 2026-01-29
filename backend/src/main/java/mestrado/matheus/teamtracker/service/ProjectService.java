package mestrado.matheus.teamtracker.service;

import java.io.IOException;
import java.util.ArrayList;
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

import mestrado.matheus.teamtracker.domain.Developer;
import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.NumLocProgrammingLanguage;
import mestrado.matheus.teamtracker.domain.Project;
import mestrado.matheus.teamtracker.domain.factory.ProjectBuilder;
import mestrado.matheus.teamtracker.domain.factory.ProjectBuilderFactory;
import mestrado.matheus.teamtracker.util.CLOC;
import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;

import org.springframework.stereotype.Service;

/**
 * Serviço centralizado para lógica de negócio de Projetos.
 * Consolida cálculos de métricas, orquestração de overview e integração com Git.
 */
@Service
public class ProjectService {

	private static final ExecutorService GIT_EXECUTOR = Executors.newFixedThreadPool(
		Math.max(4, Runtime.getRuntime().availableProcessors())
	);

	/**
	 * Constrói um projeto básico sem calcular todas as métricas do overview.
	 * Usado quando apenas precisamos do projeto para cálculos específicos.
	 */
	public Project buildProject(Filter filter, String checkout) throws IOException, InterruptedException {
		ProjectBuilder builder = ProjectBuilderFactory.createBuilder(filter);
		Project project = builder.build(filter, checkout);
		return project;
	}

	/**
	 * Constrói o overview completo do projeto.
	 */
	public Project buildOverview(Filter filter, String checkout) throws IOException, InterruptedException {
		ProjectBuilder builder = ProjectBuilderFactory.createBuilder(filter);
		Project project = builder.build(filter, checkout);

		final CompletableFuture<List<Developer>> calcDeveloperListRun = CompletableFuture
				.supplyAsync(() -> calculateDeveloperList(project));

		final CompletableFuture<List<Developer>> calcTruckFactorRun = CompletableFuture
				.supplyAsync(() -> calculateTruckFactor(project));

		final CompletableFuture<Integer> calcNumCommitsRun = CompletableFuture
				.supplyAsync(() -> calculateNumCommits(project));

		final CompletableFuture<List<NumLocProgrammingLanguage>> calcNumLocProgrammingLanguageListRun = 
			CompletableFuture.supplyAsync(() -> calculateNumLocProgrammingLanguageList(project));

		try {
			project.developerList = calcDeveloperListRun.get();
			calculateNumLocProjectByDeveloperList(project);
			project.numCommits = calcNumCommitsRun.get();
			project.numLocProgrammingLanguageList = calcNumLocProgrammingLanguageListRun.get();
			List<Developer> devTFList = calcTruckFactorRun.get();

			markTruckFactorAndCount(project, devTFList);

			return project;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Erro ao construir overview do projeto", e);
		}
	}

	public List<Developer> calculateLocCommitDeveloperList(Project project, String filterPath,
			List<Developer> devTFList) throws IOException, InterruptedException {

		// Verifica se já existe no cache
		String normalizedCheckout = project.checkout != null ? project.checkout : "master";
		List<Developer> cachedResult = FileMetricsCache.get(project.localRepository, normalizedCheckout, filterPath);
		if (cachedResult != null) {
			// Cria uma cópia para não modificar o cache e marca Truck Factor
			List<Developer> result = new ArrayList<>();
			for (Developer dev : cachedResult) {
				Developer copy = new Developer(dev.name, dev.email, dev.numLoc, dev.numCommits);
				result.add(copy);
			}
			markTruckFactorDevelopers(result, devTFList);
			return result;
		}

		// Se não estiver em cache, calcula
		final CompletableFuture<List<Developer>> calcLocsDeveloperListRun = 
			CompletableFuture.supplyAsync(() -> calculateLocsDeveloperList(project, filterPath));

		final CompletableFuture<HashMap<String, Integer>> calcCommitsDeveloperListRun = 
			CompletableFuture.supplyAsync(() -> calculateCommitsDeveloperList(project, filterPath));

		try {
			List<Developer> developerList = calcLocsDeveloperListRun.get();
			HashMap<String, Integer> developerCommitsMap = calcCommitsDeveloperListRun.get();

			for (Developer developer : developerList) {
				if (developerCommitsMap.containsKey(developer.name))
					developer.numCommits = developerCommitsMap.get(developer.name);
			}

			markTruckFactorDevelopers(developerList, devTFList);
			
			// Armazena no cache (sem Truck Factor marcado, pois isso pode variar)
			List<Developer> cacheCopy = new ArrayList<>();
			for (Developer dev : developerList) {
				Developer copy = new Developer(dev.name, dev.email, dev.numLoc, dev.numCommits);
				cacheCopy.add(copy);
			}
			FileMetricsCache.put(project.localRepository, normalizedCheckout, filterPath, cacheCopy);
			
			return developerList;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Erro ao calcular métricas de desenvolvedores", e);
		}
	}

	private Integer calculateNumCommits(Project project) {
		try {
			GitOutput gitOutput = Git.runCommand(project, "git rev-list --count HEAD", true);
			return Integer.parseInt(gitOutput.outputList.get(0));
		} catch (Exception e) {
			return 0;
		}
	}

	private List<NumLocProgrammingLanguage> calculateNumLocProgrammingLanguageList(Project project) {
		try {
			return CLOC.buildNumLocProgrammingLanguageList(project);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	private List<Developer> calculateDeveloperList(Project project) {
		// Tenta usar o cache primeiro (quando filterPath = null, significa projeto inteiro)
		String normalizedCheckout = project.checkout != null ? project.checkout : "master";
		List<Developer> cachedResult = FileMetricsCache.get(project.localRepository, normalizedCheckout, null);
		if (cachedResult != null) {
			// Retorna cópia para não modificar o cache
			List<Developer> result = new ArrayList<>();
			for (Developer dev : cachedResult) {
				result.add(new Developer(dev.name, dev.email, dev.numLoc, dev.numCommits));
			}
			return result;
		}
		
		// Se não estiver em cache, calcula e armazena
		List<Developer> result = calculateLocsDeveloperList(project, null);
		
		// Armazena no cache
		List<Developer> cacheCopy = new ArrayList<>();
		for (Developer dev : result) {
			cacheCopy.add(new Developer(dev.name, dev.email, dev.numLoc, dev.numCommits));
		}
		FileMetricsCache.put(project.localRepository, normalizedCheckout, null, cacheCopy);
		
		return result;
	}

	private List<Developer> calculateLocsDeveloperList(Project project, String filterPath) {
		Map<String, AtomicInteger> authorLocMap = new ConcurrentHashMap<>();
		try {
			String lsFilesCommand = (filterPath == null || filterPath.isEmpty()) ? "git ls-files" : "git ls-files " + filterPath;
			GitOutput filesOutput = Git.runCommand(project, lsFilesCommand, true);
			
			List<CompletableFuture<Void>> tasks = new ArrayList<>();
			for (String filePath : filesOutput.outputList) {
				if (filePath == null || filePath.trim().isEmpty()) continue;
				tasks.add(CompletableFuture.runAsync(() -> {
					try {
						GitOutput blameOutput = Git.runCommand(project, "git blame --line-porcelain \"" + filePath.trim() + "\"", true);
						for (String line : blameOutput.outputList) {
							if (line != null && line.startsWith("author ")) {
								String authorName = line.substring(7).trim();
								if (!authorName.isEmpty()) {
									authorLocMap.computeIfAbsent(authorName, k -> new AtomicInteger(0)).incrementAndGet();
								}
							}
						}
					} catch (Exception ignored) {}
				}, GIT_EXECUTOR));
			}
			CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

			List<Developer> list = new ArrayList<>();
			authorLocMap.forEach((name, loc) -> list.add(new Developer(name, "email", loc.get(), 0)));
			list.sort((d1, d2) -> Integer.compare(d2.numLoc, d1.numLoc));
			return list;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	private HashMap<String, Integer> calculateCommitsDeveloperList(Project project, String filterPath) {
		HashMap<String, Integer> map = new HashMap<>();
		try {
			String command = (filterPath == null) ? "git log --pretty=format:\"%an\"" : "git log --pretty=format:\"%an\" --follow \"" + filterPath + "\"";
			GitOutput gitOutput = Git.runCommand(project, command, true);
			for (String line : gitOutput.outputList) {
				if (line != null && !line.trim().isEmpty()) {
					String name = line.trim();
					map.put(name, map.getOrDefault(name, 0) + 1);
				}
			}
		} catch (Exception ignored) {}
		return map;
	}

	private void calculateNumLocProjectByDeveloperList(Project project) {
		project.numLoc = project.developerList.stream().mapToInt(d -> d.numLoc).sum();
	}

	private List<Developer> calculateTruckFactor(Project project) {
		List<Developer> list = new ArrayList<>();
		try {
			GitOutput gitOutput = Git.runGitTruckFactor(project);
			boolean startParsing = false;
			for (String line : gitOutput.outputList) {
				if (line.contains("TF authors")) { startParsing = true; continue; }
				if (startParsing && line.contains(";")) {
					list.add(new Developer(line.substring(0, line.indexOf(";"))));
				}
			}
		} catch (Exception ignored) {}
		return list;
	}

	private void markTruckFactorAndCount(Project project, List<Developer> devTFList) {
		Set<String> names = new HashSet<>();
		devTFList.forEach(d -> names.add(d.name));
		project.truckFactor = 0;
		for (Developer d : project.developerList) {
			if (names.contains(d.name)) {
				project.truckFactor++;
				d.truckFactor = true;
			}
		}
	}

	private void markTruckFactorDevelopers(List<Developer> list, List<Developer> tfList) {
		Set<String> names = new HashSet<>();
		tfList.forEach(d -> names.add(d.name));
		list.forEach(d -> { if (names.contains(d.name)) d.truckFactor = true; });
	}
}
