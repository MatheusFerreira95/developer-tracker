package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Explore {

	public final static String START_READ = "developer-tracker-start-read-info-file";
	public final static String STOP_READ = "developer-tracker-stop-read-info-file";

	public List<NodeExplore> nodeList = new ArrayList<NodeExplore>();
	public List<LinkExplore> linkList = new ArrayList<LinkExplore>();

	public static Explore build(Filter filter) throws IOException, InterruptedException {

		Project project = Project.builderProject(filter);

		if (isZoomLevelProject(filter)) {

			return buildExploreLevelProject(project);

		}

		return buildExplore(filter, project);

	}

	private static Explore buildExplore(Filter filter, Project project) {
//for (String line : gitOutputLOC.outputList) {
//			
//			if(line.equals(START_READ_ARTIFACT)) {
//				
//			}
//		}
		return null;
	}

	private static Explore buildExploreLevelProject(Project project) throws IOException, InterruptedException {

		Explore explore = new Explore();

		NodeExplore nodeProject = new NodeExplore(NodeExplore.NODE_PROJECT, null, null);
		explore.nodeList.add(nodeProject);
		
		
		project.calcDeveloperList();
		for (Developer developer : project.developerList) {

			NodeExplore nodeDeveloper = new NodeExplore(NodeExplore.NODE_DEVELOPER, developer.name, null);
			explore.nodeList.add(nodeDeveloper);
			
			LinkExplore link = new LinkExplore(nodeProject.name, developer.name, developer.numLoc,
					developer.numCommits);
			explore.linkList.add(link);
		}

		return explore;
	}

	private static boolean isZoomLevelProject(Filter filter) {

		return filter.zoomPath.equals("./");
	}
}
