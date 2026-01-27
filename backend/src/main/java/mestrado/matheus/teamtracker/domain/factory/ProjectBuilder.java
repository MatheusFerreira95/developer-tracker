package mestrado.matheus.teamtracker.domain.factory;

import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;

/**
 * Interface para builders de Project seguindo o padrão Strategy/Factory.
 * Permite diferentes estratégias de construção de projetos sem modificar
 * código existente (Open/Closed Principle).
 */
public interface ProjectBuilder {
	
	/**
	 * Constrói um Project baseado no Filter fornecido.
	 * 
	 * @param filter Filtro contendo informações sobre o repositório
	 * @param checkout Branch ou tag para checkout
	 * @return Project construído e com checkout realizado
	 */
	Project build(Filter filter, String checkout);
}
