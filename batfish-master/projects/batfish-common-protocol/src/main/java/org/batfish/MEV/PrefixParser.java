package org.batfish.MEV;

public class PrefixParser {

    public static String convertToBinaryIP(String cidr) {
        String[] parts = cidr.split("/");
        String ip = parts[0];
        String[] octets = ip.split("\\.");
        StringBuilder binaryIP = new StringBuilder();

        for (String octet : octets) {
            int decimal = Integer.parseInt(octet);
            String binaryString = String.format("%08d", Integer.parseInt(Integer.toBinaryString(decimal)));
            binaryIP.append(binaryString);
        }

        // Remove the trailing dot
        return binaryIP.substring(0, Integer.parseInt(parts[1]));
    }

    /**
     * Extracts the prefix length from a CIDR notation.
     * Example: "192.168.0.0/12" -> 12
     */
    public static int extractPrefixLength(String cidr) {
        String[] parts = cidr.split("/");
        return Integer.parseInt(parts[1]);
    }
}
