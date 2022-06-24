package net.streamline.api.placeholder.addons;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import net.streamline.base.Streamline;
import net.streamline.base.configs.MainMessagesHandler;
import net.streamline.api.placeholder.RATExpansion;
import net.streamline.api.savables.UserManager;
import net.streamline.api.savables.users.SavablePlayer;
import net.streamline.api.savables.users.SavableUser;

public class StreamlineExpansion extends RATExpansion {
    public StreamlineExpansion() {
        super("streamline", "Quaint", "0.0.0.1");
    }

    @Override
    public String onLogic(String params) {
        if (params.equals("version")) return Streamline.getInstance().getVersion();
        if (params.equals("players_online")) return String.valueOf(Streamline.getInstance().onlinePlayers().size());
        if (params.equals("players_loaded")) return String.valueOf(UserManager.getLoadedUsers().size());

        if (params.matches("([a][u][t][h][o][r][\\[]([0-2])[\\]])")) {
            Pattern pattern = Pattern.compile("([a][u][t][h][o][r][\\[]([0-9])[\\]])");
            Matcher matcher = pattern.matcher(params);
            while (matcher.find()) {
                return Streamline.getInstance().getDescription().getAuthor();
            }
        }

        if (params.equals("null")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.IS_NULL.get();
        }
        if (params.equals("true")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.IS_TRUE.get();
        }
        if (params.equals("false")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.IS_FALSE.get();
        }
        if (params.equals("online")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.IS_ONLINE.get();
        }
        if (params.equals("offline")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.IS_OFFLINE.get();
        }
        if (params.equals("placeholders_null")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_NULL.get();
        }
        if (params.equals("placeholders_true")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get();
        }
        if (params.equals("placeholders_false")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();
        }
        if (params.equals("placeholders_online")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_ONLINE.get();
        }
        if (params.equals("placeholders_offline")) {
            return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_OFFLINE.get();
        }

        return null;
    }

    @Override
    public String onRequest(SavableUser user, String params) {
        if (params.equals("user_ping")) {
            if (user.updateOnline()) return String.valueOf(Streamline.getInstance().getPlayer(user.uuid).getPing());
            else return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_OFFLINE.get();
        }
        if (params.equals("user_online")) return user.updateOnline() ?
                MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_ONLINE.get() :
                MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_OFFLINE.get();
        if (params.equals("user_uuid")) return user.uuid;

        if (params.equals("user_absolute")) return UserManager.getAbsolute(user);
        if (params.equals("user_absolute_onlined")) return UserManager.getOffOnAbsolute(user);
        if (params.equals("user_formatted")) return UserManager.getFormatted(user);
        if (params.equals("user_formatted_onlined")) return UserManager.getOffOnFormatted(user);

        if (params.equals("user_prefix")) return UserManager.getLuckPermsPrefix(user.latestName);
        if (params.equals("user_suffix")) return UserManager.getLuckPermsSuffix(user.latestName);

        if (params.equals("user_points")) return String.valueOf(user.points);
        if (params.equals("user_level")) return (user instanceof SavablePlayer sp ? String.valueOf(sp.level) : null);
        if (params.equals("user_xp_current")) return (user instanceof SavablePlayer sp ? String.valueOf(sp.currentXP) : null);
        if (params.equals("user_xp_total")) return (user instanceof SavablePlayer sp ? String.valueOf(sp.totalXP) : null);

        if (params.equals("user_play_seconds")) return (user instanceof SavablePlayer sp ? sp.getPlaySecondsAsString() : null);
        if (params.equals("user_play_minutes")) return (user instanceof SavablePlayer sp ? sp.getPlayMinutesAsString() : null);
        if (params.equals("user_play_hours")) return (user instanceof SavablePlayer sp ? sp.getPlayHoursAsString() : null);
        if (params.equals("user_play_days")) return (user instanceof SavablePlayer sp ? sp.getPlayDaysAsString() : null);

        return null;
    }
}