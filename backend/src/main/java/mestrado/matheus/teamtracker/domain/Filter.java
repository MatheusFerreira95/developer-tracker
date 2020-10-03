package mestrado.matheus.teamtracker.domain;

import java.util.ArrayList;
import java.util.List;

public class Filter {

	public String remoteRepository;
	public String localRepository;
	public String directory;
	public String zoomPath = "./";
	public String user;
	public String password;
	public List<Developer> devTFList = new ArrayList<Developer>();

}
