package net.streamline.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.streamline.api.command.*;
import net.streamline.api.scheduler.SimpleScheduler;
import net.streamline.base.Streamline;
import net.streamline.base.configs.MainConfigHandler;
import net.streamline.base.configs.MainMessagesHandler;
import net.streamline.api.entities.IPlayer;
import net.streamline.api.entities.Player;
import net.streamline.api.help.HelpMap;
import net.streamline.api.modules.ServicesManager;
import net.streamline.api.modules.SimpleModuleManager;
import net.streamline.api.placeholder.RATAPI;
import net.streamline.api.savables.UserManager;
import net.streamline.api.savables.users.SavableConsole;
import net.streamline.api.savables.users.SavablePlayer;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.api.scheduler.StreamlineScheduler;
import net.streamline.base.listeners.BaseListener;
import net.streamline.utils.MessagingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public abstract class BasePlugin extends Plugin implements IPlugin {
    TreeMap<String, ModuleCommand> loadedCommands = new TreeMap<>();

    static String name;
    static String version;
    static BasePlugin instance;
    static RATAPI ratapi;
    static File userFolder;
    static File moduleFolder;
    static File updateFolder;
    static File mainCommandsFolder;
    static String commandsFolderChild = "commands" + File.separator;
    static MainConfigHandler mainConfigHandler;
    static MainMessagesHandler mainMessagesHandler;
    static SimpleModuleManager moduleManager;
    static LuckPerms luckPerms;
    static UnsafeValues unsafeValues;
    static Warning.WarningState warningState;
    static StreamlineScheduler scheduler;
    static ServicesManager servicesManager;
    static HelpMap helpMap;

    @Override
    public void onEnable() {
        name = "StreamlineAPI";
        version = "${project.version}";
        instance = this;

        ratapi = new RATAPI();
        mainConfigHandler = new MainConfigHandler();
        mainMessagesHandler = new MainMessagesHandler();

        scheduler = new SimpleScheduler();

        luckPerms = LuckPermsProvider.get();

        userFolder = new File(this.getDataFolder(), "users" + File.separator);
        moduleFolder = new File(this.getDataFolder(), "modules" + File.separator);
        mainCommandsFolder = new File(this.getDataFolder(), commandsFolderChild);
        userFolder.mkdirs();
        moduleFolder.mkdirs();
        mainCommandsFolder.mkdirs();

        helpMap = null;
        moduleManager = new SimpleModuleManager(this, new SimpleCommandMap(this));

        registerListener(new BaseListener());

        UserManager.loadUser(new SavableConsole());

        this.enable();
    }

    @Override
    public void onDisable() {
        this.disable();
    }

    @Override
    public void onLoad() {
        this.load();
    }

    abstract public void enable();

    abstract public void disable();

    abstract public void load();

    abstract public void reload();

    public static void registerListener(Listener listener) {
        getInstance().getProxy().getPluginManager().registerListener(getInstance(), listener);
    }

    @Override
    public void reloadData() {
        mainConfigHandler.reloadResource();
        mainMessagesHandler.reloadResource();
        for (SavableUser user : UserManager.getLoadedUsers()) {
            user.saveAll();
            user.reload();
        }
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public @NotNull Collection<? extends IPlayer> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();

        for (ProxiedPlayer player : onlinePlayers()) {
            players.add(new Player(this, player.getName(), player.getUniqueId().toString()));
        }

        return players;
    }

    @Override
    public int getMaxPlayers() {
        return getProxy().getConfig().getPlayerLimit();
    }

    @Override
    public @NotNull String getResourcePack() {
        return "";
    }

    @Override
    public @NotNull String getResourcePackHash() {
        return "";
    }

    @Override
    public @NotNull String getResourcePackPrompt() {
        return "";
    }

    @Override
    public boolean isResourcePackRequired() {
        return false;
    }

    @Override
    public boolean hasWhitelist() {
        return false;
    }

    @Override
    public void setWhitelist(boolean value) {

    }

    @Override
    public boolean isWhitelistEnforced() {
        return false;
    }

    @Override
    public void setWhitelistEnforced(boolean value) {

    }

    @Override
    public @NotNull Set<SavablePlayer> getWhitelistedPlayers() {
        return null;
    }

    @Override
    public void reloadWhitelist() {

    }

    @Override
    public int broadcastMessage(@NotNull String message) {
        return 0;
    }

    public static void flushCommands() {
        getInstance().getProxy().getPluginManager().unregisterCommands(getInstance());
    }

    public static void registerProperCommand(net.md_5.bungee.api.plugin.Command command) {
        getInstance().getProxy().getPluginManager().registerCommand(getInstance(), command);
    }

    public static void registerCommand(StreamlineCommand command) {
        getInstance().getModuleManager().getCommandMap().register(command.getName(), command.getLabel(), command);
        Streamline.registerProperCommand(new ProperCommandBuilder(command));
    }

    public static void unregisterCommand(StreamlineCommand command) {
        command.unregister(getInstance().getModuleManager().getCommandMap());
    }

    public static BasePlugin getInstance() {
        return instance;
    }

    public static RATAPI getRATAPI() {
        return ratapi;
    }

    public static MainConfigHandler getMainConfig() {
        return mainConfigHandler;
    }

    public static MainMessagesHandler getMainMessages() {
        return mainMessagesHandler;
    }

    public SimpleModuleManager getModuleManager() {
        return moduleManager;
    }

    public StreamlineScheduler getScheduler() {
        return scheduler;
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public List<String> getOnlinePlayerNames() {
        List<String> r = new ArrayList<>();

        getOnlinePlayers().forEach(a -> {
            r.add(a.getName());
        });

        return r;
    }

    @Override
    public @NotNull List<ServerInfo> getServers() {
        return new ArrayList<>(getProxy().getServers().values());
    }

    @Override
    public boolean unloadServer(@NotNull String name, boolean save) {
        return false;
    }

    @Override
    public boolean unloadServer(@NotNull Server server, boolean save) {
        return false;
    }

    @Override
    public @Nullable ServerInfo getServer(@NotNull String name) {
        return getProxy().getServerInfo(name);
    }

    @Override
    public @Nullable Server getServer(@NotNull UUID uid) {
        return null;
    }

    public static LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public static File getUserFolder() {
        return userFolder;
    }

    public static File getModuleFolder() {
        return moduleFolder;
    }

    public static File getMainCommandsFolder() {
        return mainCommandsFolder;
    }

    public static String getCommandsFolderChild() {
        return commandsFolderChild;
    }

    public @NotNull String getUpdateFolder() {
        return updateFolder.getPath();
    }

    @Override
    public @NotNull File getUpdateFolderFile() {
        return updateFolder;
    }

    @Override
    public long getConnectionThrottle() {
        return getProxy().getConfig().getThrottle();
    }

    @Override
    public @Nullable SavablePlayer getSavedPlayer(@NotNull String name) {
        return UserManager.getOrGetPlayer(getUUIDFromName(name));
    }

    @Override
    public @Nullable SavablePlayer getSavedPlayerByUUID(@NotNull String uuid) {
        return UserManager.getOrGetPlayer(uuid);
    }

    public UnsafeValues getUnsafe() {
        return unsafeValues;
    }

    public Warning.WarningState getWarningState() {
        return warningState;
    }

    @Override
    public @Nullable <T extends Keyed> Tag<T> getTag(@NotNull String registry, @NotNull NamespacedKey tag, @NotNull Class<T> clazz) {
        return null;
    }

    @Override
    public @NotNull <T extends Keyed> Iterable<Tag<T>> getTags(@NotNull String registry, @NotNull Class<T> clazz) {
        return null;
    }

    public List<ProxiedPlayer> onlinePlayers() {
        return new ArrayList<>(getProxy().getPlayers());
    }

    public List<ProxiedPlayer> playersOnServer(String serverName) {
        return new ArrayList<>(getProxy().getServerInfo(serverName).getPlayers());
    }

    public ProxiedPlayer getPlayer(String uuid) {
        for (ProxiedPlayer player : onlinePlayers()) {
            if (player.getUniqueId().toString().equals(uuid)) return player;
        }

        return null;
    }

    public String getUUIDFromName(String name) {
        for (ProxiedPlayer sender : getInstance().onlinePlayers()) {
            if (sender.getName().equals(name)) return sender.getUniqueId().toString();
        }

        return null;
    }

    @Override
    public @Nullable ProxiedPlayer getPlayerExact(@NotNull String name) {
        return getPlayer(getUUIDFromName(name));
    }

    @Override
    public @NotNull List<ProxiedPlayer> matchPlayer(@NotNull String name) {
        ProxiedPlayer player = getPlayerExact(name);
        if (player == null) return new ArrayList<>();
        return List.of(player);
    }

    @Override
    public @Nullable ProxiedPlayer getPlayer(@NotNull UUID id) {
        return getPlayer(id.toString());
    }

    public ProxiedPlayer getPlayer(CommandSender sender) {
        return getProxy().getPlayer(sender.getName());
    }

    public ModuleCommand getModuleCommand(String name) {
        return loadedCommands.get(name);
    }

    @Override
    public void savePlayers() {
        for (SavableUser user : UserManager.getLoadedUsers()) {
            user.saveAll();
        }
    }

    public void registerModuleCommand(ModuleCommand command) {
        this.loadedCommands.put(command.getName(), command);
    }

    public boolean dispatchCommand(@NotNull ICommandSender sender, @NotNull String commandLine) throws CommandException {
        return getInstance().getProxy().getPluginManager().dispatchCommand(getInstance().getPlayer(sender.getUUID()), commandLine);
    }

    public Map<String, String[]> getCommandAliases() {
        Map<String, String[]> r = new HashMap<>();

        for (ModuleCommand command : loadedCommands.values()) {
            r.put(command.getLabel(), command.getAliases().toArray(new String[0]));
        }

        return r;
    }

    @Override
    public boolean shouldSendChatPreviews() {
        return false;
    }

    @Override
    public boolean isEnforcingSecureProfiles() {
        return false;
    }

    @Override
    public boolean getHideOnlinePlayers() {
        return false;
    }

    @Override
    public boolean getOnlineMode() {
        return getProxy().getConfig().isOnlineMode();
    }

    @Override
    public void shutdown() {
        getProxy().stop();
    }

    @Override
    public int broadcast(@NotNull String message, @NotNull String permission) {
        int people = 0;

        for (ProxiedPlayer player : onlinePlayers()) {
            if (! player.hasPermission(permission)) continue;
            MessagingUtils.sendMessage(player, message);
            people ++;
        }

        return people;
    }

    @Override
    public @NotNull SavablePlayer getOfflinePlayer(@NotNull String name) {
        return UserManager.getOrGetPlayer(getUUIDFromName(name));
    }

    @Override
    public @NotNull SavablePlayer getOfflinePlayer(@NotNull UUID id) {
        return UserManager.getOrGetPlayer(id.toString());
    }

    @Override
    public @NotNull SavablePlayer createPlayerProfile(@Nullable UUID uniqueId, @Nullable String name) {
        if (uniqueId != null) {
            return createPlayerProfile(uniqueId);
        } else {
            return createPlayerProfile(name);
        }
    }

    @Override
    public @NotNull SavablePlayer createPlayerProfile(@NotNull UUID uniqueId) {
        return (SavablePlayer) UserManager.loadUser(new SavablePlayer(uniqueId.toString()));
    }

    @Override
    public @NotNull SavablePlayer createPlayerProfile(@NotNull String name) {
        return (SavablePlayer) UserManager.loadUser(new SavablePlayer(getUUIDFromName(name)));
    }

    @Override
    public @NotNull Set<String> getIPBans() {
        return null;
    }

    @Override
    public void banIP(@NotNull String address) {

    }

    @Override
    public void unbanIP(@NotNull String address) {

    }

    @Override
    public @NotNull Set<SavablePlayer> getBannedPlayers() {
        return null;
    }

    @Override
    public @NotNull BanList getBanList(BanList.@NotNull Type type) {
        return null;
    }

    @Override
    public @NotNull Set<SavablePlayer> getOperators() {
        return null;
    }

    @Override
    public @NotNull IConsoleCommandSender getConsoleSender() {
        return (IConsoleCommandSender) getProxy().getConsole();
    }

    @Override
    public @NotNull SavablePlayer[] getOfflinePlayers() {
        return new SavablePlayer[0];
    }

    @Override
    public @NotNull HelpMap getHelpMap() {
        return helpMap;
    }

    @Override
    public boolean isPrimaryThread() {
        return false;
    }

    @Override
    public @NotNull String getMotd() {
        return "";
    }

    @Override
    public @Nullable String getShutdownMessage() {
        return null;
    }
}
