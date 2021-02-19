package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

	public void calcNumCommits() throws IOException, InterruptedException {

		GitOutput gitOutput = Git.runCommand(this, "git rev-list --count HEAD");
		this.numCommits = Integer.parseInt(gitOutput.outputList.get(0));
	}

	public void calcNumLoc() throws IOException, InterruptedException {

		GitOutput gitOutput = Git.runCommand(this, "git ls-files | xargs cat | wc -l");
		this.numLoc = Integer.parseInt(gitOutput.outputList.get(0));
	}

	public void calcNumActiveDaysAndFirstCommitAndLastCommit() throws IOException, InterruptedException {

		GitOutput gitOutput = Git.runCommand(this, "git log --date=short --pretty=format:%ad | sort | uniq -c");
		this.numActiveDays = gitOutput.outputList.size();
		this.firstCommit = gitOutput.outputList.get(0).substring(gitOutput.outputList.get(0).lastIndexOf(" "));
		this.lastCommit = gitOutput.outputList.get(this.numActiveDays - 1)
				.substring(gitOutput.outputList.get(this.numActiveDays - 1).lastIndexOf(" "));

	}

	private void calcNumLocProgrammingLanguageList() throws IOException, InterruptedException {

		CLOC.buildNumLocProgrammingLanguageList(this);

	}

	public void calcDeveloperList(String filterPath, List<Developer> devTFList) throws IOException, InterruptedException {

		String filePath = filterPath == null ? "" : filterPath;

		// GitOutput gitOutputEmail = Git.runCommand(this, " git ls-files " + filePath
		// 		+ " | xargs -n1 git blame --line-porcelain | sed -n 's/^author-mail //p' | sort -f | uniq -ic | sort -nr");
		GitOutput gitOutputName = Git.runCommand(this, " git ls-files " + filePath
				+ " | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort -f | uniq -ic | sort -nr");

		Integer avatar = 0;
		this.developerList = new ArrayList<Developer>();
		for (String line : gitOutputName.outputList) {

			try {

				//String emailNotTrim = gitOutputEmail.outputList.get(avatar);

				//String email = emailNotTrim.substring(emailNotTrim.indexOf("<") + 1, emailNotTrim.indexOf(">"));

				String name = line.substring(8);

				Integer numLoc = Integer.parseInt(line.substring(0, 8).trim());

				Developer dev = new Developer(name, "email", numLoc, avatar++);

				if (this.developerList.contains(dev)) {

					for (Developer developer : this.developerList) {

						if (developer.equals(dev)) {

							developer.numLoc += dev.numLoc;
						}
					}

				} else {

					this.developerList.add(dev);

				}
			} catch (Exception e) {

				System.out.println("Developer not add. See the line: " + line);
			}
		}

		for (Developer dev : this.developerList) {
			//print System.out.println("Developer..................................: " + dev.name);

			for (Developer devTF : devTFList) {
				if(dev.equals(devTF)) {
					dev.truckFactor = true;
				}
			}
		}

		calcCommitsDeveloperList(filterPath);
	}

	public void calcDeveloperList() throws IOException, InterruptedException {

		// GitOutput gitOutputEmail = Git.runCommand(this,
		// 		" git ls-files | xargs -n1 git blame --line-porcelain | sed -n 's/^author-mail //p' | sort -f | uniq -ic | sort -nr");
		GitOutput gitOutputName = Git.runCommand(this,
				" git ls-files | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort -f | uniq -ic | sort -nr");

		Integer avatar = 0;
		for (String line : gitOutputName.outputList) {

			try {

				//String emailNotTrim = gitOutputEmail.outputList.get(avatar);

				//String email = emailNotTrim.substring(emailNotTrim.indexOf("<") + 1, emailNotTrim.indexOf(">"));

				String name = line.substring(8);

				Integer numLoc = Integer.parseInt(line.substring(0, 8).trim());

				Developer dev = new Developer(name, "email", numLoc, avatar++);

				if (this.developerList.contains(dev)) {

					for (Developer developer : this.developerList) {

						if (developer.equals(dev)) {

							developer.numLoc += dev.numLoc;
						}
					}

				} else {

					this.developerList.add(dev);

				}
			} catch (Exception e) {

				System.out.println("Devloper not add. See the line: " + line);
			}
		}

		calcNumLocProjectByDeveloperList();

		// float percentageTotal = 0;
		// for (Developer developer : developerList) {
		// 	float percentage = (float) (1000 * developer.numLoc / this.numLoc / 10.0);
		// 	float percentageAnterior = percentageTotal;
		// 	percentageTotal += percentage;
		// 	if (percentageTotal > 100) {
		// 		developer.percentLoc = 100 - percentageAnterior;
		// 		percentageAnterior = 100;
		// 		percentageTotal = 100;
		// 	} else {
		// 		developer.percentLoc = percentage;
		// 	}
		// }

		// if (percentageTotal < 100) {
		// 	developerList.get(0).percentLoc += 100 - percentageTotal;
		// }

		calcTruckFactor();

		calcCommitsDeveloperList(null);
	}

	public void calcCommitsDeveloperList(String pathFile) throws IOException, InterruptedException {
		GitOutput gitOutputName;
		if (pathFile == null) { // project level

			gitOutputName = Git.runCommand(this, " git log | grep Author: | sort | uniq -c | sort -nr");

			for (String line : gitOutputName.outputList) {

				try {

					String name = line.substring(line.indexOf(": ") + 2, line.indexOf(" <"));

					Integer numCommits = Integer.parseInt(line.substring(0, 8).trim());

					for (Developer developer : developerList) {
						if (developer.name.equals(name)) {
							if(numCommits == null) numCommits = 0;
							if(developer.numCommits == null) developer.numCommits = 0;
							developer.numCommits += numCommits;
							break;
						}
					}
				} catch (Exception e) {

					System.out.println("Devloper not add. See the line: " + line);
				}
			}
		} else {
			gitOutputName = Git.runCommand(this,
					" git log --pretty=format:\"%an\" --follow \"" + pathFile + "\" | uniq -c | sort -nr");

			for (String line : gitOutputName.outputList) {

				try {

					String name = line.substring(8);

					Integer numCommits = Integer.parseInt(line.substring(0, 8).trim());

					for (Developer developer : developerList) {
						if (developer.name.equals(name)) {

							if (developer.numCommits == null) developer.numCommits = 0;
							if (numCommits == null) numCommits = 0;
							
							developer.numCommits += numCommits;
						}
					}
				} catch (Exception e) {

					System.out.println("Devloper not add. See the line: " + line);
				}

			}
		}
	}

	private void calcNumLocProjectByDeveloperList() {
		this.numLoc = 0;
		for (Developer developer : developerList) {
			this.numLoc += developer.numLoc;
		}

	}

	private void calcTruckFactor() throws IOException, InterruptedException {

		GitOutput gitOutput = Git.runGitTruckFactor(this);

		int isAuthors = 0;
		for (String line : gitOutput.outputList) {

			//print System.out.println(line);

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

			Developer developerTF = new Developer(line.substring(0, line.indexOf(";")));

			for (Developer developer : this.developerList) {

				if (developer.equals(developerTF)) {

					this.truckFactor++;
					developer.truckFactor = true;
				}
			}

		}

	}

	public static Project buildOverview(Filter filter, String checkout) throws IOException, InterruptedException {

		Project project = Project.builderProject(filter, checkout);

		project.calcNumCommits();
		// project.calcNumLoc();
		// project.calcNumActiveDaysAndFirstCommitAndLastCommit();
		project.calcNumLocProgrammingLanguageList();
		project.calcDeveloperList();

		return project;

	}

	public static Project builderProject(Filter filter, String checkout) {

		if (filter.localRepository != null && !filter.localRepository.isEmpty()) {

			//print System.out.println("info...................BuilderProject is checkouting (" + checkout + ") in local: " + filter.localRepository);

			Project project = new Project(filter.localRepository, checkout);
			
			Git.runCheckout(project);
			
			return project;

		} else if (filter.remoteRepository != null && !filter.remoteRepository.isEmpty()) {
			
			//print System.out.println("info...................BuilderProject is clonning from: " + filter.remoteRepository);

			if(filter.user != null && !filter.user.isEmpty() && filter.password != null && !filter.password.isEmpty()) {

				return Git.clone("https://" + encodeValue(filter.user) + ":" + encodeValue(filter.password) + "@" + filter.remoteRepository.substring(8), checkout);
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
