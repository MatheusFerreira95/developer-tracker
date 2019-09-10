package mestrado.matheus.teamtracker.domain;

public class LinkExplore {
	public String source;
	public String target;
	public String loc;
	public String commits;
	public String color = "#3f51b5";

	public LinkExplore(String source, String target, Integer loc, Integer commits) {

		this.source = source;
		this.target = target;
		this.loc = loc + " LOC";
		this.commits = commits + " commits";
	}

}
