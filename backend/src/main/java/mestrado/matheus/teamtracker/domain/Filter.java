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
	public String checkout1;
	public String checkout2;
	public List<Developer> devTFListV1 = new ArrayList<Developer>();
	public List<Developer> devTFListV2 = new ArrayList<Developer>();

}
