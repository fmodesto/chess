package com.fmotech.chess.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreFix {

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("nohup.out"));
        Map<Integer, String> scores = new HashMap<>();
        int insert = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (StringUtils.startsWith(lines.get(i), "[Black")) {
                insert = i;
                scores.put(insert, "*");
            }
            if (StringUtils.contains(lines.get(i), "1-0"))
                scores.put(insert, "1-0");
            if (StringUtils.contains(lines.get(i), "0-1"))
                scores.put(insert, "0-1");
            if (StringUtils.contains(lines.get(i), "1/2-1/2"))
                scores.put(insert, "1/2-1/2");
        }

        List<String> result = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            result.add(lines.get(i));
            if (scores.containsKey(i))
                result.add("[Result \"" + scores.get(i) + "\"]");
        }

        Files.write(Paths.get("games.pgn"), result);
    }
}
