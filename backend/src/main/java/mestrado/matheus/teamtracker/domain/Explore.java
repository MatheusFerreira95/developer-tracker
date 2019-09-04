package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Explore {

	public List<Artifact> artifactList = new ArrayList<Artifact>();
	public final static String START_READ_ARTIFACT = "developer-tracker-start-read-info-file";
	public final static String STOP_READ_ARTIFACT = "developer-tracker-stop-read-info-file";

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

		explore.artifactList.add(new Artifact(project.developerList));

		return explore;
	}

	private static boolean isZoomLevelProject(Filter filter) {

		return filter.zoomPath.equals("./");
	}
}
