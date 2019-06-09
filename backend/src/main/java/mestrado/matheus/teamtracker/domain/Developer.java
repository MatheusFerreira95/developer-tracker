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

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Developer other = (Developer) obj;

		if (!name.equals(other.name) && !email.equals(other.email))
			return false;

		return true;
	}

}
