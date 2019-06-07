package mestrado.matheus.teamtracker.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;

@RestController()
@RequestMapping("/project")
public class ProjectController {

	/**
	 * A partir do filtro deve retornar os dados de gerais do projeto (loc, commits,
	 * data de primeiro e último commit, dias ativos (qtd de dias com commit),
	 * linguagens de programaçao e Desenvolvedores)
	 **/
	@RequestMapping(path = "", method = RequestMethod.POST)
	public @ResponseBody Project getProjectOverview(@RequestBody Filter filter) {

		try {

			return Project.buildOverview(filter);

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
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody String getExploreProject(@RequestParam Filter filter) {

		return filter.toString();
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
