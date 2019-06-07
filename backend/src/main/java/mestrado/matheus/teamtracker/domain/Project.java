package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;

public class Project {

	public Integer numLoc;
	public Integer numCommits;
	public Integer numActiveDays;
	public String firstCommit;
	public String lastCommit;
	public String localRepository;
	public List<NumFileProgrammingLanguage> numFileProgrammingLanguageList;
	public List<Developer> developerList;

	public Project(String localRepository) {

		this.localRepository = localRepository;

		// Fake
		this.numLoc = 5090;
		this.numCommits = 356;
		this.numActiveDays = 42;
		this.firstCommit = "03/10/2016";
		this.lastCommit = "03/10/2018";

		NumFileProgrammingLanguage lp = new NumFileProgrammingLanguage();
		lp.nameProgrammingLanguage = "Java";
		lp.numFile = 20;

		this.numFileProgrammingLanguageList = new ArrayList<NumFileProgrammingLanguage>();
		this.numFileProgrammingLanguageList.add(lp);

		Developer dev = new Developer();
		dev.email = "email.com";
		dev.name = "Toe";
		dev.firstCommit = "10/10/2016";
		dev.lastCommit = "10/10/2018";
		dev.numLoc = 1000;
		dev.numCommits = 98;
		dev.numActiveDays = 30;
		dev.numFileProgrammingLanguageList = new ArrayList<NumFileProgrammingLanguage>();
		dev.numFileProgrammingLanguageList.add(lp);
		dev.fileAuthorList = new ArrayList<String>();
		dev.fileAuthorList.add("com/arquivo.java");
		dev.avatar = "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg";

		this.developerList = new ArrayList<Developer>();
		this.developerList.add(dev);
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

	public static Project buildOverview(Filter filter) throws IOException, InterruptedException {

		Project project = Project.builderProject(filter);

		project.calcNumCommits();
		project.calcNumLoc();
		project.calcNumActiveDaysAndFirstCommitAndLastCommit();

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
