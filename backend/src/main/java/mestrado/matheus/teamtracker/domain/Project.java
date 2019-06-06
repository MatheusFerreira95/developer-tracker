package mestrado.matheus.teamtracker.domain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Project {

	public Integer numLoc;
	public Integer numCommits;
	public Integer numActiveDays;
	public String firstCommit;
	public String LastCommit;
	public String localRepository;
	public List<NumFileProgrammingLanguage> numFileProgrammingLanguageList;
	public List<Developer> developerList;

	public Project(String locaRepository) {
		
		this.localRepository = locaRepository;
	}

	public Path getPathLocalRepository() {
		
		return Paths.get(this.localRepository);
	}
}
