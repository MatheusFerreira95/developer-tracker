package mestrado.matheus.teamtracker.domain.factory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;
import mestrado.matheus.teamtracker.util.Git;

/**
 * Builder para projetos que precisam ser clonados de um repositório remoto.
 * Usado quando filter.remoteRepository está preenchido e não é "[local]".
 */
public class RemoteProjectBuilder implements ProjectBuilder {

	@Override
	public Project build(Filter filter, String checkout) {
		System.out.println("info...................BuilderProject is clonning from: " + filter.remoteRepository);

		if (filter.user != null && !filter.user.isEmpty() && filter.password != null
				&& !filter.password.isEmpty()) {

			return Git.clone("https://" + encodeValue(filter.user) + ":" + encodeValue(filter.password) + "@"
					+ filter.remoteRepository.substring(8), checkout);
		}

		return Git.clone(filter.remoteRepository, checkout);
	}

	private String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}
}
