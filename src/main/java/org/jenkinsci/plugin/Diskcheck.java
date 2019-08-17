package org.jenkinsci.plugin;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import hudson.model.Node;
import hudson.slaves.OfflineCause;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.IOException;
import java.io.PrintStream;
import jenkins.model.Jenkins;
import org.jvnet.localizer.Localizable;

public class Diskcheck extends BuildWrapper {

    @DataBoundConstructor
    public Diskcheck() {
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        return new NoopEnv();
    }

    @Override
    public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        PrintStream log = listener.getLogger();

        FilePath workspace = build.getWorkspace();
        if (workspace == null) {
            return; // No valid workspace, no way for us to validate.
        }
        
        Node node = build.getBuiltOn();
        if (node == null) {
            return; // No valid node, no way for us to validate.
        }
        
        int requiredSpaceInMb = PluginImpl.getInstance().getRequiredSpaceInMb();

        log.println("[DiskCheck] Disk space threshold is set to: " + requiredSpaceInMb + "Mb");
        log.println("[DiskCheck] Checking disk space...");

        // Ensure the workspace exists.
        if (!workspace.exists()) {
            workspace.mkdirs();
        }
        
        String nodeName = node.getNodeName();
        if (nodeName.equals("")) {
            nodeName = "master";
        }

        long roundedFreeSpaceInBytes = workspace.getUsableDiskSpace();
        long roundedFreeSpaceInKb = roundedFreeSpaceInBytes / (long)1024;
        long roundedFreeSpaceInMb = roundedFreeSpaceInKb / (long)1024;
        
        log.println("[DiskCheck] Total disk space in " + nodeName + "'s workspace is: " + roundedFreeSpaceInMb + "Mb");

        if (roundedFreeSpaceInMb < requiredSpaceInMb) {
            Computer computer = node.toComputer();
            if (computer != null) {
                Localizable message = hudson.slaves.Messages._SlaveComputer_DisconnectedBy(
                        "[DiskCheck Plugin]", 
                        " : Disk Space is less than " + requiredSpaceInMb + "Mb"
                );
                computer.setTemporarilyOffline(true, OfflineCause.create(message));
            }
            throw new AbortException("[DiskCheck] Disk Space is too low please look into it before starting a build");
        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {

        @Override
        public String getDisplayName() {
            return "Enable disk check";
        }

        public DescriptorImpl() {
            load();
        }
    }

    class NoopEnv extends Environment {
    }
}
