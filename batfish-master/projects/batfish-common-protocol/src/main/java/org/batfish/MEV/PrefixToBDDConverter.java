package org.batfish.MEV;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class PrefixToBDDConverter {

    // 将一个前缀转换为 BDD
    public static BDD convertPrefixToBDD(BDDFactory factory, String prefix) {
        String[] parts = prefix.split("/");
        String ip = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        // 将IP地址转换为二进制数组
        int[] ipBits = ipToBinary(ip);

        // 创建初始BDD (true, 1)
        BDD resultBDD = factory.one();

        // 根据前缀长度生成BDD
        for (int i = 0; i < prefixLength; i++) {
            boolean bit = ipBits[i] == 1;
            BDD bitBDD = factory.ithVar(i); // 创建BDD变量

            if (bit) {
                resultBDD = resultBDD.and(bitBDD); // 与操作，bit为1
            } else {
                resultBDD = resultBDD.and(bitBDD.not()); // 与操作，bit为0
            }
        }

        return resultBDD;
    }

    // 将IP地址转换为二进制数组
    private static int[] ipToBinary(String ip) {
        String[] octets = ip.split("\\.");
        int[] binary = new int[32];

        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            for (int j = 0; j < 8; j++) {
                binary[i * 8 + j] = (octet >> (7 - j)) & 1; // 将每个字节转换为二进制位
            }
        }

        return binary;
    }
}

