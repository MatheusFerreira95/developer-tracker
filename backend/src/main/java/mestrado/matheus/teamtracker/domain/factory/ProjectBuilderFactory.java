package mestrado.matheus.teamtracker.domain.factory;

import mestrado.matheus.teamtracker.domain.Filter;

/**
 * Factory para criar o ProjectBuilder apropriado baseado no Filter.
 * Implementa o padrão Factory Method do GoF.
 * 
 * Este padrão permite:
 * - Adicionar novos tipos de builders sem modificar código existente (OCP)
 * - Centralizar a lógica de seleção do builder
 * - Facilitar testes unitários através de injeção de dependência
 */
public class ProjectBuilderFactory {

	/**
	 * Cria o ProjectBuilder apropriado baseado nas propriedades do Filter.
	 * 
	 * @param filter Filtro contendo informações sobre o repositório
	 * @return ProjectBuilder apropriado para o tipo de projeto
	 * @throws IllegalArgumentException se nenhum builder apropriado for encontrado
	 */
	public static ProjectBuilder createBuilder(Filter filter) {
		if (filter == null) {
			throw new IllegalArgumentException("Filter não pode ser nulo");
		}

		// Prioridade 1: Projeto usando repositório local padrão do sistema
		if (filter.remoteRepository != null && filter.remoteRepository.equals("[local]")) {
			return new DefaultLocalProjectBuilder();
		}

		// Prioridade 2: Projeto local existente em caminho específico
		if (filter.localRepository != null && !filter.localRepository.isEmpty()) {
			return new LocalProjectBuilder();
		}

		// Prioridade 3: Clonar de repositório remoto
		if (filter.remoteRepository != null && !filter.remoteRepository.isEmpty()) {
			return new RemoteProjectBuilder();
		}

		// Nenhuma condição satisfeita
		throw new IllegalArgumentException(
				"Não foi possível determinar o tipo de projeto. "
				+ "Forneça localRepository ou remoteRepository válido.");
	}
}
