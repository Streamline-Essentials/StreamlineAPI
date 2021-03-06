package net.streamline.api.savables.users;

import de.leonhard.storage.internal.FlatFile;
import lombok.Getter;
import lombok.Setter;
import net.streamline.base.Streamline;
import net.streamline.api.configs.FlatFileResource;
import net.streamline.api.configs.StorageUtils;
import net.streamline.base.configs.MainMessagesHandler;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.UserManager;

import java.util.List;

public abstract class SavableUser extends SavableResource {
    private final SavableUser savableUser;
    public String latestName;
    public String displayName;
    public List<String> tagList;
    public double points;
    public String lastMessage;
    public boolean online;
    public String latestServer;
    @Getter @Setter
    private boolean bypassPermissions;

    public SavableUser asSavableUser() {
        return this.savableUser;
    }

    public boolean updateOnline() {
        if (uuid.equals("%")) this.online = false;

        this.online = UserManager.isOnline(this.uuid);
        return this.online;
    }

    public SavableUser(String uuid) {
        super(uuid, UserManager.newStorageResource(uuid, uuid.equals("%") ? SavableConsole.class : SavablePlayer.class));

        this.savableUser = this;
    }

    @Override
    public void populateDefaults() {
        // Profile.
        String username = UserManager.getUsername(this.uuid);
        latestName = getOrSetDefault("profile.latest.name", username == null ? "null" : username);
        latestServer = getOrSetDefault("profile.latest.server", MainMessagesHandler.MESSAGES.DEFAULTS.IS_NULL.get());
        displayName = getOrSetDefault("profile.display-name", latestName);
        tagList = getOrSetDefault("profile.tags", getTagsFromConfig());
        points = getOrSetDefault("profile.points", Streamline.getMainConfig().userCombinedPointsDefault());

        populateMoreDefaults();
    }

    abstract public List<String> getTagsFromConfig();

    abstract public void populateMoreDefaults();

    @Override
    public void loadValues() {
        // Profile.
        latestName = getOrSetDefault("profile.latest.name", latestName);
        latestServer = getOrSetDefault("profile.latest.server", latestServer);
        displayName = getOrSetDefault("profile.display-name", displayName);
        tagList = getOrSetDefault("profile.tags", tagList);
        points = getOrSetDefault("profile.points", points);
        // Online.
        online = updateOnline();
        // More.
        loadMoreValues();
    }

    abstract public void loadMoreValues();

    public void saveAll() {
        // Profile.
        set("profile.latest.name", latestName);
        set("profile.latest.server", latestServer);
        set("profile.display-name", latestName);
        set("profile.tags", tagList);
        set("profile.points", points);
        // More.
        saveMore();
        storageResource.push();
    }

    abstract public void saveMore();

    public void addTag(String tag) {
        //        loadValues();
        if (tagList.contains(tag)) return;

        tagList.add(tag);
        //        saveAll();
    }

    public void removeTag(String tag) {
        //        loadValues();
        if (! tagList.contains(tag)) return;

        tagList.remove(tag);
        //        saveAll();
    }

    public void setPoints(double amount) {
        points = amount;
    }

    public void addPoints(double amount) {
        setPoints(points + amount);
    }

    public void removePoints(double amount) {
        setPoints(points - amount);
    }

    public void updateLastMessage(String message) {
        lastMessage = message;
    }

    public void setLatestServer(String server) {
        latestServer = server;
    }

    public void setLatestName(String name) {
        latestName = name;
    }

    public void setDisplayName(String name) {
        displayName = name;
    }

    public String getName() {
        return latestName;
    }

    public void dispose() throws Throwable {
        try {
            UserManager.unloadUser(this);
            this.uuid = null;
            if (StorageUtils.areUsersFlatFiles()) {
                FlatFileResource<? extends FlatFile> resource = (FlatFileResource<? extends FlatFile>) this.storageResource;
                resource.delete();
            }
        } finally {
            super.finalize();
        }
    }
}
