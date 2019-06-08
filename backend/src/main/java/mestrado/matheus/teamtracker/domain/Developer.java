package mestrado.matheus.teamtracker.domain;

import java.util.List;

public class Developer {
	public String name;
	public String email;
	public List<String> fileAuthorList;
	public Integer numLoc;
	public Integer numCommits;
	public Integer numActiveDays;
	public String firstCommit;
	public String lastCommit;
	public List<NumLocProgrammingLanguage> numLocProgrammingLanguageList;
	public Integer avatar;
	
	public Developer(String name, String email, Integer numCommits, Integer avatar) {
		this.name = name;
		this.email = email;
		this.numCommits = numCommits;
		this.avatar = avatar;
	}
	
	

}
