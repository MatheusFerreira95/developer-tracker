package mestrado.matheus.teamtracker.domain;

import java.util.List;

public class Developer implements Comparable<Developer> {
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
	public Boolean truckFactor = false;
	public Float percentLoc;

	public Developer(String name, String email, Integer numLoc, Integer avatar) {
		this.name = name;
		this.email = email;
		this.numLoc = numLoc;
		this.avatar = avatar;
	}

	public Developer(String name) {

		this.name = name;
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

		if (name.equals(other.name))
			return true;

		return false;
	}

	@Override
	public int compareTo(Developer developer) {

		return this.numLoc.compareTo(developer.numLoc);
	}

}
