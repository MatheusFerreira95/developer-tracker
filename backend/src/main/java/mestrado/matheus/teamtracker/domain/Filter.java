package mestrado.matheus.teamtracker.domain;

public class Filter {
	
	public String repositoryPath;
	public String dateRange;
	public String directory;
	public String branch;
	@Override
	public String toString() {
		return "Filter [repositoryPath=" + repositoryPath + ", dateRange=" + dateRange + ", directory=" + directory
				+ ", branch=" + branch + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

}
