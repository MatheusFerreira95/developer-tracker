package mestrado.matheus.teamtracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import mestrado.matheus.teamtracker.domain.Filter;

@RestController()
@RequestMapping("/project")
public class ProjectController {


	@RequestMapping(path = "/", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody String addNewUser(@RequestParam Filter filter) {

		return filter.toString();
	}
	
	/*
	private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
	public static final String CLONE_WITH_CREDENTIALS = "CLONE WITH CREDENTIALS";

	@RequestMapping(path = "/cloneWithCredentials")
	public @ResponseBody String cloneWithCredentials() {
		LOG.warn(CLONE_WITH_CREDENTIALS);

		try {
			File local = File.createTempFile("criado2", "");
			Path localPath = Paths.get(System.getProperty("java.io.tmpdir") + "/criado2");
			Git.gitClone(localPath, "https://USUARIO_AQUI:SENHA_AQUI@github.com/MatheusFerreira95/team-tracker.git");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return CLONE_WITH_CREDENTIALS;
	}
*/
}
