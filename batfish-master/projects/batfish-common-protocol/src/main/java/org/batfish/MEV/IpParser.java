package org.batfish.MEV;

public class IpParser {

    public static String convertToBinaryIP(String ip) {
        String[] octets = ip.split("\\.");
        StringBuilder binaryIP = new StringBuilder();

        for (String octet : octets) {
            int decimal = Integer.parseInt(octet);
            String binaryString = String.format("%08d", Integer.parseInt(Integer.toBinaryString(decimal)));
            binaryIP.append(binaryString);
        }

        // Remove the trailing dot
        return binaryIP.toString();
    }
}
