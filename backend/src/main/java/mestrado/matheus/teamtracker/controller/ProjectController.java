package mestrado.matheus.teamtracker.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import mestrado.matheus.teamtracker.domain.Explore;
import mestrado.matheus.teamtracker.domain.ExploreVersions;
import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;
import mestrado.matheus.teamtracker.domain.ProjectVersions;

@RestController()
@RequestMapping("/api/project")
public class ProjectController {

	/**
	 * A partir do filtro deve retornar os dados de gerais do projeto (loc, commits,
	 * data de primeiro e último commit, dias ativos (qtd de dias com commit),
	 * linguagens de programaçao e Desenvolvedores)
	 **/
	@RequestMapping(path = "", method = RequestMethod.POST)
	@CrossOrigin(origins = "http://localhost:8081")
	public @ResponseBody ProjectVersions getProjectOverview(@RequestBody Filter filter) {

		try {

			Project projectVersion1 = Project.buildOverview(filter, filter.checkout1);

			Project projectVersion2 = null;
			if (filter.checkout2 != null && !filter.checkout2.isEmpty()) {

				projectVersion2 = Project.buildOverview(filter, filter.checkout2);
			}

			return new ProjectVersions(projectVersion1, projectVersion2);

		} catch (IOException e) {

			e.printStackTrace();

		} catch (InterruptedException e) {

			e.printStackTrace();

		}

		return null;
	}

	/**
	 * A partir do filtro deve retornar os dados de exploração do projeto (relação
	 * de desenvolvedores com artefatos)
	 **/
	@RequestMapping(path = "/explore", method = RequestMethod.POST)
	@CrossOrigin(origins = "http://localhost:8081")
	public @ResponseBody ExploreVersions getExploreProject(@RequestBody Filter filter) {

		try {

			Project projectVersion1 = Project.builderProject(filter, filter.checkout1);
			Explore explore1 = Explore.build(filter, projectVersion1, filter.devTFListV1);

			Explore explore2 = null;
			if (filter.checkout2 != null && !filter.checkout2.isEmpty()) {
				Project projectVersion2 = Project.builderProject(filter, filter.checkout2);
				explore2 = Explore.build(filter, projectVersion2, filter.devTFListV2);
			}

			return new ExploreVersions(explore1, explore2);

		} catch (IOException e) {

			e.printStackTrace();

		} catch (InterruptedException e) {

			e.printStackTrace();

		}

		return null;
	}

	/*
	 * private static final Logger LOG =
	 * LoggerFactory.getLogger(ProjectController.class); public static final String
	 * CLONE_WITH_CREDENTIALS = "CLONE WITH CREDENTIALS";
	 * 
	 * @RequestMapping(path = "/cloneWithCredentials") public @ResponseBody String
	 * cloneWithCredentials() { LOG.warn(CLONE_WITH_CREDENTIALS);
	 * 
	 * try { File local = File.createTempFile("criado2", ""); Path localPath =
	 * Paths.get(System.getProperty("java.io.tmpdir") + "/criado2");
	 * Git.gitClone(localPath,
	 * "https://USUARIO_AQUI:SENHA_AQUI@github.com/MatheusFerreira95/team-tracker.git"
	 * );
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } return
	 * CLONE_WITH_CREDENTIALS; }
	 */
}
