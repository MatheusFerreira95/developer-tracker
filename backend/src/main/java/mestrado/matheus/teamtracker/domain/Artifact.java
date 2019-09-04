package mestrado.matheus.teamtracker.domain;

import java.util.List;

public class Artifact {
	public String name;
	public String path;
	public List<Developer> developerList;

	public Artifact(List<Developer> developerList) {
		this.name = "project";
		this.developerList = developerList;
		
	}
}
