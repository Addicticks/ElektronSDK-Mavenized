package com.thomsonreuters.upa.transport;

/**
 * Options used for configuring a segmented network (separate send and receive networks).
 * 
 * @see ConnectOptions
 */
public interface SegmentedNetworkInfo
{
    /**
     * Configures the receive address or hostname to use in a segmented network configuration.
     * All content is received on this recvAddress:recvServiceName pair.
     * 
     * @param recvAddress the recvAddress to set
     */
    public SegmentedNetworkInfo recvAddress(String recvAddress);

    /**
     * Address or hostname to use in a segmented network configuration.
     * All content is received on this recvAddress:recvServiceName pair.
     * 
     * @return the recvAddress
     */
    public String recvAddress();

    /**
     * Configures the receive network's numeric port number or service name (as
     * defined in etc/services file) to use in a segmented network configuration.
     * All content is received on this recvAddress:recvServiceName pair.
     * 
     * @param recvServiceName the recvServiceName to set
     */
    public SegmentedNetworkInfo recvServiceName(String recvServiceName);

    /**
     * The receive network's numeric port number or service name (as defined in
     * etc/services file) to use in a segmented network configuration.
     * All content is received on this recvAddress:recvServiceName pair.
     * 
     * @return the recvServiceName
     */
    public String recvServiceName();

    /**
     * Configures the numeric port number or service name (as defined in
     * etc/services file) to use for all unicast UDP traffic in a unified
     * network configuration. This parameter is only required for multicast
     * connection types (ConnectionTypes.RELIABLE_MCAST). If multiple
     * connections or applications are running on the same host, this must be
     * unique for each connection.
     * 
     * @param unicastServiceName the unicastServiceName to set
     */
    public SegmentedNetworkInfo unicastServiceName(String unicastServiceName);

    /**
     * The numeric port number or service name (as defined in etc/services file)
     * to use for all unicast UDP traffic in a unified network configuration.
     * This parameter is only required for multicast connection types
     * (ConnectionTypes.RELIABLE_MCAST). If multiple connections or applications
     * are running on the same host, this must be unique for each connection.
     * 
     * @return the unicastServiceName
     */
    public String unicastServiceName();

    /**
     * Network interface card to bind to for send and recv networks.
     * If NULL, will use default NIC.
     * 
     * @param interfaceName the interfaceName to set
     */
    public SegmentedNetworkInfo interfaceName(String interfaceName);

    /**
     * A character representation of an IP address or hostname associated with
     * the local network interface to use for sending and receiving content.
     * This value is intended for use in systems which have multiple network
     * interface cards, and if not specified the default network interface will be used.
     * 
     * @return the interfaceName
     */
    public String interfaceName();

    /**
     * Configures the send address or hostname to use in a segmented network configuration.
     * All content is sent on this sendAddress:sendServiceName pair.
     * 
     * @param sendAddress the sendAddress to set
     */
    public SegmentedNetworkInfo sendAddress(String sendAddress);

    /**
     * The send address or hostname to use in a segmented network configuration.
     * All content is sent on this sendAddress:sendServiceName pair.
     * 
     * @return the sendAddress
     */
    public String sendAddress();

    /**
     * Configures the send network's numeric port number or service name (as
     * defined in etc/services file) to use in a segmented network configuration.
     * All content is sent on this sendAddress:sendServiceName pair.
     * 
     * @param sendServiceName the sendServiceName to set
     */
    public SegmentedNetworkInfo sendServiceName(String sendServiceName);

    /**
     * The send network's numeric port number or service name (as defined in
     * etc/services file) to use in a segmented network configuration.
     * All content is sent on this sendAddress:sendServiceName pair.
     * 
     * @return the sendServiceName
     */
    public String sendServiceName();
}
