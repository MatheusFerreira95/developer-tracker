package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import mestrado.matheus.teamtracker.util.CLOC;
import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;

public class Project {

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

			gitOutput = Git.runCommand(project, "git rev-list --count HEAD");
			return Integer.parseInt(gitOutput.outputList.get(0));

		} catch (IOException | InterruptedException e) {

			e.printStackTrace();
		}

		return 0;
	}

	public void calcNumActiveDaysAndFirstCommitAndLastCommit() throws IOException, InterruptedException {

		GitOutput gitOutput = Git.runCommand(this, "git log --date=short --pretty=format:%ad | sort | uniq -c");
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

			for (Developer dev : developerList) {

				for (Developer devTF : devTFList) {
					if (dev.equals(devTF)) {
						dev.truckFactor = true;
					}
				}
			}

			return developerList;

		} catch (InterruptedException | ExecutionException e) {

			System.err.println("findResume process error" + e);
			throw new RuntimeException();
		}

	}

	private static List<Developer> calcLocsDeveloperList(Project project, String filterPath) {

		GitOutput gitOutputName = null;

		String filePath = filterPath == null ? "" : filterPath;

		List<Developer> locDeveloperList = new ArrayList<Developer>();

		try {

			gitOutputName = Git.runCommand(project, " git ls-files " + filePath
					+ " | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort -f | uniq -ic | sort -nr");

			for (String line : gitOutputName.outputList) {

				try {

					String name = line.substring(8);

					Integer numLoc = Integer.parseInt(line.substring(0, 8).trim());

					Developer dev = new Developer(name, "email", numLoc, 0);

					if (locDeveloperList.contains(dev)) {

						for (Developer developer : locDeveloperList) {

							if (developer.equals(dev)) {

								developer.numLoc += dev.numLoc;
							}
						}

					} else {

						locDeveloperList.add(dev);

					}

				} catch (Exception e) {

					System.out.println("Developer not add. See the line: " + line);
				}
			}

		} catch (IOException | InterruptedException e1) {

			e1.printStackTrace();
		}

		return locDeveloperList;
	}

	public static List<Developer> calcDeveloperList(Project project) {

		GitOutput gitOutputName = null;

		try {

			gitOutputName = Git.runCommand(project,
					" git ls-files | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort -f | uniq -ic | sort -nr");

			for (String line : gitOutputName.outputList) {

				try {

					String name = line.substring(8);

					Integer numLoc = Integer.parseInt(line.substring(0, 8).trim());

					Developer dev = new Developer(name, "email", numLoc, 0);

					if (project.developerList.contains(dev)) {

						for (Developer developer : project.developerList) {

							if (developer.equals(dev)) {

								developer.numLoc += dev.numLoc;
							}
						}

					} else {

						project.developerList.add(dev);

					}

				} catch (Exception e) {

					System.out.println("Devloper not add. See the line: " + line);
				}
			}

		} catch (IOException | InterruptedException e1) {

			e1.printStackTrace();
		}

		return project.developerList;
	}

	public static HashMap<String, Integer> calcCommitsDeveloperList(Project project, String filterPath) {

		GitOutput gitOutputName = null;

		HashMap<String, Integer> developerCommitsMap = new HashMap<String, Integer>();

		String pathFile = filterPath == null ? "." : filterPath;

		try {

			gitOutputName = Git.runCommand(project,
					" git log --pretty=format:\"%an\" --follow \"" + pathFile + "\" | sort -f | uniq -ic | sort -nr");

			for (String line : gitOutputName.outputList) {

				try {

					String name = line.substring(8);

					Integer numCommits = Integer.parseInt(line.substring(0, 8).trim());

					numCommits = numCommits == null ? 0 : numCommits;

					if (developerCommitsMap.containsKey(name)) {

						developerCommitsMap.put(name, developerCommitsMap.get(name) + numCommits);

					} else {

						developerCommitsMap.put(name, numCommits);
					}

				} catch (Exception e) {

					System.out.println("Devloper not add. See the line: " + line);
				}

			}

		} catch (IOException | InterruptedException e1) {

			System.err.println(e1.getMessage());
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

			for (Developer devTF : devTFList) {
				for (Developer developer : project.developerList) {

					if (developer.equals(devTF)) {

						project.truckFactor++;
						developer.truckFactor = true;
					}
				}
			}

			return project;

		} catch (InterruptedException | ExecutionException e) {

			System.err.println("findResume process error" + e);
			throw new RuntimeException();
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
