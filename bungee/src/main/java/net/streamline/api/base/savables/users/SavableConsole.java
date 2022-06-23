package net.streamline.api.base.savables.users;

import net.md_5.bungee.api.ProxyServer;
import net.streamline.api.base.Streamline;

import java.util.ArrayList;
import java.util.List;

public class SavableConsole extends SavableUser {
    public ProxyServer server;

    public List<String> savedKeys = new ArrayList<>();

    public SavableConsole() {
        super("%");

        if (this.uuid == null) return;
        if (this.uuid.equals("")) return;

        this.server = Streamline.getInstance().getProxy();
    }

    @Override
    public List<String> getTagsFromConfig(){
        return Streamline.getMainConfig().userConsoleDefaultTags();
    }

    @Override
    public void populateMoreDefaults() {
        latestName = getOrSetDefault("profile.latest.name", Streamline.getMainConfig().userConsoleNameRegular());
        displayName = getOrSetDefault("profile.display-name", Streamline.getMainConfig().userConsoleNameFormatted());
    }

    @Override
    public void loadMoreValues() {

    }

    @Override
    public void saveMore() {

    }
}
