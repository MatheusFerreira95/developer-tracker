package mestrado.matheus.teamtracker.domain;

import java.util.List;

public class Project {

	public Integer numLoc;
	public Integer numCommits;
	public Integer numActiveDays;
	public String firstCommit;
	public String LastCommit;
	public List<NumFileProgrammingLanguage> numFileProgrammingLanguageList;
	public List<Developer> developerList;
}
