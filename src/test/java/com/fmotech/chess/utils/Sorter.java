package com.fmotech.chess.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.split;

public class Sorter {

    public enum GameResult {
        WHITE,
        BLACK,
        DRAW,
        UNKNOWN
    }

    public static class Game {
        int whiteRating;
        int blackRating;
        GameResult result;
        String moves;

        public Game(GameResult result, int whiteRating, int blackRating, String moves) {
            this.whiteRating = whiteRating;
            this.blackRating = blackRating;
            this.result = result;
            this.moves = moves;
        }

        @Override
        public String toString() {
            return "Game{" +
                    "whiteRating=" + whiteRating +
                    ", blackRating=" + blackRating +
                    ", result=" + result +
                    ", moves='" + moves.hashCode() + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, List<Game>> games = Files.newBufferedReader(Paths.get("zip.txt"), StandardCharsets.UTF_8).lines()
                .map(e -> split(e, "\t"))
                .map(e -> new Game(GameResult.valueOf(e[0]), parseInt(e[1]), parseInt(e[2]), e[3]))
                .collect(Collectors.groupingBy(e -> e.moves));

        List<Game> gg = games.entrySet().stream()
                .map(Map.Entry::getValue)
                .map(Sorter::aggregate)
                .collect(Collectors.toList());

        List<String> lines = gg.stream()
                .map(e -> e.result + "\t" + e.whiteRating + "\t" + e.blackRating + "\t" + e.moves)
                .collect(Collectors.toList());

        Files.write(Paths.get("games.txt"), lines);
    }

