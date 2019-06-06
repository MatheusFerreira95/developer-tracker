package mestrado.matheus.teamtracker.domain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

	public Path getPathLocalRepository() {

		return Paths.get(this.localRepository);
	}
}
