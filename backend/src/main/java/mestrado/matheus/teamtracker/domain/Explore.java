package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;

public class Explore {

	public final static String START_READ = "developer-tracker-start-read-info-file";
	public final static String STOP_READ = "developer-tracker-stop-read-info-file";

	public List<NodeExplore> nodeList = new ArrayList<NodeExplore>();
	public List<LinkExplore> linkList = new ArrayList<LinkExplore>();

	public static Explore build(Filter filter, Project project, List<Developer> devTFList) throws IOException, InterruptedException {

		if (isZoomLevelProject(filter)) {

			return buildExploreLevelProject(project, devTFList);

		}

		if (isFirstZoomLevel(filter)) {

			filter.zoomPath = "";
		
		} else {

			filter.zoomPath += "/";
		}

		return buildExplore(filter, project, devTFList);

	}

	private static Explore buildExplore(Filter filter, Project project, List<Developer> devTFList) {

		Explore explore = new Explore();
		GitOutput gitOutputName;

		try {
			gitOutputName = Git.runCommand(project, " git ls-files " + filter.zoomPath);

			for (String filePath : gitOutputName.outputList) {

				if (filePath.startsWith(filter.zoomPath)) {

					NodeExplore node = buildNode(filePath, filter.zoomPath);
					if (!explore.nodeList.contains(node))
						explore.nodeList.add(node);

					calcLinks(project, explore, node, filePath, devTFList);

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return explore;
	}

	private static void calcLinks(Project project, Explore explore, NodeExplore node, String filePath, List<Developer> devTFList) {

		try {
			project.calcDeveloperList(filePath, devTFList);

			for (Developer developer : project.developerList) {

				NodeExplore nodeDeveloper = new NodeExplore(NodeExplore.NODE_DEVELOPER, developer.name, null,
						developer.truckFactor);
				if (!explore.nodeList.contains(nodeDeveloper))
					explore.nodeList.add(nodeDeveloper);

				LinkExplore link = new LinkExplore(developer.name, node.name, developer.numLoc, developer.numCommits);
				Integer indexLink = explore.linkList.indexOf(link);
				if (indexLink == -1)
					explore.linkList.add(link);
				else {
					explore.linkList.get(indexLink).increaseLoc(link.numLoc);
					explore.linkList.get(indexLink).increaseCommits(link.numCommits);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private static NodeExplore buildNode(String filePath, String prefixFromFilter) {

		NodeExplore node = null;

		String pathRemovedFilter = prefixFromFilter.isEmpty() ? filePath
				: filePath.substring(prefixFromFilter.length());

		if (pathRemovedFilter.contains("/")) {

			String formattedName = pathRemovedFilter.substring(0, pathRemovedFilter.indexOf("/"));

			node = new NodeExplore(NodeExplore.NODE_FOLDER, formattedName,
					prefixFromFilter.isEmpty() ? formattedName : prefixFromFilter + formattedName, false);
		} else {

			node = new NodeExplore(NodeExplore.NODE_FILE, pathRemovedFilter, filePath, false);
		}

		return node;

	}

	private static Explore buildExploreLevelProject(Project project, List<Developer> devTFList) throws IOException, InterruptedException {

		Explore explore = new Explore();

		NodeExplore nodeProject = new NodeExplore(NodeExplore.NODE_PROJECT, null, NodeExplore.NODE_PROJECT, false);
		explore.nodeList.add(nodeProject);

		project.calcDeveloperList(null, devTFList);
		for (Developer developer : project.developerList) {

			NodeExplore nodeDeveloper = new NodeExplore(NodeExplore.NODE_DEVELOPER, developer.name, null,
					developer.truckFactor);
			explore.nodeList.add(nodeDeveloper);

			LinkExplore link = new LinkExplore(developer.name, nodeProject.name, developer.numLoc,
					developer.numCommits);
			explore.linkList.add(link);
		}

		return explore;
	}

	private static boolean isZoomLevelProject(Filter filter) {

		return filter.zoomPath.equals("Root");
	}

	private static boolean isFirstZoomLevel(Filter filter) {

		return filter.zoomPath.equals("Project");
	}

}
