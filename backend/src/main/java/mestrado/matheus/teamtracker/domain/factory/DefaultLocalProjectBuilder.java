package mestrado.matheus.teamtracker.domain.factory;

import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;
import mestrado.matheus.teamtracker.util.Git;

/**
 * Builder para projetos que usam o repositório local padrão do sistema.
 * Usado quando filter.remoteRepository.equals("[local]").
 * 
 * Este builder utiliza o caminho pré-configurado do sistema para projetos locais.
 */
public class DefaultLocalProjectBuilder implements ProjectBuilder {

	@Override
	public Project build(Filter filter, String checkout) {
		System.out.println("info...................BuilderProject is checkouting (" + checkout + ") in 100% local: "
				+ filter.localRepository);

		Project project = new Project(Git.getLocalRepositoryFromLocalProject(), checkout);
		// entre na pasta do seu projeto local, use 'docker cp .
		// nomeContainer:/root/team-tracker-clones/local/
		Git.runCheckout(project);

		return project;
	}
}
