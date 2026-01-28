package mestrado.matheus.teamtracker.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import mestrado.matheus.teamtracker.domain.FileExtension;
import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;

/**
 * Serviço especializado em gerar recomendações e inteligência sobre o projeto.
 * Aplica heurísticas sobre as métricas calculadas para sugerir desenvolvedores.
 */
@Service
public class RecommendationService {

	private final ProjectService projectService;

	public RecommendationService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * Gera recomendações de desenvolvedores baseadas em extensões de arquivo.
	 * 
	 * @param filter Filtro do projeto
	 * @param project Objeto do projeto
	 * @param listFileExtension Lista de extensões para recomendar
	 * @param version Identificador da versão (V1, V2, etc)
	 * @return String formatada com as recomendações
	 * @throws IOException Erro de I/O no Git
	 * @throws InterruptedException Operação interrompida
	 */
	public String generateRecommendations(Filter filter, Project project, List<FileExtension> listFileExtension,
			String version) throws IOException, InterruptedException {

		StringBuilder result = new StringBuilder("<b>Recomendações " + version + ":</b><br>");
		for (FileExtension fileExtension : listFileExtension) {
			// Consulta o ProjectService para obter os dados necessários para a recomendação
			project.developerList = projectService.calculateLocCommitDeveloperList(project, "*" + fileExtension.extension,
					new ArrayList<>());

			if (!project.developerList.isEmpty()) {
				result.append("Para a extensão <b>").append(fileExtension.extension).append("</b> o desenvolvedor <b>")
						.append(project.developerList.get(0).name).append("</b> é o mais indicado.<br>");
			} else {
				result.append("Para a extensão <b>").append(fileExtension.extension)
						.append("</b> não há desenvolvedor indicado.<br>");
			}
		}
		return result.toString();
	}
}
