package org.jenkinsci.plugin;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Computer;
import hudson.tasks.BuildWrapper;
import hudson.node_monitors.*;
import hudson.model.Node;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;

import hudson.node_monitors.DiskSpaceMonitorDescriptor;

public class Diskcheck extends BuildWrapper {

	public final boolean failOnError;

	@DataBoundConstructor
	public Diskcheck(boolean failOnError) {
		this.failOnError = failOnError;
	}
	
	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		return new NoopEnv();
	}

	@Override
	public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		PrintStream log = listener.getLogger();

		int spaceThreshold = PluginImpl.getInstance().getSpacecheck();

		log.println("[DiskCheck] Disk space threshold is set to: " + spaceThreshold + "Gb");
		log.println("[DiskCheck] Checking disk space now...");

		// Ensure the workspace exists.
		if (!build.getWorkspace().exists()) {
			build.getWorkspace().mkdirs();
		}

		Node node = build.getBuiltOn();
		Computer computer = node.toComputer();
		String nodeName = build.getBuiltOnStr();
		if (nodeName == null || nodeName == "") {
			nodeName = "master";
		}
		
		int roundedFreeSpaceInGb = 0;
		try 
		{
			if (computer != null) {

				if (DiskSpaceMonitor.DESCRIPTOR.get(computer) == null) {
					log.println("[DiskCheck] No slave data available trying to get data from slave");
					Thread.sleep(1000);

					if (DiskSpaceMonitor.DESCRIPTOR.get(computer) == null) {
						log.println("[DiskCheck] Could not get slave information, exiting disk check for this slave");
						return;
					}
				}

				AbstractDiskSpaceMonitor.getAll();

				long size = DiskSpaceMonitor.DESCRIPTOR.get(computer).size;
				String baseName = build.getWorkspace().getBaseName();
				
				DiskSpaceMonitorDescriptor.DiskSpace diskSpaceMonitor = new DiskSpaceMonitorDescriptor.DiskSpace(baseName, size);
				String diskSpace = diskSpaceMonitor.getGbLeft();
				roundedFreeSpaceInGb = (int) (Double.parseDouble(diskSpace));
			}
		}
		catch (NullPointerException e){
			log.println("Could not get slave disk size information, exiting disk check for this slave");
			return;
		}
		
		log.println("Total disk space in " + nodeName + "'s workspace is: " + roundedFreeSpaceInGb + "Gb");
		
		if (roundedFreeSpaceInGb < spaceThreshold) {
			throw new AbortException("Disk Space is too low please look into it before starting a build");
		}
	}


	@Extension
	public static final class DescriptorImpl extends Descriptor<BuildWrapper> {
		public String getDisplayName() {
			return "Enable Disk Check";
		}

		public DescriptorImpl() {
			load();
		}
	}

	class NoopEnv extends Environment {
	}
}
