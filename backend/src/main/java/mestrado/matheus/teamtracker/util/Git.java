package mestrado.matheus.teamtracker.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import mestrado.matheus.teamtracker.domain.Project;

public class Git {

	public static Project clone(String remoteRepository) {

		Project project = null;

		try {

			project = new Project(getLocalRepository(remoteRepository));

			runCommand(project, "git clone " + remoteRepository);

			project.localRepository = project.localRepository += "/" + project.localRepository
					.substring(project.localRepository.lastIndexOf("/") + 1, project.localRepository.lastIndexOf("-"));

		} catch (IOException e) {

			e.printStackTrace();

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		return project;
	}

	public static GitOutput runCommand(Project project, String command) throws IOException, InterruptedException {

		GitOutput gitOutput = new GitOutput();

		validateLocalRepository(project.localRepository);

		List<String> commands = new ArrayList<String>();
		commands.add("/bin/sh");
		commands.add("-c");
		commands.add(command);

		ProcessBuilder pb = new ProcessBuilder().command(commands).directory(new File(project.localRepository));
		Process p = pb.start();

		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", gitOutput);
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", gitOutput);
		outputGobbler.start();
		errorGobbler.start();

		int exit = p.waitFor();

		errorGobbler.join();
		outputGobbler.join();

		if (exit != 0) {

			throw new AssertionError(String.format("runCommand returned %d", exit));
		}

		return gitOutput;
	}

	public static void runCommand(Project project, String command, File outputFile)
			throws IOException, InterruptedException {

		GitOutput gitOutput = new GitOutput();

		List<String> commands = new ArrayList<String>();
		commands.add("/bin/sh");
		commands.add("-c");
		commands.add(command);

		validateLocalRepository(project.localRepository);

		ProcessBuilder pb = new ProcessBuilder().command(commands).directory(new File(project.localRepository));
		Process p = pb.start();

		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = new String();
		Boolean ok = true;
		try {
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				ok = line.endsWith("0") ? Boolean.valueOf(true) : Boolean.valueOf(false);
			}
			if (ok.booleanValue()) {
				System.out.println("================fim");

			} else {
				System.out.println("================erro");
			}
		} catch (IOException e2) {
			System.out.println("================erro mais ainda");

		}

	}
	

	public static void runtime(Project project, String command, File outputFile)
			throws IOException, InterruptedException {

		GitOutput gitOutput = new GitOutput();

		List<String> commands = new ArrayList<String>();
		commands.add("/bin/sh");
		commands.add("-c");
		commands.add(command);

		validateLocalRepository(project.localRepository);
		
		ProcessBuilder builder = new ProcessBuilder(commands).directory(new File(project.localRepository));
		builder.redirectErrorStream(true);
		final Process process = builder.start();

		// Watch the process
		watch(process);

	}
	
	private static void watch(final Process process) {
	    new Thread() {
	        public void run() {
	            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            String line = null; 
	            try {
	                while ((line = input.readLine()) != null) {
	                    System.out.println("+++" + line);
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }.start();
	}

	private static void validateLocalRepository(String localRepository) {

		Path pathLocalRepository = Paths.get(localRepository);

		Objects.requireNonNull(pathLocalRepository, "directory");

		if (!Files.exists(pathLocalRepository)) {

			throw new RuntimeException("can't run command in non-existing directory '" + pathLocalRepository + "'");

		}
	}

	private static String getLocalRepository(String remoteRepository) throws IOException {

		String nameLocalRepository = remoteRepository.substring(remoteRepository.lastIndexOf("/"),
				remoteRepository.indexOf(".git"));

		File cloneFolder = new File("/home/team-tracker-clones");
		if (!cloneFolder.exists()) {
			System.out.println("pasta de clones não existe");
			throw new RuntimeException();
		}

		File localRepository = new File(
				"/home/team-tracker-clones/" + nameLocalRepository + "-" + new Date().getTime());
		if (!localRepository.exists())
			localRepository.mkdir();

		return localRepository.getPath();

	}

	private static class StreamGobbler extends Thread {

		private final InputStream is;
		private final String type;
		private final GitOutput gitOutput;

		private StreamGobbler(InputStream is, String type, GitOutput gitOutput) {
			this.is = is;
			this.type = type;
			this.gitOutput = gitOutput;
		}

		@Override
		public void run() {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
				String line;
				while ((line = br.readLine()) != null) {

					if (type == "ERROR")
						this.gitOutput.errorList.add(line);
					if (type == "OUTPUT")
						this.gitOutput.outputList.add(line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
