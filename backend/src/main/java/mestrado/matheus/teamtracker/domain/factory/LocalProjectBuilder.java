package mestrado.matheus.teamtracker.domain.factory;

import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;
import mestrado.matheus.teamtracker.util.Git;

/**
 * Builder para projetos que já existem localmente em um caminho específico.
 * Usado quando filter.localRepository está preenchido.
 */
public class LocalProjectBuilder implements ProjectBuilder {

	@Override
	public Project build(Filter filter, String checkout) {
		System.out.println("info...................BuilderProject is checkouting (" + checkout + ") in local: "
				+ filter.localRepository);

		Project project = new Project(filter.localRepository, checkout);
		Git.runCheckout(project);

		return project;
	}
}
