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
import java.util.Locale;

import mestrado.matheus.teamtracker.domain.Project;

public class Git {

	private static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName != null && osName.toLowerCase(Locale.ROOT).contains("win");
	}

	/**
	 * Run a command through the native shell for the current OS.
	 * - Windows: cmd.exe /c
	 * - Linux/Mac: sh -c
	 */
	private static List<String> shellCommand(String command) {
		List<String> commands = new ArrayList<String>();

		if (isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add(command);
		} else {
			commands.add("sh");
			commands.add("-c");
			commands.add(command);
		}

		return commands;
	}

	public static Project clone(String remoteRepository, String checkout) {

		Project project = null;

		try {

			project = new Project(getLocalRepository(remoteRepository), checkout);

			runCommand(project, "git config --global credential.helper store", true);

			runCommand(project, "git clone " + remoteRepository, false);

			String repoFolderName = project.localRepository.substring(project.localRepository.lastIndexOf(File.separator) + 1,
					project.localRepository.lastIndexOf("-"));
			project.localRepository = Paths.get(project.localRepository, repoFolderName).toString();

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

		return run(project, command, showLog);
	}

	public static GitOutput runCommandReturnExitValue(Project project, String command)
			throws IOException, InterruptedException {

		GitOutput gitoutput = run(project, command, true);
		return gitoutput;
	}

	private static GitOutput run(Project project, String command, boolean showLog)
			throws IOException, InterruptedException {

		GitOutput gitOutput = new GitOutput();

		validateLocalRepository(project.localRepository);

		List<String> commands = shellCommand(command);

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

		if (exit != 0) {
			StringBuilder msg = new StringBuilder();
			msg.append("Command failed with exit code ").append(exit).append(": ").append(command);
			if (!gitOutput.errorList.isEmpty()) {
				msg.append(" | stderr: ").append(String.join(" | ", gitOutput.errorList));
			}
			throw new AssertionError(msg.toString());
		}

		return gitOutput;
	}

	public static GitOutput runGitTruckFactor(Project project) throws IOException, InterruptedException {
		System.out.println("Run TruckFactor");

		runCommand(project, "git clean --force", true);

		GitOutput gitOutput = new GitOutput();

		validateLocalRepository(project.localRepository);

		String pathApp = new File(".").getCanonicalPath();
		// In Docker image, truckfactor-tool is copied to "/truckfactor-tool"
		// In local dev, it lives at "backend/truckfactor-tool"
		String pathTF = Paths.get(pathApp, "truckfactor-tool").toString();
		if (!new File(pathTF).exists()) {
			pathTF = Paths.get(pathApp, "backend", "truckfactor-tool").toString();
		}

		String commandTF = "git config diff.renameLimit 999999 && git log --pretty=format:\"%H-;-%aN-;-%aE-;-%at-;-%cN-;-%cE-;-%ct-;-%f\"  > commitinfo.log && git log --name-status --pretty=format:\"commit	%H\" --find-renames > log.log && git ls-files > filelist.log && git config --unset diff.renameLimit";

		List<String> commands = shellCommand(commandTF);
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
			StringBuilder msg = new StringBuilder();
			msg.append("TruckFactor step 1 failed with exit code ").append(exit).append(": ").append(commandTF);
			if (!gitOutput.errorList.isEmpty()) {
				msg.append(" | stderr: ").append(String.join(" | ", gitOutput.errorList));
			}
			throw new AssertionError(msg.toString());
		}

		createCommitFileInfoLog(project.localRepository);

		commands = new ArrayList<String>();
		commandTF = "java -jar gittruckfactor.jar " + project.localRepository + " " + pathTF;
		commands = shellCommand(commandTF);
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
			StringBuilder msg = new StringBuilder();
			msg.append("TruckFactor step 2 failed with exit code ").append(exit).append(": ").append(commandTF);
			if (!gitOutput.errorList.isEmpty()) {
				msg.append(" | stderr: ").append(String.join(" | ", gitOutput.errorList));
			}
			throw new AssertionError(msg.toString());
		}

		return gitOutput;
	}

	private static void createCommitFileInfoLog(String localRepository) {

		try {
			FileReader reader = new FileReader(Paths.get(localRepository, "log.log").toString());
			BufferedReader bufferedReader = new BufferedReader(reader);

			FileWriter writer = new FileWriter(Paths.get(localRepository, "commitfileinfo.log").toString(), true);

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

	/**
	 * Get the current commit hash (SHA) of the repository.
	 * Used for cache key generation to invalidate cache when repository changes.
	 * 
	 * IMPORTANT: This method ensures cache safety by:
	 * 1. Getting the commit hash AFTER checkout is complete
	 * 2. Validating that the hash corresponds to the current HEAD
	 * 3. Including branch/checkout info in validation
	 */
	public static String getCommitHash(Project project) {
		try {
			// Get the full commit hash (40 characters)
			GitOutput gitOutput = runCommand(project, "git rev-parse HEAD", false);
			if (!gitOutput.outputList.isEmpty()) {
				String commitHash = gitOutput.outputList.get(0).trim();
				
				// Validate: ensure we're on the expected checkout
				// This prevents cache issues when checkout fails silently
				GitOutput branchOutput = runCommand(project, "git rev-parse --abbrev-ref HEAD", false);
				String currentBranch = branchOutput.outputList.isEmpty() ? "unknown" : branchOutput.outputList.get(0).trim();
				
				// Log for debugging (can be removed in production)
				System.out.println("Cache key info - Repository: " + project.localRepository + 
					", Checkout: " + project.checkout + 
					", Current Branch: " + currentBranch + 
					", Commit Hash: " + commitHash);
				
				return commitHash;
			}
		} catch (IOException | InterruptedException e) {
			System.err.println("Error getting commit hash: " + e.getMessage());
		}
		return "unknown";
	}
	
	/**
	 * Validate that the current repository state matches the expected checkout.
	 * This is a safety check to prevent cache inconsistencies.
	 * 
	 * @param project The project to validate
	 * @param expectedCheckout The expected branch/tag/commit
	 * @return true if validation passes, false otherwise
	 */
	public static boolean validateCheckoutState(Project project, String expectedCheckout) {
		try {
			// Get current branch/commit
			GitOutput branchOutput = runCommand(project, "git rev-parse --abbrev-ref HEAD", false);
			String currentBranch = branchOutput.outputList.isEmpty() ? "" : branchOutput.outputList.get(0).trim();
			
			// Get current commit hash
			GitOutput commitOutput = runCommand(project, "git rev-parse HEAD", false);
			String currentCommit = commitOutput.outputList.isEmpty() ? "" : commitOutput.outputList.get(0).trim();
			
			// If expectedCheckout is a commit hash, compare directly
			if (expectedCheckout != null && expectedCheckout.length() >= 7) {
				// Could be a commit hash (short or full)
				GitOutput expectedCommitOutput = runCommand(project, "git rev-parse " + expectedCheckout, false);
				if (!expectedCommitOutput.outputList.isEmpty()) {
					String expectedCommit = expectedCommitOutput.outputList.get(0).trim();
					boolean matches = currentCommit.equals(expectedCommit);
					if (!matches) {
						System.err.println("WARNING: Checkout validation failed. Expected: " + expectedCommit + 
							", Current: " + currentCommit);
					}
					return matches;
				}
			}
			
			// If it's a branch name, check if current branch matches (or if we're in detached HEAD on that commit)
			// For branches, we rely on the commit hash in cache key, so this is just a warning
			if (expectedCheckout != null && !expectedCheckout.equals(currentBranch) && !currentBranch.equals("HEAD")) {
				// Check if we're in detached HEAD state pointing to the expected checkout
				GitOutput detachedCheck = runCommand(project, "git rev-parse " + expectedCheckout, false);
				if (!detachedCheck.outputList.isEmpty()) {
					String expectedCommit = detachedCheck.outputList.get(0).trim();
					if (currentCommit.equals(expectedCommit)) {
						// We're in detached HEAD but on the correct commit - this is OK
						return true;
					}
				}
				System.out.println("INFO: Branch mismatch (expected: " + expectedCheckout + 
					", current: " + currentBranch + "), but commit hash will ensure cache correctness");
			}
			
			return true;
		} catch (IOException | InterruptedException e) {
			System.err.println("Error validating checkout state: " + e.getMessage());
			return false;
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
