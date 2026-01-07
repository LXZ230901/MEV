package org.batfish.MEV;



import net.sf.javabdd.BDD;

import java.util.ArrayList;
import java.util.List;

public class BDDPrefixExtractor {


    // 递归方法从BDD中提取所有前缀
    public static List<String> extractAllPrefixes(BDD bdd, int maxDepth) {
        List<String> prefixes = new ArrayList<>();
        extractAllPrefixesHelper(bdd, maxDepth, 0, "", prefixes);
        return prefixes;
    }

    private static void extractAllPrefixesHelper(BDD bdd, int maxDepth, int currentDepth, String currentPrefix, List<String> prefixes) {
        if (bdd.isZero()) {
            // BDD为零，表示当前路径无效，返回
            return;
        }

        if (bdd.isOne() || currentDepth == maxDepth) {
            // BDD为一或已达到最大深度，表示找到一个完整的前缀
            String ipPrefix = convertBinaryPrefixToIPv4(currentPrefix) + "/" + currentDepth;
            prefixes.add(ipPrefix);
            return;
        }

        // 向下递归左子树
        extractAllPrefixesHelper(bdd.low(), maxDepth, currentDepth + 1, currentPrefix + "0", prefixes);
        // 向下递归右子树
        extractAllPrefixesHelper(bdd.high(), maxDepth, currentDepth + 1, currentPrefix + "1", prefixes);
    }


    private static String convertBinaryPrefixToIPv4(String binaryPrefix) {
        StringBuilder ip = new StringBuilder();
        int len = binaryPrefix.length();
        for (int i = 0; i < 32; i += 8) {
            int byteValue = 0;
            for (int j = 0; j < 8; j++) {
                if (i + j < len && binaryPrefix.charAt(i + j) == '1') {
                    byteValue |= (1 << (7 - j));
                }
            }
            ip.append(byteValue);
            if (i < 24) {
                ip.append(".");
            }
        }
        return ip.toString();
    }
}

