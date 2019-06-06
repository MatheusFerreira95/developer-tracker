package mestrado.matheus.teamtracker.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import mestrado.matheus.teamtracker.domain.Filter;
import mestrado.matheus.teamtracker.domain.Project;

public class Git {

	public List<String> outputList;
	public List<String> errorList;
	public Project project;

	public Git(String remoteRepository) throws IOException, InterruptedException {

		this.project = new Project(this.getLocalPath(remoteRepository));

		runCommand("git", "clone", remoteRepository);
	}

	public Git() {

		this.outputList = new ArrayList<String>();
		this.errorList = new ArrayList<String>();
	}

	public static Git gitBuilder(Filter filter) {

		Git git = null;

		if (filter.localRepository != null && !filter.localRepository.isEmpty()) {

			git = new Git();

			git.project = new Project(filter.localRepository);

			return git;
		}

		try {

			git = new Git(filter.remoteRepository);

		} catch (IOException e) {

			e.printStackTrace();

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		return git;
	}

	public void runCommand(String... command) throws IOException, InterruptedException {

		this.validateLocalRepository();

		ProcessBuilder pb = new ProcessBuilder().command(command).directory(new File(project.localRepository));
		Process p = pb.start();

		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", this);
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", this);
		outputGobbler.start();
		errorGobbler.start();

		int exit = p.waitFor();

		errorGobbler.join();
		outputGobbler.join();

		if (exit != 0) {

			throw new AssertionError(String.format("runCommand returned %d", exit));
		}
	}

	private void validateLocalRepository() {

		Path pathLocalRepository = project.getPathLocalRepository();

		Objects.requireNonNull(pathLocalRepository, "directory");

		if (!Files.exists(pathLocalRepository)) {

			throw new RuntimeException("can't run command in non-existing directory '" + pathLocalRepository + "'");

		}
	}

	private String getLocalPath(String repositoryPath) throws IOException {

		String nameLocalRepository = repositoryPath.substring(repositoryPath.lastIndexOf("/"),
				repositoryPath.indexOf(".git"));

		File cloneFolder = new File("clones");
		if (!cloneFolder.exists())
			cloneFolder.mkdir();

		File localRepository = new File("clones/" + nameLocalRepository + "-" + new Date().getTime());
		if (!localRepository.exists())
			localRepository.mkdir();

		return localRepository.getPath();

	}

	private static class StreamGobbler extends Thread {

		private final InputStream is;
		private final String type;
		private final Git gitInstance;

		private StreamGobbler(InputStream is, String type, Git gitInstance) {
			this.is = is;
			this.type = type;
			this.gitInstance = gitInstance;
		}

		@Override
		public void run() {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
				String line;
				while ((line = br.readLine()) != null) {

					if (type == "ERROR")
						this.gitInstance.errorList.add(line);
					if (type == "OUTPUT")
						this.gitInstance.outputList.add(line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
