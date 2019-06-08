package mestrado.matheus.teamtracker.domain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;
import mestrado.matheus.teamtracker.util.LanguageExcludeCLOC;

public class Project {

	public Integer numLoc;
	public Integer numCommits;
	public Integer numActiveDays;
	public String firstCommit;
	public String lastCommit;
	public String localRepository;
	public List<NumLocProgrammingLanguage> numLocProgrammingLanguageList;
	public List<Developer> developerList;

	public Project(String localRepository) {

		this.localRepository = localRepository;

		// Fake
		this.numLoc = 5090;
		this.numCommits = 356;
		this.numActiveDays = 42;
		this.firstCommit = "03/10/2016";
		this.lastCommit = "03/10/2018";

//		NumLocProgrammingLanguage lp = new NumLocProgrammingLanguage("Java", "20%");

		this.numLocProgrammingLanguageList = new ArrayList<NumLocProgrammingLanguage>();
//		this.numLocProgrammingLanguageList.add(lp);

		Developer dev = new Developer();
		dev.email = "email.com";
		dev.name = "Toe";
		dev.firstCommit = "10/10/2016";
		dev.lastCommit = "10/10/2018";
		dev.numLoc = 1000;
		dev.numCommits = 98;
		dev.numActiveDays = 30;
		dev.numLocProgrammingLanguageList = new ArrayList<NumLocProgrammingLanguage>();
//		dev.numLocProgrammingLanguageList.add(lp);
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

	private void calcNumLocProgrammingLanguageList() throws IOException, InterruptedException {

		// Requer https://github.com/AlDanial/cloc/blob/master/cloc instalada

		GitOutput gitOutput = Git.runCommand(this,
				"cloc ./ --json --exclude-lang=\"" + LanguageExcludeCLOC.ALL + "\" --out cloc-out.txt");

		File outpupFile = new File(this.localRepository + "/cloc-out.txt");

		Boolean outputFileCreated = false;
		for (String line : gitOutput.outputList) {
			if (line.contains("cloc-out.txt")) {
				outputFileCreated = true;
				break;
			}
		}

		if (!outputFileCreated) return;

		while (!outpupFile.exists()) {

			System.out.println("loading...");
		}

		Scanner scanner = new Scanner(outpupFile);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			String nameProgrammingLanguage = line.substring(1, line.lastIndexOf("\""));
			if (!line.contains("{") || line.contains("header"))
				continue;

			String lastCaracter = ",";
			if (!nameProgrammingLanguage.contentEquals("SUM")) {

				lastCaracter = "}";
				line = scanner.nextLine();

			}

			line = scanner.nextLine();
			String blank = line.substring(line.lastIndexOf(" ") + 1, line.indexOf(","));

			line = scanner.nextLine();
			String comment = line.substring(line.lastIndexOf(" ") + 1, line.indexOf(","));

			line = scanner.nextLine();
			String code = line.substring(line.lastIndexOf(" ") + 1, line.indexOf(lastCaracter));

			Integer sumLOC = Integer.parseInt(blank) + Integer.parseInt(comment) + Integer.parseInt(code);

			this.numLocProgrammingLanguageList.add(new NumLocProgrammingLanguage(nameProgrammingLanguage, sumLOC));
		}

		Integer sumTotal = this.numLocProgrammingLanguageList
				.get(this.numLocProgrammingLanguageList.size() - 1).percentLOC;
		this.numLocProgrammingLanguageList.remove(this.numLocProgrammingLanguageList.size() - 1);

		Integer hightTotal = 0;
		Integer index = 0;

		// recalculando porcentagem e excluindo os insignificantes
		for (NumLocProgrammingLanguage numLocProgrammingLanguage : this.numLocProgrammingLanguageList) {

			index++;

			if (hightTotal < 93) {

				numLocProgrammingLanguage.percentLOC = numLocProgrammingLanguage.percentLOC * 100 / sumTotal;
				hightTotal += numLocProgrammingLanguage.percentLOC;

			} else {

				this.numLocProgrammingLanguageList.add(index - 1,
						new NumLocProgrammingLanguage("Others", 100 - hightTotal));
				this.numLocProgrammingLanguageList = this.numLocProgrammingLanguageList.subList(0, index);
				break;
			}

		}
	}

	public static Project buildOverview(Filter filter) throws IOException, InterruptedException {

		Project project = Project.builderProject(filter);

		project.calcNumCommits();
		project.calcNumLoc();
		project.calcNumActiveDaysAndFirstCommitAndLastCommit();
		project.calcNumLocProgrammingLanguageList();

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