    private static Game aggregate(List<Game> games) {
        if (games.size() == 1) return games.get(0);
        long res = games.stream().map(e -> e.result).distinct().count();
        int white = (int) Math.round(games.stream().mapToInt(e -> e.blackRating).average().getAsDouble());
        int black = (int) Math.round(games.stream().mapToInt(e -> e.blackRating).average().getAsDouble());
        return new Game(res > 1 ? GameResult.UNKNOWN : games.get(0).result, white, black, games.get(0).moves);
    }
}
/*
/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin/java "-javaagent:/Applications/IntelliJ IDEA CE.app/Contents/lib/idea_rt.jar=51528:/Applications/IntelliJ IDEA CE.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/deploy.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/dnsns.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/jaccess.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/jfxrt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/localedata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/nashorn.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/sunec.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/zipfs.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/javaws.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jfxswt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/management-agent.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/plugin.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/ant-javafx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/javafx-mx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/packager.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/sa-jdi.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/tools.jar:/Users/fran/IdeaProjects/chess/target/classes:/Users/fran/.m2/repository/org/apache/commons/commons-lang3/3.6/commons-lang3-3.6.jar:/Users/fran/.m2/repository/it/unimi/dsi/fastutil/8.1.1/fastutil-8.1.1.jar:/Users/fran/.m2/repository/junit/junit/4.12/junit-4.12.jar:/Users/fran/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar com.fmotech.chess.AI
objc[74795]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin/java (0x106def4c0) and /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/libinstrument.dylib (0x106edd4e0). One of the two will be used. Which one is undefined.
0 10 g1f3 {10} in 1 nodes, cutting: 0.0, pvs: 0.84210527, table size: 1 time: 4ms
0 0 g1f3 {10} b8c6 {0} in 25 nodes, cutting: 0.25, pvs: 0.84210527, table size: 21 time: 4ms
0 10 g1f3 {10} b8c6 {0} b1c3 {10} in 329 nodes, cutting: 0.38257575, pvs: 0.86632824, table size: 227 time: 11ms
0 0 g1f3 {10} b8c6 {0} b1c3 {10} a8a8 {210} in 3724 nodes, cutting: 0.46401027, pvs: 0.8743769, table size: 2340 time: 18ms
0 12 e2e3 {4} b8c6 {-6} d1f3 {-4} d7d5 {-12} f3d5 {12} in 25177 nodes, cutting: 0.5081975, pvs: 0.89628756, table size: 15425 time: 55ms
0 -8 e2e3 {4} e7e6 {0} b1c3 {10} a8a8 {210} h1h1 {210} a8a8 {210} in 229915 nodes, cutting: 0.611541, pvs: 0.90003157, table size: 117593 time: 313ms
0 19 b1c3 {10} e7e6 {6} e2e4 {14} g8e7 {5} f1c4 {9} b8c6 {-1} c4e6 {19} in 1351546 nodes, cutting: 0.6823467, pvs: 0.92182934, table size: 592464 time: 1390ms
0 -12 e2e3 {4} b8c6 {-6} g1f3 {4} g8f6 {-6} h1h1 {-6} a8a8 {194} h1h1 {194} a8a8 {194} in 15022945 nodes, cutting: 0.75956905, pvs: 0.9426614, table size: 4770055 time: 15624ms
9 26213376 1780973
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
1 6 b8c6 {-6} in 1 nodes, cutting: 0.0, pvs: 0.84210527, table size: 1 time: 0ms
1 -4 b8c6 {-6} g1f3 {4} in 26 nodes, cutting: 0.46666667, pvs: 0.87431693, table size: 21 time: 0ms
1 6 b8c6 {-6} g1f3 {4} g8f6 {-6} in 425 nodes, cutting: 0.46648043, pvs: 0.8861314, table size: 292 time: 1ms
1 -12 b8c6 {-6} d1f3 {-4} d7d5 {-12} f3d5 {12} in 4602 nodes, cutting: 0.64945793, pvs: 0.9119318, table size: 2982 time: 6ms
1 8 e7e6 {0} b1c3 {10} d8f6 {8} d2d4 {16} f6d4 {-8} in 42537 nodes, cutting: 0.59650147, pvs: 0.90624744, table size: 27140 time: 43ms
1 -18 d7d5 {-4} b1c3 {6} d5d4 {5} e3d4 {34} d8d4 {8} g1f3 {18} in 345890 nodes, cutting: 0.7300554, pvs: 0.92249864, table size: 195008 time: 356ms
1 12 b8c6 {-6} g1f3 {4} g8f6 {-6} h1h1 {-6} a8a8 {194} h1h1 {194} a8a8 {194} in 3017798 nodes, cutting: 0.7186577, pvs: 0.9314247, table size: 1386768 time: 2945ms
1 -16 e7e5 {-4} b1c3 {6} d7d5 {-2} d1f3 {0} e5e4 {-1} f3f4 {-1} b8c6 {-11} c3e4 {16} in 21192404 nodes, cutting: 0.7913978, pvs: 0.9418982, table size: 7494194 time: 22963ms
9 24459264 1301035
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
2 6 g1f3 {6} in 1 nodes, cutting: 0.0, pvs: 0.86206895, table size: 1 time: 0ms
2 -4 g1f3 {6} b8c6 {-4} in 35 nodes, cutting: 0.34615386, pvs: 0.8908046, table size: 31 time: 0ms
2 15 g1f3 {6} e5e4 {5} b1c3 {15} in 571 nodes, cutting: 0.6506024, pvs: 0.8979592, table size: 431 time: 1ms
2 -8 d2d4 {4} b8c6 {-6} d4e5 {19} c6e5 {-8} in 6494 nodes, cutting: 0.6682316, pvs: 0.9110365, table size: 5017 time: 8ms
2 22 b1c3 {6} b8c6 {-4} g1f3 {6} g8f6 {-4} h1h1 {-4} in 47061 nodes, cutting: 0.7378281, pvs: 0.928538, table size: 32557 time: 51ms
2 -9 d2d4 {4} d8f6 {2} d4e5 {27} f6e5 {2} h1h1 {2} a8a8 {202} in 327093 nodes, cutting: 0.7873989, pvs: 0.933505, table size: 213843 time: 319ms
2 16 b1c3 {6} d7d5 {-2} d1f3 {0} e5e4 {-1} f3f4 {-1} b8c6 {-11} c3e4 {16} in 3223273 nodes, cutting: 0.8304002, pvs: 0.94494104, table size: 1612068 time: 3587ms
8 25284608 531659
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
3 4 b8c6 {-4} in 1 nodes, cutting: 0.0, pvs: 0.8965517, table size: 1 time: 0ms
3 -6 b8c6 {-4} g1f3 {6} in 35 nodes, cutting: 0.30769232, pvs: 0.9032258, table size: 31 time: 1ms
3 10 f8b4 {3} c3e4 {5} b4d2 {-10} in 688 nodes, cutting: 0.64656615, pvs: 0.9290718, table size: 497 time: 0ms
3 -22 b8c6 {-4} g1f3 {6} g8f6 {-4} h1h1 {-4} in 5117 nodes, cutting: 0.8277713, pvs: 0.93989575, table size: 3311 time: 6ms
3 14 d8f6 {4} f1d3 {8} b8c6 {-2} g1e2 {7} f6f2 {-14} in 62545 nodes, cutting: 0.8142107, pvs: 0.9451196, table size: 40427 time: 62ms
3 -16 d7d5 {-2} d1f3 {0} e5e4 {-1} f3f4 {-1} b8c6 {-11} c3e4 {16} in 346366 nodes, cutting: 0.8758925, pvs: 0.94832677, table size: 196050 time: 316ms
3 15 g8f6 {-4} g1f3 {6} e5e4 {5} f3e5 {7} f8d6 {3} e5c4 {2} d6h2 {-15} in 3367897 nodes, cutting: 0.8611737, pvs: 0.9542394, table size: 1827591 time: 3565ms
3 -16 g8f6 {-4} g1f3 {6} e5e4 {5} f3d4 {7} f8b4 {4} c3b5 {3} d7d5 {-5} b5c7 {16} in 17215856 nodes, cutting: 0.9058109, pvs: 0.9649765, table size: 7568170 time: 15369ms
9 26054656 1056769
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
4 6 g1f3 {6} in 1 nodes, cutting: 0.0, pvs: 0.875, table size: 1 time: 0ms
4 -4 g1f3 {6} b8c6 {-4} in 38 nodes, cutting: 0.5862069, pvs: 0.8895349, table size: 34 time: 0ms
4 29 d1f3 {-2} f6g8 {8} f3f7 {29} in 509 nodes, cutting: 0.7726218, pvs: 0.9397463, table size: 312 time: 1ms
4 -13 g1f3 {6} f8b4 {3} c3e2 {2} b4d2 {-13} in 5098 nodes, cutting: 0.7934327, pvs: 0.94248414, table size: 3788 time: 5ms
4 25 d1f3 {-2} f8b4 {-5} c3e2 {-6} f6g8 {4} f3f7 {25} in 41331 nodes, cutting: 0.86056507, pvs: 0.95928866, table size: 25888 time: 39ms
4 -15 g1f3 {6} e5e4 {5} f3e5 {7} f8d6 {3} e5c4 {2} d6h2 {-15} in 428964 nodes, cutting: 0.85315365, pvs: 0.95238096, table size: 268520 time: 433ms
4 16 g1f3 {6} e5e4 {5} f3d4 {7} f8b4 {4} c3b5 {3} d7d5 {-5} b5c7 {16} in 2384370 nodes, cutting: 0.89437896, pvs: 0.96348685, table size: 1269717 time: 2285ms
4 -13 d2d4 {4} e5e4 {3} a2a3 {3} c7c5 {5} f1c4 {9} d8a5 {9} c4f7 {29} e8f7 {-31} in 21322877 nodes, cutting: 0.8844924, pvs: 0.95460385, table size: 8534026 time: 21812ms
9 26245120 845489
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5 21 e5d4 {-21} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
5 -8 e5d4 {-21} e3d4 {8} in 31 nodes, cutting: 1.0, pvs: 1.0, table size: 30 time: 0ms
5 36 f8b4 {1} d4e5 {26} b4c3 {-36} in 304 nodes, cutting: 0.9699248, pvs: 0.9837133, table size: 204 time: 1ms
5 -11 f8b4 {1} d4e5 {26} b4c3 {-36} b2c3 {21} in 2452 nodes, cutting: 0.9746533, pvs: 0.96823597, table size: 1697 time: 2ms
5 25 f8b4 {1} a2a3 {1} e5d4 {-24} a3b4 {36} d4c3 {-25} in 15190 nodes, cutting: 0.9316087, pvs: 0.9572845, table size: 9247 time: 13ms
5 -11 f8b4 {1} d4e5 {26} b4c3 {-36} b2c3 {21} f6e4 {19} g1f3 {29} in 123983 nodes, cutting: 0.9463677, pvs: 0.9567123, table size: 75758 time: 110ms
5 13 e5e4 {3} a2a3 {3} c7c5 {5} f1c4 {9} d8a5 {9} c4f7 {29} e8f7 {-31} in 666567 nodes, cutting: 0.9069757, pvs: 0.9514334, table size: 330778 time: 560ms
5 -2 e5d4 {-21} e3d4 {8} b8c6 {-2} h1h1 {-2} a8a8 {198} h1h1 {198} a8a8 {198} h1h1 {198} in 2808323 nodes, cutting: 0.94730425, pvs: 0.9440565, table size: 1312934 time: 1967ms
9 30163968 527057
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
6 8 e3d4 {8} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
6 -2 e3d4 {8} b8c6 {-2} in 39 nodes, cutting: 0.9722222, pvs: 0.96938777, table size: 38 time: 0ms
6 35 d1d4 {6} f6h5 {14} d4g7 {35} in 223 nodes, cutting: 0.9493671, pvs: 0.96242774, table size: 139 time: 1ms
6 6 e3d4 {8} f8e7 {6} g1f3 {16} b8c6 {6} in 2644 nodes, cutting: 0.92735577, pvs: 0.94037354, table size: 2133 time: 2ms
6 25 d1d4 {6} b8c6 {-4} d4f4 {-4} f6h5 {4} f4f7 {25} in 22586 nodes, cutting: 0.93157923, pvs: 0.95028603, table size: 14828 time: 22ms
6 -6 e3d4 {8} f8b4 {5} d1e2 {6} d8e7 {5} e1d1 {5} e7e2 {-175} in 242980 nodes, cutting: 0.9070752, pvs: 0.9406304, table size: 135817 time: 185ms
6 2 e3d4 {8} b8c6 {-2} h1h1 {-2} a8a8 {198} h1h1 {198} a8a8 {198} h1h1 {198} in 1253864 nodes, cutting: 0.92869276, pvs: 0.9338058, table size: 666419 time: 1007ms
6 0 e3d4 {8} f8e7 {6} f1c4 {10} e7b4 {9} d1e2 {10} d8e7 {9} e1f1 {11} a8a8 {211} in 24246742 nodes, cutting: 0.9158095, pvs: 0.93063384, table size: 9062708 time: 22277ms
9 30534656 546381
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
7 2 b8c6 {-2} in 1 nodes, cutting: 0.0, pvs: 0.88461536, table size: 1 time: 0ms
7 3 d8e7 {7} e1d2 {7} in 48 nodes, cutting: 0.8235294, pvs: 0.92619926, table size: 35 time: 0ms
7 -6 f8e7 {6} g1f3 {16} b8c6 {6} in 844 nodes, cutting: 0.88204455, pvs: 0.91764706, table size: 694 time: 1ms
7 -4 f8b4 {5} g1e2 {14} b4c3 {-48} e2c3 {14} in 10620 nodes, cutting: 0.8895662, pvs: 0.9317089, table size: 8041 time: 9ms
7 6 f8b4 {5} d1e2 {6} d8e7 {5} e1d1 {5} e7e2 {-175} in 160950 nodes, cutting: 0.8911671, pvs: 0.9358602, table size: 88537 time: 123ms
7 -2 b8c6 {-2} h1h1 {-2} a8a8 {198} h1h1 {198} a8a8 {198} h1h1 {198} in 958839 nodes, cutting: 0.9164104, pvs: 0.93105847, table size: 520121 time: 717ms
7 0 f8e7 {6} f1c4 {10} e7b4 {9} d1e2 {10} d8e7 {9} e1f1 {11} a8a8 {211} in 15965799 nodes, cutting: 0.92020035, pvs: 0.9303977, table size: 6422249 time: 14005ms
8 30548992 692368
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
8 16 g1f3 {16} in 1 nodes, cutting: 0.0, pvs: 0.92105263, table size: 1 time: 0ms
8 6 g1f3 {16} b8c6 {6} in 43 nodes, cutting: 0.4722222, pvs: 0.910828, table size: 40 time: 0ms
8 61 c1g5 {9} b8c6 {-1} g5f6 {61} in 638 nodes, cutting: 0.7967626, pvs: 0.9496612, table size: 468 time: 1ms
8 -2 f1b5 {9} b8c6 {-1} b5c6 {61} d7c6 {-2} in 5642 nodes, cutting: 0.8245506, pvs: 0.9484489, table size: 3991 time: 5ms
8 47 d1e2 {7} a8a8 {207} h1h1 {207} a8a8 {207} h1h1 {207} in 83851 nodes, cutting: 0.8644898, pvs: 0.9553034, table size: 57809 time: 84ms
8 0 f1c4 {10} e7b4 {9} d1e2 {10} d8e7 {9} e1f1 {11} a8a8 {211} in 751196 nodes, cutting: 0.8575901, pvs: 0.9459727, table size: 387165 time: 681ms
8 31 a2a3 {6} e8g8 {0} d1e2 {1} a8a8 {201} h1h1 {201} a8a8 {201} h1h1 {201} in 5581864 nodes, cutting: 0.89207846, pvs: 0.94432104, table size: 2985238 time: 6046ms
8 27080704 812275
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
9 13 e7a3 {-13} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
9 -6 b8c6 {-4} g1f3 {6} in 33 nodes, cutting: 0.75, pvs: 0.943128, table size: 29 time: 0ms
9 20 b8c6 {-4} g1f3 {6} c6d4 {-20} in 477 nodes, cutting: 0.9312039, pvs: 0.9590909, table size: 305 time: 1ms
9 -8 e7d6 {4} c1e3 {8} b8c6 {-2} g1f3 {8} in 7257 nodes, cutting: 0.852011, pvs: 0.94037324, table size: 5285 time: 7ms
9 9 h7h6 {6} f1b5 {9} a7a6 {9} b5d3 {10} e7a3 {-9} in 58894 nodes, cutting: 0.8974232, pvs: 0.9597093, table size: 41397 time: 54ms
9 -31 e8g8 {0} d1e2 {1} b8c6 {-9} d4d5 {-8} f6d5 {-35} c3d5 {31} in 489189 nodes, cutting: 0.8907138, pvs: 0.95620143, table size: 328247 time: 571ms
9 8 e8f8 {4} c1g5 {7} h7h6 {7} g5f6 {69} e7f6 {6} g1f3 {16} f6d4 {-8} in 5155708 nodes, cutting: 0.9105848, pvs: 0.963873, table size: 2570452 time: 4803ms
8 23273472 895231
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
10 14 g1f3 {14} in 1 nodes, cutting: 0.0, pvs: 0.9230769, table size: 1 time: 0ms
10 -5 g1f3 {14} e7a3 {-5} in 45 nodes, cutting: 1.0, pvs: 0.9516129, table size: 41 time: 0ms
10 50 c1g5 {7} e7a3 {-12} g5f6 {50} in 631 nodes, cutting: 0.8432836, pvs: 0.9604743, table size: 427 time: 1ms
10 -4 c1g5 {7} b8c6 {-3} g5f6 {59} e7f6 {-4} in 5743 nodes, cutting: 0.91574943, pvs: 0.9651196, table size: 3805 time: 6ms
10 35 d1e2 {5} b8c6 {-5} d4d5 {-4} f6d5 {-31} c3d5 {35} in 66363 nodes, cutting: 0.86101943, pvs: 0.9659398, table size: 44806 time: 73ms
10 -8 c1g5 {7} h7h6 {7} g5f6 {69} e7f6 {6} g1f3 {16} f6d4 {-8} in 427889 nodes, cutting: 0.9014466, pvs: 0.96676695, table size: 244815 time: 390ms
10 35 d1e2 {5} a8a8 {205} h1h1 {205} a8a8 {205} h1h1 {205} a8a8 {205} h1h1 {205} in 4684552 nodes, cutting: 0.89830256, pvs: 0.9665008, table size: 2499662 time: 5255ms
10 -7 g1f3 {14} a8a8 {214} h1h1 {214} a8a8 {214} h1h1 {214} a8a8 {214} h1h1 {214} a8a8 {214} in 21504030 nodes, cutting: 0.906552, pvs: 0.963872, table size: 9217960 time: 17823ms
9 26835968 903862
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
11 5 e7a3 {-5} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
11 -8 b8c6 {4} f1d3 {8} in 33 nodes, cutting: 0.9166667, pvs: 0.9408867, table size: 29 time: 0ms
11 18 b8c6 {4} f1d3 {8} c6d4 {-18} in 404 nodes, cutting: 0.9426934, pvs: 0.9559748, table size: 250 time: 1ms
11 -13 e7d6 {12} f3e5 {14} d6e5 {-50} d4e5 {13} in 4681 nodes, cutting: 0.88206625, pvs: 0.95494366, table size: 3833 time: 5ms
11 10 d7d5 {6} c1g5 {9} f6g4 {10} g5f4 {11} g4f2 {-10} in 44896 nodes, cutting: 0.9017862, pvs: 0.96576655, table size: 30499 time: 41ms
11 -4 e7b4 {13} a3b4 {73} d8e7 {72} c3e2 {71} b8c6 {61} c1f4 {65} in 442987 nodes, cutting: 0.89966565, pvs: 0.9485338, table size: 281935 time: 500ms
11 0 a8a8 {214} h1h1 {214} a8a8 {214} h1h1 {214} a8a8 {214} h1h1 {214} a8a8 {214} in 3208210 nodes, cutting: 0.9045089, pvs: 0.9648241, table size: 1632796 time: 2970ms
8 26179584 636195
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
12 266 a8b8 {266} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
12 247 a8b8 {266} e7a3 {247} in 42 nodes, cutting: 1.0, pvs: 1.0, table size: 41 time: 0ms
12 305 a8b8 {266} e7a3 {247} b8c8 {305} in 148 nodes, cutting: 1.0, pvs: 0.9951923, table size: 106 time: 0ms
12 250 a8b8 {266} f6g4 {267} f1d3 {271} g4f2 {250} in 2318 nodes, cutting: 0.963409, pvs: 0.9723451, table size: 1638 time: 2ms
12 288 a8b8 {266} e7a3 {247} f3e5 {249} a3b2 {225} c1b2 {288} in 11927 nodes, cutting: 0.9779668, pvs: 0.97402596, table size: 8911 time: 17ms
12 251 a8b8 {266} d8e8 {266} f3g5 {265} e7b4 {264} e1d2 {264} b4c3 {202} in 146348 nodes, cutting: 0.9640853, pvs: 0.96659535, table size: 79107 time: 123ms
12 297 a8b8 {266} c7c6 {270} c1e3 {274} f6d5 {272} b8c8 {330} d8c8 {231} c3d5 {297} in 560739 nodes, cutting: 0.9723267, pvs: 0.9710101, table size: 282629 time: 679ms
12 257 a8b8 {266} a8a8 {266} h1h1 {266} a8a8 {266} h1h1 {266} a8a8 {266} h1h1 {266} a8a8 {266} in 6809280 nodes, cutting: 0.966618, pvs: 0.9625318, table size: 2862036 time: 5497ms
9 24875008 438911
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
13 -247 e7a3 {247} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
13 -305 e7a3 {247} b8c8 {305} in 28 nodes, cutting: 1.0, pvs: 0.989011, table size: 27 time: 0ms
13 -250 f6g4 {267} f1d3 {271} g4f2 {250} in 495 nodes, cutting: 0.9305556, pvs: 0.96657753, table size: 330 time: 1ms
13 -288 e7a3 {247} f3e5 {249} a3b2 {225} c1b2 {288} in 2370 nodes, cutting: 0.96117866, pvs: 0.97003156, table size: 1894 time: 1ms
13 -251 d8e8 {266} f3g5 {265} e7b4 {264} e1d2 {264} b4c3 {202} in 61733 nodes, cutting: 0.9230645, pvs: 0.96577984, table size: 39321 time: 55ms
13 -297 c7c6 {270} c1e3 {274} f6d5 {272} b8c8 {330} d8c8 {231} c3d5 {297} in 215956 nodes, cutting: 0.95231754, pvs: 0.97052306, table size: 150524 time: 194ms
13 0 a8a8 {266} h1h1 {266} a8a8 {266} h1h1 {266} a8a8 {266} h1h1 {266} a8a8 {266} in 3210410 nodes, cutting: 0.94128525, pvs: 0.96242493, table size: 1663029 time: 2697ms
13 -298 c7c5 {268} f1e2 {270} d7d5 {262} d4c5 {280} a8a8 {280} b8c8 {338} d8c8 {239} h1h1 {239} in 21589790 nodes, cutting: 0.9629255, pvs: 0.97157997, table size: 10813727 time: 23971ms
9 25001984 563847
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
14 326 b8c8 {326} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
14 262 d4c5 {286} e7c5 {262} in 45 nodes, cutting: 1.0, pvs: 0.99224806, table size: 43 time: 0ms
14 320 d4c5 {286} e7c5 {262} b8c8 {320} in 203 nodes, cutting: 1.0, pvs: 0.99331105, table size: 132 time: 0ms
14 257 f3g5 {267} c5d4 {238} g5h7 {250} h8h7 {199} in 2705 nodes, cutting: 0.9541701, pvs: 0.9820574, table size: 1915 time: 2ms
14 304 c1f4 {272} d8e8 {272} f4e5 {272} c5d4 {243} e5f6 {304} in 25892 nodes, cutting: 0.9653648, pvs: 0.97349477, table size: 19756 time: 31ms
14 263 f3g5 {267} d8e8 {267} g5f7 {288} h8g8 {288} f7e5 {292} c5d4 {263} in 148813 nodes, cutting: 0.96948266, pvs: 0.97437733, table size: 83409 time: 102ms
14 298 f1e2 {270} d7d5 {262} d4c5 {280} e7c5 {256} e1g1 {262} c5f2 {242} f1f2 {302} in 1698419 nodes, cutting: 0.9722277, pvs: 0.9702825, table size: 951271 time: 1992ms
14 259 d4c5 {286} e7c5 {262} c1e3 {266} d8e8 {266} h1h1 {266} a8a8 {266} h1h1 {266} a8a8 {266} in 7201923 nodes, cutting: 0.97090226, pvs: 0.97090375, table size: 3356608 time: 5214ms
9 25907200 411387
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
15 -262 e7c5 {262} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
15 -320 e7c5 {262} b8c8 {320} in 27 nodes, cutting: 1.0, pvs: 1.0, table size: 26 time: 0ms
15 -246 e7c5 {262} f1d3 {266} c5f2 {246} in 150 nodes, cutting: 0.80733943, pvs: 0.97421205, table size: 116 time: 0ms
15 -300 e7c5 {262} e1d2 {262} c5f2 {242} b8c8 {300} in 2406 nodes, cutting: 0.9510588, pvs: 0.9698795, table size: 1776 time: 3ms
15 -256 e7c5 {262} c1g5 {265} d7d5 {257} g5f6 {319} d8f6 {256} in 22924 nodes, cutting: 0.9277078, pvs: 0.97022974, table size: 17178 time: 20ms
15 -263 e7c5 {262} h1h1 {262} a8a8 {262} h1h1 {262} a8a8 {262} h1h1 {262} in 189397 nodes, cutting: 0.9599896, pvs: 0.972025, table size: 150632 time: 161ms
15 -259 e7c5 {262} c1e3 {266} d8e8 {266} h1h1 {266} a8a8 {266} h1h1 {266} a8a8 {266} in 1493826 nodes, cutting: 0.9503335, pvs: 0.97073644, table size: 1020846 time: 1412ms
15 -285 e7c5 {262} h1h1 {262} a8a8 {262} h1h1 {262} a8a8 {262} h1h1 {262} a8a8 {262} h1h1 {262} in 15292686 nodes, cutting: 0.9613447, pvs: 0.9715171, table size: 8628686 time: 15582ms
9 27215872 454598
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
16 320 b8c8 {320} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
16 246 f1d3 {266} c5f2 {246} in 53 nodes, cutting: 0.6923077, pvs: 0.96750903, table size: 47 time: 0ms
16 300 e1d2 {262} c5f2 {242} b8c8 {300} in 1283 nodes, cutting: 0.94081813, pvs: 0.96716696, table size: 935 time: 1ms
16 256 c1g5 {265} d7d5 {257} g5f6 {319} d8f6 {256} in 11567 nodes, cutting: 0.93364745, pvs: 0.96936804, table size: 7925 time: 9ms
16 0 h1h1 {262} a8a8 {262} h1h1 {262} a8a8 {262} h1h1 {262} in 164825 nodes, cutting: 0.9608548, pvs: 0.971441, table size: 133835 time: 151ms
16 259 c1e3 {266} d8e8 {266} f3e5 {268} f8g8 {264} e3c5 {325} a8a8 {325} in 759760 nodes, cutting: 0.96216875, pvs: 0.97060263, table size: 489078 time: 616ms
16 0 h1h1 {262} a8a8 {262} h1h1 {262} a8a8 {262} h1h1 {262} a8a8 {262} h1h1 {262} in 11810881 nodes, cutting: 0.9610436, pvs: 0.971494, table size: 6616177 time: 12327ms
8 27747328 454418
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
17 -242 c5f2 {242} in 1 nodes, cutting: 0.0, pvs: 0.96875, table size: 1 time: 0ms
17 -241 d8e7 {261} e1d2 {261} in 78 nodes, cutting: 0.962963, pvs: 0.96517414, table size: 51 time: 0ms
17 -238 d7d5 {254} f1d3 {258} c5f2 {238} in 911 nodes, cutting: 0.9068242, pvs: 0.9641927, table size: 518 time: 1ms
17 -225 d8e7 {261} c1e3 {265} e7e8 {266} b8c8 {324} in 16579 nodes, cutting: 0.9629069, pvs: 0.97233605, table size: 10634 time: 18ms
17 0 a8a8 {262} h1h1 {262} a8a8 {262} h1h1 {262} a8a8 {262} in 71596 nodes, cutting: 0.9494239, pvs: 0.96826386, table size: 47591 time: 59ms
17 -250 d8e8 {262} d1e2 {263} e8d8 {263} c1e3 {267} c5d6 {267} b8a8 {267} in 1815317 nodes, cutting: 0.96203536, pvs: 0.9722571, table size: 1090750 time: 1762ms
17 -243 d7d5 {254} c1f4 {258} f8g8 {254} f1d3 {258} g7g6 {261} f3e5 {263} c5f2 {243} in 6512331 nodes, cutting: 0.9600197, pvs: 0.97096866, table size: 3408960 time: 4923ms
8 27973632 543005
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
18 312 b8c8 {312} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
18 238 f1d3 {258} c5f2 {238} in 51 nodes, cutting: 0.7222222, pvs: 0.9705015, table size: 44 time: 0ms
18 292 f3e5 {256} c5f2 {236} e1f2 {296} in 997 nodes, cutting: 0.9361234, pvs: 0.97183096, table size: 887 time: 1ms
18 240 c1g5 {257} c5d6 {257} b8a8 {257} d6a3 {240} in 13610 nodes, cutting: 0.9281836, pvs: 0.97183096, table size: 9819 time: 14ms
18 255 c1e3 {258} d8c7 {257} b8a8 {257} c5e3 {195} f2e3 {255} in 130622 nodes, cutting: 0.9647896, pvs: 0.9736344, table size: 104280 time: 122ms
18 243 c1f4 {258} f8g8 {254} f1d3 {258} g7g6 {261} f3e5 {263} c5f2 {243} in 1001758 nodes, cutting: 0.9571287, pvs: 0.97244656, table size: 630392 time: 866ms
18 280 c1e3 {258} c5e3 {196} f2e3 {256} f6e4 {254} d1d5 {280} d8d5 {97} c3d5 {280} in 5213292 nodes, cutting: 0.9621263, pvs: 0.9730171, table size: 3421371 time: 5047ms
8 27681792 386479
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
19 -196 c5e3 {196} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
19 -256 c5e3 {196} f2e3 {256} in 40 nodes, cutting: 1.0, pvs: 1.0, table size: 39 time: 0ms
19 -218 d5d4 {257} f3d4 {284} d8d4 {218} in 451 nodes, cutting: 0.992268, pvs: 0.9819277, table size: 352 time: 1ms
19 -255 d8c7 {257} b8a8 {257} c5e3 {195} f2e3 {255} in 3701 nodes, cutting: 0.9737716, pvs: 0.97214854, table size: 2266 time: 4ms
19 -233 c5d6 {258} e3a7 {275} d6b8 {179} a7b8 {237} c8e6 {233} in 25302 nodes, cutting: 0.9702697, pvs: 0.971904, table size: 14074 time: 20ms
19 -280 c5e3 {196} f2e3 {256} f6e4 {254} d1d5 {280} d8d5 {97} c3d5 {280} in 109552 nodes, cutting: 0.9725856, pvs: 0.9754266, table size: 75746 time: 96ms
19 -244 c5e3 {196} f2e3 {256} d8c7 {255} b8c8 {313} c7c8 {215} h1h1 {215} a8a8 {215} in 511220 nodes, cutting: 0.9668664, pvs: 0.95281667, table size: 302777 time: 370ms
19 -277 c5e3 {196} f2e3 {256} d8c7 {255} b8a8 {255} f6e4 {253} d1d5 {279} e4c3 {219} a8c8 {277} in 5341053 nodes, cutting: 0.9710934, pvs: 0.9690076, table size: 2712198 time: 5685ms
9 28595200 36412
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
20 256 f2e3 {256} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
20 252 f2e3 {256} c8e6 {252} in 43 nodes, cutting: 1.0, pvs: 0.9727273, table size: 42 time: 0ms
20 310 f2e3 {256} f8g8 {252} b8c8 {310} in 249 nodes, cutting: 0.97969544, pvs: 0.98275864, table size: 203 time: 0ms
20 238 f2e3 {256} d8e8 {256} f1d3 {260} e8e3 {238} in 2209 nodes, cutting: 0.9240636, pvs: 0.9590985, table size: 1787 time: 2ms
20 280 f2e3 {256} f6e4 {254} d1d5 {280} d8d5 {97} c3d5 {280} in 11791 nodes, cutting: 0.96343464, pvs: 0.9731744, table size: 9101 time: 9ms
20 244 f2e3 {256} d8c7 {255} b8c8 {313} c7c8 {215} h1h1 {215} a8a8 {215} in 150467 nodes, cutting: 0.9354756, pvs: 0.94623977, table size: 109581 time: 125ms
20 277 f2e3 {256} d8c7 {255} b8a8 {255} f6e4 {253} d1d5 {279} e4c3 {219} a8c8 {277} in 711459 nodes, cutting: 0.9596567, pvs: 0.9681653, table size: 468931 time: 595ms
20 258 f2e3 {256} a8a8 {256} h1h1 {256} a8a8 {256} h1h1 {256} a8a8 {256} h1h1 {256} a8a8 {256} in 8655122 nodes, cutting: 0.9438954, pvs: 0.96952754, table size: 3890731 time: 8066ms
9 25794560 232327
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
21 -252 c8e6 {252} in 1 nodes, cutting: 0.0, pvs: 0.9, table size: 1 time: 0ms
21 -310 f8g8 {252} b8c8 {310} in 36 nodes, cutting: 0.962963, pvs: 0.9736842, table size: 32 time: 0ms
21 -238 d8e8 {256} f1d3 {260} e8e3 {238} in 610 nodes, cutting: 0.82889736, pvs: 0.95279384, table size: 419 time: 1ms
21 -280 f6e4 {254} d1d5 {280} d8d5 {97} c3d5 {280} in 4651 nodes, cutting: 0.9314765, pvs: 0.9714739, table size: 3556 time: 4ms
21 -244 d8c7 {255} b8c8 {313} c7c8 {215} h1h1 {215} a8a8 {215} in 55261 nodes, cutting: 0.8944057, pvs: 0.9449213, table size: 35800 time: 40ms
21 -277 d8c7 {255} b8a8 {255} f6e4 {253} d1d5 {279} e4c3 {219} a8c8 {277} in 383231 nodes, cutting: 0.9416277, pvs: 0.9678731, table size: 264788 time: 382ms
21 0 a8a8 {256} h1h1 {256} a8a8 {256} h1h1 {256} a8a8 {256} h1h1 {256} a8a8 {256} in 1427759 nodes, cutting: 0.9026642, pvs: 0.96923625, table size: 765462 time: 928ms
8 24538112 297209
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
22 314 b8c8 {314} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
22 256 f1d3 {260} c8e6 {256} in 51 nodes, cutting: 0.6388889, pvs: 0.9293286, table size: 44 time: 0ms
22 316 d1d4 {258} f8g8 {254} d4f6 {316} in 910 nodes, cutting: 0.9305556, pvs: 0.9732191, table size: 615 time: 1ms
22 268 d1d4 {258} f8g8 {254} d4a7 {272} c8e6 {268} in 12994 nodes, cutting: 0.87644196, pvs: 0.953378, table size: 8088 time: 11ms
22 309 d1d2 {257} d8d6 {255} b8a8 {255} f8g8 {251} a8c8 {309} in 85226 nodes, cutting: 0.9420836, pvs: 0.96855795, table size: 63142 time: 70ms
22 259 c3d5 {282} a8a8 {282} h1h1 {282} a8a8 {282} h1h1 {282} a8a8 {282} in 250571 nodes, cutting: 0.87631196, pvs: 0.970117, table size: 163389 time: 144ms
22 315 d1d4 {258} f6d7 {259} b8a8 {259} d7b6 {259} a8b8 {259} f8g8 {255} d4b6 {315} in 4104895 nodes, cutting: 0.9400652, pvs: 0.9717388, table size: 2509817 time: 4607ms
22 312 d1d4 {258} a8a8 {258} h1h1 {258} a8a8 {258} h1h1 {258} a8a8 {258} h1h1 {258} a8a8 {258} in 20910692 nodes, cutting: 0.94940424, pvs: 0.9671488, table size: 9572484 time: 15772ms
9 28904448 1061599
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
23 -254 c8e6 {254} in 1 nodes, cutting: 0.0, pvs: 0.9310345, table size: 1 time: 0ms
23 -316 f8g8 {254} d4f6 {316} in 34 nodes, cutting: 0.44444445, pvs: 0.9728507, table size: 31 time: 0ms
23 -268 f8g8 {254} d4a7 {272} c8e6 {268} in 1249 nodes, cutting: 0.95246327, pvs: 0.9627949, table size: 1057 time: 1ms
23 -281 f6e4 {256} b8c8 {314} d8c8 {215} c3e4 {281} in 4660 nodes, cutting: 0.8967509, pvs: 0.9719061, table size: 3418 time: 5ms
23 -257 f8g8 {254} f1d3 {258} f6e4 {256} c3e4 {322} d5e4 {257} in 152826 nodes, cutting: 0.96173614, pvs: 0.9689972, table size: 96988 time: 140ms
23 -315 f6d7 {259} b8a8 {259} d7b6 {259} a8b8 {259} f8g8 {255} d4b6 {315} in 445491 nodes, cutting: 0.9353633, pvs: 0.9753348, table size: 260558 time: 419ms
23 0 a8a8 {258} h1h1 {258} a8a8 {258} h1h1 {258} a8a8 {258} h1h1 {258} a8a8 {258} in 5647595 nodes, cutting: 0.9690399, pvs: 0.97001296, table size: 2716988 time: 5150ms
8 23034880 850764
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
24 320 d4f6 {320} in 1 nodes, cutting: 0.0, pvs: 0.9814815, table size: 1 time: 0ms
24 312 d4c5 {258} f8g8 {254} in 81 nodes, cutting: 1.0, pvs: 0.972549, table size: 64 time: 0ms
24 330 d4a7 {276} f8g8 {272} b8c8 {330} in 463 nodes, cutting: 0.9690141, pvs: 0.9790136, table size: 333 time: 1ms
24 388 d4c5 {258} f8g8 {254} b8c8 {312} d8c8 {213} in 32972 nodes, cutting: 0.96855956, pvs: 0.984543, table size: 23812 time: 30ms
24 424 d4c5 {258} f8e8 {260} b8c8 {318} f6d7 {319} c8d8 {498} in 67183 nodes, cutting: 0.9642603, pvs: 0.98419887, table size: 40931 time: 47ms
24 435 d4c5 {258} f8e8 {260} f1b5 {263} c8d7 {261} b8c8 {261} d8c8 {162} in 628698 nodes, cutting: 0.98482746, pvs: 0.9819644, table size: 261209 time: 578ms
24 492 d4c5 {258} f8e8 {260} b8c8 {318} a7a6 {318} c5c7 {317} d8c8 {218} c7c8 {394} in 4406737 nodes, cutting: 0.9759809, pvs: 0.97348493, table size: 1553940 time: 5108ms
8 27993088 175175
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
25 -312 f8g8 {254} in 7 nodes, cutting: 1.0, pvs: 0.9875776, table size: 5 time: 0ms
25 -268 f8g8 {254} c5a7 {272} in 429 nodes, cutting: 0.9973262, pvs: 0.9687124, table size: 235 time: 1ms
25 -388 f8g8 {254} b8c8 {312} d8c8 {213} in 1749 nodes, cutting: 0.9963873, pvs: 0.97743815, table size: 859 time: 2ms
25 -424 f8e8 {260} b8c8 {318} f6d7 {319} c8d8 {498} in 13177 nodes, cutting: 0.9922323, pvs: 0.9822375, table size: 6165 time: 12ms
25 -435 f8e8 {260} f1b5 {263} c8d7 {261} b8c8 {261} d8c8 {162} in 99519 nodes, cutting: 0.99474025, pvs: 0.9791615, table size: 36577 time: 99ms
25 -492 f8e8 {260} b8c8 {318} a7a6 {318} c5c7 {317} d8c8 {218} c7c8 {394} in 721511 nodes, cutting: 0.994778, pvs: 0.9811236, table size: 240353 time: 733ms
25 -490 f8e8 {260} b8c8 {318} a7a6 {318} c5c7 {317} d8c8 {218} c7c8 {394} a8a8 {394} in 5253085 nodes, cutting: 0.9956753, pvs: 0.9764031, table size: 1519339 time: 5067ms
8 26401792 168090
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
26 318 b8c8 {318} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
26 324 f1b5 {263} f6d7 {264} in 69 nodes, cutting: 1.0, pvs: 0.9617021, table size: 62 time: 0ms
26 394 b8c8 {318} d8c8 {219} c5c8 {394} in 435 nodes, cutting: 0.99441344, pvs: 0.9746479, table size: 365 time: 0ms
26 424 b8c8 {318} f6d7 {319} c8d8 {498} e8d8 {398} in 4631 nodes, cutting: 0.99648243, pvs: 0.98475224, table size: 3429 time: 6ms
26 435 f1b5 {263} c8d7 {261} b8c8 {261} d8c8 {162} c5c8 {337} in 45034 nodes, cutting: 0.99699146, pvs: 0.97302324, table size: 23146 time: 54ms
26 492 b8c8 {318} a7a6 {318} c5c7 {317} d8c8 {218} c7c8 {394} a8a8 {394} in 289830 nodes, cutting: 0.99757516, pvs: 0.9805026, table size: 141156 time: 319ms
26 490 b8c8 {318} a7a6 {318} c5c7 {317} d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} in 2268886 nodes, cutting: 0.9984621, pvs: 0.9772765, table size: 904469 time: 2368ms
26 517 b8c8 {318} f6d7 {319} c5c7 {318} d8c8 {219} c7c8 {395} e8e7 {395} c8h8 {493} d7c5 {491} in 16982674 nodes, cutting: 0.9982349, pvs: 0.98464316, table size: 5741347 time: 17710ms
9 25527296 45962
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
27 -219 d8c8 {219} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
27 -394 d8c8 {219} c5c8 {394} in 20 nodes, cutting: 1.0, pvs: 1.0, table size: 19 time: 0ms
27 -424 f6d7 {319} c8d8 {498} e8d8 {398} in 655 nodes, cutting: 0.9813559, pvs: 0.9935205, table size: 511 time: 0ms
27 -420 f6d7 {319} c8d8 {498} e8d8 {398} c5d5 {422} in 3629 nodes, cutting: 0.98237574, pvs: 0.9626556, table size: 2329 time: 5ms
27 -492 a7a6 {318} c5c7 {317} d8c8 {218} c7c8 {394} e8e7 {394} in 41998 nodes, cutting: 0.9876698, pvs: 0.9787175, table size: 27082 time: 45ms
27 -490 a7a6 {318} c5c7 {317} d8c8 {218} c7c8 {394} e8e7 {394} c8h8 {492} in 266231 nodes, cutting: 0.99159896, pvs: 0.9750904, table size: 150217 time: 271ms
27 -517 f6d7 {319} c5c7 {318} d8c8 {219} c7c8 {395} e8e7 {395} c8h8 {493} d7c5 {491} in 2759763 nodes, cutting: 0.99390054, pvs: 0.98450583, table size: 1230286 time: 2652ms
27 -497 a7a6 {318} c5c7 {317} d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} in 15203719 nodes, cutting: 0.9937672, pvs: 0.98091525, table size: 5669470 time: 13855ms
9 27027456 61001
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
28 497 c8d8 {497} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
28 440 f1b5 {321} a6b5 {261} in 67 nodes, cutting: 1.0, pvs: 0.9952153, table size: 63 time: 0ms
28 457 c8d8 {497} e8d8 {397} c5d6 {397} in 500 nodes, cutting: 0.96640825, pvs: 0.94666666, table size: 412 time: 1ms
28 492 c5c7 {317} d8c8 {218} c7c8 {394} e8e7 {394} in 7049 nodes, cutting: 0.9774624, pvs: 0.97498864, table size: 5235 time: 9ms
28 490 c5c7 {317} d8c8 {218} c7c8 {394} e8e7 {394} c8h8 {492} in 52174 nodes, cutting: 0.9849419, pvs: 0.97885954, table size: 33312 time: 46ms
28 518 c5c7 {317} d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 1001671 nodes, cutting: 0.98908633, pvs: 0.9876771, table size: 537006 time: 1085ms
28 497 c5c7 {317} d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} in 6515877 nodes, cutting: 0.9892156, pvs: 0.98413795, table size: 2790617 time: 6171ms
8 27568128 106192
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
29 -218 d8c8 {218} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
29 -394 d8c8 {218} c7c8 {394} in 19 nodes, cutting: 1.0, pvs: 1.0, table size: 18 time: 0ms
29 -492 d8c8 {218} c7c8 {394} e8e7 {394} in 116 nodes, cutting: 1.0, pvs: 1.0, table size: 97 time: 1ms
29 -490 d8c8 {218} c7c8 {394} e8e7 {394} c8h8 {492} in 649 nodes, cutting: 0.9980769, pvs: 0.9883041, table size: 531 time: 0ms
29 -518 d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 4405 nodes, cutting: 0.9994635, pvs: 0.990228, table size: 3118 time: 4ms
29 -497 d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} in 28576 nodes, cutting: 0.9977598, pvs: 0.9558199, table size: 17047 time: 20ms
29 -531 d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 161809 nodes, cutting: 0.9978297, pvs: 0.9706573, table size: 76739 time: 130ms
29 -524 d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} in 1098517 nodes, cutting: 0.99826294, pvs: 0.97002465, table size: 450467 time: 890ms
29 -550 d8c8 {218} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 6638957 nodes, cutting: 0.997527, pvs: 0.9770364, table size: 2184362 time: 5475ms
10 27949056 38359
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
30 394 c7c8 {394} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
30 492 c7c8 {394} e8e7 {394} in 63 nodes, cutting: 1.0, pvs: 1.0, table size: 62 time: 0ms
30 490 c7c8 {394} e8e7 {394} c8h8 {492} in 557 nodes, cutting: 1.0, pvs: 0.9856115, table size: 492 time: 0ms
30 518 c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 4208 nodes, cutting: 1.0, pvs: 0.98876405, table size: 3013 time: 4ms
30 497 c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} in 28283 nodes, cutting: 0.9978252, pvs: 0.95189637, table size: 16897 time: 20ms
30 531 c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 160226 nodes, cutting: 0.99805504, pvs: 0.9693126, table size: 75396 time: 127ms
30 524 c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} in 1095823 nodes, cutting: 0.998348, pvs: 0.96927863, table size: 448267 time: 876ms
30 550 c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 6597639 nodes, cutting: 0.99757886, pvs: 0.9768723, table size: 2154494 time: 5452ms
9 27847680 38215
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
31 -492 e8e7 {394} in 2 nodes, cutting: 0.0, pvs: 1.0, table size: 2 time: 0ms
31 -490 e8e7 {394} c8h8 {492} in 67 nodes, cutting: 1.0, pvs: 0.9672131, table size: 65 time: 0ms
31 -518 e8e7 {394} c8h8 {492} h7h6 {492} in 611 nodes, cutting: 1.0, pvs: 0.9815951, table size: 540 time: 1ms
31 -497 e8e7 {394} c8h8 {492} g7g6 {495} f1d3 {499} in 6138 nodes, cutting: 0.990065, pvs: 0.9366626, table size: 4659 time: 5ms
31 -531 e8e7 {394} c8h8 {492} g7g6 {495} f3e5 {497} f6h5 {505} in 36626 nodes, cutting: 0.9916281, pvs: 0.96482176, table size: 23667 time: 32ms
31 -524 e8e7 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} in 404344 nodes, cutting: 0.99561006, pvs: 0.9669387, table size: 217746 time: 386ms
31 -550 e8e7 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 2960759 nodes, cutting: 0.9948002, pvs: 0.97638476, table size: 1249919 time: 2788ms
31 -31990 e8e7 {394} c8c7 {396} f6d7 {397} c3d5 {423} e7e6 {427} f3d4 {429} e6d5 {369} c2c4 {367} in 25591264 nodes, cutting: 0.9970791, pvs: 0.97466415, table size: 8768501 time: 25057ms
8 25591264 37812
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
32 492 c8h8 {492} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
32 490 c8h8 {492} f6e4 {490} in 65 nodes, cutting: 1.0, pvs: 0.9672131, table size: 64 time: 0ms
32 518 c8h8 {492} h7h6 {492} c3d5 {518} in 608 nodes, cutting: 1.0, pvs: 0.9815951, table size: 539 time: 0ms
32 497 c8h8 {492} g7g6 {495} f1d3 {499} f6e4 {497} in 6134 nodes, cutting: 0.990065, pvs: 0.9366626, table size: 4658 time: 5ms
32 531 c8h8 {492} g7g6 {495} f3e5 {497} f6h5 {505} c3d5 {531} in 36621 nodes, cutting: 0.9916281, pvs: 0.96482176, table size: 23666 time: 28ms
32 0 h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 404338 nodes, cutting: 0.99561006, pvs: 0.9669387, table size: 217746 time: 389ms
32 0 h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} a8a8 {394} h1h1 {394} in 2960752 nodes, cutting: 0.9948002, pvs: 0.97638476, table size: 1249919 time: 2802ms
32 31991 c8c7 {396} f6d7 {397} c3d5 {423} e7e6 {427} f3d4 {429} e6d5 {369} c2c4 {367} d5e4 {369} in 25591256 nodes, cutting: 0.9970791, pvs: 0.97466415, table size: 8768501 time: 25338ms
8 25591256 37812
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
33 -420 e7f8 {394} in 7 nodes, cutting: 1.0, pvs: 0.9872612, table size: 5 time: 0ms
33 -455 e7f8 {394} c7d8 {393} in 285 nodes, cutting: 0.9911111, pvs: 0.9759887, table size: 197 time: 0ms
33 -451 e7f8 {394} c7c5 {395} f8g8 {391} in 2693 nodes, cutting: 0.99022657, pvs: 0.9698647, table size: 1451 time: 3ms
33 -483 f6d7 {397} f3d4 {399} h8e8 {398} c3d5 {424} in 28791 nodes, cutting: 0.9737885, pvs: 0.9759936, table size: 21326 time: 25ms
33 -497 e7e8 {396} c7c8 {394} a8a8 {394} h1h1 {394} a8a8 {394} in 328789 nodes, cutting: 0.9881862, pvs: 0.96866316, table size: 170174 time: 343ms
33 -531 e7e8 {396} c7c8 {394} e8e7 {394} c8h8 {492} a8a8 {492} h1h1 {492} in 2470482 nodes, cutting: 0.9918155, pvs: 0.9760114, table size: 1008155 time: 2482ms
33 -31992 f6d7 {397} c3d5 {423} e7e6 {427} f3d4 {429} e6d5 {369} c2c4 {367} d5e4 {369} in 9287678 nodes, cutting: 0.99241865, pvs: 0.96704596, table size: 3661140 time: 7790ms
7 9287678 98865
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
34 458 c7d7 {458} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
34 482 c3d5 {423} e7f8 {421} in 68 nodes, cutting: 1.0, pvs: 0.9917355, table size: 62 time: 0ms
34 483 c3d5 {423} e7e6 {427} d5f4 {426} in 632 nodes, cutting: 0.99607074, pvs: 0.96443814, table size: 474 time: 1ms
34 483 f3d4 {399} h8e8 {398} c3d5 {424} e7f8 {422} in 7770 nodes, cutting: 0.9598312, pvs: 0.9766206, table size: 5670 time: 7ms
34 31993 c3d5 {423} e7e6 {427} f3d4 {429} e6d5 {369} c2c4 {367} in 60726 nodes, cutting: 0.9789733, pvs: 0.9647516, table size: 34664 time: 59ms
5 60726 7206
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
35 -482 e7f8 {421} in 5 nodes, cutting: 1.0, pvs: 0.99038464, table size: 4 time: 0ms
35 -483 e7e6 {427} d5f4 {426} in 246 nodes, cutting: 0.995, pvs: 0.9563107, table size: 170 time: 0ms
35 -482 e7e6 {427} d5f4 {426} e6e7 {422} in 2342 nodes, cutting: 0.96914226, pvs: 0.96498454, table size: 1418 time: 3ms
35 -31994 e7e6 {427} f3d4 {429} e6d5 {369} c2c4 {367} in 27846 nodes, cutting: 0.977856, pvs: 0.96393746, table size: 15633 time: 25ms
4 27846 5435
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
36 488 c7d7 {488} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
36 483 d5f4 {426} e6e7 {422} in 70 nodes, cutting: 1.0, pvs: 0.96595746, table size: 63 time: 0ms
36 482 d5f4 {426} e6e7 {422} f4d5 {423} in 788 nodes, cutting: 0.96879876, pvs: 0.9675052, table size: 617 time: 1ms
36 31995 f3d4 {429} e6d5 {369} c2c4 {367} d5e4 {369} in 9372 nodes, cutting: 0.9658511, pvs: 0.9659319, table size: 6727 time: 12ms
4 9372 2994
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
37 -430 e6d5 {369} in 2 nodes, cutting: 0.0, pvs: 1.0, table size: 2 time: 0ms
37 -450 e6d5 {369} c7d7 {430} in 64 nodes, cutting: 1.0, pvs: 0.9929078, table size: 62 time: 0ms
37 -31996 e6d5 {369} c2c4 {367} d5e4 {369} in 757 nodes, cutting: 0.9851485, pvs: 0.9752322, table size: 645 time: 1ms
3 757 323
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
38 430 c7d7 {430} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
38 450 c7d7 {430} d5c5 {428} in 62 nodes, cutting: 1.0, pvs: 0.9929078, table size: 61 time: 0ms
38 31997 c2c4 {367} d5e4 {369} c7f4 {370} in 754 nodes, cutting: 0.9851485, pvs: 0.9752322, table size: 644 time: 1ms
3 754 323
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
39 -430 d5e4 {369} in 2 nodes, cutting: 0.0, pvs: 1.0, table size: 2 time: 0ms
39 -31998 d5e4 {369} c7f4 {370} in 54 nodes, cutting: 1.0, pvs: 0.98540145, table size: 48 time: 0ms
2 54 137
Size: 33554431
Keys: 0
Values: 0
Stats: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
5%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
95%: IntSummaryStatistics{count=0, sum=0, min=2147483647, average=0.000000, max=-2147483648}
40 430 c7d7 {430} in 1 nodes, cutting: 0.0, pvs: 1.0, table size: 1 time: 0ms
40 31999 c7f4 {370} a8a8 {370} in 52 nodes, cutting: 1.0, pvs: 0.98540145, table size: 47 time: 0ms

 */