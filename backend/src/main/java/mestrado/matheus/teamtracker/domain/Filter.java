package mestrado.matheus.teamtracker.domain;

public class Filter {

	public String remoteRepository;
	public String localRepository;
	public String dateRange;
	public String directory;
	public String branch;
	
	@Override
	public String toString() {
		return "Filter [repositoryPath=" + remoteRepository + ", dateRange=" + dateRange + ", directory=" + directory
				+ ", branch=" + branch + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

}
