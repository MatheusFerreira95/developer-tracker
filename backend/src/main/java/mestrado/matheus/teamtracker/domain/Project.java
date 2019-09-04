package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	public List<NumLocProgrammingLanguage> numLocProgrammingLanguageList = new ArrayList<NumLocProgrammingLanguage>();
	public List<Developer> developerList = new ArrayList<Developer>();
	public Integer truckFactor = 0;

	public Project(String localRepository) {

		this.localRepository = localRepository;
	}

	public void calcNumCommits() throws IOException, InterruptedException {

		GitOutput gitOutput = Git.runCommand(this, "git rev-list --all --count");
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

	private void calcDeveloperList() throws IOException, InterruptedException {

		GitOutput gitOutputEmail = Git.runCommand(this,
				" git ls-files | xargs -n1 git blame --line-porcelain | sed -n 's/^author-mail //p' | sort -f | uniq -ic | sort -nr");
		GitOutput gitOutputName = Git.runCommand(this,
				" git ls-files | xargs -n1 git blame --line-porcelain | sed -n 's/^author //p' | sort -f | uniq -ic | sort -nr");

		Integer avatar = 0;
		for (String line : gitOutputName.outputList) {

			try {

				String emailNotTrim = gitOutputEmail.outputList.get(avatar);

				String email = emailNotTrim.substring(emailNotTrim.indexOf("<") + 1, emailNotTrim.indexOf(">"));

				String name = line.substring(8);

				Integer numLoc = Integer.parseInt(line.substring(0, 8).trim());

				Developer dev = new Developer(name, email, numLoc, avatar++);

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

		float percentageTotal = 0;
		for (Developer developer : developerList) {
			float percentage = (float) (1000 * developer.numLoc / this.numLoc / 10.0);
			float percentageAnterior = percentageTotal;
			percentageTotal += percentage;
			if (percentageTotal > 100) {
				developer.percentLoc = 100 - percentageAnterior;
				percentageAnterior = 100;
				percentageTotal = 100;
			} else {
				developer.percentLoc = percentage;
			}
		}

		if (percentageTotal < 100) {
			developerList.get(0).percentLoc += 100 - percentageTotal;
		}

		calcTruckFactor();

		Collections.sort(this.developerList, Collections.reverseOrder());
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

			System.out.println(line);

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
					developer.truckFactor = developer.equals(developerTF);
				}
			}

		}

	}

	public static Project buildOverview(Filter filter) throws IOException, InterruptedException {

		Project project = Project.builderProject(filter);

		project.calcNumCommits();
//		project.calcNumLoc();
		project.calcNumActiveDaysAndFirstCommitAndLastCommit();
		project.calcNumLocProgrammingLanguageList();
		project.calcDeveloperList();

		return project;

	}

	private static Project builderProject(Filter filter) {

		if (filter.localRepository != null && !filter.localRepository.isEmpty()) {

			return new Project(filter.localRepository);

		} else if (filter.remoteRepository != null && !filter.remoteRepository.isEmpty()) {

			return Git.clone(filter.remoteRepository);

		} else {

			throw new RuntimeException();
		}
	}
}
