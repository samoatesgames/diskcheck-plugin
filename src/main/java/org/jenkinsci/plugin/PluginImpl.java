package org.jenkinsci.plugin;

import hudson.Plugin;
import hudson.model.Descriptor.FormException;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class PluginImpl extends Plugin {
	private int spacecheck = 2;
	
    public static PluginImpl getInstance() {
        return Jenkins.getInstance().getPlugin(PluginImpl.class);
    }
	
	@Override
	public void configure(StaplerRequest req, JSONObject formData) throws IOException, ServletException, FormException {
		formData = formData.getJSONObject("disk-check");
		spacecheck = formData.getInt("spacecheck");
		
		save();
		super.configure(req, formData);
	}
		
	public int getSpacecheck() {
		return spacecheck;
	}
	
	public void setSpacecheck(int spaceheck) {
		this.spacecheck = spacecheck;
	}
}