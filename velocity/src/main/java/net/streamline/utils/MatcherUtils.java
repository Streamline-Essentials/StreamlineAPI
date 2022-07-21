package net.streamline.utils;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import java.util.ArrayList;
import java.util.List;

public class MatcherUtils {
    public static Matcher matcherBuilder(String regex, String from) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(from);
    }

    public static List<String[]> getGroups(Matcher matcher, int expectedAmount) {
        List<String[]> groups = new ArrayList<>();

        while (matcher.find()) {
            String[] strings = new String[expectedAmount];
            for (int i = 0; i < strings.length; i ++) {
                strings[i] = matcher.group(i + 1);
            }
            groups.add(strings);
        }

        return groups;
    }

    public static String getMatched(List<String[]> groups) {
        StringBuilder builder = new StringBuilder();

        for (String[] match : groups) {
            builder.append(match[0]);
        }

        return builder.toString();
    }
}
