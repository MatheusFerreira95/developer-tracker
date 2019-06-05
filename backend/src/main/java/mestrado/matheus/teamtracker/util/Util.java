package mestrado.matheus.teamtracker.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class Util {

	public static URI getLocalPath(String repositoryPath) throws IOException {

		String nameLocalRepository = repositoryPath.substring(repositoryPath.lastIndexOf("/"),
				repositoryPath.indexOf(".git"));

		File localRepository = File.createTempFile("clone-" + nameLocalRepository, "");

		return localRepository.toURI();
	}

}
