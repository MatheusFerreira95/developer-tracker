package mestrado.matheus.teamtracker.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import mestrado.matheus.teamtracker.domain.Developer;
import mestrado.matheus.teamtracker.domain.Explore;
import mestrado.matheus.teamtracker.domain.FileExtension;
import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.LinkExplore;
import mestrado.matheus.teamtracker.domain.NodeExplore;
import mestrado.matheus.teamtracker.domain.Project;
import mestrado.matheus.teamtracker.util.Git;
import mestrado.matheus.teamtracker.util.GitOutput;

/**
 * Serviço responsável pela lógica de exploração da estrutura do projeto
 * e recomendações de desenvolvedores.
 */
@Service
public class ExploreService {

	private final ProjectService projectService;

	public ExploreService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public Explore build(Filter filter, Project project, List<Developer> devTFList)
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

	private Explore buildExplore(Filter filter, Project project, List<Developer> devTFList) {
		Explore explore = new Explore();
		try {
			GitOutput gitOutputName = Git.runCommand(project, " git ls-tree --name-only HEAD " + filter.zoomPath, true);

			for (String filePath : gitOutputName.outputList) {
				NodeExplore node = buildNode(filePath, filter.zoomPath, project.localRepository);
				if (!explore.nodeList.contains(node))
					explore.nodeList.add(node);

				calcLinks(project, explore, node, filePath, devTFList);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return explore;
	}

	private void calcLinks(Project project, Explore explore, NodeExplore node, String filePath,
			List<Developer> devTFList) {
		try {
			// Usa o ProjectService injetado em vez do método estático da classe Project
			project.developerList = projectService.calculateLocCommitDeveloperList(project, filePath, devTFList);

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
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private NodeExplore buildNode(String filePath, String prefixFromFilter, String localRepository) {
		String pathRemovedFilter = prefixFromFilter.isEmpty() ? filePath
				: filePath.substring(prefixFromFilter.length());

		if (Paths.get(localRepository, filePath).toFile().isDirectory()) {
			return new NodeExplore(NodeExplore.NODE_FOLDER, pathRemovedFilter, filePath, false);
		} else {
			return new NodeExplore(NodeExplore.NODE_FILE, pathRemovedFilter, filePath, false);
		}
	}

	private Explore buildExploreLevelProject(Project project, List<Developer> devTFList)
			throws IOException, InterruptedException {
		Explore explore = new Explore();
		NodeExplore nodeProject = new NodeExplore(NodeExplore.NODE_PROJECT, null, NodeExplore.NODE_PROJECT, false);
		explore.nodeList.add(nodeProject);

		// Usa o ProjectService injetado
		List<Developer> devList = projectService.calculateLocCommitDeveloperList(project, null, devTFList);

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

	private boolean isZoomLevelProject(Filter filter) {
		return filter.zoomPath.equals("Root");
	}

	private boolean isFirstZoomLevel(Filter filter) {
		return filter.zoomPath.equals("Project");
	}
}
