package org.jenkinsci.plugin;

import hudson.Plugin;
import hudson.model.Descriptor.FormException;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class PluginImpl extends Plugin {

    private int m_requiredSpaceInMb = 2048;

    public static PluginImpl getInstance() {
        return Jenkins.getInstance().getPlugin(PluginImpl.class);
    }

    @Override
    public void configure(StaplerRequest req, JSONObject formData) throws IOException, ServletException, FormException {
        formData = formData.getJSONObject("disk-check");
        m_requiredSpaceInMb = formData.getInt("requiredSpaceInMb");

        save();
        super.configure(req, formData);
    }

    public int getRequiredSpaceInMb() {
        return m_requiredSpaceInMb;
    }

    public void setRequiredSpaceInMb(int requiredSpaceInMb) {
        m_requiredSpaceInMb = requiredSpaceInMb;
    }
}
