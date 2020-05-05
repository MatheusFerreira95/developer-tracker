package mestrado.matheus.teamtracker.domain;

public class LinkExplore {
	public String source;
	public String target;
	public String loc;
	public String commits;
	public Integer numLoc;
	public Integer numCommits;
	public String color = "#3f51b5";

	public LinkExplore(String source, String target, Integer loc, Integer commits) {

		this.source = source;
		this.target = target;
		this.loc = loc + " LOC";
		this.commits = commits + " commits";
		this.numLoc = loc;
		this.numCommits = commits;
	}

	@Override
	public boolean equals(Object obj) {
		LinkExplore link = (LinkExplore) obj;
		if (link.source.equals(this.source) && link.target.equals(this.target))
			return true;
		return false;
	}

	public void increaseLoc(Integer numLoc) {
		this.numLoc += numLoc;
		this.loc = this.numLoc + " LOC";

	}

	public void increaseCommits(Integer numCommits) {
		this.numCommits += numCommits;
		this.commits = numCommits + " commits";

	}
}
