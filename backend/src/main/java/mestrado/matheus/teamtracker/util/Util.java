package mestrado.matheus.teamtracker.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class Util {

	public static Path getLocalPath(String repositoryPath) throws IOException {

		String nameLocalRepository = repositoryPath.substring(repositoryPath.lastIndexOf("/"),
				repositoryPath.indexOf(".git"));
		
		File cloneFolder = new File("clones");
		if (!cloneFolder.exists()) cloneFolder.mkdir();
		
		File localRepository = new File("clones/" + nameLocalRepository + "-" + new Date().getTime());
		if (!localRepository.exists()) localRepository.mkdir();
		
		
		return Paths.get(localRepository.toURI());

	}

}
