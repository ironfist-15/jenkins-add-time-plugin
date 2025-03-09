package io.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Extension
public class GitCommitTimestamp extends Recorder {

    @DataBoundConstructor
    public GitCommitTimestamp() {
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        String workspacePath = Optional.ofNullable(build.getWorkspace())
                .map(FilePath::getRemote)
                .orElse(null);

        if (null == workspacePath) {
            listener.getLogger().println("Workspace is null. Aborting.");
            return false;
        }
        File workspace = new File(workspacePath);

        // Get the last commit message
        ProcessBuilder getMessage = new ProcessBuilder("git", "log", "-1", "--pretty=%B");
        getMessage.directory(workspace);
        Process process = getMessage.start();

        // Read output safely
        String commitMessage = readProcessOutput(process).trim();
        process.waitFor();

        // Append timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String newCommitMessage = commitMessage + " @" + timestamp;

        // Commit with modified message
        ProcessBuilder commit = new ProcessBuilder("git", "commit", "--amend", "-m", newCommitMessage);
        commit.directory(workspace);
        Process commitProcess = commit.start();
        commitProcess.waitFor();

        listener.getLogger().println("Updated commit message: " + newCommitMessage);
        return true;
    }

    // Helper method to read process output
    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder output = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Append Timestamp to Git Commit";
        }
    }
}
