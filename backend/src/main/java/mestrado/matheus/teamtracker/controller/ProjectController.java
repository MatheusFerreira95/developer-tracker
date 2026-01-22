package mestrado.matheus.teamtracker.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mestrado.matheus.teamtracker.domain.Explore;
import mestrado.matheus.teamtracker.domain.ExploreVersions;
import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;
import mestrado.matheus.teamtracker.domain.ProjectVersions;

@RestController
@RequestMapping("/api/project")
@CrossOrigin(origins = "http://localhost:8081")
public class ProjectController {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

	/**
	 * A partir do filtro deve retornar os dados gerais do projeto (loc, commits,
	 * data de primeiro e último commit, dias ativos (qtd de dias com commit),
	 * linguagens de programação e Desenvolvedores)
	 */
	@PostMapping
	public ResponseEntity<ProjectVersions> getProjectOverview(@RequestBody Filter filter) {
		try {
			validateFilter(filter);

			Project projectVersion1 = Project.buildOverview(filter, filter.checkout1);
			Project projectVersion2 = null;

			if (hasSecondCheckout(filter)) {
				filter.localRepository = projectVersion1.localRepository;
				projectVersion2 = Project.buildOverview(filter, filter.checkout2);
			}

			return ResponseEntity.ok(new ProjectVersions(projectVersion1, projectVersion2));

		} catch (IOException e) {
			LOG.error("Erro de I/O ao processar overview do projeto", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

		} catch (InterruptedException e) {
			LOG.error("Operação interrompida ao processar overview do projeto", e);
			Thread.currentThread().interrupt();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

		} catch (IllegalArgumentException e) {
			LOG.warn("Filtro inválido: {}", e.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * A partir do filtro deve retornar os dados de exploração do projeto (relação
	 * de desenvolvedores com artefatos)
	 */
	@PostMapping("/explore")
	public ResponseEntity<ExploreVersions> getExploreProject(@RequestBody Filter filter) {
		try {
			validateFilter(filter);

			String originalZoomPath = filter.zoomPath != null ? filter.zoomPath : "./";
			Project projectVersion1 = Project.builderProject(filter, filter.checkout1);
			Explore explore1 = Explore.build(filter, projectVersion1, filter.devTFListV1);

			Explore explore2 = null;
			if (hasSecondCheckout(filter)) {
				filter.zoomPath = originalZoomPath;
				Project projectVersion2 = Project.builderProject(filter, filter.checkout2);
				explore2 = Explore.build(filter, projectVersion2, filter.devTFListV2);
			}

			return ResponseEntity.ok(new ExploreVersions(explore1, explore2));

		} catch (IOException e) {
			LOG.error("Erro de I/O ao processar exploração do projeto", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

		} catch (InterruptedException e) {
			LOG.error("Operação interrompida ao processar exploração do projeto", e);
			Thread.currentThread().interrupt();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

		} catch (IllegalArgumentException e) {
			LOG.warn("Filtro inválido: {}", e.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * A partir da lista de extensões de arquivo deve retornar o desenvolvedor mais
	 * indicado para trabalhar no tipo de arquivo em questão
	 */
	@PostMapping("/recomendationByFileExtension")
	public ResponseEntity<String> getRecommendationByFileExtension(@RequestBody Filter filter) {
		try {
			validateFilter(filter);

			Project projectVersion1 = Project.builderProject(filter, filter.checkout1);
			String recommendationsV1 = Explore.generateRecommendations(filter, projectVersion1,
					filter.extensionListVersion1, "V1");

			String recommendationsV2 = null;
			if (hasSecondCheckout(filter)) {
				Project projectVersion2 = Project.builderProject(filter, filter.checkout2);
				recommendationsV2 = Explore.generateRecommendations(filter, projectVersion2,
						filter.extensionListVersion2, "V2");
			}

			String result = recommendationsV1 + (recommendationsV2 != null ? "<br>" + recommendationsV2 : "");
			return ResponseEntity.ok(result);

		} catch (IOException e) {
			LOG.error("Erro de I/O ao gerar recomendações por extensão de arquivo", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

		} catch (InterruptedException e) {
			LOG.error("Operação interrompida ao gerar recomendações por extensão de arquivo", e);
			Thread.currentThread().interrupt();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

		} catch (IllegalArgumentException e) {
			LOG.warn("Filtro inválido: {}", e.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Valida se o filtro possui os dados mínimos necessários.
	 * Nota: checkout1 pode ser vazio/null, pois será preenchido com "master" como padrão na classe Project.
	 */
	private void validateFilter(Filter filter) {
		if (filter == null) {
			throw new IllegalArgumentException("Filtro não pode ser nulo");
		}
	}

	/**
	 * Verifica se existe um segundo checkout configurado
	 */
	private boolean hasSecondCheckout(Filter filter) {
		return filter.checkout2 != null && !filter.checkout2.trim().isEmpty();
	}
}
