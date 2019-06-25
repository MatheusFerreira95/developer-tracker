package mestrado.matheus.teamtracker.domain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;
import mestrado.matheus.teamtracker.util.CLOC;

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

		GitOutput gitOutput = Git.runCommand(this, "git log | grep Author: | sort | uniq -c | sort -nr");

		Integer avatar = 0;
		for (String line : gitOutput.outputList) {

			String email = line.substring(line.indexOf("<") + 1, line.indexOf(">"));
			line = line.substring(0, line.indexOf("<")); // remove email

			String name = line.substring(line.indexOf("Author: ") + 8, line.lastIndexOf(" "));

			Integer numCommits = Integer.parseInt(line.substring(0, line.indexOf(" Author:")).trim());

			Developer dev = new Developer(name, email, numCommits, avatar++);

//			descomente if/else para usar normalização de devs
//			if (this.developerList.contains(dev)) {
//
//				for (Developer developer : this.developerList) {
//
//					if (developer.equals(dev)) {
//
//						developer.numCommits += dev.numCommits;
//					}
//				}
//
//			} else {

			this.developerList.add(dev);
//			}
		}
	}

	public static Project buildOverview(Filter filter) throws IOException, InterruptedException {

		Project project = Project.builderProject(filter);

		project.calcNumCommits();
		project.calcNumLoc();
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
