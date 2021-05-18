package mestrado.matheus.teamtracker.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;
import java.io.File;

public class Explore {

	public final static String START_READ = "developer-tracker-start-read-info-file";
	public final static String STOP_READ = "developer-tracker-stop-read-info-file";

	public List<NodeExplore> nodeList = new ArrayList<NodeExplore>();
	public List<LinkExplore> linkList = new ArrayList<LinkExplore>();

	public static Explore build(Filter filter, Project project, List<Developer> devTFList)
			throws IOException, InterruptedException {

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
			gitOutputName = Git.runCommand(project, " git ls-tree --name-only HEAD " + filter.zoomPath, true);

			for (String filePath : gitOutputName.outputList) {

					NodeExplore node = buildNode(filePath, filter.zoomPath, project.localRepository);
					if (!explore.nodeList.contains(node))
						explore.nodeList.add(node);

					calcLinks(project, explore, node, filePath, devTFList);

			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return explore;
	}

	private static void calcLinks(Project project, Explore explore, NodeExplore node, String filePath,
			List<Developer> devTFList) {

		try {
			project.developerList = Project.calcLocCommitDeveloperList(project, filePath, devTFList);

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

	private static NodeExplore buildNode(String filePath, String prefixFromFilter, String localRepository) {

		NodeExplore node = null;

		String pathRemovedFilter = prefixFromFilter.isEmpty() ? filePath
				: filePath.substring(prefixFromFilter.length());

		if (new File(localRepository + "/" + filePath).isDirectory()) {

			node = new NodeExplore(NodeExplore.NODE_FOLDER, pathRemovedFilter, filePath, false);

		} else {

			node = new NodeExplore(NodeExplore.NODE_FILE, pathRemovedFilter, filePath, false);
		}

		return node;

	}

	private static Explore buildExploreLevelProject(Project project, List<Developer> devTFList)
			throws IOException, InterruptedException {

		Explore explore = new Explore();

		NodeExplore nodeProject = new NodeExplore(NodeExplore.NODE_PROJECT, null, NodeExplore.NODE_PROJECT, false);
		explore.nodeList.add(nodeProject);

		List<Developer> devList = Project.calcLocCommitDeveloperList(project, null, devTFList);

		for (Developer developer : devList) {

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

	public static String generateRecommendations(Filter filter, Project project,
			List<FileExtension> extensionListVersion1, String version) throws IOException, InterruptedException {

		String recommentationsText = "Recommended developers for programming languages (referring to version " + version
				+ "):<br>";

		for (FileExtension fileExtension : extensionListVersion1) {

			project.developerList = Project.calcLocCommitDeveloperList(project, "*" + fileExtension.extension, new ArrayList<Developer>());

			Collections.sort(project.developerList, Collections.reverseOrder());

			if (!project.developerList.isEmpty()) {

				recommentationsText += fileExtension.extensionDescription + ": ";
				
				int count = 0;
				for (Developer dev : project.developerList){
					if(count++ == 3) break;
					recommentationsText += dev.name + "; ";
				}
				
				recommentationsText += "<br>";
		}
			}

		return recommentationsText;
	}

}
