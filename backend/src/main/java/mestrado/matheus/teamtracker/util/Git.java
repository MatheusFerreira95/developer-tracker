package mestrado.matheus.teamtracker.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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

	public static Project clone(String remoteRepository, String checkout) {

		Project project = null;

		try {

			project = new Project(getLocalRepository(remoteRepository), checkout);

			runCommand(project, "git config --global credential.helper store", true);

			runCommand(project, "git clone " + remoteRepository, false);

			project.localRepository = project.localRepository += "/" + project.localRepository
					.substring(project.localRepository.lastIndexOf("/") + 1, project.localRepository.lastIndexOf("-"));

			runCheckout(project);

		} catch (IOException e) {

			e.printStackTrace();

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		return project;
	}

	public static GitOutput runCommand(Project project, String command, boolean showLog)
			throws IOException, InterruptedException {

		return run(project, command, null, showLog);
	}

	public static GitOutput runCommandReturnExitValue(Project project, String command)
			throws IOException, InterruptedException {

		Integer exit = 0;
		GitOutput gitoutput = run(project, command, exit, true);

		gitoutput.errorList.add("resultError: " + exit);

		return gitoutput;
	}

	private static GitOutput run(Project project, String command, Integer exitValue, boolean showLog)
			throws IOException, InterruptedException {

		GitOutput gitOutput = new GitOutput();

		validateLocalRepository(project.localRepository);

		List<String> commands = new ArrayList<String>();
		commands.add("/bin/sh");
		commands.add("-c");
		commands.add(command);

		String logCommand = showLog ? command : "command in execution not logged";
		System.out.println("run......................................." + logCommand);

		ProcessBuilder pb = new ProcessBuilder().command(commands).directory(new File(project.localRepository));
		Process p = pb.start();

		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", gitOutput);
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", gitOutput);
		outputGobbler.start();
		errorGobbler.start();

		int exit = p.waitFor();

		errorGobbler.join();
		outputGobbler.join();

		if (exit != 0 && exitValue == null) {

			throw new AssertionError(String.format("runCommand returned %d", exit));
		}

		exitValue = exit;

		return gitOutput;
	}

	public static GitOutput runGitTruckFactor(Project project) throws IOException, InterruptedException {
		System.out.println("Run TruckFactor");

		runCommand(project, "git clean --force", true);

		GitOutput gitOutput = new GitOutput();

		validateLocalRepository(project.localRepository);

		String pathApp = new File(".").getCanonicalPath();
		String pathTF = pathApp.equals("/") ? pathApp + "truckfactor-tool" : "backend/truckfactor-tool"; // tratando
																											// para
																											// imagens
																											// docker
																											// (ver
																											// cópia
																											// realizada
																											// no
																											// arquivo
																											// dockerfile)

		List<String> commands = new ArrayList<String>();
		commands.add("/bin/sh");
		commands.add("-c");
		String commandTF = "git config diff.renameLimit 999999 && git log --pretty=format:\"%H-;-%aN-;-%aE-;-%at-;-%cN-;-%cE-;-%ct-;-%f\"  > commitinfo.log && git log --name-status --pretty=format:\"commit	%H\" --find-renames > log.log && git ls-files > filelist.log && git config --unset diff.renameLimit";

		commands.add(commandTF);
		System.out.println(
				"Run TruckFactor 1 (on " + project.localRepository + ")................................." + commandTF);

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

		createCommitFileInfoLog(project.localRepository);

		commands = new ArrayList<String>();
		commands.add("/bin/sh");
		commands.add("-c");
		commandTF = "java -jar gittruckfactor.jar " + project.localRepository + " " + pathTF;
		commands.add(commandTF);
		System.out.println("Run TruckFactor 2................................." + commandTF);

		pb = new ProcessBuilder().command(commands).directory(new File(pathTF));
		p = pb.start();

		errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", gitOutput);
		outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", gitOutput);
		outputGobbler.start();
		errorGobbler.start();

		exit = p.waitFor();

		errorGobbler.join();
		outputGobbler.join();

		if (exit != 0) {

			throw new AssertionError(String.format("runCommand returned %d", exit));
		}

		return gitOutput;
	}

	private static void createCommitFileInfoLog(String localRepository) {

		try {
			FileReader reader = new FileReader(localRepository + "/log.log");
			BufferedReader bufferedReader = new BufferedReader(reader);

			FileWriter writer = new FileWriter(localRepository + "/commitfileinfo.log", true);

			String readedLine;
			String commitHash = "";
			String action = "";
			String filePath = "";

			while ((readedLine = bufferedReader.readLine()) != null) {

				if (readedLine.isEmpty())
					continue;

				if (readedLine.startsWith("commit")) {
					commitHash = readedLine.split("	")[1];
					continue;
				}

				action = readedLine.startsWith("A") ? "ADDED" : readedLine.startsWith("M") ? "MODIFIED" : "";

				if (action.isEmpty())
					continue;

				filePath = readedLine.split("	")[1];

				writer.write(commitHash + ";" + action + "; ;" + filePath);
				writer.write("\r\n");

			}

			reader.close();
			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		String cloneFolderPath = getCloneFolderPath();

		File localRepository = new File(
				cloneFolderPath + File.separator + nameLocalRepository + "-" + new Date().getTime());
		if (!localRepository.exists())
			localRepository.mkdir();

		return localRepository.getPath();

	}

	public static String getLocalRepositoryFromLocalProject() {

		String localFromLocal = "";

		try {

			String cloneFolderPath = getCloneFolderPath();

			File localRepository = new File(cloneFolderPath + File.separator + "local");
			if (!localRepository.exists())
				localRepository.mkdir();

			localFromLocal = localRepository.getPath();

		} catch (IOException e) {

			e.printStackTrace();

		}

		return localFromLocal;
	}

	private static String getCloneFolderPath() throws IOException {

		String currentUsersHomePath = System.getProperty("user.home");
		String cloneFolderPath = currentUsersHomePath + File.separator + "team-tracker-clones";

		File cloneFolder = new File(cloneFolderPath);
		if (!cloneFolder.exists()) {
			cloneFolder.mkdir();
		}

		return cloneFolderPath;

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

	public static void runCheckout(Project project) {

		try {

			GitOutput testCheckout = runCommandReturnExitValue(project, "git checkout -f " + project.checkout);

			if (testCheckout.errorList.get(testCheckout.errorList.size() - 1).equals("resultError: 0")) {
				System.err.println(".............................checkout error, default checkout used");
			}

			GitOutput currentVersionInfo1 = runCommand(project, "git log --pretty=format:'%d' -n 1", true);
			GitOutput currentVersionInfo2 = runCommand(project, "git log -n 1", true);

			project.currentVersion = currentVersionInfo1.outputList.get(0).split(",")[0].replace("(", "").replace(")",
					"") + " - " + currentVersionInfo2.outputList.get(0).split(" ")[1];

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
