package org.batfish.MEV;

public class PacketWrapper {
    private Packet packet;
    private String wrapperId;  // Example: A unique identifier for the wrapper
    private long timestamp;    // Example: Timestamp for when the packet was wrapped
    private String wrapperDescription;  // Description or metadata for this wrapper

    // Constructor
    public PacketWrapper(Packet packet, String wrapperId, String description) {
        this.packet = packet;
        this.wrapperId = wrapperId;
        this.timestamp = System.currentTimeMillis();  // Set the current time
        this.wrapperDescription = description;
    }

    // Getters and Setters for wrapper properties
    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public String getWrapperId() {
        return wrapperId;
    }

    public void setWrapperId(String wrapperId) {
        this.wrapperId = wrapperId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getWrapperDescription() {
        return wrapperDescription;
    }

    public void setWrapperDescription(String wrapperDescription) {
        this.wrapperDescription = wrapperDescription;
    }

    /**
     * Unwraps the packet by returning the original Packet object.
     */
    public Packet unwrapPacket() {
        return this.packet;
    }

    /**
     * Displays the wrapper details and the enclosed packet as a string.
     */
    @Override
    public String toString() {
        return "PacketWrapper{" +
                "packet=" + packet +
                ", wrapperId='" + wrapperId + '\'' +
                ", timestamp=" + timestamp +
                ", wrapperDescription='" + wrapperDescription + '\'' +
                '}';
    }
}
