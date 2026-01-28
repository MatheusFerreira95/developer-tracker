package mestrado.matheus.teamtracker.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de domínio que representa um projeto.
 * Focada apenas em armazenar os dados do projeto (POJO).
 */
public class Project {

	public Integer numLoc;
	public Integer numCommits;
	public Integer numActiveDays;
	public String firstCommit;
	public String lastCommit;
	public String localRepository;
	public String checkout;
	public List<NumLocProgrammingLanguage> numLocProgrammingLanguageList = new ArrayList<>();
	public List<Developer> developerList = new ArrayList<>();
	public Integer truckFactor = 0;
	public String currentVersion;

	public Project(String localRepository, String checkout) {
		this.localRepository = localRepository;
		this.checkout = checkout != null && !checkout.isEmpty() ? checkout : "master";
	}
}
