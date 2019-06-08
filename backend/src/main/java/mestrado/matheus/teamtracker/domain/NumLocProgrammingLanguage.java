package mestrado.matheus.teamtracker.domain;

public class NumLocProgrammingLanguage {

	public String nameProgrammingLanguage;
	public Integer percentLOC;

	public NumLocProgrammingLanguage(String nameProgrammingLanguage, Integer numFile) {

		this.nameProgrammingLanguage = nameProgrammingLanguage;
		this.percentLOC = numFile;
	}
}
