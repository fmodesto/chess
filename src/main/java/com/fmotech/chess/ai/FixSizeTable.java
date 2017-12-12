package com.fmotech.chess.ai;

public class FixSizeTable {

    private final long[] table;
    private final int mask;
    private int size = 0;
    private int overwrite = 0;

    public FixSizeTable(int sizeInMb) {
        int size = Integer.highestOneBit(sizeInMb * 1024 * 1024 / 16);
        this.table = new long[2 * size];
        this.mask = size - 1;
    }

    public boolean containsKey(long key) {
        int hash = hash(key);
        return table[hash] == key;
    }

    public long get(long key) {
        int hash = hash(key);
        return table[hash] == key ? table[hash + 1] : 0;
    }

    public void put(long key, long value) {
        updateStats(key);
        int hash = hash(key);
        table[hash] = key;
        table[hash + 1] = value;
    }

    private void updateStats(long key) {
        int hash = hash(key);
        if (table[hash] == 0)
            size++;
        else if (table[hash] != key)
            overwrite++;
    }

    private int hash(long key) {
        return (int) (key & mask) << 1;
    }

    public int size() {
        return size;
    }

    public int overwrite() {
        return overwrite;
    }
}
/*
/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin/java "-javaagent:/Applications/IntelliJ IDEA CE.app/Contents/lib/idea_rt.jar=55228:/Applications/IntelliJ IDEA CE.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/deploy.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/dnsns.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/jaccess.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/jfxrt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/localedata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/nashorn.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/sunec.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/ext/zipfs.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/javaws.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jfxswt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/management-agent.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/plugin.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/ant-javafx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/javafx-mx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/packager.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/sa-jdi.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/tools.jar:/Users/fran/IdeaProjects/chess/target/classes:/Users/fran/.m2/repository/org/apache/commons/commons-lang3/3.6/commons-lang3-3.6.jar:/Users/fran/.m2/repository/it/unimi/dsi/fastutil/8.1.1/fastutil-8.1.1.jar:/Users/fran/.m2/repository/junit/junit/4.12/junit-4.12.jar:/Users/fran/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar com.fmotech.chess.Game
objc[16566]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin/java (0x10cccb4c0) and /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/jre/lib/libinstrument.dylib (0x10cd574e0). One of the two will be used. Which one is undefined.
 1. e2e4 e7e5  2. b1c3 b8c6  3. f1c4 f8c5  4. d2d3 d7d6  5. d1h5 g7g6  6. h5d1 d8h4  7. g2g3 h4d8  8. c3d5 c8e6
 9. g1f3 e6d5 10. e4d5 c6a5 11. c1e3 c5e3 12. f2e3 a5c4 13. d3c4 f7f5 14. c2c3 g8f6 15. d1a4 d8d7 16. a4d7 e8d7
17. a1d1 h8e8 18. f3g5 c7c6 19. d1d3 f6e4 20. g5e4 f5e4 21. d3d1 c6d5 22. c4d5 e8f8 23. e1e2 g6g5 24. g3g4 d7e7
25. h1f1 f8f1 26. d1f1 a8c8 27. f1f5 h7h6 28. f5f1 c8c5 29. f1d1 c5a5 30. a2a3 a5c5 31. e2f2 e7f8 32. f2g1 f8g8
33. d1d2 g8h8 34. d2d1 h8h7 35. d1d2 h7g8 36. d2d1 g8h8 37. d1d2 h8h7 38. d2d1 h7g7 39. d1d2 g7f7 40. d2f2 f7e8
41. f2d2 e8d8 42. d2d1 d8c8 43. d1d2 c8b8 44. d2d1 b8c7 45. g1g2 c7b6 46. a3a4 c5c4 47. a4a5 b6a6 48. d1c1 c4c5
49. c3c4 a6a5 50. c1a1 a5b4 51. a1a7 b4c4 52. a7b7 c4d5 53. b7f7 d5e6 54. f7h7 c5c2 55. g2g1 c2b2 56. h7h6 e6d5
57. g1f1 d5c5 58. h6g6 b2h2 59. g6g5 c5c4 60. g5h5 h2c2 61. g4g5 c4d3 62. h5h6 d6d5 63. h6e6 d3e3 64. e6e5 d5d4
65. g5g6 d4d3 66. g6g7 d3d2 67. e5d5 c2c8 68. g7g8r c8g8 69. d5d7 g8f8 70. f1g1 e3e2 71. d7d4 e4e3 72. d4d7 d2d1q
73. d7d1 e2d1 74. g1h1 e3e2 75. h1g1 e2e1q 76. g1h2 f8g8 77. h2h3 e1g3

info score cp 30 depth 1 nodes 24 time 3 pv e2e4
ordering 0.000000 pvs 0.842105 hash 0/21 pv 1
info score cp 0 depth 2 nodes 146 time 3 pv e2e4 e7e5
ordering 0.476190 pvs 0.894737 hash 0/117 pv 2
info score cp 25 depth 3 nodes 1017 time 8 pv e2e4 d7d5 f1d3
ordering 0.486726 pvs 0.908497 hash 0/841 pv 4
info score cp 0 depth 4 nodes 3511 time 15 pv e2e4 d7d5 f1b5 c8d7
ordering 0.591865 pvs 0.928571 hash 0/2584 pv 7
info score cp 25 depth 5 nodes 23705 time 35 pv e2e4 e7e5 d2d4 d7d5 c1e3
ordering 0.630824 pvs 0.940182 hash 6/18068 pv 21
info score cp 0 depth 6 nodes 76820 time 62 pv e2e4 e7e5 d2d4 d7d5 c1e3 c8e6
ordering 0.668226 pvs 0.947761 hash 118/53315 pv 22
info score cp 25 depth 7 nodes 409459 time 304 pv e2e4 e7e5 g1f3 g8f6 b1c3 f8d6 d2d4
ordering 0.732996 pvs 0.943429 hash 3206/303552 pv 115
info score cp 5 depth 8 nodes 1898067 time 914 pv e2e4 e7e5 g1f3 g8f6 b1c3 f8d6 d2d4 b8c6
ordering 0.735981 pvs 0.946287 hash 59017/1266568 pv 116
info score cp 20 depth 9 nodes 37092466 time 14305 pv d2d4
ordering 0.704920 pvs 0.945745 hash 14999927/13220341 pv 1598
 1. e2e4 info score cp 5 depth 10 nodes 377039638 time 142002 pv e2e4
ordering 0.728000 pvs 0.922746 hash 262252086/16776724 pv 15734
info score cp 0 depth 1 nodes 24 time 0 pv e7e5
ordering 0.000000 pvs 0.842105 hash 0/21 pv 1
info score cp -25 depth 2 nodes 218 time 0 pv d7d5 f1d3
ordering 0.722222 pvs 0.885417 hash 0/123 pv 3
info score cp 0 depth 3 nodes 1073 time 0 pv d7d5 f1b5 c8d7
ordering 0.788462 pvs 0.916667 hash 0/772 pv 6
info score cp -25 depth 4 nodes 10677 time 3 pv e7e5 d2d4 d7d5 c1e3
ordering 0.672300 pvs 0.936202 hash 1/7132 pv 20
info score cp 0 depth 5 nodes 29518 time 7 pv e7e5 d2d4 d7d5 c1e3 c8e6
ordering 0.725573 pvs 0.944175 hash 15/21550 pv 21
info score cp -25 depth 6 nodes 175716 time 60 pv e7e5 g1f3 g8f6 b1c3 f8d6 d2d4
ordering 0.760498 pvs 0.940307 hash 430/114469 pv 117
info score cp -5 depth 7 nodes 707072 time 197 pv e7e5 g1f3 g8f6 b1c3 f8d6 d2d4 b8c6
ordering 0.756324 pvs 0.943089 hash 7935/483286 pv 118
info score cp -15 depth 8 nodes 3505245 time 1097 pv e7e5 b1c3 g8f6 f1c4 f8b4 g1e2
ordering 0.786217 pvs 0.940990 hash 174313/2137060 pv 400
info score cp -5 depth 9 nodes 42639868 time 16257 pv e7e5
ordering 0.759950 pvs 0.939410 hash 17133975/13857266 pv 1002
info score cp -13 depth 10 nodes 315045043 time 114919 pv e7e5
ordering 0.765701 pvs 0.945015 hash 212987972/16776477 pv 4405
e7e5 info score cp 30 depth 1 nodes 37 time 0 pv d2d4
ordering 1.000000 pvs 0.892857 hash 0/30 pv 1
info score cp 0 depth 2 nodes 293 time 0 pv d2d4 d7d5
ordering 0.578947 pvs 0.929412 hash 0/201 pv 2
info score cp 25 depth 3 nodes 2046 time 1 pv d2d4 d7d5 c1e3
ordering 0.645604 pvs 0.944134 hash 0/1481 pv 3
info score cp 0 depth 4 nodes 10344 time 3 pv d2d4 d7d5 c1e3 c8e6
ordering 0.673505 pvs 0.958064 hash 0/7199 pv 4
info score cp 25 depth 5 nodes 110677 time 35 pv g1f3 g8f6 b1c3 f8d6 d2d4
ordering 0.735712 pvs 0.945117 hash 167/73169 pv 92
info score cp 5 depth 6 nodes 225183 time 51 pv g1f3 g8f6 b1c3 f8d6 d2d4 b8c6
ordering 0.793264 pvs 0.947969 hash 633/129152 pv 93
info score cp 15 depth 7 nodes 2197428 time 808 pv b1c3 g8f6 f1c4 f8b4 g1e2 b8c6 d2d3
ordering 0.772658 pvs 0.941271 hash 70991/1442076 pv 387
info score cp 5 depth 8 nodes 10982976 time 3541 pv g1f3 g8f6 f1b5 f6e4 b5d3 e4f6 f3e5 d7d5
ordering 0.793244 pvs 0.940643 hash 1411204/5658591 pv 929
info score cp 13 depth 9 nodes 90726847 time 32780 pv d2d4
ordering 0.784303 pvs 0.945722 hash 48301199/16224376 pv 1626

Process finished with exit code 130 (interrupted by signal 2: SIGINT)

 */