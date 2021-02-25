package mestrado.matheus.teamtracker.domain;

public class NumLocProgrammingLanguage {

	public String nameProgrammingLanguage;
	public Integer percentLOC;

	public NumLocProgrammingLanguage(String nameProgrammingLanguage, Integer sumLOC) {

		this.nameProgrammingLanguage = nameProgrammingLanguage;
		this.percentLOC = sumLOC;
	}
}
