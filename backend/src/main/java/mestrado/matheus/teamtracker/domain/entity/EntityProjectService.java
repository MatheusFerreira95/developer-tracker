package mestrado.matheus.teamtracker.domain.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import mestrado.matheus.teamtracker.domain.Project;

@Service
public class EntityProjectService {

	private static final Logger LOGGER = Logger.getLogger(EntityProjectService.class.getName());

	private final EntityProjectRepository repository;

	public EntityProjectService(EntityProjectRepository repository) {
		this.repository = repository;
	}

	public static Project getProject(String localRepository, String checkout, String remoteRepository) {

		Project project = null;

		if (localRepository == null || localRepository.isEmpty()) {
			// buscar por ele e retornar sem checkout
		}

		// fazer com checkout

		return project;
	}

	public static void saveProject(Project project) {

//		Project project;

//		repository.save(project);

	}
}
