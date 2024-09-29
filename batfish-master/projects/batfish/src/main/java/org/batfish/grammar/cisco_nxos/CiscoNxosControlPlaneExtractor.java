package org.batfish.grammar.cisco_nxos;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import com.google.common.primitives.Ints;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DscpType;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Nve;
import org.batfish.datamodel.Nve.HostReachabilityProtocol;
import org.batfish.datamodel.Nve.IngressReplicationProtocol;
import org.batfish.datamodel.NveVni;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;



import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Aaagr_source_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Aaagr_use_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Aaagt_source_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Aaagt_use_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acl_fragmentsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acl_l3_protocolContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acl_lineContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acll_actionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acll_remarkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Aclla_icmpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Aclla_tcpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Aclla_udpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3_address_specContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3_dst_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3_fragmentsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3_protocol_specContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3_src_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3o_dscpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3o_logContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3o_packet_lengthContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3o_packet_length_specContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3o_precedenceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal3o_ttlContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4icmp_optionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4igmp_optionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcp_destination_portContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcp_optionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcp_port_specContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcp_port_spec_literalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcp_port_spec_port_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcp_source_portContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcpo_establishedContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcpo_flagsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4tcpo_tcp_flags_maskContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4udp_destination_portContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4udp_optionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4udp_port_specContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4udp_port_spec_literalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4udp_port_spec_port_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Acllal4udp_source_portContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Allowas_in_max_occurrencesContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.As_path_regexContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Banner_execContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Banner_motdContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Bgp_asnContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Bgp_asn_rangeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Bgp_instanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Both_export_importContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Channel_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cisco_nxos_configurationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Class_map_cp_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Class_map_network_qos_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Class_map_qos_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Class_map_queuing_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cm_control_planeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cm_network_qosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cm_qosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cm_queuingContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cmcpm_access_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cmqm_access_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cp_service_policyContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Dscp_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Dscp_specContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ebgp_multihop_ttlContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Eigrp_asnContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Eigrp_instanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ev_vniContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Evv_rdContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Evv_route_targetContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Fe_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Fe_sourceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ff_admin_distanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ff_anycast_gateway_macContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Flow_exporterContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Flow_monitorContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Flow_recordContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Fm_exporterContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Fm_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Fm_recordContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Fr_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Generic_access_list_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_autostateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_bandwidthContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_channel_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_delayContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_encapsulationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_fabric_forwardingContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_access_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_address_concreteContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_address_dhcpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_bandwidthContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_delayContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_dhcp_relayContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_eigrpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_forwardContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_hello_intervalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_hold_timeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_passive_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_policyContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_proxy_arpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ipv6_address_concreteContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ipv6_address_dhcpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_mtuContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_no_autostateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_no_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_no_vrf_memberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_private_vlanContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_speed_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_accessContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_mode_accessContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_mode_dot1q_tunnelContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_mode_fex_fabricContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_mode_monitorContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_mode_trunkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_monitorContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_trunk_allowedContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_trunk_nativeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_vrf_memberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Icl_expandedContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Icl_standardContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ih_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ih_versionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihd_minimumContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihd_reloadContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg4_ipContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg_ipv4Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg_ipv6Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg_no_preemptContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg_preemptContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg_priorityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg_timersContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihg_trackContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ihgam_key_chainContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iip6r_ospfv3Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iip_port_access_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipdl_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipdl_route_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipi_access_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipo_bfdContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipo_costContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipo_dead_intervalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipo_hello_intervalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipo_networkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipo_passive_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipo_priorityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipp_jp_policy_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipp_jp_policy_route_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipp_neighbor_policy_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipp_neighbor_policy_route_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipr_eigrpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipr_ospfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iipr_ripContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iiprip_rf_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Iiprip_rf_route_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Il_min_linksContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Inherit_sequence_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Inoip_forwardContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Inoip_proxy_arpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Inoipo_passive_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Inos_accessContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Inos_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.InoshutContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_bandwidth_eigrp_kbpsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_bandwidth_kbpsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_ipv6_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_mtuContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_prefixContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Interface_rangeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_access_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_access_list_line_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_access_list_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_as_path_access_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_as_path_access_list_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_as_path_access_list_seqContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_community_list_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_community_list_seqContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_domain_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_name_serverContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_prefixContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_prefix_list_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_prefix_list_line_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_prefix_list_line_prefix_lengthContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_prefix_list_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ip_route_networkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipp_rp_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipp_rp_candidateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipt_source_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipv6_access_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipv6_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipv6_prefixContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipv6_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipv6_prefix_list_line_prefix_lengthContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ipv6_routeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Isis_instanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ispt_qosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ispt_queuingContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Last_as_num_prependsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Line_actionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Literal_standard_communityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Logging_serverContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Logging_source_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Lv6_access_classContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Lv_access_classContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Mac_access_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Mac_access_list_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Mac_address_literalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Maxas_limitContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Maximum_pathsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Monitor_session_destinationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Monitor_session_source_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Monitor_session_source_vlanContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Name_serverContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.No_ip_route_networkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.No_sysds_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.No_sysds_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ntp_serverContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ntp_source_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ntpag_peerContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ntpag_query_onlyContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ntpag_serveContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ntpag_serve_onlyContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ntps_preferContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ntps_use_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nve_host_reachabilityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nve_memberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nve_no_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nve_source_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nvg_ingress_replicationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nvg_mcast_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nvg_suppress_arpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nvm_ingress_replicationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nvm_mcast_groupContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nvm_peer_ipContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Nvm_suppress_arpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Object_group_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ogip_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ogip_portContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ogipa_lineContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ogipp_lineContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ospf_area_default_costContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ospf_area_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ospf_area_range_costContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ospf_instanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ospf_priorityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ospfv3_instanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Packet_lengthContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pl6_actionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pl6_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pl_actionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pl_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pm_control_planeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pm_network_qosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pm_qosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pm_queuingContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pmcp_classContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pmnq_classContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pmq_classContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Pmqu_classContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Policy_map_cp_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Policy_map_network_qos_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Policy_map_qos_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Policy_map_queuing_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Protocol_distanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af4_aggregate_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af4_networkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af4_no_aggregate_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af4_redistributeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af6_aggregate_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af6_networkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af6_no_aggregate_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af6_redistributeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af_ipv4_multicastContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af_ipv4_unicastContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af_ipv6_multicastContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af_ipv6_unicastContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_af_l2vpnContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_aa_tailContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_additional_pathsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_client_to_clientContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_dampeningContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_default_informationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_default_metricContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_distanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_inject_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_maximum_pathsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_nexthop_route_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_suppress_inactiveContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afip_table_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afl2v_maximum_pathsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_afl2v_retainContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_bestpathContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_cluster_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_confederation_identifierContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_confederation_peersContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_enforce_first_asContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_log_neighbor_changesContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_maxas_limitContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_address_familyContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_advertise_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_allowas_inContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_as_overrideContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_default_originateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_disable_peer_as_checkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_filter_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_inheritContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_next_hop_selfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_next_hop_third_partyContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_no_default_originateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_route_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_route_reflector_clientContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_send_communityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_suppress_inactiveContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_af_unsuppress_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_ebgp_multihopContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_inheritContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_local_asContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_no_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_remote_asContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_remove_private_asContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_n_update_sourceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_neighborContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_no_enforce_first_asContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_no_neighborContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_router_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_template_peerContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_template_peer_policyContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_template_peer_sessionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_v_local_asContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rb_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Re_isolateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Re_no_isolateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Re_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rec_autonomous_systemContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rec_distanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rec_no_router_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rec_router_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Recaf4_redistributeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Recaf6_redistributeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Recaf_default_metricContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Recaf_ipv4Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Recaf_ipv6Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rip_instanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rm_continueContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_as_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_as_pathContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_communityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_metricContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_route_typeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_source_protocolContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_tagContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmm_vlanContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmmip6a_pbrContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmmip6a_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmmip_multicastContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmmipa_pbrContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmmipa_prefix_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rms_comm_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rms_communityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rms_local_preferenceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rms_metricContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rms_metric_typeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rms_originContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rms_tagContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rms_weightContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmsapp_last_asContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmsapp_literalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmsipnh_literalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rmsipnh_unchangedContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro3_af6_a_filter_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro3_af6_default_informationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro3_af6_rd_routing_instanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro3_af6_table_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro3_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro3a_nssa_o_default_information_originateContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_areaContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_auto_costContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_bfdContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_default_informationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_distanceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_max_metricContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_networkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_passive_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_router_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_summary_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ro_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Roa_authenticationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Roa_default_costContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Roa_filter_listContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Roa_nssaContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Roa_rangeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Roa_stubContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ror_redistribute_route_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rot_lsa_arrivalContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rot_lsa_group_pacingContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rott_lsaContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_distinguisherContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_distinguisher_or_autoContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_map_entryContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_map_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_map_pbr_statisticsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_map_sequenceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_networkContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_targetContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Route_target_or_autoContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_bgpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_eigrpContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_eigrp_process_tagContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_isis_process_tagContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_ospfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_ospf_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_ospfv3Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_ospfv3_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_ripContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Router_rip_process_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Routing_instance_v4Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Routing_instance_v6Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rr_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rrv_af4_default_informationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rrv_af4_redistributeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rrv_af6_default_informationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Rrv_af6_redistributeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_evpnContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_hostnameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_interface_nveContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_interface_regularContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_route_mapContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_trackContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_vdcContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_vrf_contextContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Snmp_communityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Snmps_communityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Snmps_community_use_aclContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Snmps_community_use_ipv4aclContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Snmps_community_use_ipv6aclContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Snmps_hostContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Snmpssi_informsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Snmpssi_trapsContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Standard_communityContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Static_route_definitionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Static_route_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Subnet_maskContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Sysds_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Sysds_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Sysqosspt_network_qosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Sysqosspt_qosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Sysqosspt_queueingContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Sysvlan_reserveContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Tcp_flags_maskContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Tcp_portContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Tcp_port_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Template_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Tir_vrfContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Track_interfaceContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Track_interface_modeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Track_ip_routeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Track_ip_slaContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Track_object_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Ts_hostContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Udp_portContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Udp_port_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Uint16Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Uint32Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Uint8Context;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Unreserved_vlan_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Unreserved_vlan_id_rangeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vc_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vc_no_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vc_rdContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vc_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vc_vniContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vcaf4u_route_targetContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vcaf6u_route_targetContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vcspt_qosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vdc_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vlan_idContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vlan_id_rangeContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vlan_vlanContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vni_numberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vrf_descriptionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vrf_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vrf_non_default_nameContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Vv_vn_segmentContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.cisco_nxos.ActionIpAccessListLine;
import org.batfish.representation.cisco_nxos.AddrGroupIpAddressSpec;
import org.batfish.representation.cisco_nxos.AddressFamily;
import org.batfish.representation.cisco_nxos.BgpVrfAddressFamilyAggregateNetworkConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfAddressFamilyConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfAddressFamilyConfiguration.Type;
import org.batfish.representation.cisco_nxos.BgpVrfConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfIpAddressFamilyConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfIpv4AddressFamilyConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfIpv6AddressFamilyConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfL2VpnEvpnAddressFamilyConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfL2VpnEvpnAddressFamilyConfiguration.RetainRouteType;
import org.batfish.representation.cisco_nxos.BgpVrfNeighborAddressFamilyConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfNeighborConfiguration;
import org.batfish.representation.cisco_nxos.BgpVrfNeighborConfiguration.RemovePrivateAsMode;
import org.batfish.representation.cisco_nxos.CiscoNxosConfiguration;
import org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType;
import org.batfish.representation.cisco_nxos.CiscoNxosStructureType;
import org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage;
import org.batfish.representation.cisco_nxos.DefaultVrfOspfProcess;
import org.batfish.representation.cisco_nxos.DistributeList;
import org.batfish.representation.cisco_nxos.DistributeList.DistributeListFilterType;
import org.batfish.representation.cisco_nxos.EigrpMetric;
import org.batfish.representation.cisco_nxos.EigrpProcessConfiguration;
import org.batfish.representation.cisco_nxos.EigrpVrfConfiguration;
import org.batfish.representation.cisco_nxos.EigrpVrfIpAddressFamilyConfiguration;
import org.batfish.representation.cisco_nxos.Evpn;
import org.batfish.representation.cisco_nxos.EvpnVni;
import org.batfish.representation.cisco_nxos.ExtendedCommunityOrAuto;
import org.batfish.representation.cisco_nxos.FragmentsBehavior;
import org.batfish.representation.cisco_nxos.HsrpGroup;
import org.batfish.representation.cisco_nxos.HsrpGroupIpv4;
import org.batfish.representation.cisco_nxos.HsrpGroupIpv6;
import org.batfish.representation.cisco_nxos.HsrpTrack;
import org.batfish.representation.cisco_nxos.IcmpOptions;
import org.batfish.representation.cisco_nxos.Interface;
import org.batfish.representation.cisco_nxos.InterfaceAddressWithAttributes;
import org.batfish.representation.cisco_nxos.InterfaceIpv6AddressWithAttributes;
import org.batfish.representation.cisco_nxos.IpAccessList;
import org.batfish.representation.cisco_nxos.IpAccessListLine;
import org.batfish.representation.cisco_nxos.IpAddressSpec;
import org.batfish.representation.cisco_nxos.IpAsPathAccessList;
import org.batfish.representation.cisco_nxos.IpAsPathAccessListLine;
import org.batfish.representation.cisco_nxos.IpCommunityList;
import org.batfish.representation.cisco_nxos.IpCommunityListExpanded;
import org.batfish.representation.cisco_nxos.IpCommunityListExpandedLine;
import org.batfish.representation.cisco_nxos.IpCommunityListStandard;
import org.batfish.representation.cisco_nxos.IpCommunityListStandardLine;
import org.batfish.representation.cisco_nxos.IpPrefixList;
import org.batfish.representation.cisco_nxos.IpPrefixListLine;
import org.batfish.representation.cisco_nxos.Ipv6AccessList;
import org.batfish.representation.cisco_nxos.Ipv6PrefixList;
import org.batfish.representation.cisco_nxos.Ipv6PrefixListLine;
import org.batfish.representation.cisco_nxos.IsisProcess;
import org.batfish.representation.cisco_nxos.Layer3Options;
import org.batfish.representation.cisco_nxos.LiteralIpAddressSpec;
import org.batfish.representation.cisco_nxos.LiteralPortSpec;
import org.batfish.representation.cisco_nxos.LoggingServer;
import org.batfish.representation.cisco_nxos.NameServer;
import org.batfish.representation.cisco_nxos.NtpServer;
import org.batfish.representation.cisco_nxos.ObjectGroup;
import org.batfish.representation.cisco_nxos.ObjectGroupIpAddress;
import org.batfish.representation.cisco_nxos.ObjectGroupIpAddressLine;
import org.batfish.representation.cisco_nxos.ObjectGroupIpPort;
import org.batfish.representation.cisco_nxos.ObjectGroupIpPortLine;
import org.batfish.representation.cisco_nxos.OspfArea;
import org.batfish.representation.cisco_nxos.OspfAreaAuthentication;
import org.batfish.representation.cisco_nxos.OspfAreaNssa;
import org.batfish.representation.cisco_nxos.OspfAreaRange;
import org.batfish.representation.cisco_nxos.OspfAreaStub;
import org.batfish.representation.cisco_nxos.OspfDefaultOriginate;
import org.batfish.representation.cisco_nxos.OspfInterface;
import org.batfish.representation.cisco_nxos.OspfMaxMetricRouterLsa;
import org.batfish.representation.cisco_nxos.OspfNetworkType;
import org.batfish.representation.cisco_nxos.OspfProcess;
import org.batfish.representation.cisco_nxos.OspfSummaryAddress;
import org.batfish.representation.cisco_nxos.OspfVrf;
import org.batfish.representation.cisco_nxos.PortGroupPortSpec;
import org.batfish.representation.cisco_nxos.PortSpec;
import org.batfish.representation.cisco_nxos.RemarkIpAccessListLine;
import org.batfish.representation.cisco_nxos.RouteDistinguisherOrAuto;
import org.batfish.representation.cisco_nxos.RouteMap;
import org.batfish.representation.cisco_nxos.RouteMapEntry;
import org.batfish.representation.cisco_nxos.RouteMapMatchAsNumber;
import org.batfish.representation.cisco_nxos.RouteMapMatchAsPath;
import org.batfish.representation.cisco_nxos.RouteMapMatchCommunity;
import org.batfish.representation.cisco_nxos.RouteMapMatchInterface;
import org.batfish.representation.cisco_nxos.RouteMapMatchIpAddress;
import org.batfish.representation.cisco_nxos.RouteMapMatchIpAddressPrefixList;
import org.batfish.representation.cisco_nxos.RouteMapMatchIpMulticast;
import org.batfish.representation.cisco_nxos.RouteMapMatchIpv6Address;
import org.batfish.representation.cisco_nxos.RouteMapMatchIpv6AddressPrefixList;
import org.batfish.representation.cisco_nxos.RouteMapMatchMetric;
import org.batfish.representation.cisco_nxos.RouteMapMatchRouteType;
import org.batfish.representation.cisco_nxos.RouteMapMatchSourceProtocol;
import org.batfish.representation.cisco_nxos.RouteMapMatchTag;
import org.batfish.representation.cisco_nxos.RouteMapMatchVlan;
import org.batfish.representation.cisco_nxos.RouteMapMetricType;
import org.batfish.representation.cisco_nxos.RouteMapSetAsPathPrependLastAs;
import org.batfish.representation.cisco_nxos.RouteMapSetAsPathPrependLiteralAs;
import org.batfish.representation.cisco_nxos.RouteMapSetCommListDelete;
import org.batfish.representation.cisco_nxos.RouteMapSetCommunity;
import org.batfish.representation.cisco_nxos.RouteMapSetIpNextHop;
import org.batfish.representation.cisco_nxos.RouteMapSetIpNextHopLiteral;
import org.batfish.representation.cisco_nxos.RouteMapSetIpNextHopUnchanged;
import org.batfish.representation.cisco_nxos.RouteMapSetLocalPreference;
import org.batfish.representation.cisco_nxos.RouteMapSetMetric;
import org.batfish.representation.cisco_nxos.RouteMapSetMetricEigrp;
import org.batfish.representation.cisco_nxos.RouteMapSetMetricType;
import org.batfish.representation.cisco_nxos.RouteMapSetOrigin;
import org.batfish.representation.cisco_nxos.RouteMapSetTag;
import org.batfish.representation.cisco_nxos.RouteMapSetWeight;
import org.batfish.representation.cisco_nxos.RoutingProtocolInstance;
import org.batfish.representation.cisco_nxos.SnmpCommunity;
import org.batfish.representation.cisco_nxos.SnmpServer;
import org.batfish.representation.cisco_nxos.StaticRoute;
import org.batfish.representation.cisco_nxos.StaticRouteV6;
import org.batfish.representation.cisco_nxos.SwitchportMode;
import org.batfish.representation.cisco_nxos.TacacsServer;
import org.batfish.representation.cisco_nxos.TcpOptions;
import org.batfish.representation.cisco_nxos.Track;
import org.batfish.representation.cisco_nxos.TrackInterface;
import org.batfish.representation.cisco_nxos.TrackIpRoute;
import org.batfish.representation.cisco_nxos.TrackUnsupported;
import org.batfish.representation.cisco_nxos.UdpOptions;
import org.batfish.representation.cisco_nxos.Vlan;
import org.batfish.representation.cisco_nxos.Vrf;
import org.batfish.representation.cisco_nxos.VrfAddressFamily;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.naturalOrder;
import static org.batfish.datamodel.IpWildcard.ipWithWildcardMask;
import static org.batfish.datamodel.Names.bgpNeighborStructureName;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.DEFAULT_CLASS_MAP_NAME;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.DEFAULT_POLICY_MAP_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.DEFAULT_POLICY_MAP_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.DEFAULT_VRF_NAME;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.MANAGEMENT_VRF_NAME;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.computeRouteMapEntryName;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.getAclLineName;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.getCanonicalInterfaceNamePrefix;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.BGP_NEIGHBOR;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.BGP_TEMPLATE_PEER;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.BGP_TEMPLATE_PEER_POLICY;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.BGP_TEMPLATE_PEER_SESSION;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.CLASS_MAP_CONTROL_PLANE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.CLASS_MAP_NETWORK_QOS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.CLASS_MAP_QOS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.CLASS_MAP_QUEUING;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.FLOW_EXPORTER;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.FLOW_MONITOR;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.FLOW_RECORD;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IPV6_ACCESS_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IPV6_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_ACCESS_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_ACCESS_LIST_ABSTRACT_REF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_ACCESS_LIST_LINE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_AS_PATH_ACCESS_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_COMMUNITY_LIST_ABSTRACT_REF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_COMMUNITY_LIST_EXPANDED;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_COMMUNITY_LIST_STANDARD;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_OR_MAC_ACCESS_LIST_ABSTRACT_REF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.IP_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.MAC_ACCESS_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.NVE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.OBJECT_GROUP_IP_ADDRESS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.OBJECT_GROUP_IP_PORT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.POLICY_MAP_CONTROL_PLANE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.POLICY_MAP_NETWORK_QOS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.POLICY_MAP_QOS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.POLICY_MAP_QUEUING;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.PORT_CHANNEL;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.ROUTER_EIGRP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.ROUTER_ISIS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.ROUTER_OSPF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.ROUTER_OSPFV3;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.ROUTER_RIP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.ROUTE_MAP_ENTRY;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.VLAN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureType.VRF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.AAA_GROUP_SERVER_RADIUS_SOURCE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.AAA_GROUP_SERVER_RADIUS_USE_VRF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.AAA_GROUP_SERVER_TACACSP_SOURCE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.AAA_GROUP_SERVER_TACACSP_USE_VRF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_ADDITIONAL_PATHS_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_ADVERTISE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_ATTRIBUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_DAMPENING_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_DEFAULT_ORIGINATE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_EXIST_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_INJECT_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_L2VPN_EVPN_RETAIN_ROUTE_TARGET_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR6_FILTER_LIST_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR6_FILTER_LIST_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR6_PREFIX_LIST_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR6_PREFIX_LIST_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_ADVERTISE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_EXIST_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_FILTER_LIST_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_FILTER_LIST_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_INHERIT_PEER;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_INHERIT_PEER_POLICY;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_INHERIT_PEER_SESSION;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_NON_EXIST_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_PREFIX_LIST_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_PREFIX_LIST_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_REMOTE_AS_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_SELF_REF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEIGHBOR_UPDATE_SOURCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NETWORK6_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NETWORK_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_NEXTHOP_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_REDISTRIBUTE_INSTANCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_REDISTRIBUTE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_SUPPRESS_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_TABLE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BGP_UNSUPPRESS_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.BUILT_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.CLASS_MAP_CP_MATCH_ACCESS_GROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.CLASS_MAP_QOS_MATCH_ACCESS_GROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.CONTROL_PLANE_SERVICE_POLICY;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.EIGRP_DISTRIBUTE_LIST_PREFIX_LIST_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.EIGRP_DISTRIBUTE_LIST_PREFIX_LIST_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.EIGRP_DISTRIBUTE_LIST_ROUTE_MAP_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.EIGRP_DISTRIBUTE_LIST_ROUTE_MAP_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.EIGRP_REDISTRIBUTE_INSTANCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.EIGRP_REDISTRIBUTE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.FLOW_EXPORTER_SOURCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.FLOW_MONITOR_EXPORTER;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.FLOW_MONITOR_RECORD;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_CHANNEL_GROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_ACCESS_GROUP_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_ACCESS_GROUP_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_EIGRP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_HELLO_INTERVAL_EIGRP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_HOLD_TIME_EIGRP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_IGMP_ACCESS_GROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_PIM_JP_POLICY_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_PIM_JP_POLICY_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_PIM_NEIGHBOR_POLICY_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_PIM_NEIGHBOR_POLICY_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_POLICY;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_PORT_ACCESS_GROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_RIP_ROUTE_FILTER_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_RIP_ROUTE_FILTER_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_ROUTER_EIGRP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_ROUTER_OSPF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_IP_ROUTER_RIP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_SELF_REFERENCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_SERVICE_POLICY_QOS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_SERVICE_POLICY_QUEUING;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_VLAN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.INTERFACE_VRF_MEMBER;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IPV6_ROUTE_NEXT_HOP_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IPV6_ROUTE_NEXT_HOP_VRF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_ACCESS_LIST_DESTINATION_ADDRGROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_ACCESS_LIST_DESTINATION_PORTGROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_ACCESS_LIST_LINE_SELF_REFERENCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_ACCESS_LIST_SOURCE_ADDRGROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_ACCESS_LIST_SOURCE_PORTGROUP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_PIM_RP_ADDRESS_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_PIM_RP_ADDRESS_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_PIM_RP_CANDIDATE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_PIM_RP_CANDIDATE_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_PIM_RP_CANDIDATE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_ROUTE_NEXT_HOP_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.IP_ROUTE_NEXT_HOP_VRF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.LINE_VTY_ACCESS_CLASS_IN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.LINE_VTY_ACCESS_CLASS_OUT;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.LOGGING_SOURCE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.MONITOR_SESSION_DESTINATION_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.MONITOR_SESSION_SOURCE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.MONITOR_SESSION_SOURCE_VLAN;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.NTP_ACCESS_GROUP_PEER;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.NTP_ACCESS_GROUP_QUERY_ONLY;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.NTP_ACCESS_GROUP_SERVE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.NTP_ACCESS_GROUP_SERVE_ONLY;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.NTP_SOURCE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.NVE_SELF_REFERENCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.NVE_SOURCE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPFV3_AREA_FILTER_LIST_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPFV3_DEFAULT_INFORMATION_ORIGINATE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPFV3_NSSA_DEFAULT_INFORMATION_ORIGINATE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPFV3_REDISTRIBUTE_INSTANCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPFV3_REDISTRIBUTE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPFV3_TABLE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPFV3_VRF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPF_AREA_NSSA_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPF_DEFAULT_INFORMATION_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPF_REDISTRIBUTE_INSTANCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.OSPF_REDISTRIBUTE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.POLICY_MAP_CLASS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.RIP_AF4_DEFAULT_INFORMATION_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.RIP_AF4_REDISTRIBUTE_INSTANCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.RIP_AF4_REDISTRIBUTE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.RIP_AF6_DEFAULT_INFORMATION_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.RIP_AF6_REDISTRIBUTE_INSTANCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.RIP_AF6_REDISTRIBUTE_ROUTE_MAP;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTER_EIGRP_SELF_REFERENCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTER_OSPFV3_SELF_REFERENCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTER_OSPF_SELF_REFERENCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTER_RIP_SELF_REFERENCE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTER_RIP_VRF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_CONTINUE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_ENTRY_PREV_REF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_MATCH_AS_PATH;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_MATCH_COMMUNITY;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_MATCH_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_MATCH_IPV6_ADDRESS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_MATCH_IPV6_ADDRESS_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.SNMP_SERVER_COMMUNITY_USE_ACL;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.SNMP_SERVER_COMMUNITY_USE_IPV4ACL;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.SNMP_SERVER_COMMUNITY_USE_IPV6ACL;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.SNMP_SERVER_SOURCE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.SYSQOS_NETWORK_QOS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.SYSQOS_QOS;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.SYSQOS_QUEUING;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.TACACS_SOURCE_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.TRACK_INTERFACE;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.TRACK_IP_ROUTE_VRF;
import static org.batfish.representation.cisco_nxos.CiscoNxosStructureUsage.VLAN_CONFIGURATION_QOS;
import static org.batfish.representation.cisco_nxos.Interface.VLAN_RANGE;
import static org.batfish.representation.cisco_nxos.Interface.newNonVlanInterface;
import static org.batfish.representation.cisco_nxos.Interface.newVlanInterface;




/**
 * Given a parse tree, builds a {@link CiscoNxosConfiguration} that has been prepopulated with
 * metadata and defaults by {@link CiscoNxosPreprocessor}.
 */
@ParametersAreNonnullByDefault
public final class CiscoNxosControlPlaneExtractor extends CiscoNxosParserBaseListener
    implements SilentSyntaxListener {

  private static final LongSpace BANDWIDTH_RANGE = LongSpace.of(Range.closed(1L, 100_000_000L));
  private static final LongSpace BANDWIDTH_EIGRP_RANGE =
      LongSpace.of(Range.closed(1L, 2_560_000_000L));
  private static final LongSpace BANDWIDTH_PORT_CHANNEL_RANGE =
      LongSpace.of(Range.closed(1L, 3_200_000_000L));
  private static final IntegerSpace BGP_ALLOWAS_IN = IntegerSpace.of(Range.closed(1, 10));
  private static final LongSpace BGP_ASN_RANGE = LongSpace.of(Range.closed(1L, 4294967295L));
  private static final IntegerSpace BGP_EBGP_MULTIHOP_TTL_RANGE =
      IntegerSpace.of(Range.closed(2, 255));
  private static final IntegerSpace BGP_INHERIT_RANGE = IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace BGP_MAXAS_LIMIT_RANGE = IntegerSpace.of(Range.closed(1, 512));
  private static final IntegerSpace BGP_MAXIMUM_PATHS_RANGE = IntegerSpace.of(Range.closed(1, 64));
  private static final IntegerSpace BGP_NEIGHBOR_DESCRIPTION_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 80));
  private static final IntegerSpace BGP_TEMPLATE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace CLASS_MAP_CONTROL_PLANE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 64));
  private static final IntegerSpace CLASS_MAP_NETWORK_QOS_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 40));
  private static final IntegerSpace CLASS_MAP_QOS_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 40));
  private static final IntegerSpace CLASS_MAP_QUEUING_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 40));
  private static final IntegerSpace DSCP_RANGE = IntegerSpace.of(Range.closed(0, 63));
  private static final IntegerSpace EIGRP_ASN_RANGE = IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace EIGRP_PROCESS_TAG_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 20));
  private static final IntegerSpace FIRST_RESERVED_VLAN_RANGE =
      IntegerSpace.of(Range.closed(2, 3968));
  private static final IntegerSpace FLOW_EXPORTER_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace FLOW_MONITOR_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace FLOW_RECORD_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace GENERIC_ACCESS_LIST_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 64));
  private static final IntegerSpace HSRP_DELAY_RELOAD_S_RANGE =
      IntegerSpace.of(Range.closed(0, 10000));
  private static final IntegerSpace HSRP_DELAY_MINIMUM_S_RANGE =
      IntegerSpace.of(Range.closed(0, 10000));
  private static final IntegerSpace HSRP_GROUP_RANGE = IntegerSpace.of(Range.closed(0, 4095));
  private static final IntegerSpace HSRP_GROUP_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 250));
  private static final IntegerSpace HSRP_HELLO_INTERVAL_MS_RANGE =
      IntegerSpace.of(Range.closed(250, 999));
  private static final IntegerSpace HSRP_HELLO_INTERVAL_S_RANGE =
      IntegerSpace.of(Range.closed(1, 254));
  private static final IntegerSpace HSRP_HOLD_TIME_MS_RANGE =
      IntegerSpace.of(Range.closed(750, 3000));
  private static final IntegerSpace HSRP_HOLD_TIME_S_RANGE = IntegerSpace.of(Range.closed(3, 255));
  private static final IntegerSpace HSRP_PREEMPT_DELAY_S_RANGE =
      IntegerSpace.of(Range.closed(0, 3600));
  private static final IntegerSpace HSRP_TRACK_DECREMENT_RANGE =
      IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace HSRP_VERSION_RANGE = IntegerSpace.of(Range.closed(1, 2));
  private static final IntegerSpace INTERFACE_DELAY_10US_RANGE =
      IntegerSpace.of(Range.closed(1, 16777215));
  private static final IntegerSpace INTERFACE_DESCRIPTION_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 254));
  private static final IntegerSpace INTERFACE_OSPF_COST_RANGE =
      IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace INTERFACE_SPEED_RANGE_MBPS =
      IntegerSpace.builder()
          .including(100)
          .including(1_000)
          .including(10_000)
          .including(25_000)
          .including(40_000)
          .including(100_000)
          .build();
  private static final LongSpace IP_ACCESS_LIST_LINE_NUMBER_RANGE =
      LongSpace.of(Range.closed(1L, 4294967295L));
  private static final IntegerSpace IP_AS_PATH_ACCESS_LIST_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace IP_AS_PATH_ACCESS_LIST_REGEX_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final LongSpace IP_AS_PATH_ACCESS_LIST_SEQ_RANGE =
      LongSpace.of(Range.closed(1L, 4294967294L));
  private static final LongSpace IP_COMMUNITY_LIST_LINE_NUMBER_RANGE =
      LongSpace.of(Range.closed(1L, 4294967294L));
  private static final IntegerSpace IP_COMMUNITY_LIST_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace IP_DOMAIN_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 64));
  private static final IntegerSpace IP_PREFIX_LIST_DESCRIPTION_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 90));
  private static final LongSpace IP_PREFIX_LIST_LINE_NUMBER_RANGE =
      LongSpace.of(Range.closed(1L, 4294967294L));
  private static final IntegerSpace IP_PREFIX_LIST_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace IP_PREFIX_LIST_PREFIX_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 32));
  private static final IntegerSpace IPV6_PREFIX_LIST_PREFIX_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 128));
  private static final IntegerSpace ISIS_PROCESS_TAG_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 20));
  private static final IntegerSpace LACP_MIN_LINKS_RANGE = IntegerSpace.of(Range.closed(1, 32));
  private static final IntegerSpace NUM_AS_PATH_PREPENDS_RANGE =
      IntegerSpace.of(Range.closed(1, 10));
  private static final IntegerSpace OBJECT_GROUP_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 64));
  private static final IntegerSpace OBJECT_GROUP_PORT_GT_SPACE =
      IntegerSpace.of(Range.closed(0, 65534));
  private static final IntegerSpace OBJECT_GROUP_PORT_LT_SPACE =
      IntegerSpace.of(Range.closed(1, 65535));
  private static final LongSpace OBJECT_GROUP_SEQUENCE_RANGE =
      LongSpace.of(Range.closed(1L, 4294967295L));
  private static final IntegerSpace OSPF_AREA_DEFAULT_COST_RANGE =
      IntegerSpace.of(Range.closed(0, 16777215));
  private static final IntegerSpace OSPF_AREA_RANGE_COST_RANGE =
      IntegerSpace.of(Range.closed(0, 16777215));
  private static final IntegerSpace OSPF_AUTO_COST_REFERENCE_BANDWIDTH_GBPS_RANGE =
      IntegerSpace.of(Range.closed(1, 4_000));
  private static final IntegerSpace OSPF_AUTO_COST_REFERENCE_BANDWIDTH_MBPS_RANGE =
      IntegerSpace.of(Range.closed(1, 4_000_000));
  private static final IntegerSpace OSPF_DEAD_INTERVAL_S_RANGE =
      IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace OSPF_HELLO_INTERVAL_S_RANGE =
      IntegerSpace.of(Range.closed(1, 65535));
  private static final IntegerSpace OSPF_MAX_METRIC_EXTERNAL_LSA_RANGE =
      IntegerSpace.of(Range.closed(1, 16777215));
  private static final IntegerSpace OSPF_MAX_METRIC_SUMMARY_LSA_RANGE =
      IntegerSpace.of(Range.closed(1, 16777215));
  private static final IntegerSpace OSPF_PROCESS_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 20));
  private static final IntegerSpace OSPF_TIMERS_LSA_ARRIVAL_MS_RANGE =
      IntegerSpace.of(Range.closed(10, 600_000));
  private static final IntegerSpace OSPF_TIMERS_LSA_GROUP_PACING_S_RANGE =
      IntegerSpace.of(Range.closed(1, 1800));
  private static final IntegerSpace OSPF_TIMERS_LSA_HOLD_INTERVAL_MS_RANGE =
      IntegerSpace.of(Range.closed(50, 30000));
  private static final IntegerSpace OSPF_TIMERS_LSA_MAX_INTERVAL_MS_RANGE =
      IntegerSpace.of(Range.closed(50, 30000));
  private static final IntegerSpace OSPF_TIMERS_LSA_START_INTERVAL_MS_RANGE =
      IntegerSpace.of(Range.closed(0, 5000));
  private static final IntegerSpace OSPFV3_PROCESS_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 20));
  private static final IntegerSpace PROTOCOL_DISTANCE_RANGE = IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace RIP_PROCESS_ID_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 20));
  private static final IntegerSpace SNMP_COMMUNITY_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 32));

  @VisibleForTesting
  public static final IntegerSpace PACKET_LENGTH_RANGE = IntegerSpace.of(Range.closed(20, 9210));

  private static final IntegerSpace POLICY_MAP_CONTROL_PLANE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 64));
  private static final IntegerSpace POLICY_MAP_NETWORK_QOS_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 40));
  private static final IntegerSpace POLICY_MAP_QOS_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 40));
  private static final IntegerSpace POLICY_MAP_QUEUING_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 40));
  private static final IntegerSpace PORT_CHANNEL_RANGE = IntegerSpace.of(Range.closed(1, 4096));
  private static final IntegerSpace PROTOCOL_INSTANCE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 32));
  private static final IntegerSpace ROUTE_MAP_ENTRY_SEQUENCE_RANGE =
      IntegerSpace.of(Range.closed(0, 65535));
  private static final IntegerSpace ROUTE_MAP_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace TCP_FLAGS_MASK_RANGE = IntegerSpace.of(Range.closed(0, 63));

  @VisibleForTesting
  public static final IntegerSpace TCP_PORT_RANGE = IntegerSpace.of(Range.closed(0, 65535));

  public static final IntegerSpace TRACK_OBJECT_ID_RANGE = IntegerSpace.of(Range.closed(1, 500));

  @VisibleForTesting
  public static final IntegerSpace UDP_PORT_RANGE = IntegerSpace.of(Range.closed(0, 65535));

  private static final IntegerSpace VDC_ID_RANGE = IntegerSpace.of(Range.closed(1, 4));
  private static final IntegerSpace VNI_RANGE = IntegerSpace.of(Range.closed(0, 16777214));
  private static final IntegerSpace VRF_DESCRIPTION_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace VRF_NAME_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 32));

  private static @Nonnull IpAddressSpec toAddressSpec(Acllal3_address_specContext ctx) {
    if (ctx.address != null) {
      // address and wildcard
      Ip address = toIp(ctx.address);
      Ip wildcard = toIp(ctx.wildcard);
      return new LiteralIpAddressSpec(IpWildcard.ipWithWildcardMask(address, wildcard).toIpSpace());
    } else if (ctx.prefix != null) {
      return new LiteralIpAddressSpec(toPrefix(ctx.prefix).toIpSpace());
    } else if (ctx.group != null) {
      return new AddrGroupIpAddressSpec(ctx.group.getText());
    } else if (ctx.host != null) {
      return new LiteralIpAddressSpec(toIp(ctx.host).toIpSpace());
    } else {
      // ANY
      checkArgument(ctx.ANY() != null, "Expected 'any', but got %s", ctx.getText());
      return new LiteralIpAddressSpec(UniverseIpSpace.INSTANCE);
    }
  }

  private static int toInteger(Subnet_maskContext ctx) {
    return Ip.parse(ctx.getText()).numSubnetBits();
  }

  private static int toInteger(Uint16Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private static int toInteger(Uint8Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private static @Nonnull InterfaceAddressWithAttributes toInterfaceAddress(
          Interface_addressContext ctx) {
    // TODO: support exotic address types
    return ctx.iaddress != null
        ? new InterfaceAddressWithAttributes(ConcreteInterfaceAddress.parse(ctx.getText()))
        : new InterfaceAddressWithAttributes(
            ConcreteInterfaceAddress.create(toIp(ctx.address), toInteger(ctx.mask)));
  }

  private static @Nonnull InterfaceIpv6AddressWithAttributes toInterfaceIpv6Address(
          Interface_ipv6_addressContext ctx) {
    // TODO: support exotic address types
    // TODO: implement and use datamodel Ipv6InterfaceAddress instead of Prefix6
    return InterfaceIpv6AddressWithAttributes.parse(ctx.address6_with_length.getText());
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull Ip6 toIp6(Ipv6_addressContext ctx) {
    return Ip6.parse(ctx.getText());
  }

  private static @Nonnull IpProtocol toIpProtocol(Acl_l3_protocolContext ctx) {
    if (ctx.num != null) {
      return IpProtocol.fromNumber(toInteger(ctx.num));
    } else if (ctx.AHP() != null) {
      return IpProtocol.AHP;
    } else if (ctx.EIGRP() != null) {
      return IpProtocol.EIGRP;
    } else if (ctx.ESP() != null) {
      return IpProtocol.ESP;
    } else if (ctx.GRE() != null) {
      return IpProtocol.GRE;
    } else if (ctx.NOS() != null) {
      return IpProtocol.IPIP;
    } else if (ctx.OSPF() != null) {
      return IpProtocol.OSPF;
    } else if (ctx.PCP() != null) {
      return IpProtocol.IPCOMP;
    } else if (ctx.PIM() != null) {
      return IpProtocol.PIM;
    } else {
      // All variants should be covered, so just throw if we get here
      throw new IllegalArgumentException(String.format("Unsupported protocol: %s", ctx.getText()));
    }
  }

  private static @Nonnull LineAction toLineAction(Line_actionContext ctx) {
    if (ctx.deny != null) {
      return LineAction.DENY;
    } else {
      return LineAction.PERMIT;
    }
  }

  private static long toLong(Bgp_asnContext ctx) {
    if (ctx.large != null) {
      return toLong(ctx.large);
    } else {
      return (((long) toInteger(ctx.high)) << 16) | ((long) toInteger(ctx.low));
    }
  }

  private static long toLong(Ospf_area_idContext ctx) {
    if (ctx.ip != null) {
      return toIp(ctx.ip).asLong();
    } else {
      assert ctx.num != null;
      return toLong(ctx.num);
    }
  }

  private static long toLong(Uint32Context ctx) {
    return Long.parseLong(ctx.getText());
  }

  private static @Nonnull PortSpec toPortSpec(Acllal4tcp_port_spec_port_groupContext ctx) {
    return new PortGroupPortSpec(ctx.name.getText());
  }

  private static @Nonnull PortSpec toPortSpec(Acllal4udp_port_spec_port_groupContext ctx) {
    return new PortGroupPortSpec(ctx.name.getText());
  }

  private static @Nonnull Prefix toPrefix(Ip_prefixContext ctx) {
    return Prefix.parse(ctx.getText());
  }

  private static @Nonnull Prefix6 toPrefix6(Ipv6_prefixContext ctx) {
    return Prefix6.parse(ctx.getText());
  }

  private static @Nonnull Prefix toPrefix(Route_networkContext ctx) {
    if (ctx.address != null) {
      return Prefix.create(toIp(ctx.address), toInteger(ctx.mask));
    } else {
      return toPrefix(ctx.prefix);
    }
  }

  private static @Nonnull RouteDistinguisher toRouteDistinguisher(Route_distinguisherContext ctx) {
    if (ctx.hi0 != null) {
      assert ctx.lo0 != null;
      return RouteDistinguisher.from(toInteger(ctx.hi0), toLong(ctx.lo0));
    } else if (ctx.hi1 != null) {
      assert ctx.lo1 != null;
      return RouteDistinguisher.from(toIp(ctx.hi1), toInteger(ctx.lo1));
    } else {
      assert ctx.hi2 != null;
      assert ctx.lo2 != null;
      return RouteDistinguisher.from(toLong(ctx.hi2), toInteger(ctx.lo2));
    }
  }

  private static @Nonnull RouteDistinguisherOrAuto toRouteDistinguisher(
      Route_distinguisher_or_autoContext ctx) {
    if (ctx.AUTO() != null) {
      return RouteDistinguisherOrAuto.auto();
    }
    assert ctx.route_distinguisher() != null;
    return RouteDistinguisherOrAuto.of(toRouteDistinguisher(ctx.route_distinguisher()));
  }

  private static @Nonnull Optional<RoutingProtocolInstance> toRoutingProtocolInstance(
      Bgp_instanceContext ctx) {
    return Optional.of(RoutingProtocolInstance.bgp(toLong(ctx.bgp_asn())));
  }

  private @Nonnull Optional<RoutingProtocolInstance> toRoutingProtocolInstance(
      ParserRuleContext messageCtx, Eigrp_instanceContext ctx) {
    return toString(messageCtx, ctx.router_eigrp_process_tag()).map(RoutingProtocolInstance::eigrp);
  }

  private @Nonnull Optional<RoutingProtocolInstance> toRoutingProtocolInstance(
      ParserRuleContext messageCtx, Isis_instanceContext ctx) {
    return toString(messageCtx, ctx.router_isis_process_tag()).map(RoutingProtocolInstance::isis);
  }

  private @Nonnull Optional<RoutingProtocolInstance> toRoutingProtocolInstance(
      ParserRuleContext messageCtx, Ospf_instanceContext ctx) {
    return toString(messageCtx, ctx.router_ospf_name()).map(RoutingProtocolInstance::ospf);
  }

  private @Nonnull Optional<RoutingProtocolInstance> toRoutingProtocolInstance(
      ParserRuleContext messageCtx, Ospfv3_instanceContext ctx) {
    return toString(messageCtx, ctx.router_ospfv3_name()).map(RoutingProtocolInstance::ospfv3);
  }

  private @Nonnull Optional<RoutingProtocolInstance> toRoutingProtocolInstance(
      ParserRuleContext messageCtx, Rip_instanceContext ctx) {
    return toString(messageCtx, ctx.router_rip_process_id()).map(RoutingProtocolInstance::rip);
  }

  private @Nonnull Optional<RoutingProtocolInstance> toRoutingProtocolInstance(
      ParserRuleContext messageCtx, Routing_instance_v4Context ctx) {
    if (ctx.bgp_instance() != null) {
      return toRoutingProtocolInstance(ctx.bgp_instance());
    } else if (ctx.DIRECT() != null) {
      return Optional.of(RoutingProtocolInstance.direct());
    } else if (ctx.eigrp_instance() != null) {
      return toRoutingProtocolInstance(messageCtx, ctx.eigrp_instance());
    } else if (ctx.isis_instance() != null) {
      return toRoutingProtocolInstance(messageCtx, ctx.isis_instance());
    } else if (ctx.LISP() != null) {
      return Optional.of(RoutingProtocolInstance.lisp());
    } else if (ctx.ospf_instance() != null) {
      return toRoutingProtocolInstance(messageCtx, ctx.ospf_instance());
    } else if (ctx.rip_instance() != null) {
      return toRoutingProtocolInstance(messageCtx, ctx.rip_instance());
    } else if (ctx.STATIC() != null) {
      return Optional.of(RoutingProtocolInstance.staticc());
    }
    warn(messageCtx, "Unknown routing protocol instance");
    return Optional.empty();
  }

  private @Nonnull Optional<RoutingProtocolInstance> toRoutingProtocolInstance(
      ParserRuleContext messageCtx, Routing_instance_v6Context ctx) {
    if (ctx.bgp_instance() != null) {
      return toRoutingProtocolInstance(ctx.bgp_instance());
    } else if (ctx.DIRECT() != null) {
      return Optional.of(RoutingProtocolInstance.direct());
    } else if (ctx.eigrp_instance() != null) {
      return toRoutingProtocolInstance(messageCtx, ctx.eigrp_instance());
    } else if (ctx.isis_instance() != null) {
      return toRoutingProtocolInstance(messageCtx, ctx.isis_instance());
    } else if (ctx.LISP() != null) {
      return Optional.of(RoutingProtocolInstance.lisp());
    } else if (ctx.ospfv3_instance() != null) {
      return toRoutingProtocolInstance(messageCtx, ctx.ospfv3_instance());
    } else if (ctx.rip_instance() != null) {
      return toRoutingProtocolInstance(messageCtx, ctx.rip_instance());
    } else if (ctx.STATIC() != null) {
      return Optional.of(RoutingProtocolInstance.staticc());
    }
    warn(messageCtx, "Unknown routing protocol instance");
    return Optional.empty();
  }

  private static @Nonnull ExtendedCommunity toExtendedCommunity(Route_targetContext ctx) {
    if (ctx.hi0 != null) {
      assert ctx.lo0 != null;
      return ExtendedCommunity.target((long) toInteger(ctx.hi0), toLong(ctx.lo0));
    } else {
      assert ctx.hi2 != null;
      assert ctx.lo2 != null;
      return ExtendedCommunity.target(toLong(ctx.hi2), (long) toInteger(ctx.lo2));
    }
  }

  private static @Nonnull ExtendedCommunityOrAuto toExtendedCommunityOrAuto(
      Route_target_or_autoContext ctx) {
    if (ctx.AUTO() != null) {
      return ExtendedCommunityOrAuto.auto();
    }
    assert ctx.route_target() != null;
    return ExtendedCommunityOrAuto.of(toExtendedCommunity(ctx.route_target()));
  }

  private ActionIpAccessListLine.Builder _currentActionIpAccessListLineBuilder;
  private Boolean _currentActionIpAccessListLineUnusable;
  private BgpVrfIpAddressFamilyConfiguration _currentBgpVrfIpAddressFamily;
  private BgpVrfL2VpnEvpnAddressFamilyConfiguration _currentBgpVrfL2VpnEvpnAddressFamily;
  private BgpVrfAddressFamilyAggregateNetworkConfiguration
      _currentBgpVrfAddressFamilyAggregateNetwork;
  private String _currentBgpVrfName;
  private BgpVrfConfiguration _currentBgpVrfConfiguration;
  private BgpVrfNeighborConfiguration _currentBgpVrfNeighbor;
  private BgpVrfNeighborAddressFamilyConfiguration _currentBgpVrfNeighborAddressFamily;
  private DefaultVrfOspfProcess _currentDefaultVrfOspfProcess;
  private EigrpProcessConfiguration _currentEigrpProcess;
  private EigrpVrfConfiguration _currentEigrpVrf;
  private EigrpVrfIpAddressFamilyConfiguration _currentEigrpVrfIpAf;
  private EvpnVni _currentEvpnVni;
  private Function<Interface, HsrpGroup> _currentHsrpGroupGetter;
  private Optional<Integer> _currentHsrpGroupNumber;
  private List<Interface> _currentInterfaces;
  private IpAccessList _currentIpAccessList;
  private Optional<Long> _currentIpAccessListLineNum;
  private IpPrefixList _currentIpPrefixList;

  @SuppressWarnings("unused")
  private Ipv6AccessList _currentIpv6AccessList;

  private Ipv6PrefixList _currentIpv6PrefixList;
  private Layer3Options.Builder _currentLayer3OptionsBuilder;

  @SuppressWarnings("unused")
  private LoggingServer _currentLoggingServer;

  private NtpServer _currentNtpServer;
  private List<Nve> _currentNves;
  private List<NveVni> _currentNveVnis;
  private ObjectGroupIpAddress _currentObjectGroupIpAddress;
  private ObjectGroupIpPort _currentObjectGroupIpPort;
  private OspfArea _currentOspfArea;
  private OspfProcess _currentOspfProcess;
  private RouteMapEntry _currentRouteMapEntry;
  private Optional<String> _currentRouteMapName;

  private SnmpCommunity _currentSnmpCommunity;

  @SuppressWarnings("unused")
  private SnmpServer _currentSnmpServer;

  @SuppressWarnings("unused")
  private TacacsServer _currentTacacsServer;

  private TcpFlags.Builder _currentTcpFlagsBuilder;
  private TcpOptions.Builder _currentTcpOptionsBuilder;
  private UdpOptions.Builder _currentUdpOptionsBuilder;
  private IntegerSpace _currentValidVlanRange;
  private List<Vlan> _currentVlans;
  private Vrf _currentVrf;
  private boolean _inIpv6BgpPeer;
  private Track _currentTrack;
  private TrackIpRoute _currentTrackIpRoute;

  private IsisProcess _currentIsisProcess;

  private Interface _currentIsisInterface;

  /**
   * On NX-OS, many structure names are case-insensitive but capitalized according to how they were
   * entered at first use. This table keeps track of the preferred name for each of the structures.
   *
   * <p>{@code (Structure Type, LowerCaseName) -> Preferred name}.
   */
  private final @Nonnull Table<CiscoNxosStructureType, String, String> _preferredNames;

  /** Returns the preferred name for a structure with the given name and type. */
  private @Nonnull String getPreferredName(String configName, CiscoNxosStructureType type) {
    return _preferredNames
        .row(type)
        .computeIfAbsent(configName.toLowerCase(), lcName -> configName);
  }

  private final @Nonnull CiscoNxosConfiguration _c;
  private final @Nonnull CiscoNxosCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  public CiscoNxosControlPlaneExtractor(
      String text,
      CiscoNxosCombinedParser parser,
      Warnings warnings,
      CiscoNxosConfiguration configuration,
      SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _preferredNames = HashBasedTable.create();
    _w = warnings;
    _c = configuration;
    _silentSyntax = silentSyntax;

    // initialize preferred names
    getPreferredName(DEFAULT_VRF_NAME, VRF);
    getPreferredName(MANAGEMENT_VRF_NAME, VRF);
  }

  @Override
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Override
  public String getInputText() {
    return _text;
  }

  @Override
  public BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public Warnings getWarnings() {
    return _w;
  }

  /**
   * Clears layer-3 configuration of an interface to enable safe assignment to a new VRF.
   *
   * <p>NX-OS switches clear all layer-3 configuration from interfaces when an interface is assigned
   * to a VRF, presumably to prevent accidental leakage of any connected routes from the old VRF
   * into the new one.
   */
  private void clearLayer3Configuration(Interface iface) {
    iface.setAddress(null);
    iface.getSecondaryAddresses().clear();
  }

  /** This function must be kept in sync with {@link #copyPortChannelCompatibilitySettings}. */
  private boolean checkPortChannelCompatibilitySettings(Interface referenceIface, Interface iface) {
    return Objects.equals(iface.getAccessVlan(), referenceIface.getAccessVlan())
        && Objects.equals(iface.getAllowedVlans(), referenceIface.getAllowedVlans())
        && Objects.equals(iface.getNativeVlan(), referenceIface.getNativeVlan())
        && iface.getSwitchportModeEffective(_c.getSystemDefaultSwitchport())
            == referenceIface.getSwitchportModeEffective(_c.getSystemDefaultSwitchport());
  }

  /** This function must be kept in sync with {@link #checkPortChannelCompatibilitySettings}. */
  private void copyPortChannelCompatibilitySettings(Interface referenceIface, Interface iface) {
    iface.setAccessVlan(referenceIface.getAccessVlan());
    iface.setAllowedVlans(referenceIface.getAllowedVlans());
    iface.setNativeVlan(referenceIface.getNativeVlan());
    if (iface.getSwitchportModeEffective(_c.getSystemDefaultSwitchport())
        != referenceIface.getSwitchportModeEffective(_c.getSystemDefaultSwitchport())) {
      iface.setSwitchportMode(
          referenceIface.getSwitchportModeEffective(_c.getSystemDefaultSwitchport()));
    }
    assert checkPortChannelCompatibilitySettings(referenceIface, iface);
  }

  @Override
  public void enterAcl_line(Acl_lineContext ctx) {
    if (ctx.num != null) {
      _currentIpAccessListLineNum = toLong(ctx, ctx.num);
    } else if (!_currentIpAccessList.getLines().isEmpty()) {
      _currentIpAccessListLineNum = Optional.of(_currentIpAccessList.getLines().lastKey() + 10L);
    } else {
      _currentIpAccessListLineNum = Optional.of(10L);
    }
  }

  @Override
  public void enterAcll_action(Acll_actionContext ctx) {
    _currentActionIpAccessListLineBuilder =
        ActionIpAccessListLine.builder()
            .setAction(toLineAction(ctx.action))
            .setText(getFullText(ctx.getParent()));
    _currentIpAccessListLineNum.ifPresent(
        num -> _currentActionIpAccessListLineBuilder.setLine(num));
    _currentLayer3OptionsBuilder = Layer3Options.builder();
    _currentActionIpAccessListLineUnusable = false;
  }

  @Override
  public void enterAclla_tcp(Aclla_tcpContext ctx) {
    _currentActionIpAccessListLineBuilder.setProtocol(IpProtocol.TCP);
  }

  @Override
  public void enterAclla_udp(Aclla_udpContext ctx) {
    _currentActionIpAccessListLineBuilder.setProtocol(IpProtocol.UDP);
  }

  @Override
  public void exitBanner_exec(Banner_execContext ctx) {
    String body = ctx.body != null ? ctx.body.getText() : "";
    _c.setBannerExec(body);
  }

  @Override
  public void exitBanner_motd(Banner_motdContext ctx) {
    String body = ctx.body != null ? ctx.body.getText() : "";
    _c.setBannerMotd(body);
  }

  @Override
  public void enterCisco_nxos_configuration(Cisco_nxos_configurationContext ctx) {
    _currentValidVlanRange = VLAN_RANGE.difference(_c.getReservedVlanRange());
    _currentVrf = _c.getDefaultVrf();
    // define built-ins at line 0 (before first line of file).
    // vrfs
    _c.defineSingleLineStructure(VRF, DEFAULT_VRF_NAME, 0);
    _c.defineSingleLineStructure(VRF, MANAGEMENT_VRF_NAME, 0);
    _c.referenceStructure(VRF, DEFAULT_VRF_NAME, BUILT_IN, 0);
    _c.referenceStructure(VRF, MANAGEMENT_VRF_NAME, BUILT_IN, 0);
    // class-maps
    _c.defineSingleLineStructure(CLASS_MAP_CONTROL_PLANE, DEFAULT_CLASS_MAP_NAME, 0);
    _c.referenceStructure(CLASS_MAP_CONTROL_PLANE, DEFAULT_CLASS_MAP_NAME, BUILT_IN, 0);
    _c.defineSingleLineStructure(CLASS_MAP_NETWORK_QOS, DEFAULT_CLASS_MAP_NAME, 0);
    _c.referenceStructure(CLASS_MAP_NETWORK_QOS, DEFAULT_CLASS_MAP_NAME, BUILT_IN, 0);
    _c.defineSingleLineStructure(CLASS_MAP_QOS, DEFAULT_CLASS_MAP_NAME, 0);
    _c.referenceStructure(CLASS_MAP_QOS, DEFAULT_CLASS_MAP_NAME, BUILT_IN, 0);
    _c.defineSingleLineStructure(CLASS_MAP_QUEUING, DEFAULT_CLASS_MAP_NAME, 0);
    _c.referenceStructure(CLASS_MAP_QUEUING, DEFAULT_CLASS_MAP_NAME, BUILT_IN, 0);
    // policy-maps
    _c.defineSingleLineStructure(POLICY_MAP_QUEUING, DEFAULT_POLICY_MAP_IN, 0);
    _c.referenceStructure(POLICY_MAP_QUEUING, DEFAULT_POLICY_MAP_IN, BUILT_IN, 0);
    _c.defineSingleLineStructure(POLICY_MAP_QUEUING, DEFAULT_POLICY_MAP_OUT, 0);
    _c.referenceStructure(POLICY_MAP_QUEUING, DEFAULT_POLICY_MAP_OUT, BUILT_IN, 0);
  }

  @Override
  public void enterCm_control_plane(Cm_control_planeContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(CLASS_MAP_CONTROL_PLANE, name, ctx);
  }

  @Override
  public void enterCm_network_qos(Cm_network_qosContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(CLASS_MAP_NETWORK_QOS, name, ctx);
  }

  @Override
  public void enterCm_qos(Cm_qosContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(CLASS_MAP_QOS, name, ctx);
  }

  @Override
  public void enterCm_queuing(Cm_queuingContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(CLASS_MAP_QUEUING, name, ctx);
  }

  @Override
  public void exitCmcpm_access_group(Cmcpm_access_groupContext ctx) {
    Optional<String> acl = toString(ctx, ctx.name);
    if (!acl.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_OR_MAC_ACCESS_LIST_ABSTRACT_REF,
        acl.get(),
        CLASS_MAP_CP_MATCH_ACCESS_GROUP,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitCmqm_access_group(Cmqm_access_groupContext ctx) {
    Optional<String> acl = toString(ctx, ctx.name);
    if (!acl.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_OR_MAC_ACCESS_LIST_ABSTRACT_REF,
        acl.get(),
        CLASS_MAP_QOS_MATCH_ACCESS_GROUP,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitCp_service_policy(Cp_service_policyContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(
        POLICY_MAP_CONTROL_PLANE, name, CONTROL_PLANE_SERVICE_POLICY, ctx.getStart().getLine());
  }

  @Override
  public void enterEv_vni(Ev_vniContext ctx) {
    Optional<Integer> vniOrError = toInteger(ctx, ctx.vni);
    if (!vniOrError.isPresent()) {
      // Create a dummy for subsequent configuration commands.
      _currentEvpnVni = new EvpnVni(0);
      return;
    }
    int vni = vniOrError.get();
    Evpn e = _c.getEvpn();
    assert e != null;
    _currentEvpnVni = e.getVni(vni);
  }

  @Override
  public void exitEv_vni(Ev_vniContext ctx) {
    _currentEvpnVni = null;
  }

  @Override
  public void exitFe_source(Fe_sourceContext ctx) {
    toString(ctx, ctx.iface)
        .ifPresent(
            name ->
                _c.referenceStructure(
                    INTERFACE, name, FLOW_EXPORTER_SOURCE, ctx.iface.getStart().getLine()));
  }

  @Override
  public void enterFlow_exporter(Flow_exporterContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(FLOW_EXPORTER, name, ctx);
  }

  @Override
  public void enterFlow_monitor(Flow_monitorContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(FLOW_MONITOR, name, ctx);
  }

  @Override
  public void exitFm_exporter(Fm_exporterContext ctx) {
    toString(ctx, ctx.exporter)
        .ifPresent(
            name ->
                _c.referenceStructure(
                    FLOW_EXPORTER, name, FLOW_MONITOR_EXPORTER, ctx.exporter.getStart().getLine()));
  }

  @Override
  public void exitFm_record(Fm_recordContext ctx) {
    toString(ctx, ctx.record)
        .ifPresent(
            name ->
                _c.referenceStructure(
                    FLOW_RECORD, name, FLOW_MONITOR_RECORD, ctx.record.getStart().getLine()));
  }

  @Override
  public void enterFlow_record(Flow_recordContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(FLOW_RECORD, name, ctx);
  }

  @Override
  public void exitI_speed_number(I_speed_numberContext ctx) {
    toIntegerInSpace(ctx, ctx.speed, INTERFACE_SPEED_RANGE_MBPS, "interface speed")
        .ifPresent(speed -> _currentInterfaces.forEach(iface -> iface.setSpeed(speed)));
  }

  @Override
  public void enterIcl_expanded(Icl_expandedContext ctx) {
    Long explicitSeq;
    if (ctx.seq != null) {
      Optional<Long> seqOpt = toLong(ctx, ctx.seq);
      if (!seqOpt.isPresent()) {
        return;
      }
      explicitSeq = seqOpt.get();
    } else {
      explicitSeq = null;
    }
    Optional<String> nameOpt = toString(ctx, ctx.name);
    if (!nameOpt.isPresent()) {
      return;
    }
    String name = nameOpt.get();
    String regex =
        ctx.quoted != null
            ? ctx.quoted.text != null ? ctx.quoted.text.getText() : ""
            : ctx.regex.getText();
    IpCommunityList communityList =
        _c.getIpCommunityLists().computeIfAbsent(name, IpCommunityListExpanded::new);
    if (!(communityList instanceof IpCommunityListExpanded)) {
      warn(
          ctx,
          String.format(
              "Cannot define expanded community-list '%s' because another community-list with that"
                  + " name but a different type already exists.",
              name));
      return;
    }
    IpCommunityListExpanded communityListExpanded = (IpCommunityListExpanded) communityList;
    SortedMap<Long, IpCommunityListExpandedLine> lines = communityListExpanded.getLines();
    long seq;
    if (explicitSeq != null) {
      seq = explicitSeq;
    } else if (!lines.isEmpty()) {
      seq = lines.lastKey() + 1L;
    } else {
      seq = 1L;
    }
    communityListExpanded
        .getLines()
        .put(seq, new IpCommunityListExpandedLine(toLineAction(ctx.action), seq, regex));
    _c.defineStructure(IP_COMMUNITY_LIST_EXPANDED, name, ctx);
  }

  @Override
  public void enterIcl_standard(Icl_standardContext ctx) {
    Long explicitSeq;
    if (ctx.seq != null) {
      Optional<Long> seqOpt = toLong(ctx, ctx.seq);
      if (!seqOpt.isPresent()) {
        return;
      }
      explicitSeq = seqOpt.get();
    } else {
      explicitSeq = null;
    }
    Optional<Set<StandardCommunity>> communities = toStandardCommunitySet(ctx.communities);
    if (!communities.isPresent()) {
      return;
    }
    Optional<String> nameOpt = toString(ctx, ctx.name);
    if (!nameOpt.isPresent()) {
      return;
    }
    String name = nameOpt.get();
    IpCommunityList communityList =
        _c.getIpCommunityLists().computeIfAbsent(name, IpCommunityListStandard::new);
    if (!(communityList instanceof IpCommunityListStandard)) {
      warn(
          ctx,
          String.format(
              "Cannot define standard community-list '%s' because another community-list with that"
                  + " name but a different type already exists.",
              name));
      return;
    }
    IpCommunityListStandard communityListStandard = (IpCommunityListStandard) communityList;
    SortedMap<Long, IpCommunityListStandardLine> lines = communityListStandard.getLines();
    long seq;
    if (explicitSeq != null) {
      seq = explicitSeq;
    } else if (!lines.isEmpty()) {
      seq = lines.lastKey() + 1L;
    } else {
      seq = 1L;
    }
    communityListStandard
        .getLines()
        .put(
            seq, new IpCommunityListStandardLine(toLineAction(ctx.action), seq, communities.get()));
    _c.defineStructure(IP_COMMUNITY_LIST_STANDARD, name, ctx);
  }

  @Override
  public void exitIhd_minimum(Ihd_minimumContext ctx) {
    toIntegerInSpace(ctx, ctx.delay_s, HSRP_DELAY_MINIMUM_S_RANGE, "hsrp minimum delay seconds")
        .ifPresent(
            delay ->
                _currentInterfaces.forEach(
                    iface -> iface.getOrCreateHsrp().setDelayMinimumSeconds(delay)));
  }

  @Override
  public void exitIhd_reload(Ihd_reloadContext ctx) {
    toIntegerInSpace(ctx, ctx.delay_s, HSRP_DELAY_RELOAD_S_RANGE, "hsrp reload delay seconds")
        .ifPresent(
            delay ->
                _currentInterfaces.forEach(
                    iface -> iface.getOrCreateHsrp().setDelayReloadSeconds(delay)));
  }

  @Override
  public void enterIh_group(Ih_groupContext ctx) {
    _currentHsrpGroupNumber = toIntegerInSpace(ctx, ctx.group, HSRP_GROUP_RANGE, "hsrp group");
  }

  @Override
  public void exitIh_group(Ih_groupContext ctx) {
    _currentHsrpGroupNumber = null;
  }

  @Override
  public void enterIhg_ipv4(Ihg_ipv4Context ctx) {
    if (!_currentHsrpGroupNumber.isPresent()) {
      // dummy
      _currentHsrpGroupGetter = iface -> new HsrpGroupIpv4(0);
    } else {
      _currentHsrpGroupGetter =
          iface ->
              iface
                  .getOrCreateHsrp()
                  .getIpv4Groups()
                  .computeIfAbsent(_currentHsrpGroupNumber.get(), HsrpGroupIpv4::new);
    }
  }

  @Override
  public void exitIhg_ipv4(Ihg_ipv4Context ctx) {
    _currentHsrpGroupGetter = null;
  }

  @Override
  public void enterIhg_ipv6(Ihg_ipv6Context ctx) {
    // TODO: implement HSRP for IPv6
    // dummy
    _currentHsrpGroupGetter = iface -> new HsrpGroupIpv6(0);
  }

  @Override
  public void exitIhg_ipv6(Ihg_ipv6Context ctx) {
    _currentHsrpGroupGetter = null;
  }

  @Override
  public void exitIhgam_key_chain(Ihgam_key_chainContext ctx) {
    // TODO: support HSRP md5 authentication key-chain
    todo(ctx);
  }

  @Override
  public void exitI_delay(I_delayContext ctx) {
    toIntegerInSpace(
            ctx, ctx.delay, INTERFACE_DELAY_10US_RANGE, "Interface delay (tens of microseconds)")
        .ifPresent(
            delay -> _currentInterfaces.forEach(iface -> iface.setDelayTensOfMicroseconds(delay)));
  }

  @Override
  public void exitI_switchport_switchport(I_switchport_switchportContext ctx) {
    _currentInterfaces.stream()
        .filter(
            iface ->
                iface.getSwitchportMode() == null
                    || iface.getSwitchportMode() == SwitchportMode.NONE)
        .forEach(iface -> iface.setSwitchportMode(SwitchportMode.ACCESS));
  }

  /**
   * Returns boolean indicating if the interface's configured IP subnet(s) contain the specified IP
   * address.
   */
  private boolean ifaceContainsIp(Interface iface, Ip ip) {
    return ifaceAddrContainsIp(iface.getAddress(), ip)
        || iface.getSecondaryAddresses().stream().anyMatch(a -> ifaceAddrContainsIp(a, ip));
  }

  private boolean ifaceAddrContainsIp(
      @Nullable InterfaceAddressWithAttributes ifaceAddrWithAttributes, Ip ip) {
    if (ifaceAddrWithAttributes == null) {
      return false;
    }

    InterfaceAddress addr = ifaceAddrWithAttributes.getAddress();
    if (addr instanceof ConcreteInterfaceAddress) {
      return ((ConcreteInterfaceAddress) addr).getPrefix().containsIp(ip);
    }
    assert addr instanceof LinkLocalAddress;
    return ((LinkLocalAddress) addr).getPrefix().containsIp(ip);
  }

  @Override
  public void exitIhg4_ip(Ihg4_ipContext ctx) {
    if (ctx.prefix != null) {
      todo(ctx);
      return;
    }
    assert ctx.ip != null;
    Ip ip = toIp(ctx.ip);
    _currentInterfaces.forEach(
        iface -> {
          // Device allows configuring HSRP IPs if either:
          // 1. No interface IPs are configured yet OR
          // 2. HSRP IPs are in the subnet(s) associated with the interface
          if (iface.getAddress() == null || ifaceContainsIp(iface, ip)) {
            HsrpGroup group = _currentHsrpGroupGetter.apply(iface);
            assert group instanceof HsrpGroupIpv4;
            if (ctx.SECONDARY() != null) {
              ((HsrpGroupIpv4) group).getIpSecondaries().add(ip);
            } else {
              ((HsrpGroupIpv4) group).setIp(ip);
            }
          } else {
            warn(
                ctx,
                "HSRP IP must be contained by its interface subnets. This HSRP IP will be"
                    + " ignored.");
          }
        });
  }

  @Override
  public void exitIhg_name(Ihg_nameContext ctx) {
    Optional<String> nameOrError =
        toStringWithLengthInSpace(ctx, ctx.name, HSRP_GROUP_NAME_LENGTH_RANGE, "hsrp group name");
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _currentInterfaces.forEach(
        iface -> {
          HsrpGroup group = _currentHsrpGroupGetter.apply(iface);
          group.setName(name);
        });
  }

  @Override
  public void exitIhg_no_preempt(Ihg_no_preemptContext ctx) {
    for (Interface iface : _currentInterfaces) {
      HsrpGroup group = _currentHsrpGroupGetter.apply(iface);
      group.setPreempt(false);
      group.setPreemptDelayMinimumSeconds(null);
      group.setPreemptDelayReloadSeconds(null);
      group.setPreemptDelaySyncSeconds(null);
    }
  }

  @Override
  public void exitIhg_preempt(Ihg_preemptContext ctx) {
    @Nullable Integer minimumSeconds = null;
    if (ctx.minimum_s != null) {
      Optional<Integer> minimumSecondsOrErr =
          toIntegerInSpace(
              ctx, ctx.minimum_s, HSRP_PREEMPT_DELAY_S_RANGE, "hspr preempt delay minimum seconds");
      if (!minimumSecondsOrErr.isPresent()) {
        return;
      }
      minimumSeconds = minimumSecondsOrErr.get();
    }
    @Nullable Integer reloadSeconds = null;
    if (ctx.reload_s != null) {
      Optional<Integer> reloadSecondsOrErr =
          toIntegerInSpace(
              ctx, ctx.reload_s, HSRP_PREEMPT_DELAY_S_RANGE, "hspr preempt delay reload seconds");
      if (!reloadSecondsOrErr.isPresent()) {
        return;
      }
      reloadSeconds = reloadSecondsOrErr.get();
    }
    @Nullable Integer syncSeconds = null;
    if (ctx.sync_s != null) {
      Optional<Integer> syncSecondsOrErr =
          toIntegerInSpace(
              ctx, ctx.sync_s, HSRP_PREEMPT_DELAY_S_RANGE, "hspr preempt delay sync seconds");
      if (!syncSecondsOrErr.isPresent()) {
        return;
      }
      syncSeconds = syncSecondsOrErr.get();
    }
    for (Interface iface : _currentInterfaces) {
      HsrpGroup group = _currentHsrpGroupGetter.apply(iface);
      group.setPreempt(true);
      if (minimumSeconds != null) {
        group.setPreemptDelayMinimumSeconds(minimumSeconds);
      }
      if (reloadSeconds != null) {
        group.setPreemptDelayReloadSeconds(reloadSeconds);
      }
      if (syncSeconds != null) {
        group.setPreemptDelaySyncSeconds(syncSeconds);
      }
    }
  }

  @Override
  public void exitIhg_priority(Ihg_priorityContext ctx) {
    int priority = toInteger(ctx.priority);
    _currentInterfaces.forEach(iface -> _currentHsrpGroupGetter.apply(iface).setPriority(priority));
  }

  @Override
  public void exitIhg_timers(Ihg_timersContext ctx) {
    int helloIntervalMs;
    if (ctx.hello_interval_ms != null) {
      Optional<Integer> helloIntervalMsOrErr =
          toIntegerInSpace(
              ctx,
              ctx.hello_interval_ms,
              HSRP_HELLO_INTERVAL_MS_RANGE,
              "hsrp timers hello-interval ms");
      if (!helloIntervalMsOrErr.isPresent()) {
        return;
      }
      helloIntervalMs = helloIntervalMsOrErr.get();
    } else {
      assert ctx.hello_interval_s != null;
      Optional<Integer> helloIntervalSecondsOrErr =
          toIntegerInSpace(
              ctx,
              ctx.hello_interval_s,
              HSRP_HELLO_INTERVAL_S_RANGE,
              "hsrp timers hello-interval seconds");
      if (!helloIntervalSecondsOrErr.isPresent()) {
        return;
      }
      helloIntervalMs = helloIntervalSecondsOrErr.get() * 1000;
    }
    int holdTimeMs;
    if (ctx.hold_time_ms != null) {
      Optional<Integer> holdTimeMsOrErr =
          toIntegerInSpace(
              ctx, ctx.hold_time_ms, HSRP_HOLD_TIME_MS_RANGE, "hsrp timers hold-time ms");
      if (!holdTimeMsOrErr.isPresent()) {
        return;
      }
      holdTimeMs = holdTimeMsOrErr.get();
    } else {
      assert ctx.hold_time_s != null;
      Optional<Integer> holdTimeSecondsOrErr =
          toIntegerInSpace(
              ctx, ctx.hold_time_s, HSRP_HOLD_TIME_S_RANGE, "hsrp timers hold-time seconds");
      if (!holdTimeSecondsOrErr.isPresent()) {
        return;
      }
      holdTimeMs = holdTimeSecondsOrErr.get() * 1000;
    }
    // TODO: check constraints on relationship between hello and hold
    _currentInterfaces.forEach(
        iface -> {
          HsrpGroup group = _currentHsrpGroupGetter.apply(iface);
          group.setHelloIntervalMs(helloIntervalMs);
          group.setHoldTimeMs(holdTimeMs);
        });
  }

  @Override
  public void exitIhg_track(Ihg_trackContext ctx) {
    Optional<Integer> trackObjectNumberOrErr =
        toIntegerInSpace(ctx, ctx.num, TRACK_OBJECT_ID_RANGE, "hsrp group track object number");
    if (!trackObjectNumberOrErr.isPresent()) {
      return;
    }
    Integer trackObjectNumber = trackObjectNumberOrErr.get();

    if (!_c.getTracks().containsKey(trackObjectNumber)) {
      warn(
          ctx,
          String.format(
              "Cannot reference undefined track %s. This line will be ignored.",
              trackObjectNumber));
      _c.undefined(
          CiscoNxosStructureType.TRACK,
          trackObjectNumber.toString(),
          CiscoNxosStructureUsage.INTERFACE_HSRP_GROUP_TRACK,
          ctx.start.getLine());
      return;
    }
    _c.referenceStructure(
        CiscoNxosStructureType.TRACK,
        trackObjectNumber.toString(),
        CiscoNxosStructureUsage.INTERFACE_HSRP_GROUP_TRACK,
        ctx.start.getLine());
    @Nullable Integer decrement;
    if (ctx.decrement != null) {
      Optional<Integer> decrementOrErr =
          toIntegerInSpace(
              ctx, ctx.decrement, HSRP_TRACK_DECREMENT_RANGE, "hsrp group track decrement");
      if (!decrementOrErr.isPresent()) {
        return;
      }
      decrement = decrementOrErr.get();
    } else {
      // disable instead of decrement when tracked object goes down
      decrement = null;
    }
    _currentInterfaces.forEach(
        iface ->
            _currentHsrpGroupGetter
                .apply(iface)
                .getTracks()
                .computeIfAbsent(trackObjectNumber, num -> new HsrpTrack(num, decrement)));
  }

  @Override
  public void exitIh_version(Ih_versionContext ctx) {
    toIntegerInSpace(ctx, ctx.version, HSRP_VERSION_RANGE, "hsrp version")
        .ifPresent(
            version ->
                _currentInterfaces.forEach(iface -> iface.getOrCreateHsrp().setVersion(version)));
  }

  @Override
  public void exitIl_min_links(Il_min_linksContext ctx) {
    toIntegerInSpace(ctx, ctx.num, LACP_MIN_LINKS_RANGE, "lacp min-links")
        .ifPresent(
            minLinks ->
                _currentInterfaces.forEach(iface -> iface.getOrCreateLacp().setMinLinks(minLinks)));
  }

  @Override
  public void exitI_ip_access_group(I_ip_access_groupContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      return;
    }
    String name = nameOrErr.get();
    int line = ctx.getStart().getLine();
    if (ctx.IN() != null) {
      _c.referenceStructure(IP_ACCESS_LIST, name, INTERFACE_IP_ACCESS_GROUP_IN, line);
      _currentInterfaces.forEach(iface -> iface.setIpAccessGroupIn(name));
    } else {
      assert ctx.OUT() != null;
      _c.referenceStructure(IP_ACCESS_LIST, name, INTERFACE_IP_ACCESS_GROUP_OUT, line);
      _currentInterfaces.forEach(iface -> iface.setIpAccessGroupOut(name));
    }
  }

  @Override
  public void exitIp_domain_name(Ip_domain_nameContext ctx) {
    toStringWithLengthInSpace(ctx, ctx.domain, IP_DOMAIN_NAME_LENGTH_RANGE, "ip domain-name")
        .ifPresent(_c::setIpDomainName);
  }

  @Override
  public void exitIp_name_server(Ip_name_serverContext ctx) {
    String useVrf = null;
    if (ctx.vrf != null) {
      Optional<String> vrfOrErr = toString(ctx, ctx.vrf);
      if (!vrfOrErr.isPresent()) {
        return;
      }
      useVrf = vrfOrErr.get();
    }
    for (Name_serverContext server : ctx.servers) {
      _currentVrf.addNameServer(new NameServer(getFullText(server), useVrf));
    }
  }

  @Override
  public void exitIip_port_access_group(Iip_port_access_groupContext ctx) {
    Optional<String> acl = toString(ctx, ctx.acl);
    if (!acl.isPresent()) {
      return;
    }
    todo(ctx);
    _c.referenceStructure(
        IP_ACCESS_LIST, acl.get(), INTERFACE_IP_PORT_ACCESS_GROUP, ctx.getStart().getLine());
  }

  @Override
  public void exitIipr_rip(Iipr_ripContext ctx) {
    Optional<RoutingProtocolInstance> proc = toRoutingProtocolInstance(ctx, ctx.rip_instance());
    if (!proc.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTER_RIP, proc.get().getTag(), INTERFACE_IP_ROUTER_RIP, ctx.getStart().getLine());
  }

  @Override
  public void exitIiprip_rf_prefix_list(Iiprip_rf_prefix_listContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_PREFIX_LIST,
        name.get(),
        INTERFACE_IP_RIP_ROUTE_FILTER_PREFIX_LIST,
        ctx.getStart().getLine());
  }

  @Override
  public void exitIiprip_rf_route_map(Iiprip_rf_route_mapContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTE_MAP, name.get(), INTERFACE_IP_RIP_ROUTE_FILTER_ROUTE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitIip6r_ospfv3(Iip6r_ospfv3Context ctx) {
    Optional<RoutingProtocolInstance> proc = toRoutingProtocolInstance(ctx, ctx.ospfv3_instance());
    if (!proc.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTER_OSPFV3, proc.get().getTag(), INTERFACE_IP_ROUTER_OSPF, ctx.getStart().getLine());
  }

  @Override
  public void exitIipi_access_group(Iipi_access_groupContext ctx) {
    toString(ctx, ctx.acl)
        .ifPresent(
            name ->
                _c.referenceStructure(
                    IP_ACCESS_LIST,
                    name,
                    INTERFACE_IP_IGMP_ACCESS_GROUP,
                    ctx.acl.getStart().getLine()));
  }

  @Override
  public void exitIipo_bfd(Iipo_bfdContext ctx) {
    _currentInterfaces.forEach(iface -> iface.getOrCreateOspf().setBfd(true));
  }

  @Override
  public void exitIipo_cost(Iipo_costContext ctx) {
    toIntegerInSpace(ctx, ctx.cost, INTERFACE_OSPF_COST_RANGE, "OSPF cost")
        .ifPresent(
            cost -> _currentInterfaces.forEach(iface -> iface.getOrCreateOspf().setCost(cost)));
  }

  @Override
  public void exitIipo_dead_interval(Iipo_dead_intervalContext ctx) {
    Optional<Integer> deadIntervalOrErr =
        toIntegerInSpace(ctx, ctx.interval_s, OSPF_DEAD_INTERVAL_S_RANGE, "OSPF dead-interval");
    deadIntervalOrErr.ifPresent(
        deadInterval ->
            _currentInterfaces.forEach(
                iface -> iface.getOrCreateOspf().setDeadIntervalS(deadInterval)));
  }

  @Override
  public void exitIipo_hello_interval(Iipo_hello_intervalContext ctx) {
    Optional<Integer> helloIntervalOrErr =
        toIntegerInSpace(ctx, ctx.interval_s, OSPF_HELLO_INTERVAL_S_RANGE, "OSPF hello-interval");
    helloIntervalOrErr.ifPresent(
        helloInterval ->
            _currentInterfaces.forEach(
                iface -> iface.getOrCreateOspf().setHelloIntervalS(helloInterval)));
  }

  @Override
  public void exitIipo_network(Iipo_networkContext ctx) {
    OspfNetworkType type;
    if (ctx.BROADCAST() != null) {
      type = OspfNetworkType.BROADCAST;
    } else if (ctx.POINT_TO_POINT() != null) {
      type = OspfNetworkType.POINT_TO_POINT;
    } else {
      // assume valid but unsupported
      todo(ctx);
      return;
    }
    _currentInterfaces.forEach(iface -> iface.getOrCreateOspf().setNetwork(type));
  }

  @Override
  public void exitIipo_passive_interface(Iipo_passive_interfaceContext ctx) {
    _currentInterfaces.forEach(iface -> iface.getOrCreateOspf().setPassive(true));
  }

  @Override
  public void exitIipo_priority(Iipo_priorityContext ctx) {
    int priority = toInteger(ctx.priority);
    todo(ctx);
    _currentInterfaces.forEach(iface -> iface.getOrCreateOspf().setPriority(priority));
  }

  @Override
  public void exitIipp_jp_policy_prefix_list(Iipp_jp_policy_prefix_listContext ctx) {
    Optional<String> list = toString(ctx, ctx.list);
    if (!list.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_PREFIX_LIST,
        list.get(),
        INTERFACE_IP_PIM_JP_POLICY_PREFIX_LIST,
        ctx.getStart().getLine());
  }

  @Override
  public void exitIipp_jp_policy_route_map(Iipp_jp_policy_route_mapContext ctx) {
    Optional<String> map = toString(ctx, ctx.map);
    if (!map.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTE_MAP, map.get(), INTERFACE_IP_PIM_JP_POLICY_ROUTE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitIipp_neighbor_policy_prefix_list(Iipp_neighbor_policy_prefix_listContext ctx) {
    Optional<String> list = toString(ctx, ctx.list);
    if (!list.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_PREFIX_LIST,
        list.get(),
        INTERFACE_IP_PIM_NEIGHBOR_POLICY_PREFIX_LIST,
        ctx.getStart().getLine());
  }

  @Override
  public void exitIipp_neighbor_policy_route_map(Iipp_neighbor_policy_route_mapContext ctx) {
    Optional<String> map = toString(ctx, ctx.map);
    if (!map.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTE_MAP, map.get(), INTERFACE_IP_PIM_NEIGHBOR_POLICY_ROUTE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitIipr_eigrp(Iipr_eigrpContext ctx) {
    Optional<RoutingProtocolInstance> eigrp = toRoutingProtocolInstance(ctx, ctx.eigrp_instance());
    if (!eigrp.isPresent()) {
      return;
    }
    String eigrpProc = eigrp.get().getTag();
    _currentInterfaces.forEach(iface -> iface.setEigrp(eigrpProc));
    _c.referenceStructure(
        ROUTER_EIGRP,
        eigrpProc,
        INTERFACE_IP_ROUTER_EIGRP,
        ctx.eigrp_instance().getStart().getLine());
  }

  @Override
  public void exitIipr_ospf(Iipr_ospfContext ctx) {
    Optional<RoutingProtocolInstance> ospf = toRoutingProtocolInstance(ctx, ctx.ospf_instance());
    if (!ospf.isPresent()) {
      return;
    }
    String ospfProc = ospf.get().getTag();
    long area = toLong(ctx.area);
    _currentInterfaces.forEach(
        iface -> {
          OspfInterface intOspf = iface.getOrCreateOspf();
          intOspf.setProcess(ospfProc);
          intOspf.setArea(area);
        });
    _c.referenceStructure(
        ROUTER_OSPF, ospfProc, INTERFACE_IP_ROUTER_OSPF, ctx.ospf_instance().getStart().getLine());
  }

  @Override
  public void enterIp_access_list(Ip_access_listContext ctx) {
    Optional<String> nameOpt = toString(ctx, ctx.name);
    if (!nameOpt.isPresent()) {
      _currentIpAccessList = new IpAccessList("dummy");
      return;
    }
    _currentIpAccessList = _c.getIpAccessLists().computeIfAbsent(nameOpt.get(), IpAccessList::new);
    _c.defineStructure(IP_ACCESS_LIST, nameOpt.get(), ctx);
  }

  @Override
  public void enterIp_as_path_access_list(Ip_as_path_access_listContext ctx) {
    Long explicitSeq;
    if (ctx.seq != null) {
      Optional<Long> seqOpt = toLong(ctx, ctx.seq);
      if (!seqOpt.isPresent()) {
        return;
      }
      explicitSeq = seqOpt.get();
    } else {
      explicitSeq = null;
    }
    Optional<String> nameOpt = toString(ctx, ctx.name);
    if (!nameOpt.isPresent()) {
      return;
    }
    Optional<String> regexOpt = toString(ctx, ctx.regex);
    if (!regexOpt.isPresent()) {
      return;
    }
    String name = nameOpt.get();
    IpAsPathAccessList asPathAccessList =
        _c.getIpAsPathAccessLists().computeIfAbsent(name, IpAsPathAccessList::new);
    SortedMap<Long, IpAsPathAccessListLine> lines = asPathAccessList.getLines();
    long seq;
    if (explicitSeq != null) {
      seq = explicitSeq;
    } else if (!lines.isEmpty()) {
      seq = lines.lastKey() + 1L;
    } else {
      seq = 1L;
    }
    asPathAccessList
        .getLines()
        .put(seq, new IpAsPathAccessListLine(toLineAction(ctx.action), seq, regexOpt.get()));
    _c.defineStructure(IP_AS_PATH_ACCESS_LIST, name, ctx);
  }

  @Override
  public void enterIp_prefix_list(Ip_prefix_listContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      _currentIpPrefixList = new IpPrefixList("dummy");
      return;
    }
    _currentIpPrefixList = _c.getIpPrefixLists().computeIfAbsent(name.get(), IpPrefixList::new);
    _c.defineStructure(IP_PREFIX_LIST, name.get(), ctx);
  }

  @Override
  public void exitIp_prefix_list(Ip_prefix_listContext ctx) {
    _currentIpPrefixList = null;
  }

  @Override
  public void exitIpp_rp_address(Ipp_rp_addressContext ctx) {
    if (ctx.pl != null) {
      Optional<String> pl = toString(ctx, ctx.pl);
      if (!pl.isPresent()) {
        return;
      }
      _c.referenceStructure(
          IP_PREFIX_LIST, pl.get(), IP_PIM_RP_ADDRESS_PREFIX_LIST, ctx.getStart().getLine());
    } else if (ctx.map != null) {
      Optional<String> map = toString(ctx, ctx.map);
      if (!map.isPresent()) {
        return;
      }
      _c.referenceStructure(
          ROUTE_MAP, map.get(), IP_PIM_RP_ADDRESS_ROUTE_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitIpp_rp_candidate(Ipp_rp_candidateContext ctx) {
    toString(ctx, ctx.interface_name())
        .ifPresent(
            name ->
                _c.referenceStructure(
                    INTERFACE,
                    name,
                    IP_PIM_RP_CANDIDATE_INTERFACE,
                    ctx.interface_name().getStart().getLine()));
    if (ctx.pl != null) {
      toString(ctx, ctx.pl)
          .ifPresent(
              name ->
                  _c.referenceStructure(
                      IP_PREFIX_LIST,
                      name,
                      IP_PIM_RP_CANDIDATE_PREFIX_LIST,
                      ctx.pl.getStart().getLine()));
    } else if (ctx.rm != null) {
      toString(ctx, ctx.rm)
          .ifPresent(
              name ->
                  _c.referenceStructure(
                      ROUTE_MAP, name, IP_PIM_RP_CANDIDATE_ROUTE_MAP, ctx.rm.getStart().getLine()));
    }
  }

  @Override
  public void exitIpt_source_interface(Ipt_source_interfaceContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (name.isPresent()) {
      _c.setTacacsSourceInterface(name.get());
      _c.referenceStructure(
          INTERFACE, name.get(), TACACS_SOURCE_INTERFACE, ctx.name.getStart().getLine());
    }
  }

  @Override
  public void enterIpv6_prefix_list(Ipv6_prefix_listContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      _currentIpv6PrefixList = new Ipv6PrefixList("dummy");
      return;
    }
    _currentIpv6PrefixList =
        _c.getIpv6PrefixLists().computeIfAbsent(nameOrErr.get(), Ipv6PrefixList::new);
    _c.defineStructure(IPV6_PREFIX_LIST, nameOrErr.get(), ctx);
  }

  @Override
  public void exitIpv6_prefix_list(Ipv6_prefix_listContext ctx) {
    _currentIpv6PrefixList = null;
  }

  @Override
  public void exitIpv6_route(Ipv6_routeContext ctx) {
    int line = ctx.getStart().getLine();
    StaticRouteV6.Builder builder = StaticRouteV6.builder(toPrefix6(ctx.network));
    if (ctx.name != null) {
      String name = toString(ctx, ctx.name);
      if (name == null) {
        return;
      }
      builder.setName(name);
    }
    if (ctx.nhint != null) {
      // TODO: if ctx.nhip is null and this version of NX-OS does not allow next-hop-int-only static
      // route, do something smart
      String nhint = _c.canonicalizeInterfaceName(ctx.nhint.getText());
      builder.setNextHopInterface(nhint);
      _c.referenceStructure(INTERFACE, nhint, IPV6_ROUTE_NEXT_HOP_INTERFACE, line);
    }
    if (ctx.nhip != null) {
      builder.setNextHopIp(toIp6(ctx.nhip));
    }
    if (ctx.nhvrf != null) {
      Optional<String> vrfOrErr = toString(ctx, ctx.nhvrf);
      if (!vrfOrErr.isPresent()) {
        return;
      }
      String vrf = vrfOrErr.get();
      _c.referenceStructure(VRF, vrf, IPV6_ROUTE_NEXT_HOP_VRF, line);
      builder.setNextHopVrf(vrf);
    }
    if (ctx.null0 != null) {
      builder.setDiscard(true);
    }
    if (ctx.pref != null) {
      Optional<Integer> pref = toInteger(ctx, ctx.pref);
      if (!pref.isPresent()) {
        return;
      }
      builder.setPreference(pref.get());
    }
    if (ctx.tag != null) {
      builder.setTag(toLong(ctx.tag));
    }
    if (ctx.track != null) {
      Optional<Integer> track = toInteger(ctx, ctx.track);
      if (!track.isPresent()) {
        return;
      }
      Integer trackNumber = track.get();
      if (!_c.getTracks().containsKey(trackNumber)) {
        warn(
            ctx,
            String.format(
                "Cannot reference undefined track %s. This line will be ignored.", trackNumber));
        _c.undefined(
            CiscoNxosStructureType.TRACK,
            trackNumber.toString(),
            CiscoNxosStructureUsage.IPV6_ROUTE_TRACK,
            ctx.start.getLine());
        return;
      }
      _c.referenceStructure(
          CiscoNxosStructureType.TRACK,
          trackNumber.toString(),
          CiscoNxosStructureUsage.IPV6_ROUTE_TRACK,
          ctx.start.getLine());

      builder.setTrack(trackNumber);
      // TODO: support track object number
      todo(ctx);
    }
    StaticRouteV6 route = builder.build();
    _currentVrf.getStaticRoutesV6().put(route.getPrefix(), route);
  }

  @Override
  public void exitIspt_qos(Ispt_qosContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _c.referenceStructure(
        POLICY_MAP_QOS, name.get(), INTERFACE_SERVICE_POLICY_QOS, ctx.getStart().getLine());
  }

  @Override
  public void exitIspt_queuing(Ispt_queuingContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _c.referenceStructure(
        POLICY_MAP_QUEUING, name.get(), INTERFACE_SERVICE_POLICY_QUEUING, ctx.getStart().getLine());
  }

  @Override
  public void enterIpv6_access_list(Ipv6_access_listContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      _currentIpv6AccessList = new Ipv6AccessList("dummy");
      return;
    }
    _currentIpv6AccessList =
        _c.getIpv6AccessLists().computeIfAbsent(name.get(), Ipv6AccessList::new);
    _c.defineStructure(IPV6_ACCESS_LIST, name.get(), ctx);
  }

  @Override
  public void exitIpv6_access_list(Ipv6_access_listContext ctx) {
    _currentIpv6AccessList = null;
  }

  @Override
  public void enterLogging_server(Logging_serverContext ctx) {
    _currentLoggingServer =
        _c.getLoggingServers().computeIfAbsent(ctx.host.getText(), LoggingServer::new);
  }

  @Override
  public void exitLogging_server(Logging_serverContext ctx) {
    _currentLoggingServer = null;
  }

  @Override
  public void exitLogging_source_interface(Logging_source_interfaceContext ctx) {
    Optional<String> inameOrError = toString(ctx, ctx.name);
    if (!inameOrError.isPresent()) {
      return;
    }
    String name = inameOrError.get();
    _c.setLoggingSourceInterface(name);
    _c.referenceStructure(INTERFACE, name, LOGGING_SOURCE_INTERFACE, ctx.name.getStart().getLine());
  }

  @Override
  public void exitLv_access_class(Lv_access_classContext ctx) {
    Optional<String> acl = toString(ctx, ctx.acl);
    if (!acl.isPresent()) {
      return;
    }
    CiscoNxosStructureUsage usage =
        ctx.IN() != null ? LINE_VTY_ACCESS_CLASS_IN : LINE_VTY_ACCESS_CLASS_OUT;
    _c.referenceStructure(IP_ACCESS_LIST, acl.get(), usage, ctx.getStart().getLine());
  }

  @Override
  public void exitLv6_access_class(Lv6_access_classContext ctx) {
    Optional<String> acl = toString(ctx, ctx.acl);
    if (!acl.isPresent()) {
      return;
    }
    CiscoNxosStructureUsage usage =
        ctx.IN() != null ? LINE_VTY_ACCESS_CLASS_IN : LINE_VTY_ACCESS_CLASS_OUT;
    _c.referenceStructure(IPV6_ACCESS_LIST, acl.get(), usage, ctx.getStart().getLine());
  }

  @Override
  public void enterMac_access_list(Mac_access_listContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    todo(ctx);
    _c.defineStructure(MAC_ACCESS_LIST, name.get(), ctx);
  }

  @Override
  public void exitMonitor_session_destination(Monitor_session_destinationContext ctx) {
    Optional<List<String>> interfaces = toStrings(ctx, ctx.range);
    if (!interfaces.isPresent()) {
      return;
    }
    interfaces
        .get()
        .forEach(
            name ->
                _c.referenceStructure(
                    INTERFACE,
                    name,
                    MONITOR_SESSION_DESTINATION_INTERFACE,
                    ctx.range.getStart().getLine()));
  }

  @Override
  public void exitMonitor_session_source_interface(Monitor_session_source_interfaceContext ctx) {
    Optional<List<String>> interfaces = toStrings(ctx, ctx.range);
    if (!interfaces.isPresent()) {
      return;
    }
    interfaces
        .get()
        .forEach(
            name ->
                _c.referenceStructure(
                    INTERFACE,
                    name,
                    MONITOR_SESSION_SOURCE_INTERFACE,
                    ctx.range.getStart().getLine()));
  }

  @Override
  public void exitMonitor_session_source_vlan(Monitor_session_source_vlanContext ctx) {
    IntegerSpace vlans = toVlanIdRange(ctx, ctx.vlans);
    if (vlans == null) {
      return;
    }
    int line = ctx.vlans.getStart().getLine();
    vlans
        .intStream()
        .forEach(
            i ->
                _c.referenceStructure(
                    VLAN, Integer.toString(i), MONITOR_SESSION_SOURCE_VLAN, line));
  }

  @Override
  public void enterNtp_server(Ntp_serverContext ctx) {
    _currentNtpServer = _c.getNtpServers().computeIfAbsent(ctx.host.getText(), NtpServer::new);
  }

  @Override
  public void exitNtp_server(Ntp_serverContext ctx) {
    _currentNtpServer = null;
  }

  @Override
  public void exitNtpag_peer(Ntpag_peerContext ctx) {
    Optional<String> acl = toString(ctx, ctx.ip_access_list_name());
    if (!acl.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_ACCESS_LIST_ABSTRACT_REF, acl.get(), NTP_ACCESS_GROUP_PEER, ctx.getStart().getLine());
  }

  @Override
  public void exitNtpag_query_only(Ntpag_query_onlyContext ctx) {
    Optional<String> acl = toString(ctx, ctx.ip_access_list_name());
    if (!acl.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_ACCESS_LIST_ABSTRACT_REF,
        acl.get(),
        NTP_ACCESS_GROUP_QUERY_ONLY,
        ctx.getStart().getLine());
  }

  @Override
  public void exitNtpag_serve(Ntpag_serveContext ctx) {
    Optional<String> acl = toString(ctx, ctx.ip_access_list_name());
    if (!acl.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_ACCESS_LIST_ABSTRACT_REF, acl.get(), NTP_ACCESS_GROUP_SERVE, ctx.getStart().getLine());
  }

  @Override
  public void exitNtpag_serve_only(Ntpag_serve_onlyContext ctx) {
    Optional<String> acl = toString(ctx, ctx.ip_access_list_name());
    if (!acl.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_ACCESS_LIST_ABSTRACT_REF,
        acl.get(),
        NTP_ACCESS_GROUP_SERVE_ONLY,
        ctx.getStart().getLine());
  }

  @Override
  public void exitNtps_prefer(Ntps_preferContext ctx) {
    _currentNtpServer.setPrefer(true);
  }

  @Override
  public void exitNtps_use_vrf(Ntps_use_vrfContext ctx) {
    toString(ctx, ctx.vrf).ifPresent(_currentNtpServer::setUseVrf);
  }

  @Override
  public void exitNtp_source_interface(Ntp_source_interfaceContext ctx) {
    Optional<String> inameOrError = toString(ctx, ctx.name);
    if (!inameOrError.isPresent()) {
      return;
    }
    String name = inameOrError.get();
    _c.setNtpSourceInterface(name);
    _c.referenceStructure(INTERFACE, name, NTP_SOURCE_INTERFACE, ctx.name.getStart().getLine());
  }

  @Override
  public void enterNve_member(Nve_memberContext ctx) {
    Optional<Integer> vniOrError = toInteger(ctx, ctx.vni);
    if (!vniOrError.isPresent()) {
      // dummy NVE member VNI
      _currentNveVnis = ImmutableList.of();
      return;
    }
    int vni = vniOrError.get();
    _currentNveVnis =
        _currentNves.stream()
            .map(nve -> nve.getMemberVni(vni))
            .collect(ImmutableList.toImmutableList());
    if (ctx.ASSOCIATE_VRF() != null) {
      _currentNveVnis.forEach(memberVni -> memberVni.setAssociateVrf(true));
    }
  }

  @Override
  public void enterRo_area(Ro_areaContext ctx) {
    long areaId = toLong(ctx.id);
    _currentOspfArea = _currentOspfProcess.getAreas().computeIfAbsent(areaId, OspfArea::new);
  }

  @Override
  public void exitRo_area(Ro_areaContext ctx) {
    _currentOspfArea = null;
  }

  @Override
  public void exitRo_auto_cost(Ro_auto_costContext ctx) {
    if (ctx.gbps != null) {
      toIntegerInSpace(
              ctx,
              ctx.gbps,
              OSPF_AUTO_COST_REFERENCE_BANDWIDTH_GBPS_RANGE,
              "router ospf auto-cost reference-bandwidth gbps")
          .ifPresent(gbps -> _currentOspfProcess.setAutoCostReferenceBandwidthMbps(gbps * 1000));
    } else {
      assert ctx.mbps != null;
      toIntegerInSpace(
              ctx,
              ctx.mbps,
              OSPF_AUTO_COST_REFERENCE_BANDWIDTH_MBPS_RANGE,
              "router ospf auto-cost reference-bandwidth mbps")
          .ifPresent(_currentOspfProcess::setAutoCostReferenceBandwidthMbps);
    }
  }

  @Override
  public void exitRo_bfd(Ro_bfdContext ctx) {
    _currentOspfProcess.setBfd(true);
  }

  @Override
  public void exitRo_default_information(Ro_default_informationContext ctx) {
    String routeMap = null;
    if (ctx.rm != null) {
      Optional<String> routeMapOrErr = toString(ctx, ctx.rm);
      if (!routeMapOrErr.isPresent()) {
        return;
      }
      routeMap = routeMapOrErr.get();
      _c.referenceStructure(
          ROUTE_MAP, routeMap, OSPF_DEFAULT_INFORMATION_ROUTE_MAP, ctx.getStart().getLine());
    }
    OspfDefaultOriginate defaultOriginate = _currentOspfProcess.getDefaultOriginate();
    if (defaultOriginate == null) {
      defaultOriginate = new OspfDefaultOriginate();
      _currentOspfProcess.setDefaultOriginate(defaultOriginate);
    }
    if (ctx.always != null) {
      defaultOriginate.setAlways(true);
    }
    if (routeMap != null) {
      defaultOriginate.setRouteMap(routeMap);
    }
  }

  @Override
  public void exitRo_distance(Ro_distanceContext ctx) {
    toInteger(ctx, ctx.protocol_distance()).ifPresent(_currentOspfProcess::setDistance);
  }

  @Override
  public void exitRo_max_metric(Ro_max_metricContext ctx) {
    if (ctx.on_startup != null) {
      return;
    }

    @Nullable Integer externalLsa = null;
    if (ctx.external_lsa != null) {
      if (ctx.manual_external_lsa != null) {
        Optional<Integer> externalLsaOrErr =
            toIntegerInSpace(
                ctx,
                ctx.manual_external_lsa,
                OSPF_MAX_METRIC_EXTERNAL_LSA_RANGE,
                "OSPF external LSA max metric");
        if (!externalLsaOrErr.isPresent()) {
          return;
        }
        externalLsa = externalLsaOrErr.get();
      } else {
        externalLsa = OspfMaxMetricRouterLsa.DEFAULT_OSPF_MAX_METRIC;
      }
    }
    @Nullable Integer summaryLsa = null;
    if (ctx.summary_lsa != null) {
      if (ctx.manual_summary_lsa != null) {
        Optional<Integer> summaryLsaOrErr =
            toIntegerInSpace(
                ctx,
                ctx.manual_summary_lsa,
                OSPF_MAX_METRIC_SUMMARY_LSA_RANGE,
                "OSPF summary LSA max metric");
        if (!summaryLsaOrErr.isPresent()) {
          return;
        }
        summaryLsa = summaryLsaOrErr.get();
      } else {
        summaryLsa = OspfMaxMetricRouterLsa.DEFAULT_OSPF_MAX_METRIC;
      }
    }

    OspfMaxMetricRouterLsa maxMetricRouterLsa = _currentOspfProcess.getMaxMetricRouterLsa();
    if (maxMetricRouterLsa == null) {
      maxMetricRouterLsa = new OspfMaxMetricRouterLsa();
      _currentOspfProcess.setMaxMetricRouterLsa(maxMetricRouterLsa);
    }
    if (externalLsa != null) {
      maxMetricRouterLsa.setExternalLsa(externalLsa);
    }
    if (ctx.include_stub != null) {
      maxMetricRouterLsa.setIncludeStub(true);
    }
    if (summaryLsa != null) {
      maxMetricRouterLsa.setSummaryLsa(summaryLsa);
    }
  }

  @Override
  public void exitRo_network(Ro_networkContext ctx) {
    IpWildcard wildcard;
    if (ctx.ip != null) {
      wildcard = ipWithWildcardMask(toIp(ctx.ip), toIp(ctx.wildcard));
    } else {
      assert ctx.prefix != null;
      wildcard = IpWildcard.create(toPrefix(ctx.prefix));
    }
    long areaId = toLong(ctx.id);
    _currentOspfProcess.getAreas().computeIfAbsent(areaId, OspfArea::new);
    _currentOspfProcess.getNetworks().put(wildcard, toLong(ctx.id));
  }

  @Override
  public void exitRo_passive_interface(Ro_passive_interfaceContext ctx) {
    _currentOspfProcess.setPassiveInterfaceDefault(true);
  }

  @Override
  public void exitRor_redistribute_route_map(Ror_redistribute_route_mapContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError =
        toRoutingProtocolInstance(ctx, ctx.routing_instance_v4());
    Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
    if (!rpiOrError.isPresent() || !mapOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String map = mapOrError.get();
    Optional<CiscoNxosStructureType> type = rpi.getProtocol().getRouterStructureType();
    if (rpi.getTag() != null && type.isPresent()) {
      _c.referenceStructure(
          type.get(), rpi.getTag(), OSPF_REDISTRIBUTE_INSTANCE, ctx.getStart().getLine());
    }
    _c.referenceStructure(ROUTE_MAP, map, OSPF_REDISTRIBUTE_ROUTE_MAP, ctx.getStart().getLine());
    _currentOspfProcess.setRedistributionPolicy(rpi, map);
  }

  @Override
  public void exitRo_no_redistribute(CiscoNxosParser.Ro_no_redistributeContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError =
        toRoutingProtocolInstance(ctx, ctx.routing_instance_v4());
    if (!rpiOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String routeMap = null;
    if (ctx.route_map_name() != null) {
      Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
      if (!mapOrError.isPresent()) {
        return;
      }
      routeMap = mapOrError.get();
    }
    if (!_currentOspfProcess.deleteRedistributionPolicy(rpi, routeMap)) {
      warn(ctx, "No matching redistribution policy to remove");
    }
  }

  @Override
  public void exitRo_router_id(Ro_router_idContext ctx) {
    _currentOspfProcess.setRouterId(toIp(ctx.id));
  }

  @Override
  public void exitRo_summary_address(Ro_summary_addressContext ctx) {
    OspfSummaryAddress summaryAddress =
        _currentOspfProcess
            .getSummaryAddresses()
            .computeIfAbsent(toPrefix(ctx.network), OspfSummaryAddress::new);
    if (ctx.not_advertise != null) {
      summaryAddress.setNotAdvertise(true);
    } else if (ctx.tag != null) {
      summaryAddress.setNotAdvertise(false);
      summaryAddress.setTag(toLong(ctx.tag));
    }
    // Not implemented yet.
    todo(ctx);
  }

  @Override
  public void enterRo_vrf(Ro_vrfContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      _currentOspfProcess = new OspfVrf("dummy");
      return;
    }
    _currentOspfProcess =
        _currentDefaultVrfOspfProcess.getVrfs().computeIfAbsent(nameOrErr.get(), OspfVrf::new);
  }

  @Override
  public void exitRo_vrf(Ro_vrfContext ctx) {
    _currentOspfProcess = _currentDefaultVrfOspfProcess;
  }

  @Override
  public void exitRot_lsa_arrival(Rot_lsa_arrivalContext ctx) {
    toIntegerInSpace(
            ctx, ctx.time_ms, OSPF_TIMERS_LSA_ARRIVAL_MS_RANGE, "OSPF LSA arrival interval")
        .ifPresent(_currentOspfProcess::setTimersLsaArrival);
  }

  @Override
  public void enterRot_lsa_group_pacing(Rot_lsa_group_pacingContext ctx) {
    toIntegerInSpace(
            ctx, ctx.time_s, OSPF_TIMERS_LSA_GROUP_PACING_S_RANGE, "OSPF LSA group pacing interval")
        .ifPresent(_currentOspfProcess::setTimersLsaGroupPacing);
  }

  @Override
  public void enterRouter_isis_stanza(CiscoNxosParser.Router_isis_stanzaContext ctx)
  {
      IsisProcess isisProcess = new IsisProcess();
      isisProcess.setLevel(IsisLevel.LEVEL_1_2);
      _currentVrf.setIsisProcess(isisProcess);
      _currentIsisProcess = isisProcess;
  }

    @Override
    public void enterInterface_is_stanza(CiscoNxosParser.Interface_is_stanzaContext ctx) {
        Interface iface = getOrAddInterface(ctx.iname);
        iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
        _currentIsisInterface = iface;
    }




    @Override
    public void enterIs_type_is_stanza(CiscoNxosParser.Is_type_is_stanzaContext ctx) {
        IsisProcess proc = _currentVrf.getIsisProcess();
        if (ctx.LEVEL_1() != null) {
            proc.setLevel(IsisLevel.LEVEL_1);
        } else if (ctx.LEVEL_2_ONLY() != null || ctx.LEVEL_2() != null) {
            proc.setLevel(IsisLevel.LEVEL_2);
        } else {
            throw new BatfishException("Unsupported is-type");
        }
    }

    @Override
    public void enterNet_is_stanza(CiscoNxosParser.Net_is_stanzaContext ctx) {
        IsisProcess proc = _currentVrf.getIsisProcess();
        IsoAddress isoAddress = new IsoAddress(ctx.ISO_ADDRESS().getText());
        proc.setNetAddress(isoAddress);
    }

    private static Ip toIp2(Token t) {
        return Ip.parse(t.getText());
    }

    private static int toInteger2(CiscoNxosParser.DecContext ctx) {
        return Integer.parseInt(ctx.getText());
    }


    @Override
    public void exitIf_ip_router_isis(CiscoNxosParser.If_ip_router_isisContext ctx) {
        for (Interface iface : _currentInterfaces) {
            iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
        }
    }




    private static long toLong(CiscoNxosParser.DecContext ctx) {
        return Long.parseLong(ctx.getText());
    }

    @Override
    public void exitIf_isis_metric(CiscoNxosParser.If_isis_metricContext ctx) {
        long metric = toLong(ctx.metric);
        for (Interface iface : _currentInterfaces) {
            iface.setIsisCost(metric);
        }
    }

    @Override
    public void exitPassive_interface_is_stanza(CiscoNxosParser.Passive_interface_is_stanzaContext ctx) {
        Interface iface = getOrAddInterface(ctx.name);
        if (ctx.NO() == null) {
            iface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
        } else {
            iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
        }
    }

    @Override
    public void exitSuppressed_iis_stanza(CiscoNxosParser.Suppressed_iis_stanzaContext ctx) {
        if (ctx.NO() != null) {
            _currentIsisInterface.setIsisInterfaceMode(IsisInterfaceMode.SUPPRESSED);
        }
    }


    @Override
    public void exitPassive_interface_default_is_stanza(
            CiscoNxosParser.Passive_interface_default_is_stanzaContext ctx) {
        for (Interface iface : _c.getInterfaces().values()) {
            iface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
        }
    }

    @Override
    public void exitPassive_iis_stanza(CiscoNxosParser.Passive_iis_stanzaContext ctx) {
        _currentIsisInterface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
    }

    @Override
    public void exitInterface_is_stanza(CiscoNxosParser.Interface_is_stanzaContext ctx) {
        _currentIsisInterface = null;
    }

    @Override
    public void exitRouter_isis_stanza(CiscoNxosParser.Router_isis_stanzaContext ctx) {
        _currentIsisProcess = null;
    }

  @Override
  public void exitRott_lsa(Rott_lsaContext ctx) {
    Optional<Integer> startIntervalOrErr =
        toIntegerInSpace(
            ctx,
            ctx.start_interval_ms,
            OSPF_TIMERS_LSA_START_INTERVAL_MS_RANGE,
            "OSPF LSA start interval");
    if (!startIntervalOrErr.isPresent()) {
      return;
    }
    Optional<Integer> holdIntervalOrErr =
        toIntegerInSpace(
            ctx,
            ctx.hold_interval_ms,
            OSPF_TIMERS_LSA_HOLD_INTERVAL_MS_RANGE,
            "OSPF LSA hold interval");
    if (!holdIntervalOrErr.isPresent()) {
      return;
    }
    Optional<Integer> maxIntervalOrErr =
        toIntegerInSpace(
            ctx,
            ctx.max_interval_ms,
            OSPF_TIMERS_LSA_MAX_INTERVAL_MS_RANGE,
            "OSPF LSA max interval");
    if (!maxIntervalOrErr.isPresent()) {
      return;
    }
    _currentOspfProcess.setTimersLsaStartInterval(startIntervalOrErr.get());
    _currentOspfProcess.setTimersLsaHoldInterval(holdIntervalOrErr.get());
    _currentOspfProcess.setTimersLsaMaxInterval(maxIntervalOrErr.get());
  }

  @Override
  public void exitRoa_authentication(Roa_authenticationContext ctx) {
    _currentOspfArea.setAuthentication(
        ctx.digest != null ? OspfAreaAuthentication.MESSAGE_DIGEST : OspfAreaAuthentication.SIMPLE);
  }

  @Override
  public void exitRoa_default_cost(Roa_default_costContext ctx) {
    toInteger(ctx, ctx.cost).ifPresent(_currentOspfArea::setDefaultCost);
  }

  @Override
  public void exitRoa_filter_list(Roa_filter_listContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      return;
    }
    String name = nameOrErr.get();
    CiscoNxosStructureUsage usage;
    if (ctx.in != null) {
      usage = CiscoNxosStructureUsage.OSPF_AREA_FILTER_LIST_IN;
      _currentOspfArea.setFilterListIn(name);
    } else {
      assert ctx.out != null;
      usage = CiscoNxosStructureUsage.OSPF_AREA_FILTER_LIST_OUT;
      _currentOspfArea.setFilterListOut(name);
    }
    _c.referenceStructure(ROUTE_MAP, name, usage, ctx.getStart().getLine());
  }

  @Override
  public void exitRoa_nssa(Roa_nssaContext ctx) {
    if (_currentOspfArea.getId() == 0L) {
      warn(ctx, "Backbone area cannot be an NSSA");
      return;
    }
    String routeMap = null;
    if (ctx.rm != null) {
      Optional<String> routeMapOrErr = toString(ctx, ctx.rm);
      if (!routeMapOrErr.isPresent()) {
        return;
      }
      routeMap = routeMapOrErr.get();
      _c.referenceStructure(
          ROUTE_MAP, routeMap, OSPF_AREA_NSSA_ROUTE_MAP, ctx.getStart().getLine());
    }
    OspfAreaNssa nssa =
        Optional.ofNullable(_currentOspfArea.getTypeSettings())
            .filter(OspfAreaNssa.class::isInstance)
            .map(OspfAreaNssa.class::cast)
            .orElseGet(
                () -> {
                  // overwrite if missing or a different area type
                  OspfAreaNssa newNssa = new OspfAreaNssa();
                  _currentOspfArea.setTypeSettings(newNssa);
                  return newNssa;
                });
    if (ctx.default_information_originate != null) {
      nssa.setDefaultInformationOriginate(true);
      if (routeMap != null) {
        nssa.setDefaultInformationOriginateMap(routeMap);
      }
    }
    if (ctx.no_redistribution != null) {
      nssa.setNoRedistribution(true);
    }
    if (ctx.no_summary != null) {
      nssa.setNoSummary(true);
    }
  }

  @Override
  public void exitRoa_range(Roa_rangeContext ctx) {
    Integer cost = null;
    if (ctx.cost != null) {
      Optional<Integer> costOrErr = toInteger(ctx, ctx.cost);
      if (!costOrErr.isPresent()) {
        return;
      }
      cost = costOrErr.get();
    }
    OspfAreaRange range =
        _currentOspfArea.getRanges().computeIfAbsent(toPrefix(ctx.network), OspfAreaRange::new);
    if (cost != null) {
      range.setCost(cost);
    }
    if (ctx.not_advertise != null) {
      range.setNotAdvertise(true);
    }
  }

  @Override
  public void exitRoa_stub(Roa_stubContext ctx) {
    if (_currentOspfArea.getId() == 0L) {
      warn(ctx, "Backbone area cannot be a stub");
      return;
    }
    OspfAreaStub stub =
        Optional.ofNullable(_currentOspfArea.getTypeSettings())
            .filter(OspfAreaStub.class::isInstance)
            .map(OspfAreaStub.class::cast)
            .orElseGet(
                () -> {
                  // overwrite if missing or a different area type
                  OspfAreaStub newStub = new OspfAreaStub();
                  _currentOspfArea.setTypeSettings(newStub);
                  return newStub;
                });
    if (ctx.no_summary != null) {
      stub.setNoSummary(true);
    }
  }

  @Override
  public void exitSysds_shutdown(Sysds_shutdownContext ctx) {
    _c.setSystemDefaultSwitchportShutdown(true);
  }

  @Override
  public void exitNo_sysds_shutdown(No_sysds_shutdownContext ctx) {
    _c.setSystemDefaultSwitchportShutdown(false);
  }

  @Override
  public void exitSysds_switchport(Sysds_switchportContext ctx) {
    _c.setSystemDefaultSwitchport(true);
  }

  @Override
  public void exitNo_sysds_switchport(No_sysds_switchportContext ctx) {
    _c.setSystemDefaultSwitchport(false);
  }

  @Override
  public void exitSysqosspt_network_qos(Sysqosspt_network_qosContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _c.referenceStructure(
        POLICY_MAP_NETWORK_QOS, name.get(), SYSQOS_NETWORK_QOS, ctx.getStart().getLine());
  }

  @Override
  public void exitSysqosspt_qos(Sysqosspt_qosContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _c.referenceStructure(POLICY_MAP_QOS, name.get(), SYSQOS_QOS, ctx.getStart().getLine());
  }

  @Override
  public void exitSysqosspt_queueing(Sysqosspt_queueingContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _c.referenceStructure(POLICY_MAP_QUEUING, name.get(), SYSQOS_QUEUING, ctx.getStart().getLine());
  }

  @Override
  public void enterRouter_eigrp(Router_eigrpContext ctx) {
    Optional<String> processTagOrErr = toString(ctx, ctx.tag);
    if (processTagOrErr.isPresent()) {
      String processTag = processTagOrErr.get();
      _currentEigrpProcess = _c.getOrCreateEigrpProcess(processTag);
      _c.defineStructure(ROUTER_EIGRP, processTag, ctx);
      _c.referenceStructure(
          ROUTER_EIGRP, processTag, ROUTER_EIGRP_SELF_REFERENCE, ctx.tag.getStart().getLine());
      toMaybeAsn(processTag).ifPresent(_currentEigrpProcess::setAsn);
    } else {
      // Dummy process, with all inner config also dummy.
      _currentEigrpProcess = new EigrpProcessConfiguration();
    }
    _currentEigrpVrf = _currentEigrpProcess.getOrCreateVrf(DEFAULT_VRF_NAME);
    _currentEigrpVrfIpAf = _currentEigrpVrf.getVrfIpv4AddressFamily();
  }

  @Override
  public void exitRouter_eigrp(Router_eigrpContext ctx) {
    _currentEigrpProcess = null;
    _currentEigrpVrf = null;
    _currentEigrpVrfIpAf = null;
  }

  @Override
  public void enterRouter_ospf(Router_ospfContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      return;
    }
    _currentDefaultVrfOspfProcess =
        _c.getOspfProcesses().computeIfAbsent(nameOrErr.get(), DefaultVrfOspfProcess::new);
    _currentOspfProcess = _currentDefaultVrfOspfProcess;
    _c.defineStructure(ROUTER_OSPF, nameOrErr.get(), ctx);
    _c.referenceStructure(
        ROUTER_OSPF, nameOrErr.get(), ROUTER_OSPF_SELF_REFERENCE, ctx.name.getStart().getLine());
  }

  @Override
  public void exitRouter_ospf(Router_ospfContext ctx) {
    _currentDefaultVrfOspfProcess = null;
    _currentOspfProcess = null;
  }

  @Override
  public void enterRouter_ospfv3(Router_ospfv3Context ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      return;
    }
    _c.defineStructure(ROUTER_OSPFV3, nameOrErr.get(), ctx);
    _c.referenceStructure(
        ROUTER_OSPFV3,
        nameOrErr.get(),
        ROUTER_OSPFV3_SELF_REFERENCE,
        ctx.name.getStart().getLine());
  }

  @Override
  public void enterRouter_rip(Router_ripContext ctx) {
    Optional<RoutingProtocolInstance> proc = toRoutingProtocolInstance(ctx, ctx.rip_instance());
    if (!proc.isPresent()) {
      return;
    }
    String id = proc.get().getTag();
    assert id != null;
    _c.defineStructure(ROUTER_RIP, id, ctx);
    _c.referenceStructure(ROUTER_RIP, id, ROUTER_RIP_SELF_REFERENCE, ctx.getStart().getLine());
  }

  @Override
  public void enterRr_vrf(Rr_vrfContext ctx) {
    Optional<String> vrf = toString(ctx, ctx.name);
    if (!vrf.isPresent()) {
      return;
    }
    _c.referenceStructure(VRF, vrf.get(), ROUTER_RIP_VRF, ctx.getStart().getLine());
  }

  @Override
  public void exitNve_member(Nve_memberContext ctx) {
    _currentNveVnis = null;
  }

  @Override
  public void exitNvm_ingress_replication(Nvm_ingress_replicationContext ctx) {
    if (ctx.BGP() != null) {
      if (_currentNves.stream()
          .anyMatch(nve -> nve.getHostReachabilityProtocol() != HostReachabilityProtocol.BGP)) {
        warn(
            ctx,
            "Cannot enable ingress replication bgp under VNI without host-reachability protocol"
                + " bgp");
        return;
      }
      _currentNveVnis.forEach(
          vni -> vni.setIngressReplicationProtocol(IngressReplicationProtocol.BGP));
    } else {
      assert ctx.STATIC() != null;
      if (_currentNves.stream().anyMatch(nve -> nve.getHostReachabilityProtocol() != null)) {
        warn(
            ctx,
            "Cannot enable ingress replication static under VNI without unset host-reachability"
                + " protocol");
        return;
      }
      _currentNveVnis.forEach(
          vni -> vni.setIngressReplicationProtocol(IngressReplicationProtocol.STATIC));
    }
  }

  @Override
  public void exitNvm_mcast_group(Nvm_mcast_groupContext ctx) {
    Ip mcastIp = toIp(ctx.first);
    if (!Prefix.MULTICAST.containsIp(mcastIp)) {
      warn(ctx, String.format("IPv4 address %s is not a valid multicast IP", mcastIp));
      return;
    }
    if (_currentNveVnis.stream()
        .anyMatch(
            vni -> vni.getIngressReplicationProtocol() == IngressReplicationProtocol.STATIC)) {
      warn(ctx, "Cannot set multicast group with ingress-replication protocol static");
      return;
    }
    _currentNveVnis.forEach(vni -> vni.setMcastGroup(mcastIp));
  }

  @Override
  public void exitNvm_peer_ip(Nvm_peer_ipContext ctx) {
    if (_currentNveVnis.stream()
        .anyMatch(
            vni -> vni.getIngressReplicationProtocol() != IngressReplicationProtocol.STATIC)) {
      warn(ctx, "Cannot set peer-ip unless ingress-replication protocol is static");
      return;
    }
    Ip peerIp = toIp(ctx.ip_address());
    _currentNveVnis.forEach(vni -> vni.addPeerIp(peerIp));
  }

  @Override
  public void exitNvm_suppress_arp(Nvm_suppress_arpContext ctx) {
    boolean value = ctx.DISABLE() == null;
    _currentNveVnis.forEach(vni -> vni.setSuppressArp(value));
  }

  @Override
  public void exitNvg_ingress_replication(Nvg_ingress_replicationContext ctx) {
    if (_currentNves.stream()
        .anyMatch(nve -> nve.getHostReachabilityProtocol() != HostReachabilityProtocol.BGP)) {
      warn(
          ctx,
          "Cannot configure Ingress replication protocol BGP for nve without host reachability"
              + " protocol bgp.");
      return;
    }
    _currentNves.forEach(
        vni -> vni.setGlobalIngressReplicationProtocol(IngressReplicationProtocol.BGP));
  }

  @Override
  public void exitNvg_mcast_group(Nvg_mcast_groupContext ctx) {
    Ip mcastIp = toIp(ctx.ip_address());
    if (!Prefix.MULTICAST.containsIp(mcastIp)) {
      warn(ctx, String.format("IPv4 address %s is not a valid multicast IP", mcastIp));
      return;
    }
    if (ctx.L2() != null) {
      _currentNves.forEach(vni -> vni.setMulticastGroupL2(mcastIp));
    } else {
      assert ctx.L3() != null;
      _currentNves.forEach(vni -> vni.setMulticastGroupL3(mcastIp));
    }
  }

  @Override
  public void exitNvg_suppress_arp(Nvg_suppress_arpContext ctx) {
    _currentNves.forEach(vni -> vni.setGlobalSuppressArp(true));
  }

  @Override
  public void enterOgip_address(Ogip_addressContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      _currentObjectGroupIpAddress = new ObjectGroupIpAddress("dummy");
      return;
    }
    String name = nameOrErr.get();
    ObjectGroup existing = _c.getObjectGroups().get(name);
    if (existing != null) {
      if (!(existing instanceof ObjectGroupIpAddress)) {
        warn(
            ctx,
            String.format(
                "Cannot create object-group '%s' of type ip address because an object-group of a"
                    + " different type already exists with that name.",
                name));
        _currentObjectGroupIpAddress = new ObjectGroupIpAddress("dummy");
        return;
      }
      _currentObjectGroupIpAddress = (ObjectGroupIpAddress) existing;
    } else {
      _currentObjectGroupIpAddress = new ObjectGroupIpAddress(name);
      _c.defineStructure(OBJECT_GROUP_IP_ADDRESS, name, ctx);
      _c.getObjectGroups().put(name, _currentObjectGroupIpAddress);
    }
  }

  @Override
  public void exitOgip_address(Ogip_addressContext ctx) {
    _currentObjectGroupIpAddress = null;
  }

  @Override
  public void exitOgipa_line(Ogipa_lineContext ctx) {
    long seq;
    if (ctx.seq != null) {
      Optional<Long> seqOrErr =
          toLongInSpace(ctx, ctx.seq, OBJECT_GROUP_SEQUENCE_RANGE, "object-group sequence number");
      if (!seqOrErr.isPresent()) {
        return;
      }
      seq = seqOrErr.get();
    } else if (_currentObjectGroupIpAddress.getLines().isEmpty()) {
      seq = 10L;
    } else {
      seq = _currentObjectGroupIpAddress.getLines().lastKey() + 10L;
    }
    IpWildcard ipWildcard;
    if (ctx.address != null) {
      Ip address = toIp(ctx.address);
      if (ctx.wildcard != null) {
        ipWildcard = IpWildcard.ipWithWildcardMask(address, toIp(ctx.wildcard));
      } else {
        // host
        ipWildcard = IpWildcard.create(address);
      }
    } else {
      assert ctx.prefix != null;
      ipWildcard = IpWildcard.create(toPrefix(ctx.prefix));
    }
    _currentObjectGroupIpAddress.getLines().put(seq, new ObjectGroupIpAddressLine(seq, ipWildcard));
  }

  @Override
  public void enterOgip_port(Ogip_portContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      _currentObjectGroupIpPort = new ObjectGroupIpPort("dummy");
      return;
    }
    String name = nameOrErr.get();
    ObjectGroup existing = _c.getObjectGroups().get(name);
    if (existing != null) {
      if (!(existing instanceof ObjectGroupIpPort)) {
        warn(
            ctx,
            String.format(
                "Cannot create object-group '%s' of type ip port because an object-group of a"
                    + " different type already exists with that name.",
                name));
        _currentObjectGroupIpPort = new ObjectGroupIpPort("dummy");
        return;
      }
      _currentObjectGroupIpPort = (ObjectGroupIpPort) existing;
    } else {
      _currentObjectGroupIpPort = new ObjectGroupIpPort(name);
      _c.defineStructure(OBJECT_GROUP_IP_PORT, name, ctx);
      _c.getObjectGroups().put(name, _currentObjectGroupIpPort);
    }
  }

  @Override
  public void exitOgip_port(Ogip_portContext ctx) {
    _currentObjectGroupIpPort = null;
  }

  @Override
  public void exitOgipp_line(Ogipp_lineContext ctx) {
    long seq;
    if (ctx.seq != null) {
      Optional<Long> seqOrErr =
          toLongInSpace(ctx, ctx.seq, OBJECT_GROUP_SEQUENCE_RANGE, "object-group sequence number");
      if (!seqOrErr.isPresent()) {
        return;
      }
      seq = seqOrErr.get();
    } else if (_currentObjectGroupIpPort.getLines().isEmpty()) {
      seq = 10L;
    } else {
      seq = _currentObjectGroupIpPort.getLines().lastKey() + 10L;
    }
    IntegerSpace ports;
    if (ctx.eq != null) {
      ports = IntegerSpace.of(toInteger(ctx.eq));
    } else if (ctx.gt != null) {
      Optional<Integer> gtOrErr =
          toIntegerInSpace(ctx, ctx.gt, OBJECT_GROUP_PORT_GT_SPACE, "object-group ip port gt");
      if (!gtOrErr.isPresent()) {
        return;
      }
      ports = IntegerSpace.of(Range.closed(gtOrErr.get() + 1, 65535));
    } else if (ctx.lt != null) {
      Optional<Integer> ltOrErr =
          toIntegerInSpace(ctx, ctx.lt, OBJECT_GROUP_PORT_LT_SPACE, "object-group ip port lt");
      if (!ltOrErr.isPresent()) {
        return;
      }
      ports = IntegerSpace.of(Range.closed(0, ltOrErr.get() - 1));
    } else if (ctx.neq != null) {
      ports = IntegerSpace.PORTS.difference(IntegerSpace.of(toInteger(ctx.neq)));
    } else {
      assert ctx.range1 != null && ctx.range2 != null;
      int range1 = toInteger(ctx.range1);
      int range2 = toInteger(ctx.range2);
      ports = IntegerSpace.of(Range.closed(Math.min(range1, range2), Math.max(range1, range2)));
    }
    _currentObjectGroupIpPort.getLines().put(seq, new ObjectGroupIpPortLine(seq, ports));
  }

  @Override
  public void enterPm_control_plane(Pm_control_planeContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(POLICY_MAP_CONTROL_PLANE, name, ctx);
  }

  @Override
  public void enterPm_network_qos(Pm_network_qosContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(POLICY_MAP_NETWORK_QOS, name, ctx);
  }

  @Override
  public void enterPm_qos(Pm_qosContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(POLICY_MAP_QOS, name, ctx);
  }

  @Override
  public void enterPm_queuing(Pm_queuingContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.defineStructure(POLICY_MAP_QUEUING, name, ctx);
  }

  @Override
  public void enterRb_af_ipv4_multicast(Rb_af_ipv4_multicastContext ctx) {
    BgpVrfAddressFamilyConfiguration af =
        _currentBgpVrfConfiguration.getOrCreateAddressFamily(Type.IPV4_MULTICAST);
    assert af instanceof BgpVrfIpv4AddressFamilyConfiguration;
    _currentBgpVrfIpAddressFamily = (BgpVrfIpAddressFamilyConfiguration) af;
  }

  @Override
  public void exitRb_af_ipv4_multicast(Rb_af_ipv4_multicastContext ctx) {
    _currentBgpVrfIpAddressFamily = null;
  }

  @Override
  public void enterRb_af_ipv4_unicast(Rb_af_ipv4_unicastContext ctx) {
    BgpVrfAddressFamilyConfiguration af =
        _currentBgpVrfConfiguration.getOrCreateAddressFamily(Type.IPV4_UNICAST);
    assert af instanceof BgpVrfIpv4AddressFamilyConfiguration;
    _currentBgpVrfIpAddressFamily = (BgpVrfIpAddressFamilyConfiguration) af;
  }

  @Override
  public void exitRb_af_ipv4_unicast(Rb_af_ipv4_unicastContext ctx) {
    _currentBgpVrfIpAddressFamily = null;
  }

  @Override
  public void enterRb_af4_aggregate_address(Rb_af4_aggregate_addressContext ctx) {
    assert _currentBgpVrfIpAddressFamily instanceof BgpVrfIpv4AddressFamilyConfiguration;
    BgpVrfIpv4AddressFamilyConfiguration afConfig =
        (BgpVrfIpv4AddressFamilyConfiguration) _currentBgpVrfIpAddressFamily;
    Prefix prefix = toPrefix(ctx.network);
    _currentBgpVrfAddressFamilyAggregateNetwork = afConfig.getOrCreateAggregateNetwork(prefix);
  }

  @Override
  public void enterRb_af4_no_aggregate_address(Rb_af4_no_aggregate_addressContext ctx) {
    assert _currentBgpVrfIpAddressFamily instanceof BgpVrfIpv4AddressFamilyConfiguration;
    BgpVrfIpv4AddressFamilyConfiguration afConfig =
        (BgpVrfIpv4AddressFamilyConfiguration) _currentBgpVrfIpAddressFamily;
    Prefix prefix = toPrefix(ctx.network);
    // dummy
    _currentBgpVrfAddressFamilyAggregateNetwork =
        new BgpVrfAddressFamilyAggregateNetworkConfiguration();
    boolean removed = afConfig.removeAggregateNetwork(prefix);
    if (!removed) {
      warn(
          ctx,
          String.format(
              "Removing non-existent aggregate network: %s in vrf: %s",
              prefix, _currentBgpVrfName));
    }
  }

  @Override
  public void exitRb_af4_no_aggregate_address(Rb_af4_no_aggregate_addressContext ctx) {
    _currentBgpVrfAddressFamilyAggregateNetwork = null;
  }

  @Override
  public void exitRb_af4_aggregate_address(Rb_af4_aggregate_addressContext ctx) {
    _currentBgpVrfAddressFamilyAggregateNetwork = null;
  }

  @Override
  public void exitRb_af4_network(Rb_af4_networkContext ctx) {
    assert _currentBgpVrfIpAddressFamily instanceof BgpVrfIpv4AddressFamilyConfiguration;
    BgpVrfIpv4AddressFamilyConfiguration afConfig =
        (BgpVrfIpv4AddressFamilyConfiguration) _currentBgpVrfIpAddressFamily;
    String mapname = null;
    if (ctx.mapname != null) {
      Optional<String> nameOrError = toString(ctx, ctx.mapname);
      if (!nameOrError.isPresent()) {
        return;
      }
      mapname = nameOrError.get();
      _c.referenceStructure(ROUTE_MAP, mapname, BGP_NETWORK_ROUTE_MAP, ctx.getStart().getLine());
    }

    Prefix prefix = toPrefix(ctx.network);
    afConfig.addNetwork(prefix, mapname);
  }

  @Override
  public void exitRb_af4_redistribute(Rb_af4_redistributeContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError =
        toRoutingProtocolInstance(ctx, ctx.routing_instance_v4());
    Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
    if (!rpiOrError.isPresent() || !mapOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String map = mapOrError.get();
    Optional<CiscoNxosStructureType> type = rpi.getProtocol().getRouterStructureType();
    if (rpi.getTag() != null && type.isPresent()) {
      _c.referenceStructure(
          type.get(), rpi.getTag(), BGP_REDISTRIBUTE_INSTANCE, ctx.getStart().getLine());
    }
    _c.referenceStructure(ROUTE_MAP, map, BGP_REDISTRIBUTE_ROUTE_MAP, ctx.getStart().getLine());
    _currentBgpVrfIpAddressFamily.setRedistributionPolicy(rpi, map);
  }

  @Override
  public void enterRb_af_ipv6_multicast(Rb_af_ipv6_multicastContext ctx) {
    BgpVrfAddressFamilyConfiguration af =
        _currentBgpVrfConfiguration.getOrCreateAddressFamily(Type.IPV6_MULTICAST);
    assert af instanceof BgpVrfIpv6AddressFamilyConfiguration;
    _currentBgpVrfIpAddressFamily = (BgpVrfIpAddressFamilyConfiguration) af;
  }

  @Override
  public void exitRb_af_ipv6_multicast(Rb_af_ipv6_multicastContext ctx) {
    _currentBgpVrfIpAddressFamily = null;
  }

  @Override
  public void enterRb_af_ipv6_unicast(Rb_af_ipv6_unicastContext ctx) {
    BgpVrfAddressFamilyConfiguration af =
        _currentBgpVrfConfiguration.getOrCreateAddressFamily(Type.IPV6_UNICAST);
    assert af instanceof BgpVrfIpv6AddressFamilyConfiguration;
    _currentBgpVrfIpAddressFamily = (BgpVrfIpAddressFamilyConfiguration) af;
  }

  @Override
  public void exitRb_af_ipv6_unicast(Rb_af_ipv6_unicastContext ctx) {
    _currentBgpVrfIpAddressFamily = null;
  }

  @Override
  public void enterRb_af6_aggregate_address(Rb_af6_aggregate_addressContext ctx) {
    Prefix6 prefix = toPrefix6(ctx.network);

    assert _currentBgpVrfIpAddressFamily instanceof BgpVrfIpv6AddressFamilyConfiguration;
    BgpVrfIpv6AddressFamilyConfiguration afConfig =
        (BgpVrfIpv6AddressFamilyConfiguration) _currentBgpVrfIpAddressFamily;
    _currentBgpVrfAddressFamilyAggregateNetwork = afConfig.getOrCreateAggregateNetwork(prefix);
  }

  @Override
  public void exitRb_af6_aggregate_address(Rb_af6_aggregate_addressContext ctx) {
    _currentBgpVrfAddressFamilyAggregateNetwork = null;
  }

  @Override
  public void enterRb_af6_no_aggregate_address(Rb_af6_no_aggregate_addressContext ctx) {
    Prefix6 prefix = toPrefix6(ctx.network);

    assert _currentBgpVrfIpAddressFamily instanceof BgpVrfIpv6AddressFamilyConfiguration;
    BgpVrfIpv6AddressFamilyConfiguration afConfig =
        (BgpVrfIpv6AddressFamilyConfiguration) _currentBgpVrfIpAddressFamily;
    // dummy
    _currentBgpVrfAddressFamilyAggregateNetwork =
        new BgpVrfAddressFamilyAggregateNetworkConfiguration();
    boolean removed = afConfig.removeAggregateNetwork(prefix);
    if (!removed) {
      warn(
          ctx,
          String.format(
              "Removing non-existent aggregate network: %s in vrf: %s",
              prefix, _currentBgpVrfName));
    }
  }

  @Override
  public void exitRb_af6_no_aggregate_address(Rb_af6_no_aggregate_addressContext ctx) {
    _currentBgpVrfAddressFamilyAggregateNetwork = null;
  }

  @Override
  public void exitRb_af6_network(Rb_af6_networkContext ctx) {
    String mapname = null;
    if (ctx.mapname != null) {
      Optional<String> nameOrError = toString(ctx, ctx.mapname);
      if (!nameOrError.isPresent()) {
        return;
      }
      mapname = nameOrError.get();
      _c.referenceStructure(ROUTE_MAP, mapname, BGP_NETWORK6_ROUTE_MAP, ctx.getStart().getLine());
    }

    Prefix6 prefix = toPrefix6(ctx.network);
    assert _currentBgpVrfIpAddressFamily instanceof BgpVrfIpv6AddressFamilyConfiguration;
    BgpVrfIpv6AddressFamilyConfiguration afConfig =
        (BgpVrfIpv6AddressFamilyConfiguration) _currentBgpVrfIpAddressFamily;
    afConfig.addNetwork(prefix, mapname);
  }

  @Override
  public void exitRb_af6_redistribute(Rb_af6_redistributeContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError =
        toRoutingProtocolInstance(ctx, ctx.routing_instance_v6());
    Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
    if (!rpiOrError.isPresent() || !mapOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String map = mapOrError.get();
    Optional<CiscoNxosStructureType> type = rpi.getProtocol().getRouterStructureType();
    if (rpi.getTag() != null && type.isPresent()) {
      _c.referenceStructure(
          type.get(), rpi.getTag(), BGP_REDISTRIBUTE_INSTANCE, ctx.getStart().getLine());
    }
    _c.referenceStructure(ROUTE_MAP, map, BGP_REDISTRIBUTE_ROUTE_MAP, ctx.getStart().getLine());
    _currentBgpVrfIpAddressFamily.setRedistributionPolicy(rpi, map);
  }

  @Override
  public void exitRb_afip_aa_tail(Rb_afip_aa_tailContext ctx) {
    int line = ctx.getStart().getLine();
    if (ctx.ADVERTISE_MAP() != null) {
      todo(ctx);
      Optional<String> nameOrError = toString(ctx, ctx.mapname);
      if (!nameOrError.isPresent()) {
        return;
      }
      String name = nameOrError.get();
      _c.referenceStructure(ROUTE_MAP, name, BGP_ADVERTISE_MAP, line);
      _currentBgpVrfAddressFamilyAggregateNetwork.setAdvertiseMap(name);
    } else if (ctx.AS_SET() != null) {
      todo(ctx);
      _currentBgpVrfAddressFamilyAggregateNetwork.setAsSet(true);
    } else if (ctx.ATTRIBUTE_MAP() != null) {
      Optional<String> nameOrError = toString(ctx, ctx.mapname);
      if (!nameOrError.isPresent()) {
        return;
      }
      String name = nameOrError.get();
      _c.referenceStructure(ROUTE_MAP, name, BGP_ATTRIBUTE_MAP, line);
      _currentBgpVrfAddressFamilyAggregateNetwork.setAttributeMap(name);
    } else if (ctx.SUMMARY_ONLY() != null) {
      _currentBgpVrfAddressFamilyAggregateNetwork.setSummaryOnly(true);
    } else if (ctx.SUPPRESS_MAP() != null) {
      todo(ctx);
      Optional<String> nameOrError = toString(ctx, ctx.mapname);
      if (!nameOrError.isPresent()) {
        return;
      }
      String name = nameOrError.get();
      _c.referenceStructure(ROUTE_MAP, name, BGP_SUPPRESS_MAP, line);
      _currentBgpVrfAddressFamilyAggregateNetwork.setSuppressMap(name);
    }
  }

  @Override
  public void exitRb_afip_additional_paths(Rb_afip_additional_pathsContext ctx) {
    todo(ctx);
    if (ctx.mapname != null) {
      toString(ctx, ctx.mapname)
          .ifPresent(
              name ->
                  _c.referenceStructure(
                      ROUTE_MAP, name, BGP_ADDITIONAL_PATHS_ROUTE_MAP, ctx.getStart().getLine()));
    }
  }

  @Override
  public void exitRb_afip_client_to_client(Rb_afip_client_to_clientContext ctx) {
    _currentBgpVrfIpAddressFamily.setClientToClientReflection(true);
  }

  @Override
  public void exitRb_afip_dampening(Rb_afip_dampeningContext ctx) {
    todo(ctx);
    if (ctx.mapname != null) {
      toString(ctx, ctx.mapname)
          .ifPresent(
              name ->
                  _c.referenceStructure(
                      ROUTE_MAP, name, BGP_DAMPENING_ROUTE_MAP, ctx.getStart().getLine()));
    }
  }

  @Override
  public void exitRb_afip_default_metric(Rb_afip_default_metricContext ctx) {
    long metric = toLong(ctx.metric);
    _currentBgpVrfIpAddressFamily.setDefaultMetric(metric);
  }

  @Override
  public void exitRb_afip_default_information(Rb_afip_default_informationContext ctx) {
    _currentBgpVrfIpAddressFamily.setDefaultInformationOriginate(true);
  }

  @Override
  public void exitRb_afip_distance(Rb_afip_distanceContext ctx) {
    Optional<Integer> ebgp = toInteger(ctx, ctx.ebgp);
    Optional<Integer> ibgp = toInteger(ctx, ctx.ibgp);
    Optional<Integer> local = toInteger(ctx, ctx.local);
    if (!ebgp.isPresent() || !ibgp.isPresent() || !local.isPresent()) {
      return;
    }
    _currentBgpVrfIpAddressFamily.setDistanceEbgp(ebgp.get());
    _currentBgpVrfIpAddressFamily.setDistanceIbgp(ibgp.get());
    _currentBgpVrfIpAddressFamily.setDistanceLocal(local.get());
  }

  @Override
  public void exitRb_afip_inject_map(Rb_afip_inject_mapContext ctx) {
    Optional<String> injectMap = toString(ctx, ctx.injectmap);
    Optional<String> existMap = toString(ctx, ctx.existmap);
    if (!injectMap.isPresent() || !existMap.isPresent()) {
      return;
    }
    todo(ctx);
    _c.referenceStructure(ROUTE_MAP, injectMap.get(), BGP_INJECT_MAP, ctx.getStart().getLine());
    _c.referenceStructure(ROUTE_MAP, existMap.get(), BGP_EXIST_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitRb_afip_maximum_paths(Rb_afip_maximum_pathsContext ctx) {
    Optional<Integer> limitOrError = toInteger(ctx, ctx.numpaths);
    if (!limitOrError.isPresent()) {
      return;
    }
    int limit = limitOrError.get();
    if (ctx.IBGP() != null) {
      _currentBgpVrfIpAddressFamily.setMaximumPathsIbgp(limit);
    } else if (ctx.EIBGP() != null) {
      _currentBgpVrfIpAddressFamily.setMaximumPathsEbgp(limit);
      _currentBgpVrfIpAddressFamily.setMaximumPathsIbgp(limit);
    } else if (ctx.MIXED() != null) {
      if (limit != 1) {
        warn(ctx, "maximum-paths mixed is not supported");
      }
    } else {
      // EBGP
      _currentBgpVrfIpAddressFamily.setMaximumPathsEbgp(limit);
    }
  }

  @Override
  public void exitRb_afip_nexthop_route_map(Rb_afip_nexthop_route_mapContext ctx) {
    Optional<String> maybeRouteMapName = toString(ctx, ctx.mapname);
    if (!maybeRouteMapName.isPresent()) {
      // already warned
      return;
    }
    String routeMapName = maybeRouteMapName.get();
    _c.referenceStructure(ROUTE_MAP, routeMapName, BGP_NEXTHOP_ROUTE_MAP, ctx.getStart().getLine());
    _currentBgpVrfIpAddressFamily.setNexthopRouteMap(routeMapName);
  }

  @Override
  public void exitRb_afip_suppress_inactive(Rb_afip_suppress_inactiveContext ctx) {
    _currentBgpVrfIpAddressFamily.setSuppressInactive(true);
  }

  @Override
  public void exitRb_afip_table_map(Rb_afip_table_mapContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.mapname);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(ROUTE_MAP, name, BGP_TABLE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void enterRb_af_l2vpn(Rb_af_l2vpnContext ctx) {
    BgpVrfAddressFamilyConfiguration af =
        _currentBgpVrfConfiguration.getOrCreateAddressFamily(Type.L2VPN_EVPN);
    assert af instanceof BgpVrfL2VpnEvpnAddressFamilyConfiguration;
    _currentBgpVrfL2VpnEvpnAddressFamily = (BgpVrfL2VpnEvpnAddressFamilyConfiguration) af;
  }

  @Override
  public void exitRb_af_l2vpn(Rb_af_l2vpnContext ctx) {
    _currentBgpVrfL2VpnEvpnAddressFamily = null;
  }

  @Override
  public void exitRb_afl2v_maximum_paths(Rb_afl2v_maximum_pathsContext ctx) {
    Optional<Integer> limitOrError = toInteger(ctx, ctx.numpaths);
    if (!limitOrError.isPresent()) {
      return;
    }
    int limit = limitOrError.get();
    if (ctx.IBGP() != null) {
      _currentBgpVrfL2VpnEvpnAddressFamily.setMaximumPathsIbgp(limit);
    } else if (ctx.EIBGP() != null) {
      _currentBgpVrfL2VpnEvpnAddressFamily.setMaximumPathsEbgp(limit);
      _currentBgpVrfL2VpnEvpnAddressFamily.setMaximumPathsIbgp(limit);
    } else {
      _currentBgpVrfL2VpnEvpnAddressFamily.setMaximumPathsEbgp(limit);
    }
  }

  @Override
  public void exitRb_afl2v_retain(Rb_afl2v_retainContext ctx) {
    if (ctx.ROUTE_MAP() != null) {
      Optional<String> mapOrError = toString(ctx, ctx.map);
      if (!mapOrError.isPresent()) {
        return;
      }
      String map = mapOrError.get();
      _currentBgpVrfL2VpnEvpnAddressFamily.setRetainMode(RetainRouteType.ROUTE_MAP);
      _currentBgpVrfL2VpnEvpnAddressFamily.setRetainRouteMap(map);
      _c.referenceStructure(
          ROUTE_MAP,
          map,
          BGP_L2VPN_EVPN_RETAIN_ROUTE_TARGET_ROUTE_MAP,
          ctx.map.getStart().getLine());
    } else {
      assert ctx.ALL() != null;
      _currentBgpVrfL2VpnEvpnAddressFamily.setRetainMode(RetainRouteType.ALL);
    }
  }

  @Override
  public void exitRb_bestpath(Rb_bestpathContext ctx) {
    if (ctx.ALWAYS_COMPARE_MED() != null) {
      _currentBgpVrfConfiguration.setBestpathAlwaysCompareMed(true);
    } else if (ctx.AS_PATH() != null && ctx.MULTIPATH_RELAX() != null) {
      _currentBgpVrfConfiguration.setBestpathAsPathMultipathRelax(true);
    } else if (ctx.COMPARE_ROUTERID() != null) {
      _currentBgpVrfConfiguration.setBestpathCompareRouterId(true);
    } else if (ctx.COST_COMMUNITY() != null && ctx.IGNORE() != null) {
      _currentBgpVrfConfiguration.setBestpathCostCommunityIgnore(true);
    } else if (ctx.MED() != null && ctx.CONFED() != null) {
      _currentBgpVrfConfiguration.setBestpathMedConfed(true);
    } else if (ctx.MED() != null && ctx.MISSING_AS_WORST() != null) {
      _currentBgpVrfConfiguration.setBestpathMedMissingAsWorst(true);
    } else if (ctx.MED() != null && ctx.NON_DETERMINISTIC() != null) {
      _currentBgpVrfConfiguration.setBestpathMedNonDeterministic(true);
    } else {
      warn(ctx, "Unsupported BGP bestpath configuration");
    }
  }

  @Override
  public void exitRb_cluster_id(Rb_cluster_idContext ctx) {
    if (ctx.ip != null) {
      _currentBgpVrfConfiguration.setClusterId(toIp(ctx.ip));
    } else {
      _currentBgpVrfConfiguration.setClusterId(Ip.create(toLong(ctx.ip_as_int)));
    }
  }

  @Override
  public void exitRb_confederation_identifier(Rb_confederation_identifierContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitRb_confederation_peers(Rb_confederation_peersContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitRb_enforce_first_as(Rb_enforce_first_asContext ctx) {
    _c.getBgpGlobalConfiguration().setEnforceFirstAs(true);
  }

  @Override
  public void exitRb_log_neighbor_changes(Rb_log_neighbor_changesContext ctx) {
    _currentBgpVrfConfiguration.setLogNeighborChanges(true);
  }

  @Override
  public void exitRb_maxas_limit(Rb_maxas_limitContext ctx) {
    toInteger(ctx, ctx.limit).ifPresent(_currentBgpVrfConfiguration::setMaxasLimit);
  }

  @Override
  public void enterRb_neighbor(Rb_neighborContext ctx) {
    String neighborName;
    if (ctx.ip != null) {
      Ip ip = toIp(ctx.ip);
      _currentBgpVrfNeighbor = _currentBgpVrfConfiguration.getOrCreateNeighbor(ip);
      neighborName = ip.toString();
    } else if (ctx.prefix != null) {
      Prefix prefix = toPrefix(ctx.prefix);
      _currentBgpVrfNeighbor = _currentBgpVrfConfiguration.getOrCreatePassiveNeighbor(prefix);
      neighborName = prefix.toString();
    } else if (ctx.ip6 != null) {
      Ip6 ip = toIp6(ctx.ip6);
      _currentBgpVrfNeighbor = _currentBgpVrfConfiguration.getOrCreateNeighbor(ip);
      neighborName = ip.toString();
    } else if (ctx.prefix6 != null) {
      Prefix6 prefix = toPrefix6(ctx.prefix6);
      _currentBgpVrfNeighbor = _currentBgpVrfConfiguration.getOrCreatePassiveNeighbor(prefix);
      neighborName = prefix.toString();
    } else {
      throw new BatfishException(
          "BGP neighbor IP definition not supported in line " + ctx.getText());
    }

    String neighborStructName = bgpNeighborStructureName(neighborName, _currentBgpVrfName);
    _c.defineStructure(BGP_NEIGHBOR, neighborStructName, ctx);
    _c.referenceStructure(
        BGP_NEIGHBOR, neighborStructName, BGP_NEIGHBOR_SELF_REF, ctx.start.getLine());

    if (ctx.REMOTE_AS() != null && ctx.bgp_asn() != null) {
      long asn = toLong(ctx.bgp_asn());
      _currentBgpVrfNeighbor.setRemoteAs(asn);
      _currentBgpVrfNeighbor.setRemoteAsRouteMap(null);
    }

    if (ctx.REMOTE_AS() != null && ctx.ROUTE_MAP() != null) {
      Optional<String> nameOrError = toString(ctx, ctx.mapname);
      if (!nameOrError.isPresent()) {
        return;
      }
      String name = nameOrError.get();
      _currentBgpVrfNeighbor.setRemoteAs(null);
      _currentBgpVrfNeighbor.setRemoteAsRouteMap(name);
      _c.referenceStructure(
          ROUTE_MAP, name, BGP_NEIGHBOR_REMOTE_AS_ROUTE_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitRb_neighbor(Rb_neighborContext ctx) {
    _currentBgpVrfNeighbor = null;
  }

  @Override
  public void exitRb_no_neighbor(Rb_no_neighborContext ctx) {
    // Note that although ctx may specify remote-as, it doesn't affect the outcome, even if the
    // specified remote AS does not match the removed neighbor's remote AS.
    if (ctx.ip != null) {
      Ip ip = toIp(ctx.ip);
      if (!_currentBgpVrfConfiguration.removeNeighbor(ip)) {
        warn(ctx, String.format("Neighbor %s does not exist", ip));
      }
    } else if (ctx.prefix != null) {
      Prefix prefix = toPrefix(ctx.prefix);
      if (!_currentBgpVrfConfiguration.removePassiveNeighbor(prefix)) {
        warn(ctx, String.format("Neighbor %s does not exist", prefix));
      }
    } else if (ctx.ip6 != null) {
      Ip6 ip = toIp6(ctx.ip6);
      if (!_currentBgpVrfConfiguration.removeNeighbor(ip)) {
        warn(ctx, String.format("Neighbor %s does not exist", ip));
      }
    } else {
      assert ctx.prefix6 != null;
      Prefix6 prefix = toPrefix6(ctx.prefix6);
      if (!_currentBgpVrfConfiguration.removePassiveNeighbor(prefix)) {
        warn(ctx, String.format("Neighbor %s does not exist", prefix));
      }
    }
  }

  @Override
  public void enterRb_n_address_family(Rb_n_address_familyContext ctx) {
    String familyStr = ctx.first.getText() + '-' + ctx.second.getText();
    _currentBgpVrfNeighborAddressFamily =
        _currentBgpVrfNeighbor.getOrCreateAddressFamily(familyStr);
  }

  @Override
  public void exitRb_n_address_family(Rb_n_address_familyContext ctx) {
    _currentBgpVrfNeighborAddressFamily = null;
  }

  @Override
  public void exitRb_n_af_advertise_map(Rb_n_af_advertise_mapContext ctx) {
    Optional<String> advMap = toString(ctx, ctx.mapname);
    Optional<String> existMap =
        ctx.EXIST_MAP() != null ? toString(ctx, ctx.existmap) : Optional.empty();
    Optional<String> nonExistMap =
        ctx.NON_EXIST_MAP() != null ? toString(ctx, ctx.nonexistmap) : Optional.empty();
    if (!advMap.isPresent()
        || ctx.EXIST_MAP() != null && !existMap.isPresent()
        || ctx.NON_EXIST_MAP() != null && !nonExistMap.isPresent()) {
      return;
    }
    todo(ctx);
    _c.referenceStructure(
        ROUTE_MAP, advMap.get(), BGP_NEIGHBOR_ADVERTISE_MAP, ctx.getStart().getLine());
    existMap.ifPresent(
        name ->
            _c.referenceStructure(
                ROUTE_MAP, name, BGP_NEIGHBOR_EXIST_MAP, ctx.getStart().getLine()));
    nonExistMap.ifPresent(
        name ->
            _c.referenceStructure(
                ROUTE_MAP, name, BGP_NEIGHBOR_NON_EXIST_MAP, ctx.getStart().getLine()));
  }

  @Override
  public void exitRb_n_af_allowas_in(Rb_n_af_allowas_inContext ctx) {
    /*
    Default value for allowas-in, if unspecified, is 3
    https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus9000/sw/7-x/command_references/configuration_commands/b_Using_N9K_Config_Commands/b_N9K_Bookmap_chapter_00.html#wp2015222148
    */
    Optional<Integer> value = ctx.num == null ? Optional.of(3) : toInteger(ctx, ctx.num);
    value.ifPresent(_currentBgpVrfNeighborAddressFamily::setAllowAsIn);
  }

  @Override
  public void exitRb_n_af_as_override(Rb_n_af_as_overrideContext ctx) {
    _currentBgpVrfNeighborAddressFamily.setAsOverride(true);
  }

  @Override
  public void exitRb_n_af_default_originate(Rb_n_af_default_originateContext ctx) {
    if (ctx.ROUTE_MAP() != null) {
      Optional<String> nameOrError = toString(ctx, ctx.mapname);
      if (!nameOrError.isPresent()) {
        return;
      }
      String name = nameOrError.get();
      _currentBgpVrfNeighborAddressFamily.setDefaultOriginateMap(name);
      _c.referenceStructure(
          ROUTE_MAP, name, BGP_DEFAULT_ORIGINATE_ROUTE_MAP, ctx.getStart().getLine());
    }
    _currentBgpVrfNeighborAddressFamily.setDefaultOriginate(true);
  }

  @Override
  public void exitRb_n_af_disable_peer_as_check(Rb_n_af_disable_peer_as_checkContext ctx) {
    _currentBgpVrfNeighborAddressFamily.setDisablePeerAsCheck(true);
  }

  @Override
  public void exitRb_n_af_filter_list(Rb_n_af_filter_listContext ctx) {
    Optional<String> filterList = toString(ctx, ctx.name);
    if (!filterList.isPresent()) {
      return;
    }
    todo(ctx);
    CiscoNxosStructureUsage usage =
        _inIpv6BgpPeer
            ? ((ctx.IN() != null) ? BGP_NEIGHBOR6_FILTER_LIST_IN : BGP_NEIGHBOR6_FILTER_LIST_OUT)
            : ((ctx.IN() != null) ? BGP_NEIGHBOR_FILTER_LIST_IN : BGP_NEIGHBOR_FILTER_LIST_OUT);
    _c.referenceStructure(
        IP_AS_PATH_ACCESS_LIST, filterList.get(), usage, ctx.getStart().getLine());
  }

  @Override
  public void exitRb_n_af_inherit(Rb_n_af_inheritContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.template);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    Optional<Integer> seqOrError = toInteger(ctx, ctx.seq);
    if (!seqOrError.isPresent()) {
      return;
    }
    int sequence = seqOrError.get();
    _currentBgpVrfNeighborAddressFamily.setInheritPeerPolicy(sequence, name);
    _c.referenceStructure(
        BGP_TEMPLATE_PEER_POLICY, name, BGP_NEIGHBOR_INHERIT_PEER_POLICY, ctx.getStart().getLine());
  }

  @Override
  public void exitRb_n_af_next_hop_self(Rb_n_af_next_hop_selfContext ctx) {
    _currentBgpVrfNeighborAddressFamily.setNextHopSelf(true);
  }

  @Override
  public void exitRb_n_af_next_hop_third_party(Rb_n_af_next_hop_third_partyContext ctx) {
    _currentBgpVrfNeighborAddressFamily.setNextHopThirdParty(true);
  }

  @Override
  public void exitRb_n_af_no_default_originate(Rb_n_af_no_default_originateContext ctx) {
    _currentBgpVrfNeighborAddressFamily.setDefaultOriginate(false);
  }

  @Override
  public void exitRb_n_af_prefix_list(Rb_n_af_prefix_listContext ctx) {
    Optional<String> prefixList = toString(ctx, ctx.listname);
    if (!prefixList.isPresent()) {
      return;
    }
    String prefixListName = prefixList.get();

    CiscoNxosStructureType type = _inIpv6BgpPeer ? IPV6_PREFIX_LIST : IP_PREFIX_LIST;
    CiscoNxosStructureUsage usage =
        _inIpv6BgpPeer
            ? ((ctx.IN() != null) ? BGP_NEIGHBOR6_PREFIX_LIST_IN : BGP_NEIGHBOR6_PREFIX_LIST_OUT)
            : ((ctx.IN() != null) ? BGP_NEIGHBOR_PREFIX_LIST_IN : BGP_NEIGHBOR_PREFIX_LIST_OUT);
    if (ctx.IN() != null) {
      _currentBgpVrfNeighborAddressFamily.setInboundPrefixList(prefixListName);
    } else {
      assert ctx.OUT() != null;
      _currentBgpVrfNeighborAddressFamily.setOutboundPrefixList(prefixListName);
    }
    _c.referenceStructure(type, prefixListName, usage, ctx.getStart().getLine());
  }

  @Override
  public void exitRb_n_af_route_map(Rb_n_af_route_mapContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.mapname);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    CiscoNxosStructureUsage usage;
    if (ctx.IN() != null) {
      usage = BGP_NEIGHBOR_ROUTE_MAP_IN;
      _currentBgpVrfNeighborAddressFamily.setInboundRouteMap(name);
    } else {
      usage = BGP_NEIGHBOR_ROUTE_MAP_OUT;
      _currentBgpVrfNeighborAddressFamily.setOutboundRouteMap(name);
    }
    _c.referenceStructure(ROUTE_MAP, name, usage, ctx.getStart().getLine());
  }

  @Override
  public void exitRb_n_af_route_reflector_client(Rb_n_af_route_reflector_clientContext ctx) {
    _currentBgpVrfNeighborAddressFamily.setRouteReflectorClient(true);
  }

  @Override
  public void exitRb_n_af_send_community(Rb_n_af_send_communityContext ctx) {
    if (ctx.BOTH() != null || ctx.EXTENDED() != null) {
      _currentBgpVrfNeighborAddressFamily.setSendCommunityExtended(true);
    }
    if (ctx.BOTH() != null || ctx.STANDARD() != null || ctx.EXTENDED() == null) {
      _currentBgpVrfNeighborAddressFamily.setSendCommunityStandard(true);
    }
  }

  @Override
  public void exitRb_n_af_suppress_inactive(Rb_n_af_suppress_inactiveContext ctx) {
    _currentBgpVrfNeighborAddressFamily.setSuppressInactive(true);
  }

  @Override
  public void exitRb_n_af_unsuppress_map(Rb_n_af_unsuppress_mapContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.mapname);
    if (!nameOrError.isPresent()) {
      return;
    }
    todo(ctx);
    String name = nameOrError.get();
    _c.referenceStructure(ROUTE_MAP, name, BGP_UNSUPPRESS_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitRb_n_description(Rb_n_descriptionContext ctx) {
    Optional<String> desc =
        toStringWithLengthInSpace(
            ctx, ctx.desc, BGP_NEIGHBOR_DESCRIPTION_LENGTH_RANGE, "bgp neighbor description");
    desc.ifPresent(_currentBgpVrfNeighbor::setDescription);
  }

  @Override
  public void exitRb_n_ebgp_multihop(Rb_n_ebgp_multihopContext ctx) {
    Optional<Integer> ttlOrError = toInteger(ctx, ctx.ebgp_ttl);
    if (!ttlOrError.isPresent()) {
      return;
    }
    _currentBgpVrfNeighbor.setEbgpMultihopTtl(ttlOrError.get());
  }

  @Override
  public void exitRb_n_inherit(Rb_n_inheritContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.peer);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    if (ctx.PEER() != null) {
      _currentBgpVrfNeighbor.setInheritPeer(name);
      _c.referenceStructure(
          BGP_TEMPLATE_PEER, name, BGP_NEIGHBOR_INHERIT_PEER, ctx.getStart().getLine());
    } else {
      _currentBgpVrfNeighbor.setInheritPeerSession(name);
      _c.referenceStructure(
          BGP_TEMPLATE_PEER_SESSION,
          name,
          BGP_NEIGHBOR_INHERIT_PEER_SESSION,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void exitRb_n_local_as(Rb_n_local_asContext ctx) {
    long asn = toLong(ctx.bgp_asn());
    _currentBgpVrfNeighbor.setLocalAs(asn);
  }

  @Override
  public void exitRb_n_no_shutdown(Rb_n_no_shutdownContext ctx) {
    _currentBgpVrfNeighbor.setShutdown(false);
  }

  @Override
  public void exitRb_n_remote_as(Rb_n_remote_asContext ctx) {
    long asn = toLong(ctx.bgp_asn());
    _currentBgpVrfNeighbor.setRemoteAs(asn);
  }

  @Override
  public void exitRb_n_remove_private_as(Rb_n_remove_private_asContext ctx) {
    if (ctx.ALL() != null) {
      _currentBgpVrfNeighbor.setRemovePrivateAs(RemovePrivateAsMode.ALL);
    } else if (ctx.REPLACE_AS() != null) {
      _currentBgpVrfNeighbor.setRemovePrivateAs(RemovePrivateAsMode.REPLACE_AS);
    }
  }

  @Override
  public void exitRb_n_shutdown(Rb_n_shutdownContext ctx) {
    _currentBgpVrfNeighbor.setShutdown(true);
  }

  @Override
  public void exitRb_n_update_source(Rb_n_update_sourceContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.interface_name());
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _currentBgpVrfNeighbor.setUpdateSource(name);
    _c.referenceStructure(INTERFACE, name, BGP_NEIGHBOR_UPDATE_SOURCE, ctx.getStart().getLine());
  }

  @Override
  public void exitRb_no_enforce_first_as(Rb_no_enforce_first_asContext ctx) {
    _c.getBgpGlobalConfiguration().setEnforceFirstAs(false);
  }

  @Override
  public void exitRb_router_id(Rb_router_idContext ctx) {
    Ip ip = toIp(ctx.ip_address());
    _currentBgpVrfConfiguration.setRouterId(ip);
  }

  @Override
  public void enterRb_template_peer(Rb_template_peerContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.peer);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _currentBgpVrfNeighbor = _c.getBgpGlobalConfiguration().getOrCreateTemplatePeer(name);
    _c.defineStructure(BGP_TEMPLATE_PEER, name, ctx);
  }

  @Override
  public void exitRb_template_peer(Rb_template_peerContext ctx) {
    _currentBgpVrfNeighbor = null;
  }

  @Override
  public void enterRb_template_peer_policy(Rb_template_peer_policyContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.policy);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _currentBgpVrfNeighborAddressFamily =
        _c.getBgpGlobalConfiguration().getOrCreateTemplatePeerPolicy(name);
    _c.defineStructure(BGP_TEMPLATE_PEER_POLICY, name, ctx);
  }

  @Override
  public void exitRb_template_peer_policy(Rb_template_peer_policyContext ctx) {
    _currentBgpVrfNeighborAddressFamily = null;
  }

  @Override
  public void enterRb_template_peer_session(Rb_template_peer_sessionContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.session);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _currentBgpVrfNeighbor = _c.getBgpGlobalConfiguration().getOrCreateTemplatePeerSession(name);
    _c.defineStructure(BGP_TEMPLATE_PEER_SESSION, name, ctx);
  }

  @Override
  public void exitRb_template_peer_session(Rb_template_peer_sessionContext ctx) {
    _currentBgpVrfNeighbor = null;
  }

  @Override
  public void enterRb_vrf(Rb_vrfContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      // Dummy BGP config so inner stuff works.
      _currentBgpVrfConfiguration = new BgpVrfConfiguration();
      _currentBgpVrfName = String.format("(vrf with invalid name: %s)", ctx.name.getText());
      return;
    }
    String name = nameOrErr.get();
    _currentBgpVrfName = name;
    _currentBgpVrfConfiguration = _c.getBgpGlobalConfiguration().getOrCreateVrf(name);
  }

  @Override
  public void exitRb_vrf(Rb_vrfContext ctx) {
    _currentBgpVrfConfiguration = _c.getBgpGlobalConfiguration().getOrCreateVrf(DEFAULT_VRF_NAME);
    _currentBgpVrfName = DEFAULT_VRF_NAME;
  }

  @Override
  public void exitRb_v_local_as(Rb_v_local_asContext ctx) {
    long asNum = toLong(ctx.bgp_asn());
    _currentBgpVrfConfiguration.setLocalAs(asNum);
  }

  @Override
  public void exitRe_isolate(Re_isolateContext ctx) {
    _currentEigrpProcess.setIsolate(true);
  }

  @Override
  public void exitRe_no_isolate(Re_no_isolateContext ctx) {
    _currentEigrpProcess.setIsolate(false);
  }

  @Override
  public void enterRe_vrf(Re_vrfContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (nameOrError.isPresent()) {
      _currentEigrpVrf = _currentEigrpProcess.getOrCreateVrf(nameOrError.get());
    } else {
      // Dummy so parsing doesn't crash.
      _currentEigrpVrf = new EigrpVrfConfiguration();
    }
    _currentEigrpVrfIpAf = _currentEigrpVrf.getVrfIpv4AddressFamily();
  }

  @Override
  public void exitRe_vrf(Re_vrfContext ctx) {
    _currentEigrpVrf = _currentEigrpProcess.getOrCreateVrf(DEFAULT_VRF_NAME);
    _currentEigrpVrfIpAf = _currentEigrpVrf.getVrfIpv4AddressFamily();
  }

  @Override
  public void exitRec_autonomous_system(Rec_autonomous_systemContext ctx) {
    Optional<Integer> asn = toInteger(ctx, ctx.eigrp_asn());
    asn.ifPresent(_currentEigrpVrf::setAsn);
  }

  @Override
  public void exitRec_distance(Rec_distanceContext ctx) {
    _currentEigrpVrf.setDistanceInternal(toInteger(ctx.internal));
    _currentEigrpVrf.setDistanceExternal(toInteger(ctx.external));
  }

  @Override
  public void exitRec_no_router_id(Rec_no_router_idContext ctx) {
    _currentEigrpVrf.setRouterId(null);
  }

  @Override
  public void exitRec_router_id(Rec_router_idContext ctx) {
    Ip routerId = toIp(ctx.id);
    _currentEigrpVrf.setRouterId(routerId);
  }

  @Override
  public void enterRecaf_ipv4(Recaf_ipv4Context ctx) {
    _currentEigrpVrfIpAf = _currentEigrpVrf.getOrCreateV4AddressFamily();
  }

  @Override
  public void exitRecaf_ipv4(Recaf_ipv4Context ctx) {
    _currentEigrpVrfIpAf = _currentEigrpVrf.getVrfIpv4AddressFamily();
  }

  @Override
  public void enterRecaf_ipv6(Recaf_ipv6Context ctx) {
    _currentEigrpVrfIpAf = _currentEigrpVrf.getOrCreateV6AddressFamily();
  }

  @Override
  public void exitRecaf_ipv6(Recaf_ipv6Context ctx) {
    _currentEigrpVrfIpAf = _currentEigrpVrf.getVrfIpv4AddressFamily();
  }

  @Override
  public void exitRecaf_default_metric(Recaf_default_metricContext ctx) {
    EigrpMetric defaultMetric =
        new EigrpMetric(
            toLong(ctx.bandwidth),
            toLong(ctx.delay),
            toInteger(ctx.reliability),
            toInteger(ctx.load),
            toLong(ctx.mtu));
    _currentEigrpVrfIpAf.setDefaultMetric(defaultMetric);
  }

  @Override
  public void exitRecaf_network(CiscoNxosParser.Recaf_networkContext ctx) {
    _currentEigrpVrfIpAf.addNetwork(toPrefix(ctx.network));
  }

  @Override
  public void exitRecaf4_redistribute(Recaf4_redistributeContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError =
        toRoutingProtocolInstance(ctx, ctx.routing_instance_v4());
    Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
    if (!rpiOrError.isPresent() || !mapOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String map = mapOrError.get();
    Optional<CiscoNxosStructureType> type = rpi.getProtocol().getRouterStructureType();
    if (rpi.getTag() != null && type.isPresent()) {
      _c.referenceStructure(
          type.get(), rpi.getTag(), EIGRP_REDISTRIBUTE_INSTANCE, ctx.getStart().getLine());
    }
    _c.referenceStructure(ROUTE_MAP, map, EIGRP_REDISTRIBUTE_ROUTE_MAP, ctx.getStart().getLine());
    _currentEigrpVrfIpAf.setRedistributionPolicy(rpi, map);
  }

  @Override
  public void exitRecaf6_redistribute(Recaf6_redistributeContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError =
        toRoutingProtocolInstance(ctx, ctx.routing_instance_v6());
    Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
    if (!rpiOrError.isPresent() || !mapOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String map = mapOrError.get();
    Optional<CiscoNxosStructureType> type = rpi.getProtocol().getRouterStructureType();
    if (rpi.getTag() != null && type.isPresent()) {
      _c.referenceStructure(
          type.get(), rpi.getTag(), EIGRP_REDISTRIBUTE_INSTANCE, ctx.getStart().getLine());
    }
    _c.referenceStructure(ROUTE_MAP, map, EIGRP_REDISTRIBUTE_ROUTE_MAP, ctx.getStart().getLine());
    _currentEigrpVrfIpAf.setRedistributionPolicy(rpi, map);
  }

  @Override
  public void exitRo3_af6_a_filter_list(Ro3_af6_a_filter_listContext ctx) {
    Optional<String> map = toString(ctx, ctx.name);
    if (!map.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTE_MAP, map.get(), OSPFV3_AREA_FILTER_LIST_ROUTE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitRo3_af6_default_information(Ro3_af6_default_informationContext ctx) {
    if (ctx.rm == null) {
      return;
    }
    Optional<String> map = toString(ctx, ctx.rm);
    if (!map.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTE_MAP,
        map.get(),
        OSPFV3_DEFAULT_INFORMATION_ORIGINATE_ROUTE_MAP,
        ctx.getStart().getLine());
  }

  @Override
  public void exitRo3_af6_rd_routing_instance(Ro3_af6_rd_routing_instanceContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError =
        toRoutingProtocolInstance(ctx, ctx.routing_instance_v6());
    Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
    if (!rpiOrError.isPresent() || !mapOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String map = mapOrError.get();
    Optional<CiscoNxosStructureType> type = rpi.getProtocol().getRouterStructureType();
    if (rpi.getTag() != null && type.isPresent()) {
      _c.referenceStructure(
          type.get(), rpi.getTag(), OSPFV3_REDISTRIBUTE_INSTANCE, ctx.getStart().getLine());
    }
    _c.referenceStructure(ROUTE_MAP, map, OSPFV3_REDISTRIBUTE_ROUTE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitRo3_af6_table_map(Ro3_af6_table_mapContext ctx) {
    Optional<String> map = toString(ctx, ctx.map);
    if (!map.isPresent()) {
      return;
    }
    _c.referenceStructure(ROUTE_MAP, map.get(), OSPFV3_TABLE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitRo3a_nssa_o_default_information_originate(
      Ro3a_nssa_o_default_information_originateContext ctx) {
    if (ctx.map == null) {
      return;
    }
    Optional<String> map = toString(ctx, ctx.map);
    if (!map.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTE_MAP,
        map.get(),
        OSPFV3_NSSA_DEFAULT_INFORMATION_ORIGINATE_ROUTE_MAP,
        ctx.getStart().getLine());
  }

  @Override
  public void enterRo3_vrf(Ro3_vrfContext ctx) {
    Optional<String> vrf = toString(ctx, ctx.name);
    if (!vrf.isPresent()) {
      return;
    }
    _c.referenceStructure(VRF, vrf.get(), OSPFV3_VRF, ctx.getStart().getLine());
  }

  @Override
  public void enterRouter_bgp(Router_bgpContext ctx) {
    _currentBgpVrfConfiguration = _c.getBgpGlobalConfiguration().getOrCreateVrf(DEFAULT_VRF_NAME);
    _currentBgpVrfName = DEFAULT_VRF_NAME;
    _c.getBgpGlobalConfiguration().setLocalAs(toLong(ctx.bgp_asn()));
  }

  @Override
  public void exitRouter_bgp(Router_bgpContext ctx) {
    _currentBgpVrfConfiguration = null;
    _currentBgpVrfName = null;
  }

  @Override
  public void exitRrv_af4_default_information(Rrv_af4_default_informationContext ctx) {
    if (ctx.name != null) {
      Optional<String> map = toString(ctx, ctx.name);
      if (!map.isPresent()) {
        return;
      }
      _c.referenceStructure(
          ROUTE_MAP, map.get(), RIP_AF4_DEFAULT_INFORMATION_ROUTE_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitRrv_af4_redistribute(Rrv_af4_redistributeContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError = toRoutingProtocolInstance(ctx, ctx.rpi);
    Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
    if (!rpiOrError.isPresent() || !mapOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String map = mapOrError.get();
    Optional<CiscoNxosStructureType> type = rpi.getProtocol().getRouterStructureType();
    if (rpi.getTag() != null && type.isPresent()) {
      _c.referenceStructure(
          type.get(), rpi.getTag(), RIP_AF4_REDISTRIBUTE_INSTANCE, ctx.getStart().getLine());
    }
    _c.referenceStructure(ROUTE_MAP, map, RIP_AF4_REDISTRIBUTE_ROUTE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void exitRrv_af6_default_information(Rrv_af6_default_informationContext ctx) {
    if (ctx.name != null) {
      Optional<String> map = toString(ctx, ctx.name);
      if (!map.isPresent()) {
        return;
      }
      _c.referenceStructure(
          ROUTE_MAP, map.get(), RIP_AF6_DEFAULT_INFORMATION_ROUTE_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitRrv_af6_redistribute(Rrv_af6_redistributeContext ctx) {
    Optional<RoutingProtocolInstance> rpiOrError = toRoutingProtocolInstance(ctx, ctx.rpi);
    Optional<String> mapOrError = toString(ctx, ctx.route_map_name());
    if (!rpiOrError.isPresent() || !mapOrError.isPresent()) {
      return;
    }
    RoutingProtocolInstance rpi = rpiOrError.get();
    String map = mapOrError.get();
    Optional<CiscoNxosStructureType> type = rpi.getProtocol().getRouterStructureType();
    if (rpi.getTag() != null && type.isPresent()) {
      _c.referenceStructure(
          type.get(), rpi.getTag(), RIP_AF6_REDISTRIBUTE_INSTANCE, ctx.getStart().getLine());
    }
    _c.referenceStructure(ROUTE_MAP, map, RIP_AF6_REDISTRIBUTE_ROUTE_MAP, ctx.getStart().getLine());
  }

  @Override
  public void enterS_evpn(S_evpnContext ctx) {
    // TODO: check feature presence
    if (_c.getEvpn() == null) {
      _c.setEvpn(new Evpn());
    }
  }

    @Override
    public void enterS_interface_nve(S_interface_nveContext ctx) {
        int line = ctx.getStart().getLine();
        int first = toInteger(ctx.nverange.iname.first);
        int last = ctx.nverange.last != null ? toInteger(ctx.nverange.last) : first;
        // flip first and last if range is backwards
        if (last < first) {
            int tmp = last;
            last = first;
            first = tmp;
        }

        _currentNves =
                IntStream.range(first, last + 1)
                        .mapToObj(
                                i -> {
                                    String nveName = "nve" + i;
                                    _c.defineStructure(NVE, nveName, ctx);
                                    _c.referenceStructure(NVE, nveName, NVE_SELF_REFERENCE, line);
                                    return _c.getNves().computeIfAbsent(i, n -> new Nve(n));
                                })
                        .collect(ImmutableList.toImmutableList());
    }


    @Override
  public void exitS_interface_nve(S_interface_nveContext ctx) {
    _currentNves = null;
  }

  @Override
  public void enterS_interface_regular(S_interface_regularContext ctx) {
    Optional<List<String>> namesOrError = toStrings(ctx, ctx.irange);
    if (!namesOrError.isPresent()) {
      _currentInterfaces = ImmutableList.of();
      return;
    }

    CiscoNxosInterfaceType type = toType(ctx.irange.iname.prefix);
    assert type != null; // should be checked in toString above

    String prefix = ctx.irange.iname.prefix.getText();
    String canonicalPrefix = getCanonicalInterfaceNamePrefix(prefix);
    assert canonicalPrefix != null; // should be checked in toString above

    String middle = ctx.irange.iname.middle != null ? ctx.irange.iname.middle.getText() : "";
    String parentInterface =
        ctx.irange.iname.parent_suffix == null
            ? null
            : String.format(
                "%s%s%s", canonicalPrefix, middle, ctx.irange.iname.parent_suffix.num.getText());

    int line = ctx.getStart().getLine();

    List<String> names = namesOrError.get();
    if (type == CiscoNxosInterfaceType.VLAN) {
      String vlanPrefix = getCanonicalInterfaceNamePrefix("Vlan");
      assert vlanPrefix != null;
      assert names.stream().allMatch(name -> name.startsWith(vlanPrefix));
      _currentInterfaces =
          names.stream()
              .map(
                  name -> {
                    String vlanId = name.substring(vlanPrefix.length());
                    int vlan = Integer.parseInt(vlanId);
                    _c.referenceStructure(VLAN, vlanId, INTERFACE_VLAN, line);
                    return _c.getInterfaces().computeIfAbsent(name, n -> newVlanInterface(n, vlan));
                  })
              .collect(ImmutableList.toImmutableList());
    } else {
      _currentInterfaces =
          names.stream()
              .map(
                  name ->
                      _c.getInterfaces()
                          .computeIfAbsent(
                              name, n -> newNonVlanInterface(name, parentInterface, type)))
              .collect(ImmutableList.toImmutableList());
    }

    // Update interface definition and self-references
    _currentInterfaces.forEach(
        i -> {
          _c.defineStructure(INTERFACE, i.getName(), ctx);
          _c.referenceStructure(INTERFACE, i.getName(), INTERFACE_SELF_REFERENCE, line);
        });

    // If applicable, reference port channel names. Only for top-level port channel, not child.
    if (type == CiscoNxosInterfaceType.PORT_CHANNEL && parentInterface == null) {
      _currentInterfaces.forEach(i -> _c.defineStructure(PORT_CHANNEL, i.getName(), ctx));
    }

    // Track declared names.
    String declaredName = getFullText(ctx.irange);
    _currentInterfaces.forEach(i -> i.getDeclaredNames().add(declaredName));
  }

  @Override
  public void exitS_interface_regular(S_interface_regularContext ctx) {
    _currentInterfaces = null;
  }

  @Override
  public void enterS_route_map(S_route_mapContext ctx) {
    _currentRouteMapName = toString(ctx, ctx.name);
  }

  @Override
  public void exitS_route_map(S_route_mapContext ctx) {
    _currentRouteMapName = null;
  }

  @Override
  public void enterRoute_map_entry(Route_map_entryContext ctx) {
    if (!_currentRouteMapName.isPresent()) {
      _currentRouteMapEntry = new RouteMapEntry(1); // dummy
      return;
    }
    String name = _currentRouteMapName.get();
    int sequence;
    if (ctx.sequence != null) {
      Optional<Integer> seqOpt = toInteger(ctx, ctx.sequence);
      if (!seqOpt.isPresent()) {
        return;
      }
      sequence = seqOpt.get();
    } else {
      sequence = 10;
    }
    _currentRouteMapEntry =
        _c.getRouteMaps()
            .computeIfAbsent(name, RouteMap::new)
            .getEntries()
            .computeIfAbsent(sequence, RouteMapEntry::new);
    _currentRouteMapEntry.setAction(toLineAction(ctx.action));

    _c.defineStructure(ROUTE_MAP, name, ctx.getParent());
    String entryName = computeRouteMapEntryName(name, sequence);
    _c.defineStructure(ROUTE_MAP_ENTRY, entryName, ctx.getParent());
    _c.referenceStructure(
        ROUTE_MAP_ENTRY, entryName, ROUTE_MAP_ENTRY_PREV_REF, ctx.getStart().getLine());
  }

  @Override
  public void exitRoute_map_entry(Route_map_entryContext ctx) {
    _currentRouteMapEntry = null;
  }

  @Override
  public void exitRoute_map_pbr_statistics(Route_map_pbr_statisticsContext ctx) {
    if (!_currentRouteMapName.isPresent()) {
      return;
    }
    _c.getRouteMaps()
        .computeIfAbsent(_currentRouteMapName.get(), RouteMap::new)
        .setPbrStatistics(true);
    _c.defineStructure(ROUTE_MAP, _currentRouteMapName.get(), ctx.getParent());
  }

  @Override
  public void exitS_track(S_trackContext ctx) {
    if (_currentTrack == null) {
      // inner track rule was invalid, so no track was created
      return;
    }
    toInteger(ctx, ctx.num)
        .ifPresent(
            id -> {
              _c.defineStructure(CiscoNxosStructureType.TRACK, id.toString(), ctx);
              _c.getTracks().put(id, _currentTrack);
            });
    _currentTrack = null;
  }

  @Override
  public void exitTrack_ip_sla(Track_ip_slaContext ctx) {
    warn(
        ctx.getParent().getParent(), "This track method is not yet supported and will be ignored.");
    _currentTrack = TrackUnsupported.INSTANCE;
  }

  @Override
  public void exitTrack_interface(Track_interfaceContext ctx) {
    Optional<String> maybeIfaceName =
        toString(
            ctx,
            ctx.interface_name(),
            // Can only track a subset of interface types
            ImmutableSet.of(
                CiscoNxosInterfaceType.ETHERNET,
                CiscoNxosInterfaceType.PORT_CHANNEL,
                CiscoNxosInterfaceType.LOOPBACK));
    if (!maybeIfaceName.isPresent()) {
      return;
    }
    String ifaceName = maybeIfaceName.get();
    _c.referenceStructure(INTERFACE, ifaceName, TRACK_INTERFACE, ctx.getStart().getLine());
    _currentTrack = new TrackInterface(ifaceName, toMode(ctx.track_interface_mode()));
  }

  private TrackInterface.Mode toMode(Track_interface_modeContext ctx) {
    if (ctx.LINE_PROTOCOL() != null) {
      return TrackInterface.Mode.LINE_PROTOCOL;
    } else if (ctx.IP() != null) {
      return TrackInterface.Mode.IP_ROUTING;
    }
    assert ctx.IPV6() != null;
    return TrackInterface.Mode.IPV6_ROUTING;
  }

  @Override
  public void enterTrack_ip_route(Track_ip_routeContext ctx) {
    _currentTrackIpRoute = new TrackIpRoute(toPrefix(ctx.prefix), ctx.HMM() != null);
  }

  @Override
  public void exitTrack_ip_route(Track_ip_routeContext ctx) {
    _currentTrack = _currentTrackIpRoute;
    _currentTrackIpRoute = null;
  }

  @Override
  public void exitTir_vrf(Tir_vrfContext ctx) {
    Optional<String> maybeVrf = toString(ctx, ctx.name);
    if (!maybeVrf.isPresent()) {
      return;
    }
    String vrf = maybeVrf.get();
    _c.referenceStructure(VRF, vrf, TRACK_IP_ROUTE_VRF, ctx.getStart().getLine());
    _currentTrackIpRoute.setVrf(vrf);
  }

  @Override
  public void enterS_vrf_context(S_vrf_contextContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      _currentVrf = new Vrf("dummy", 0);
      return;
    }
    String name = nameOrErr.get();
    _currentVrf = _c.getOrCreateVrf(name);
    _c.defineStructure(VRF, name, ctx);
  }

  @Override
  public void exitSnmpssi_informs(Snmpssi_informsContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (name.isPresent()) {
      _c.setSnmpSourceInterface(name.get());
      _c.referenceStructure(
          INTERFACE, name.get(), SNMP_SERVER_SOURCE_INTERFACE, ctx.name.getStart().getLine());
    }
  }

  @Override
  public void exitSnmpssi_traps(Snmpssi_trapsContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (name.isPresent()) {
      _c.setSnmpSourceInterface(name.get());
      _c.referenceStructure(
          INTERFACE, name.get(), SNMP_SERVER_SOURCE_INTERFACE, ctx.name.getStart().getLine());
    }
  }

  @Override
  public void enterTs_host(Ts_hostContext ctx) {
    // CLI completion does not show size limit for DNS name variant of tacacs-server host
    _currentTacacsServer =
        _c.getTacacsServers().computeIfAbsent(ctx.host.getText(), TacacsServer::new);
  }

  @Override
  public void exitTs_host(Ts_hostContext ctx) {
    _currentTacacsServer = null;
  }

  @Override
  public void enterVlan_vlan(Vlan_vlanContext ctx) {
    IntegerSpace vlans = toVlanIdRange(ctx, ctx.vlans);
    if (vlans == null) {
      _currentVlans = ImmutableList.of();
      return;
    }
    _currentVlans =
        vlans.stream()
            .map(vlanId -> _c.getVlans().computeIfAbsent(vlanId, Vlan::new))
            .collect(ImmutableList.toImmutableList());
    vlans.intStream().forEach(id -> _c.defineStructure(VLAN, Integer.toString(id), ctx));
  }

  @Override
  public void exitAaagr_source_interface(Aaagr_source_interfaceContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(
        INTERFACE, name, AAA_GROUP_SERVER_RADIUS_SOURCE_INTERFACE, ctx.name.getStart().getLine());
    todo(ctx);
  }

  @Override
  public void exitAaagr_use_vrf(Aaagr_use_vrfContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(
        VRF, name, AAA_GROUP_SERVER_RADIUS_USE_VRF, ctx.name.getStart().getLine());
    todo(ctx);
  }

  @Override
  public void exitAaagt_source_interface(Aaagt_source_interfaceContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(
        INTERFACE, name, AAA_GROUP_SERVER_TACACSP_SOURCE_INTERFACE, ctx.name.getStart().getLine());
    todo(ctx);
  }

  @Override
  public void exitAaagt_use_vrf(Aaagt_use_vrfContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(
        VRF, name, AAA_GROUP_SERVER_TACACSP_USE_VRF, ctx.name.getStart().getLine());
    todo(ctx);
  }

  @Override
  public void exitAcl_fragments(Acl_fragmentsContext ctx) {
    if (ctx.deny != null) {
      _currentIpAccessList.setFragmentsBehavior(FragmentsBehavior.DENY_ALL);
    } else {
      _currentIpAccessList.setFragmentsBehavior(FragmentsBehavior.PERMIT_ALL);
    }
  }

  @Override
  public void exitAcl_line(Acl_lineContext ctx) {
    _currentIpAccessListLineNum = null;
  }

  @Override
  public void exitAcll_action(Acll_actionContext ctx) {
    _currentIpAccessListLineNum.ifPresent(
        num -> {
          IpAccessListLine line;
          if (_currentActionIpAccessListLineUnusable) {
            // unsupported, so just add current line as a remark
            line = new RemarkIpAccessListLine(num, getFullText(ctx.getParent()));
          } else {
            line =
                _currentActionIpAccessListLineBuilder
                    .setL3Options(_currentLayer3OptionsBuilder.build())
                    .build();
          }

          {
            // structure definition tracking
            String structName = getAclLineName(_currentIpAccessList.getName(), num);
            int configLine = ctx.start.getLine();
            _c.defineSingleLineStructure(IP_ACCESS_LIST_LINE, structName, configLine);
            _c.referenceStructure(
                IP_ACCESS_LIST_LINE, structName, IP_ACCESS_LIST_LINE_SELF_REFERENCE, configLine);
          }

          _currentIpAccessList.getLines().put(num, line);
        });
    _currentActionIpAccessListLineBuilder = null;
    _currentActionIpAccessListLineUnusable = null;
    _currentLayer3OptionsBuilder = null;
  }

  @Override
  public void exitAcll_remark(Acll_remarkContext ctx) {
    _currentIpAccessListLineNum.ifPresent(
        num ->
            _currentIpAccessList
                .getLines()
                .put(num, new RemarkIpAccessListLine(num, ctx.text.getText())));
  }

  @Override
  public void exitAcllal3_dst_address(Acllal3_dst_addressContext ctx) {
    _currentActionIpAccessListLineBuilder.setDstAddressSpec(toAddressSpec(ctx.addr));
    if (ctx.addr.group != null) {
      // TODO: name validation
      String name = ctx.addr.group.getText();
      _c.referenceStructure(
          OBJECT_GROUP_IP_ADDRESS,
          name,
          IP_ACCESS_LIST_DESTINATION_ADDRGROUP,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void exitAcllal3_fragments(Acllal3_fragmentsContext ctx) {
    _currentActionIpAccessListLineBuilder.setFragments(true);
  }

  @Override
  public void exitAcllal3_protocol_spec(Acllal3_protocol_specContext ctx) {
    if (ctx.prot != null) {
      _currentActionIpAccessListLineBuilder.setProtocol(toIpProtocol(ctx.prot));
    }
  }

  @Override
  public void exitAcllal3_src_address(Acllal3_src_addressContext ctx) {
    _currentActionIpAccessListLineBuilder.setSrcAddressSpec(toAddressSpec(ctx.addr));
    if (ctx.addr.group != null) {
      // TODO: name validation
      String name = ctx.addr.group.getText();
      _c.referenceStructure(
          OBJECT_GROUP_IP_ADDRESS, name, IP_ACCESS_LIST_SOURCE_ADDRGROUP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitAcllal3o_dscp(Acllal3o_dscpContext ctx) {
    Optional<Integer> dscp = toInteger(ctx, ctx.dscp);
    if (!dscp.isPresent()) {
      _currentActionIpAccessListLineUnusable = true;
      return;
    } else {
      _currentLayer3OptionsBuilder.setDscp(dscp.get());
    }
  }

  @Override
  public void exitAcllal3o_log(Acllal3o_logContext ctx) {
    _currentActionIpAccessListLineBuilder.setLog(true);
  }

  @Override
  public void exitAcllal3o_packet_length(Acllal3o_packet_lengthContext ctx) {
    Optional<IntegerSpace> spec = toIntegerSpace(ctx, ctx.spec);
    if (!spec.isPresent()) {
      _currentActionIpAccessListLineUnusable = true;
    } else {
      _currentLayer3OptionsBuilder.setPacketLength(spec.get());
    }
  }

  @Override
  public void exitAcllal3o_precedence(Acllal3o_precedenceContext ctx) {
    // TODO: discover and implement precedence numbers for NX-OS ACL precedence option
    todo(ctx);
    _currentActionIpAccessListLineUnusable = true;
  }

  @Override
  public void exitAcllal3o_ttl(Acllal3o_ttlContext ctx) {
    _currentLayer3OptionsBuilder.setTtl(toInteger(ctx.num));
  }

  @Override
  public void exitAclla_icmp(Aclla_icmpContext ctx) {
    _currentActionIpAccessListLineBuilder.setProtocol(IpProtocol.ICMP);
  }

  @Override
  public void exitAclla_tcp(Aclla_tcpContext ctx) {
    if (_currentTcpOptionsBuilder != null) {
      if (_currentTcpFlagsBuilder != null) {
        _currentTcpOptionsBuilder.setTcpFlags(_currentTcpFlagsBuilder.build());
        _currentTcpFlagsBuilder = null;
      }
      _currentActionIpAccessListLineBuilder.setL4Options(_currentTcpOptionsBuilder.build());
    }
    _currentTcpOptionsBuilder = null;
  }

  @Override
  public void exitAclla_udp(Aclla_udpContext ctx) {
    if (_currentUdpOptionsBuilder != null) {
      _currentActionIpAccessListLineBuilder.setL4Options(_currentUdpOptionsBuilder.build());
    }
    _currentUdpOptionsBuilder = null;
  }

  @Override
  public void exitAcllal4icmp_option(Acllal4icmp_optionContext ctx) {
    // See https://www.iana.org/assignments/icmp-parameters/icmp-parameters.xhtml
    Integer type = null;
    Integer code = null;
    if (ctx.type != null) {
      type = toInteger(ctx.type);
      if (ctx.code != null) {
        code = toInteger(ctx.code);
      }
    } else if (ctx.ADMINISTRATIVELY_PROHIBITED() != null) {
      type = IcmpCode.COMMUNICATION_ADMINISTRATIVELY_PROHIBITED.getType();
      code = IcmpCode.COMMUNICATION_ADMINISTRATIVELY_PROHIBITED.getCode();
    } else if (ctx.ALTERNATE_ADDRESS() != null) {
      type = IcmpType.ALTERNATE_ADDRESS;
    } else if (ctx.CONVERSION_ERROR() != null) {
      type = IcmpType.CONVERSION_ERROR;
    } else if (ctx.DOD_HOST_PROHIBITED() != null) {
      type = IcmpCode.DESTINATION_HOST_PROHIBITED.getType();
      code = IcmpCode.DESTINATION_HOST_PROHIBITED.getCode();
    } else if (ctx.DOD_NET_PROHIBITED() != null) {
      type = IcmpCode.DESTINATION_NETWORK_PROHIBITED.getType();
      code = IcmpCode.DESTINATION_NETWORK_PROHIBITED.getCode();
    } else if (ctx.ECHO() != null) {
      type = IcmpType.ECHO_REQUEST;
    } else if (ctx.ECHO_REPLY() != null) {
      type = IcmpType.ECHO_REPLY;
    } else if (ctx.GENERAL_PARAMETER_PROBLEM() != null) {
      // Interpreting as type 12 (parameter problem), unrestricted code
      type = IcmpType.PARAMETER_PROBLEM;
    } else if (ctx.HOST_ISOLATED() != null) {
      type = IcmpCode.SOURCE_HOST_ISOLATED.getType();
      code = IcmpCode.SOURCE_HOST_ISOLATED.getCode();
    } else if (ctx.HOST_PRECEDENCE_UNREACHABLE() != null) {
      type = IcmpCode.HOST_PRECEDENCE_VIOLATION.getType();
      code = IcmpCode.HOST_PRECEDENCE_VIOLATION.getCode();
    } else if (ctx.HOST_REDIRECT() != null) {
      type = IcmpCode.HOST_ERROR.getType();
      code = IcmpCode.HOST_ERROR.getCode();
    } else if (ctx.HOST_TOS_REDIRECT() != null) {
      type = IcmpCode.TOS_AND_HOST_ERROR.getType();
      code = IcmpCode.TOS_AND_HOST_ERROR.getCode();
    } else if (ctx.HOST_TOS_UNREACHABLE() != null) {
      type = IcmpCode.HOST_UNREACHABLE_FOR_TOS.getType();
      code = IcmpCode.HOST_UNREACHABLE_FOR_TOS.getCode();
    } else if (ctx.HOST_UNKNOWN() != null) {
      type = IcmpCode.DESTINATION_HOST_UNKNOWN.getType();
      code = IcmpCode.DESTINATION_HOST_UNKNOWN.getCode();
    } else if (ctx.HOST_UNREACHABLE() != null) {
      type = IcmpCode.HOST_UNREACHABLE.getType();
      code = IcmpCode.HOST_UNREACHABLE.getCode();
    } else if (ctx.INFORMATION_REPLY() != null) {
      type = IcmpType.INFO_REPLY;
    } else if (ctx.INFORMATION_REQUEST() != null) {
      type = IcmpType.INFO_REQUEST;
    } else if (ctx.MASK_REPLY() != null) {
      type = IcmpType.MASK_REPLY;
    } else if (ctx.MASK_REQUEST() != null) {
      type = IcmpType.MASK_REQUEST;
    } else if (ctx.MOBILE_REDIRECT() != null) {
      type = IcmpType.MOBILE_REDIRECT;
    } else if (ctx.NET_REDIRECT() != null) {
      type = IcmpCode.NETWORK_ERROR.getType();
      code = IcmpCode.NETWORK_ERROR.getCode();
    } else if (ctx.NET_TOS_REDIRECT() != null) {
      type = IcmpCode.TOS_AND_NETWORK_ERROR.getType();
      code = IcmpCode.TOS_AND_NETWORK_ERROR.getCode();
    } else if (ctx.NET_TOS_UNREACHABLE() != null) {
      type = IcmpCode.NETWORK_UNREACHABLE_FOR_TOS.getType();
      code = IcmpCode.NETWORK_UNREACHABLE_FOR_TOS.getCode();
    } else if (ctx.NET_UNREACHABLE() != null) {
      type = IcmpCode.NETWORK_UNREACHABLE.getType();
      code = IcmpCode.NETWORK_UNREACHABLE.getCode();
    } else if (ctx.NETWORK_UNKNOWN() != null) {
      type = IcmpCode.DESTINATION_NETWORK_UNKNOWN.getType();
      code = IcmpCode.DESTINATION_NETWORK_UNKNOWN.getCode();
    } else if (ctx.NO_ROOM_FOR_OPTION() != null) {
      type = IcmpCode.BAD_LENGTH.getType();
      code = IcmpCode.BAD_LENGTH.getCode();
    } else if (ctx.OPTION_MISSING() != null) {
      type = IcmpCode.REQUIRED_OPTION_MISSING.getType();
      code = IcmpCode.REQUIRED_OPTION_MISSING.getCode();
    } else if (ctx.PACKET_TOO_BIG() != null) {
      type = IcmpCode.FRAGMENTATION_NEEDED.getType();
      code = IcmpCode.FRAGMENTATION_NEEDED.getCode();
    } else if (ctx.PARAMETER_PROBLEM() != null) {
      type = IcmpCode.INVALID_IP_HEADER.getType();
      code = IcmpCode.INVALID_IP_HEADER.getCode();
    } else if (ctx.PORT_UNREACHABLE() != null) {
      type = IcmpCode.PORT_UNREACHABLE.getType();
      code = IcmpCode.PORT_UNREACHABLE.getCode();
    } else if (ctx.PRECEDENCE_UNREACHABLE() != null) {
      type = IcmpCode.PRECEDENCE_CUTOFF_IN_EFFECT.getType();
      code = IcmpCode.PRECEDENCE_CUTOFF_IN_EFFECT.getCode();
    } else if (ctx.PROTOCOL_UNREACHABLE() != null) {
      type = IcmpCode.PROTOCOL_UNREACHABLE.getType();
      code = IcmpCode.PROTOCOL_UNREACHABLE.getCode();
    } else if (ctx.REASSEMBLY_TIMEOUT() != null) {
      type = IcmpCode.TIME_EXCEEDED_DURING_FRAGMENT_REASSEMBLY.getType();
      code = IcmpCode.TIME_EXCEEDED_DURING_FRAGMENT_REASSEMBLY.getCode();
    } else if (ctx.REDIRECT() != null) {
      // interpreting as unrestricted type 5 (redirect)
      type = IcmpType.REDIRECT_MESSAGE;
    } else if (ctx.ROUTER_ADVERTISEMENT() != null) {
      // interpreting as unrestricted type 9 (router advertisement)
      type = IcmpType.ROUTER_ADVERTISEMENT;
    } else if (ctx.ROUTER_SOLICITATION() != null) {
      type = IcmpType.ROUTER_SOLICITATION;
    } else if (ctx.SOURCE_QUENCH() != null) {
      type = IcmpType.SOURCE_QUENCH;
    } else if (ctx.SOURCE_ROUTE_FAILED() != null) {
      type = IcmpCode.SOURCE_ROUTE_FAILED.getType();
      code = IcmpCode.SOURCE_ROUTE_FAILED.getCode();
    } else if (ctx.TIME_EXCEEDED() != null) {
      // interpreting as unrestricted type 11 (time exceeded)
      type = IcmpType.TIME_EXCEEDED;
    } else if (ctx.TIMESTAMP_REPLY() != null) {
      type = IcmpType.TIMESTAMP_REPLY;
    } else if (ctx.TIMESTAMP_REQUEST() != null) {
      type = IcmpType.TIMESTAMP_REQUEST;
    } else if (ctx.TRACEROUTE() != null) {
      type = IcmpType.TRACEROUTE;
    } else if (ctx.TTL_EXCEEDED() != null) {
      type = IcmpCode.TTL_EQ_ZERO_DURING_TRANSIT.getType();
      code = IcmpCode.TTL_EQ_ZERO_DURING_TRANSIT.getCode();
    } else if (ctx.UNREACHABLE() != null) {
      // interpreting as unrestricted type 3 (destination unreachable)
      type = IcmpType.DESTINATION_UNREACHABLE;
    } else {
      // assume valid but unsupported
      todo(ctx);
      _currentActionIpAccessListLineUnusable = true;
    }
    if (_currentActionIpAccessListLineUnusable) {
      return;
    }
    _currentActionIpAccessListLineBuilder.setL4Options(new IcmpOptions(type, code));
  }

  @Override
  public void exitAcllal4igmp_option(Acllal4igmp_optionContext ctx) {
    // TODO: discover and implement IGMP message types/codes for NX-OS ACL IGMP options
    todo(ctx);
    _currentActionIpAccessListLineUnusable = true;
  }

  @Override
  public void exitAcllal4tcp_destination_port(Acllal4tcp_destination_portContext ctx) {
    Optional<PortSpec> portSpec = toPortSpec(ctx, ctx.port);
    if (!portSpec.isPresent()) {
      _currentActionIpAccessListLineUnusable = true;
      return;
    }
    if (_currentTcpOptionsBuilder == null) {
      _currentTcpOptionsBuilder = TcpOptions.builder();
    }
    PortSpec spec = portSpec.get();
    _currentTcpOptionsBuilder.setDstPortSpec(spec);
    if (spec instanceof PortGroupPortSpec) {
      _c.referenceStructure(
          OBJECT_GROUP_IP_PORT,
          ((PortGroupPortSpec) spec).getName(),
          IP_ACCESS_LIST_DESTINATION_PORTGROUP,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void enterAcllal4tcp_option(Acllal4tcp_optionContext ctx) {
    if (_currentTcpOptionsBuilder == null) {
      _currentTcpOptionsBuilder = TcpOptions.builder();
    }
  }

  @Override
  public void exitAcllal4tcp_source_port(Acllal4tcp_source_portContext ctx) {
    Optional<PortSpec> portSpec = toPortSpec(ctx, ctx.port);
    if (!portSpec.isPresent()) {
      _currentActionIpAccessListLineUnusable = true;
      return;
    }
    PortSpec spec = portSpec.get();
    if (_currentTcpOptionsBuilder == null) {
      _currentTcpOptionsBuilder = TcpOptions.builder();
    }
    _currentTcpOptionsBuilder.setSrcPortSpec(spec);
    if (spec instanceof PortGroupPortSpec) {
      _c.referenceStructure(
          OBJECT_GROUP_IP_PORT,
          ((PortGroupPortSpec) spec).getName(),
          IP_ACCESS_LIST_SOURCE_PORTGROUP,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void exitAcllal4tcpo_established(Acllal4tcpo_establishedContext ctx) {
    _currentTcpOptionsBuilder.setEstablished(true);
  }

  @Override
  public void exitAcllal4tcpo_flags(Acllal4tcpo_flagsContext ctx) {
    if (_currentTcpFlagsBuilder == null) {
      _currentTcpFlagsBuilder = TcpFlags.builder();
    }
    if (ctx.ACK() != null) {
      _currentTcpFlagsBuilder.setAck(true);
    } else if (ctx.FIN() != null) {
      _currentTcpFlagsBuilder.setFin(true);
    } else if (ctx.PSH() != null) {
      _currentTcpFlagsBuilder.setPsh(true);
    } else if (ctx.RST() != null) {
      _currentTcpFlagsBuilder.setRst(true);
    } else if (ctx.SYN() != null) {
      _currentTcpFlagsBuilder.setSyn(true);
    } else if (ctx.URG() != null) {
      _currentTcpFlagsBuilder.setUrg(true);
    } else {
      // assume valid but unsupported
      todo(ctx);
      _currentActionIpAccessListLineUnusable = true;
    }
  }

  @Override
  public void exitAcllal4tcpo_tcp_flags_mask(Acllal4tcpo_tcp_flags_maskContext ctx) {
    Optional<Integer> mask = toInteger(ctx, ctx.mask);
    if (!mask.isPresent()) {
      _currentActionIpAccessListLineUnusable = true;
    } else {
      _currentTcpOptionsBuilder.setTcpFlagsMask(mask.get());
    }
  }

  @Override
  public void exitAcllal4udp_destination_port(Acllal4udp_destination_portContext ctx) {
    Optional<PortSpec> portSpec = toPortSpec(ctx, ctx.port);
    if (!portSpec.isPresent()) {
      _currentActionIpAccessListLineUnusable = true;
      return;
    }
    if (_currentUdpOptionsBuilder == null) {
      _currentUdpOptionsBuilder = UdpOptions.builder();
    }
    PortSpec spec = portSpec.get();
    _currentUdpOptionsBuilder.setDstPortSpec(spec);
    if (spec instanceof PortGroupPortSpec) {
      _c.referenceStructure(
          OBJECT_GROUP_IP_PORT,
          ((PortGroupPortSpec) spec).getName(),
          IP_ACCESS_LIST_DESTINATION_PORTGROUP,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void enterAcllal4udp_option(Acllal4udp_optionContext ctx) {
    if (_currentUdpOptionsBuilder == null) {
      _currentUdpOptionsBuilder = UdpOptions.builder();
    }
  }

  @Override
  public void exitAcllal4udp_source_port(Acllal4udp_source_portContext ctx) {
    Optional<PortSpec> portSpec = toPortSpec(ctx, ctx.port);
    if (!portSpec.isPresent()) {
      _currentActionIpAccessListLineUnusable = true;
      return;
    }
    if (_currentUdpOptionsBuilder == null) {
      _currentUdpOptionsBuilder = UdpOptions.builder();
    }
    PortSpec spec = portSpec.get();
    _currentUdpOptionsBuilder.setSrcPortSpec(spec);
    if (spec instanceof PortGroupPortSpec) {
      _c.referenceStructure(
          OBJECT_GROUP_IP_PORT,
          ((PortGroupPortSpec) spec).getName(),
          IP_ACCESS_LIST_SOURCE_PORTGROUP,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void exitEvv_rd(Evv_rdContext ctx) {
    RouteDistinguisherOrAuto rd = toRouteDistinguisher(ctx.rd);
    _currentEvpnVni.setRd(rd);
  }

  @Override
  public void exitEvv_route_target(Evv_route_targetContext ctx) {
    boolean setImport = ctx.dir.BOTH() != null || ctx.dir.IMPORT() != null;
    boolean setExport = ctx.dir.BOTH() != null || ctx.dir.EXPORT() != null;
    assert setImport || setExport;
    ExtendedCommunityOrAuto ecOrAuto = toExtendedCommunityOrAuto(ctx.rt);
    if (setExport) {
      _currentEvpnVni.setExportRt(ecOrAuto);
    }
    if (setImport) {
      _currentEvpnVni.setImportRt(ecOrAuto);
    }
  }

  @Override
  public void exitI_autostate(I_autostateContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setAutostate(true));
  }

  @Override
  public void exitI_bandwidth(I_bandwidthContext ctx) {
    if (ctx.bw != null) {
      boolean hasPortChannel =
          _currentInterfaces.stream()
              .anyMatch(i -> i.getType() == CiscoNxosInterfaceType.PORT_CHANNEL);
      Optional<Long> bandwidth =
          hasPortChannel ? toPortChannelBandwidth(ctx, ctx.bw) : toBandwidth(ctx, ctx.bw);
      bandwidth.ifPresent(bw -> _currentInterfaces.forEach(iface -> iface.setBandwidth(bw)));
    }
    if (ctx.inherit != null) {
      // TODO: support bandwidth inherit
      todo(ctx);
    }
  }

  @Override
  public void exitI_channel_group(I_channel_groupContext ctx) {
    int line = ctx.getStart().getLine();
    String portChannelInterfaceName = toPortChannel(ctx, ctx.id);
    if (portChannelInterfaceName == null) {
      return;
    }
    // To be added to a channel-group, all interfaces in range must be:
    // - compatible with each other
    // - compatible with the port-channel if it already exists
    // If the port-channel does not exist, it is created with compatible settings.

    // However, if force flag is set, then compatibility is forced as follows:
    // - If port-channel already exists, all interfaces in range copy its settings
    // - Otherwise, the new port-channel and interfaces beyond the first copy settings
    //   from the first interface in the range.

    boolean portChannelExists = _c.getInterfaces().containsKey(portChannelInterfaceName);
    boolean force = ctx.force != null;

    _c.referenceStructure(PORT_CHANNEL, portChannelInterfaceName, INTERFACE_CHANNEL_GROUP, line);

    if (_currentInterfaces.isEmpty()) {
      // Stop now, since later logic requires non-empty list
      return;
    }

    // Where settings are copied from: the port-channel[ID] if it exists, or the first interface
    // in the current interface range.
    Interface referenceIface =
        portChannelExists
            ? _c.getInterfaces().get(portChannelInterfaceName)
            : _currentInterfaces.iterator().next();

    if (!force) {
      Optional<Interface> incompatibleInterface =
          _currentInterfaces.stream()
              .filter(iface -> iface != referenceIface)
              .filter(iface -> !checkPortChannelCompatibilitySettings(referenceIface, iface))
              .findFirst();
      // If some incompatible interface found, warn, do not set the group,
      // do not create the port-channel, and do not copy settings.
      if (incompatibleInterface.isPresent()) {
        warn(
            ctx,
            String.format(
                "Cannot set channel-group because interface '%s' has settings that do not conform"
                    + " to those of interface '%s'",
                incompatibleInterface.get().getName(), referenceIface.getName()));
        return;
      }
    }

    if (!portChannelExists) {
      // Make the port-channel[ID] parent interface and copy settings "up" to it.
      Interface portChannelIface =
          newNonVlanInterface(portChannelInterfaceName, null, CiscoNxosInterfaceType.PORT_CHANNEL);
      copyPortChannelCompatibilitySettings(referenceIface, portChannelIface);
      _c.getInterfaces().put(portChannelInterfaceName, portChannelIface);
      _c.defineSingleLineStructure(INTERFACE, portChannelInterfaceName, line);
      _c.referenceStructure(INTERFACE, portChannelInterfaceName, INTERFACE_SELF_REFERENCE, line);
      _c.defineSingleLineStructure(PORT_CHANNEL, portChannelInterfaceName, line);
    }

    _currentInterfaces.forEach(
        iface -> {
          iface.setChannelGroup(portChannelInterfaceName);
          if (force) {
            copyPortChannelCompatibilitySettings(referenceIface, iface);
          } else {
            assert checkPortChannelCompatibilitySettings(referenceIface, iface);
          }
        });
  }

  @Override
  public void exitI_description(I_descriptionContext ctx) {
    Optional<String> description = toString(ctx, ctx.desc);
    if (description.isPresent()) {
      _currentInterfaces.forEach(i -> i.setDescription(description.get()));
    }
  }

  @Override
  public void exitI_encapsulation(I_encapsulationContext ctx) {
    Integer vlanId = toVlanId(ctx, ctx.vlan);
    if (vlanId == null) {
      return;
    }
    _currentInterfaces.forEach(iface -> iface.setEncapsulationVlan(vlanId));
  }

  @Override
  public void exitI_ip_address_concrete(I_ip_address_concreteContext ctx) {
    InterfaceAddressWithAttributes address = toInterfaceAddress(ctx.addr);
    _currentInterfaces.forEach(iface -> iface.setIpAddressDhcp(false));
    if (ctx.SECONDARY() != null) {
      // secondary addresses are appended
      _currentInterfaces.forEach(iface -> iface.getSecondaryAddresses().add(address));
    } else {
      // primary address is replaced
      _currentInterfaces.forEach(iface -> iface.setAddress(address));
    }
    if (ctx.tag != null) {
      address.setTag(toLong(ctx.tag));
    }
    if (ctx.rp != null) {
      address.setRoutePreference(toInteger(ctx.rp));
    }
  }

  @Override
  public void exitI_ip_bandwidth(I_ip_bandwidthContext ctx) {
    toBandwidthEigrp(ctx, ctx.bw)
        .ifPresent(bw -> _currentInterfaces.forEach(iface -> iface.setEigrpBandwidth(bw)));
  }

  @Override
  public void exitI_ip_delay(I_ip_delayContext ctx) {
    toIntegerInSpace(
            ctx, ctx.delay, INTERFACE_DELAY_10US_RANGE, "Interface delay (tens of microseconds)")
        .ifPresent(delay -> _currentInterfaces.forEach(iface -> iface.setEigrpDelay(delay)));
  }

  @Override
  public void exitI_ip_address_dhcp(I_ip_address_dhcpContext ctx) {
    _currentInterfaces.forEach(
        iface -> {
          iface.setAddress(null);
          iface.setIpAddressDhcp(true);
          iface.getSecondaryAddresses().clear();
        });
  }

  @Override
  public void exitI_ip_dhcp_relay(I_ip_dhcp_relayContext ctx) {
    Ip address = toIp(ctx.ip_address());
    _currentInterfaces.forEach(i -> i.getDhcpRelayAddresses().add(address));
  }

  @Override
  public void exitIipdl_prefix_list(Iipdl_prefix_listContext ctx) {
    Optional<String> prefixList = toString(ctx, ctx.prefixlist);
    if (!prefixList.isPresent()) {
      return;
    }
    _c.referenceStructure(
        IP_PREFIX_LIST,
        prefixList.get(),
        ctx.IN() == null
            ? EIGRP_DISTRIBUTE_LIST_PREFIX_LIST_OUT
            : EIGRP_DISTRIBUTE_LIST_PREFIX_LIST_IN,
        ctx.prefixlist.getStart().getLine());
    DistributeList distributeList =
        new DistributeList(prefixList.get(), DistributeListFilterType.PREFIX_LIST);
    if (ctx.IN() != null) {
      _currentInterfaces.forEach(iface -> iface.setEigrpInboundDistributeList(distributeList));
    } else {
      _currentInterfaces.forEach(iface -> iface.setEigrpOutboundDistributeList(distributeList));
    }
  }

  @Override
  public void exitIipdl_route_map(Iipdl_route_mapContext ctx) {
    Optional<String> routeMap = toString(ctx, ctx.routemap);
    if (!routeMap.isPresent()) {
      return;
    }
    _c.referenceStructure(
        ROUTE_MAP,
        routeMap.get(),
        ctx.IN() == null ? EIGRP_DISTRIBUTE_LIST_ROUTE_MAP_OUT : EIGRP_DISTRIBUTE_LIST_ROUTE_MAP_IN,
        ctx.routemap.getStart().getLine());
    DistributeList distributeList =
        new DistributeList(routeMap.get(), DistributeListFilterType.ROUTE_MAP);
    if (ctx.IN() != null) {
      _currentInterfaces.forEach(iface -> iface.setEigrpInboundDistributeList(distributeList));
    } else {
      _currentInterfaces.forEach(iface -> iface.setEigrpOutboundDistributeList(distributeList));
    }
  }

  @Override
  public void exitI_ip_eigrp(I_ip_eigrpContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.tag);
    maybeName.ifPresent(
        name ->
            _c.referenceStructure(
                ROUTER_EIGRP, name, INTERFACE_IP_EIGRP, ctx.getStart().getLine()));
  }

  @Override
  public void exitI_ip_forward(I_ip_forwardContext ctx) {
    _currentInterfaces.forEach(i -> i.setIpForward(true));
  }

  @Override
  public void exitI_ip_hello_interval(I_ip_hello_intervalContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.tag);
    maybeName.ifPresent(
        name ->
            _c.referenceStructure(
                ROUTER_EIGRP, name, INTERFACE_IP_HELLO_INTERVAL_EIGRP, ctx.getStart().getLine()));
  }

  @Override
  public void exitI_ip_hold_time(I_ip_hold_timeContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.tag);
    maybeName.ifPresent(
        name ->
            _c.referenceStructure(
                ROUTER_EIGRP, name, INTERFACE_IP_HOLD_TIME_EIGRP, ctx.getStart().getLine()));
  }

  @Override
  public void exitI_ipv6_address_concrete(I_ipv6_address_concreteContext ctx) {
    InterfaceIpv6AddressWithAttributes address6 = toInterfaceIpv6Address(ctx.addr);
    _currentInterfaces.forEach(iface -> iface.setIpv6AddressDhcp(false));
    if (ctx.SECONDARY() != null) {
      // secondary addresses are appended
      _currentInterfaces.forEach(iface -> iface.getIpv6AddressSecondaries().add(address6));
    } else {
      // primary address is replaced
      _currentInterfaces.forEach(iface -> iface.setIpv6Address(address6));
    }
    if (ctx.tag != null) {
      address6.setTag(toLong(ctx.tag));
    }
  }

  @Override
  public void exitI_ipv6_address_dhcp(I_ipv6_address_dhcpContext ctx) {
    _currentInterfaces.forEach(
        iface -> {
          iface.setIpv6Address(null);
          iface.setIpv6AddressDhcp(true);
          iface.getIpv6AddressSecondaries().clear();
        });
  }

  @Override
  public void exitI_ip_passive_interface(I_ip_passive_interfaceContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setEigrpPassive(true));
  }

  @Override
  public void exitI_ip_policy(I_ip_policyContext ctx) {
    toString(ctx, ctx.name)
        .ifPresent(
            pbrPolicyName -> {
              _currentInterfaces.forEach(iface -> iface.setPbrPolicy(pbrPolicyName));
              _c.referenceStructure(
                  ROUTE_MAP, pbrPolicyName, INTERFACE_IP_POLICY, ctx.getStart().getLine());
            });
  }

  @Override
  public void exitI_mtu(I_mtuContext ctx) {
    Optional<Integer> mtu = toInteger(ctx, ctx.interface_mtu());
    if (!mtu.isPresent()) {
      return;
    }
    _currentInterfaces.forEach(iface -> iface.setMtu(mtu.get()));
  }

  @Override
  public void exitI_no_autostate(I_no_autostateContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setAutostate(false));
  }

  @Override
  public void exitI_no_description(I_no_descriptionContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setDescription(null));
  }

  @Override
  public void exitInoshut(InoshutContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setShutdown(false));
  }

  @Override
  public void exitInos_access(Inos_accessContext ctx) {
    if (ctx.vlan != null && toVlanId(ctx, ctx.vlan) == null) {
      // NX-OS rejects lines where vlan is invalid, even if it ignores vlan later.
      return;
    }
    // Note: NX-OS does not care what vlan id you put, it clears access vlan regardless.
    _currentInterfaces.forEach(iface -> iface.setAccessVlan(null));
  }

  @Override
  public void exitInos_switchport(Inos_switchportContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setSwitchportMode(SwitchportMode.NONE));
  }

  @Override
  public void exitI_no_vrf_member(I_no_vrf_memberContext ctx) {
    String name = null;
    if (ctx.name != null) {
      Optional<String> nameOrErr = toString(ctx, ctx.name);
      if (!nameOrErr.isPresent()) {
        return;
      }
      name = nameOrErr.get();
    }
    // TODO: Should the command fail on every affected interface if it's invalid on any one of them?
    for (Interface iface : _currentInterfaces) {
      String currentIfaceVrf = iface.getVrfMember();
      if (name != null && !name.equals(currentIfaceVrf)) {
        // No effect (CLI says "ERROR: VRF foo not configured on interface Ethernet1/1")
        warn(ctx, String.format("VRF %s not configured on interface %s", name, iface.getName()));
        continue;
      } else if (currentIfaceVrf == null) {
        // Interface is already in default VRF; no effect
        continue;
      }
      clearLayer3Configuration(iface);
      iface.setVrfMember(null);
    }
  }

  @Override
  public void exitI_ip_proxy_arp(I_ip_proxy_arpContext ctx) {
    _currentInterfaces.forEach(i -> i.setIpProxyArp(true));
  }

  @Override
  public void exitI_private_vlan(I_private_vlanContext ctx) {
    IntegerSpace space = toVlanIdRange(ctx, ctx.vlan_id_range());
    if (space == null) {
      return;
    }
    todo(ctx);
  }

  @Override
  public void exitI_shutdown(I_shutdownContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setShutdown(true));
  }

  @Override
  public void exitI_switchport_access(I_switchport_accessContext ctx) {
    Integer vlanId = toVlanId(ctx, ctx.vlan);
    if (vlanId == null) {
      return;
    }
    _currentInterfaces.forEach(
        iface -> {
          if (iface.getSwitchportMode() == null) {
            // NX-OS has these commands in show run all even for interfaces in other modes.
            iface.setSwitchportMode(SwitchportMode.ACCESS);
          }
          iface.setAccessVlan(vlanId);
        });
  }

  @Override
  public void exitI_switchport_mode_access(I_switchport_mode_accessContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setSwitchportMode(SwitchportMode.ACCESS));
  }

  @Override
  public void exitI_switchport_mode_dot1q_tunnel(I_switchport_mode_dot1q_tunnelContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setSwitchportMode(SwitchportMode.DOT1Q_TUNNEL));
  }

  @Override
  public void exitI_switchport_mode_fex_fabric(I_switchport_mode_fex_fabricContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setSwitchportMode(SwitchportMode.FEX_FABRIC));
  }

  @Override
  public void exitI_switchport_mode_monitor(I_switchport_mode_monitorContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setSwitchportMode(SwitchportMode.MONITOR));
  }

  @Override
  public void exitI_switchport_mode_trunk(I_switchport_mode_trunkContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setSwitchportMode(SwitchportMode.TRUNK));
  }

  @Override
  public void exitI_switchport_monitor(I_switchport_monitorContext ctx) {
    _currentInterfaces.forEach(iface -> iface.setSwitchportMonitor(true));
  }

  @Override
  public void exitI_switchport_trunk_allowed(I_switchport_trunk_allowedContext ctx) {
    IntegerSpace vlans;
    if (ctx.vlans != null) {
      vlans = toVlanIdRange(ctx, ctx.vlans);
      if (vlans == null) {
        // invalid VLAN in range
        return;
      }
    } else if (ctx.except != null) {
      Integer except = toVlanId(ctx, ctx.except);
      if (except == null) {
        // invalid VLAN to exclude
        return;
      }
      vlans = _currentValidVlanRange.difference(IntegerSpace.of(except));
    } else if (ctx.NONE() != null) {
      vlans = IntegerSpace.EMPTY;
    } else {
      todo(ctx);
      return;
    }
    _currentInterfaces.forEach(
        iface -> {
          if (ctx.ADD() != null) {
            iface.setAllowedVlans(iface.getAllowedVlans().union(vlans));
          } else if (ctx.REMOVE() != null) {
            IntegerSpace allowedAfterRemoval = iface.getAllowedVlans().difference(vlans);
            // If all allowed VLANs were removed, none are now set, so revert to default.
            iface.setAllowedVlans(
                allowedAfterRemoval.isEmpty() ? _currentValidVlanRange : allowedAfterRemoval);
          } else {
            iface.setAllowedVlans(vlans);
          }
        });
  }

  @Override
  public void exitI_switchport_trunk_native(I_switchport_trunk_nativeContext ctx) {
    Integer vlanId = toVlanId(ctx, ctx.vlan);
    if (vlanId == null) {
      return;
    }
    _currentInterfaces.forEach(
        iface -> {
          if (iface.getSwitchportMode() == null) {
            // NX-OS has these commands in show run all even for interfaces in other modes.
            iface.setSwitchportMode(SwitchportMode.TRUNK);
          }
          iface.setNativeVlan(vlanId);
        });
  }

  @Override
  public void exitI_vrf_member(I_vrf_memberContext ctx) {
    Optional<String> nameOrErr = toString(ctx, ctx.name);
    if (!nameOrErr.isPresent()) {
      return;
    }
    if (_currentInterfaces.stream()
        .anyMatch(
            iface ->
                iface.getSwitchportModeEffective(_c.getSystemDefaultSwitchport())
                    != SwitchportMode.NONE)) {
      warn(ctx, "Cannot assign VRF to switchport interface(s)");
      return;
    }
    String name = nameOrErr.get();
    _c.referenceStructure(VRF, name, INTERFACE_VRF_MEMBER, ctx.getStart().getLine());
    _currentInterfaces.forEach(
        iface -> {
          clearLayer3Configuration(iface);
          iface.setVrfMember(name);
        });
  }

  @Override
  public void exitInoip_forward(Inoip_forwardContext ctx) {
    _currentInterfaces.forEach(i -> i.setIpForward(false));
  }

  @Override
  public void exitInoip_proxy_arp(Inoip_proxy_arpContext ctx) {
    _currentInterfaces.forEach(i -> i.setIpProxyArp(false));
  }

  @Override
  public void exitInoipo_passive_interface(Inoipo_passive_interfaceContext ctx) {
    _currentInterfaces.forEach(i -> i.getOrCreateOspf().setPassive(false));
  }

  @Override
  public void exitIp_access_list(Ip_access_listContext ctx) {
    _currentIpAccessList = null;
  }

  /**
   * Converts {@link Static_route_definitionContext} to a {@link StaticRoute.StaticRouteKey} if
   * possible. Returns {@link Optional#empty()} if a key property of the static route isn't valid.
   */
  private Optional<StaticRoute.StaticRouteKey> toStaticRouteKey(
      Static_route_definitionContext ctx) {
    int line = ctx.getStart().getLine();
    boolean discard = ctx.null0 != null;
    String nhint = null;
    if (ctx.nhint != null) {
      // TODO: if ctx.nhip is null and this version of NX-OS does not allow next-hop-int-only static
      // route, do something smart
      nhint = _c.canonicalizeInterfaceName(ctx.nhint.getText());
      _c.referenceStructure(INTERFACE, nhint, IP_ROUTE_NEXT_HOP_INTERFACE, line);
    }
    Ip nhip = ctx.nhip == null ? null : toIp(ctx.nhip);
    String nhvrf = null;
    if (ctx.nhvrf != null) {
      Optional<String> vrfOrErr = toString(ctx, ctx.nhvrf);
      if (!vrfOrErr.isPresent()) {
        return Optional.empty();
      }
      nhvrf = vrfOrErr.get();
      _c.referenceStructure(VRF, nhvrf, IP_ROUTE_NEXT_HOP_VRF, line);
    }
    Prefix prefix = toPrefix(ctx.network);
    return Optional.of(new StaticRoute.StaticRouteKey(prefix, discard, nhint, nhip, nhvrf));
  }

  /**
   * Applies non-key properties (preference, name, tag, and track) to the given static route based
   * on the given static route definition. If any of these properties has an invalid definition,
   * returns {@code false} without modifying {@code route}.
   *
   * @return {code true} if all non-key properties in the static route definition were valid.
   */
  private boolean setNonKeyAttributesOfStaticRouteIfValid(
      StaticRoute route, Static_route_definitionContext ctx) {
    // Collect properties. Don't modify route until we know all properties to be set are valid.
    String name = null;
    Integer pref = null;
    Long tag = ctx.tag != null ? toLong(ctx.tag) : null;
    Integer trackNumber = null;
    if (ctx.name != null) {
      name = toString(ctx, ctx.name);
      if (name == null) {
        return false;
      }
    }
    if (ctx.pref != null) {
      Optional<Integer> prefOptional = toInteger(ctx, ctx.pref);
      if (!prefOptional.isPresent()) {
        return false;
      }
      pref = prefOptional.get();
    }
    CiscoNxosParser.Ip_route_network_trackContext track_ctx = ctx.ip_route_network_track();
    if (track_ctx != null) {
      Optional<Integer> trackOptional = toInteger(track_ctx, track_ctx.track);
      if (!trackOptional.isPresent()) {
        return false;
      }
      trackNumber = trackOptional.get();
      if (!_c.getTracks().containsKey(trackNumber)) {
        warn(
            ctx,
            String.format(
                "Cannot reference undefined track %s. This line will be ignored.", trackNumber));
        _c.undefined(
            CiscoNxosStructureType.TRACK,
            trackNumber.toString(),
            CiscoNxosStructureUsage.IP_ROUTE_TRACK,
            track_ctx.start.getLine());
        return false;
      }
      _c.referenceStructure(
          CiscoNxosStructureType.TRACK,
          trackNumber.toString(),
          CiscoNxosStructureUsage.IP_ROUTE_TRACK,
          track_ctx.start.getLine());
    }
    // All properties were valid. Update route and return true.
    Optional.ofNullable(name).ifPresent(route::setName);
    Optional.ofNullable(pref).ifPresent(route::setPreference);
    Optional.ofNullable(tag).ifPresent(route::setTag);
    Optional.ofNullable(trackNumber).ifPresent(route::setTrack);
    return true;
  }

  @Override
  public void exitIp_route_network(Ip_route_networkContext ctx) {
    Optional<StaticRoute.StaticRouteKey> srkOptional =
        toStaticRouteKey(ctx.static_route_definition());
    if (!srkOptional.isPresent()) {
      // Definition is invalid
      return;
    }
    StaticRoute.StaticRouteKey routeKey = srkOptional.get();

    StaticRoute existingRoute = _currentVrf.getStaticRoutes().get(routeKey);
    StaticRoute route = existingRoute != null ? existingRoute : routeKey.toRoute();

    // Apply non-key attributes from the definition to the route. This will NOT affect the route if
    // the definition is not valid.
    boolean definitionIsValid =
        setNonKeyAttributesOfStaticRouteIfValid(route, ctx.static_route_definition());
    if (definitionIsValid && existingRoute == null) {
      // New route created
      _currentVrf.getStaticRoutes().put(routeKey, route);
    }
  }

  @Override
  public void exitNo_ip_route_network(No_ip_route_networkContext ctx) {
    Optional<StaticRoute.StaticRouteKey> srkOptional =
        toStaticRouteKey(ctx.static_route_definition());
    if (!srkOptional.isPresent()) {
      // Definition is invalid
      return;
    }
    StaticRoute.StaticRouteKey routeKey = srkOptional.get();
    StaticRoute removedRoute = _currentVrf.getStaticRoutes().remove(routeKey);
    if (removedRoute == null) {
      warn(ctx, "Cannot delete non-existent route");
      return;
    }
    // The `no ip route` command may specify non-key attributes (name, preference, tag, track).
    // These specifications have no effect unless any of the provided values are invalid, in which
    // case the command will be rejected.
    if (!setNonKeyAttributesOfStaticRouteIfValid(removedRoute, ctx.static_route_definition())) {
      // The `no ip route` command contains an invalid value for a non-key property. Put the route
      // back (setNonKeyAttributesOfStaticRouteIfValid() didn't modify it).
      _currentVrf.getStaticRoutes().put(routeKey, removedRoute);
    }
  }

  @Override
  public void exitNve_host_reachability(Nve_host_reachabilityContext ctx) {
    if (_currentNves.stream()
        .flatMap(nve -> nve.getMemberVnis().values().stream())
        .anyMatch(
            vni -> vni.getIngressReplicationProtocol() == IngressReplicationProtocol.STATIC)) {
      warn(
          ctx,
          "Please remove ingress replication static under VNIs before configuring host"
              + " reachability bgp.");
      return;
    }
    _currentNves.forEach(nve -> nve.setHostReachabilityProtocol(HostReachabilityProtocol.BGP));
  }

  @Override
  public void exitNve_no_shutdown(Nve_no_shutdownContext ctx) {
    _currentNves.forEach(n -> n.setShutdown(false));
  }

  @Override
  public void exitNve_source_interface(Nve_source_interfaceContext ctx) {
    Optional<String> inameOrError = toString(ctx, ctx.name);
    if (!inameOrError.isPresent()) {
      return;
    }
    String iname = inameOrError.get();
    _c.referenceStructure(INTERFACE, iname, NVE_SOURCE_INTERFACE, ctx.name.getStart().getLine());
    _currentNves.forEach(n -> n.setSourceInterface(iname));
  }

  @Override
  public void exitPl_action(Pl_actionContext ctx) {
    if (ctx.mask != null) {
      todo(ctx);
      return;
    }
    long num;
    if (ctx.num != null) {
      Optional<Long> numOption = toLong(ctx, ctx.num);
      if (!numOption.isPresent()) {
        return;
      }
      num = numOption.get();
    } else if (!_currentIpPrefixList.getLines().isEmpty()) {
      num = _currentIpPrefixList.getLines().lastKey() + 5L;
    } else {
      num = 5L;
    }
    Prefix prefix = toPrefix(ctx.prefix);
    int low;
    int high;
    int prefixLength = prefix.getPrefixLength();
    if (ctx.eq != null) {
      Optional<Integer> eqOption = toInteger(ctx, ctx.eq);
      if (!eqOption.isPresent()) {
        // invalid line
        return;
      }
      int eq = eqOption.get();
      low = eq;
      high = eq;
    } else if (ctx.ge == null && ctx.le == null) {
      low = prefixLength;
      high = prefixLength;
    } else {
      if (ctx.ge != null) {
        Optional<Integer> geOption = toInteger(ctx, ctx.ge);
        if (!geOption.isPresent()) {
          // invalid line
          return;
        }
        low = geOption.get();
      } else {
        low = prefixLength;
      }
      if (ctx.le != null) {
        Optional<Integer> leOption = toInteger(ctx, ctx.le);
        if (!leOption.isPresent()) {
          // invalid line
          return;
        }
        high = leOption.get();
      } else {
        high = Prefix.MAX_PREFIX_LENGTH;
      }
    }
    IpPrefixListLine pll =
        new IpPrefixListLine(toLineAction(ctx.action), num, prefix, new SubRange(low, high));
    _currentIpPrefixList.getLines().put(num, pll);
  }

  @Override
  public void exitPl6_action(Pl6_actionContext ctx) {
    if (ctx.mask != null) {
      todo(ctx);
      return;
    }
    long num;
    if (ctx.num != null) {
      Optional<Long> numOption = toLong(ctx, ctx.num);
      if (!numOption.isPresent()) {
        return;
      }
      num = numOption.get();
    } else if (!_currentIpv6PrefixList.getLines().isEmpty()) {
      num = _currentIpv6PrefixList.getLines().lastKey() + 5L;
    } else {
      num = 5L;
    }
    Prefix6 prefix6 = toPrefix6(ctx.prefix);
    int low;
    int high;
    int prefixLength = prefix6.getPrefixLength();
    if (ctx.eq != null) {
      Optional<Integer> eqOption = toInteger(ctx, ctx.eq);
      if (!eqOption.isPresent()) {
        // invalid line
        return;
      }
      int eq = eqOption.get();
      low = eq;
      high = eq;
    } else if (ctx.ge != null || ctx.le != null) {
      if (ctx.ge != null) {
        Optional<Integer> geOption = toInteger(ctx, ctx.ge);
        if (!geOption.isPresent()) {
          // invalid line
          return;
        }
        low = geOption.get();
      } else {
        low = prefixLength;
      }
      if (ctx.le != null) {
        Optional<Integer> leOption = toInteger(ctx, ctx.le);
        if (!leOption.isPresent()) {
          // invalid line
          return;
        }
        high = leOption.get();
      } else {
        high = Prefix6.MAX_PREFIX_LENGTH;
      }
    } else {
      low = prefixLength;
      high = Prefix6.MAX_PREFIX_LENGTH;
    }
    Ipv6PrefixListLine pll =
        new Ipv6PrefixListLine(toLineAction(ctx.action), num, prefix6, new SubRange(low, high));
    _currentIpv6PrefixList.getLines().put(num, pll);
  }

  @Override
  public void exitPl_description(Pl_descriptionContext ctx) {
    toString(ctx, ctx.text)
        .ifPresent(description -> _currentIpPrefixList.setDescription(description));
  }

  @Override
  public void exitPl6_description(Pl6_descriptionContext ctx) {
    toString(ctx, ctx.text)
        .ifPresent(description -> _currentIpv6PrefixList.setDescription(description));
  }

  @Override
  public void exitPmcp_class(Pmcp_classContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(
        CLASS_MAP_CONTROL_PLANE, name, POLICY_MAP_CLASS, ctx.getStart().getLine());
  }

  @Override
  public void exitPmnq_class(Pmnq_classContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(CLASS_MAP_NETWORK_QOS, name, POLICY_MAP_CLASS, ctx.getStart().getLine());
  }

  @Override
  public void exitPmq_class(Pmq_classContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(CLASS_MAP_QOS, name, POLICY_MAP_CLASS, ctx.getStart().getLine());
  }

  @Override
  public void exitPmqu_class(Pmqu_classContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _c.referenceStructure(CLASS_MAP_QUEUING, name, POLICY_MAP_CLASS, ctx.getStart().getLine());
  }

  @Override
  public void exitRm_continue(Rm_continueContext ctx) {
    Optional<Integer> continueTargetOrErr = toInteger(ctx, ctx.next);
    if (!continueTargetOrErr.isPresent()) {
      return;
    }
    int continueTarget = continueTargetOrErr.get();
    if (continueTarget <= _currentRouteMapEntry.getSequence()) {
      // CLI rejects continue to lower sequence
      warn(ctx, "Cannot continue to earlier sequence");
      return;
    }
    _currentRouteMapName.ifPresent(
        routeMapName ->
            _c.referenceStructure(
                ROUTE_MAP_ENTRY,
                computeRouteMapEntryName(routeMapName, continueTarget),
                ROUTE_MAP_CONTINUE,
                ctx.getStart().getLine()));
    _currentRouteMapEntry.setContinue(continueTarget);
  }

  @Override
  public void exitRmm_as_number(Rmm_as_numberContext ctx) {
    LongSpace asns = toBgpAsnRange(ctx, ctx.numbers);
    if (asns != null) {
      _currentRouteMapEntry.setMatchAsNumber(new RouteMapMatchAsNumber(asns));
    }
  }

  @Override
  public void exitRmm_as_path(Rmm_as_pathContext ctx) {
    Optional<List<String>> optNames = toIpAsPathAccessListNames(ctx, ctx.names);
    if (!optNames.isPresent()) {
      return;
    }
    List<String> newNames = optNames.get();
    assert !newNames.isEmpty();

    ImmutableList.Builder<String> names = ImmutableList.builder();
    Optional.ofNullable(_currentRouteMapEntry.getMatchAsPath())
        .ifPresent(old -> names.addAll(old.getNames()));

    int line = ctx.getStart().getLine();
    newNames.forEach(
        name -> {
          _c.referenceStructure(IP_AS_PATH_ACCESS_LIST, name, ROUTE_MAP_MATCH_AS_PATH, line);
          names.add(name);
        });
    _currentRouteMapEntry.setMatchAsPath(new RouteMapMatchAsPath(names.build()));
  }

  @Override
  public void exitRmm_community(Rmm_communityContext ctx) {
    Optional<List<String>> optNames = toIpCommunityListNames(ctx, ctx.names);
    if (!optNames.isPresent()) {
      return;
    }
    List<String> newNames = optNames.get();
    assert !newNames.isEmpty();

    ImmutableList.Builder<String> names = ImmutableList.builder();
    Optional.ofNullable(_currentRouteMapEntry.getMatchCommunity())
        .ifPresent(old -> names.addAll(old.getNames()));

    int line = ctx.getStart().getLine();
    newNames.forEach(
        name -> {
          _c.referenceStructure(
              IP_COMMUNITY_LIST_ABSTRACT_REF, name, ROUTE_MAP_MATCH_COMMUNITY, line);
          names.add(name);
        });
    _currentRouteMapEntry.setMatchCommunity(new RouteMapMatchCommunity(names.build()));
  }

  @Override
  public void exitRmm_interface(Rmm_interfaceContext ctx) {
    Optional<List<String>> optNames = toInterfaceNames(ctx, ctx.interfaces);
    if (!optNames.isPresent()) {
      return;
    }
    List<String> newNames = optNames.get();
    assert !newNames.isEmpty();

    ImmutableList.Builder<String> names = ImmutableList.builder();
    Optional.ofNullable(_currentRouteMapEntry.getMatchInterface())
        .ifPresent(old -> names.addAll(old.getNames()));

    int line = ctx.getStart().getLine();
    newNames.forEach(
        name -> {
          _c.referenceStructure(INTERFACE, name, ROUTE_MAP_MATCH_INTERFACE, line);
          names.add(name);
        });
    _currentRouteMapEntry.setMatchInterface(new RouteMapMatchInterface(names.build()));
  }

  @Override
  public void exitRmm_metric(Rmm_metricContext ctx) {
    _currentRouteMapEntry.setMatchMetric(new RouteMapMatchMetric(toLong(ctx.metric)));
  }

  @Override
  public void exitRmm_route_type(Rmm_route_typeContext ctx) {
    ImmutableSet.Builder<RouteMapMatchRouteType.Type> types = ImmutableSet.builder();
    if (ctx.external != null) {
      types.add(RouteMapMatchRouteType.Type.EXTERNAL);
    }
    if (ctx.internal != null) {
      types.add(RouteMapMatchRouteType.Type.INTERNAL);
    }
    if (ctx.local != null) {
      types.add(RouteMapMatchRouteType.Type.LOCAL);
    }
    if (ctx.nssa_external != null) {
      types.add(RouteMapMatchRouteType.Type.NSSA_EXTERNAL);
      warn(ctx, "match route-type nssa-external is not currently supported");
    }
    if (ctx.type_1 != null) {
      types.add(RouteMapMatchRouteType.Type.TYPE_1);
    }
    if (ctx.type_2 != null) {
      types.add(RouteMapMatchRouteType.Type.TYPE_2);
    }
    _currentRouteMapEntry.setMatchRouteType(new RouteMapMatchRouteType(types.build()));
  }

  @Override
  public void exitRmm_source_protocol(Rmm_source_protocolContext ctx) {
    toStringWithLengthInSpace(
            ctx, ctx.name, PROTOCOL_INSTANCE_NAME_LENGTH_RANGE, "protocol instance name")
        .map(RouteMapMatchSourceProtocol::new)
        .ifPresent(_currentRouteMapEntry::setMatchSourceProtocol);
  }

  @Override
  public void exitRmm_tag(Rmm_tagContext ctx) {
    Set<Long> longs =
        ctx.tags.stream()
            .map(CiscoNxosControlPlaneExtractor::toLong)
            .collect(ImmutableSet.toImmutableSet());
    _currentRouteMapEntry.setMatchTag(new RouteMapMatchTag(longs));
  }

  @Override
  public void exitRmm_vlan(Rmm_vlanContext ctx) {
    IntegerSpace vlans = toVlanIdRange(ctx, ctx.range);
    if (vlans == null) {
      return;
    }
    _currentRouteMapEntry.setMatchVlan(new RouteMapMatchVlan(vlans));
  }

  @Override
  public void exitRmmipa_pbr(Rmmipa_pbrContext ctx) {
    Optional<String> nameOpt = toString(ctx, ctx.name);
    if (!nameOpt.isPresent()) {
      return;
    }
    if (_currentRouteMapEntry.getMatchIpAddress() != null) {
      warn(ctx, "route-map entry cannot match more than one ip access-list");
      return;
    }
    _currentRouteMapEntry.setMatchIpAddress(new RouteMapMatchIpAddress(nameOpt.get()));
    _c.referenceStructure(
        IP_ACCESS_LIST, nameOpt.get(), ROUTE_MAP_MATCH_IP_ADDRESS, ctx.name.getStart().getLine());
  }

  @Override
  public void exitRmmipa_prefix_list(Rmmipa_prefix_listContext ctx) {
    Optional<List<String>> optNames = toIpPrefixListNames(ctx, ctx.names);
    if (!optNames.isPresent()) {
      return;
    }
    List<String> newNames = optNames.get();
    assert !newNames.isEmpty();

    ImmutableList.Builder<String> names = ImmutableList.builder();
    Optional.ofNullable(_currentRouteMapEntry.getMatchIpAddressPrefixList())
        .ifPresent(old -> names.addAll(old.getNames()));

    int line = ctx.getStart().getLine();
    newNames.forEach(
        name -> {
          _c.referenceStructure(IP_PREFIX_LIST, name, ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST, line);
          names.add(name);
        });
    _currentRouteMapEntry.setMatchIpAddressPrefixList(
        new RouteMapMatchIpAddressPrefixList(names.build()));
  }

  @Override
  public void exitRmmip_multicast(Rmmip_multicastContext ctx) {
    todo(ctx);
    _currentRouteMapEntry.setMatchIpMulticast(new RouteMapMatchIpMulticast());
  }

  @Override
  public void exitRmmip6a_pbr(Rmmip6a_pbrContext ctx) {
    Optional<String> nameOpt = toString(ctx, ctx.name);
    if (!nameOpt.isPresent()) {
      return;
    }
    if (_currentRouteMapEntry.getMatchIpv6Address() != null) {
      warn(ctx, "route-map entry cannot match more than one ipv6 access-list");
      return;
    }
    String name = nameOpt.get();
    _currentRouteMapEntry.setMatchIpv6Address(new RouteMapMatchIpv6Address(name));
    _c.referenceStructure(
        IPV6_ACCESS_LIST,
        nameOpt.get(),
        ROUTE_MAP_MATCH_IPV6_ADDRESS,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitRmmip6a_prefix_list(Rmmip6a_prefix_listContext ctx) {
    Optional<List<String>> optNames = toIpPrefixListNames(ctx, ctx.names);
    if (!optNames.isPresent()) {
      return;
    }
    List<String> newNames = optNames.get();
    assert !newNames.isEmpty();

    ImmutableList.Builder<String> names = ImmutableList.builder();
    Optional.ofNullable(_currentRouteMapEntry.getMatchIpv6AddressPrefixList())
        .ifPresent(old -> names.addAll(old.getNames()));

    int line = ctx.getStart().getLine();
    newNames.forEach(
        name -> {
          _c.referenceStructure(
              IPV6_PREFIX_LIST, name, ROUTE_MAP_MATCH_IPV6_ADDRESS_PREFIX_LIST, line);
          names.add(name);
        });
    _currentRouteMapEntry.setMatchIpv6AddressPrefixList(
        new RouteMapMatchIpv6AddressPrefixList(names.build()));
  }

  @Override
  public void exitRmsapp_last_as(Rmsapp_last_asContext ctx) {
    Optional<Integer> numPrependsOpt = toInteger(ctx, ctx.num_prepends);
    if (!numPrependsOpt.isPresent()) {
      return;
    }
    _currentRouteMapEntry.setSetAsPathPrepend(
        new RouteMapSetAsPathPrependLastAs(numPrependsOpt.get()));
  }

  @Override
  public void exitRmsapp_literal(Rmsapp_literalContext ctx) {
    _currentRouteMapEntry.setSetAsPathPrepend(
        new RouteMapSetAsPathPrependLiteralAs(
            ctx.asns.stream()
                .map(CiscoNxosControlPlaneExtractor::toLong)
                .collect(ImmutableList.toImmutableList())));
  }

  @Override
  public void exitRms_comm_list(Rms_comm_listContext ctx) {
    Optional<String> nameOrError = toString(ctx, ctx.name);
    if (!nameOrError.isPresent()) {
      return;
    }
    String name = nameOrError.get();
    _currentRouteMapEntry.setSetCommListDelete(new RouteMapSetCommListDelete(name));
    _c.referenceStructure(
        IP_COMMUNITY_LIST_ABSTRACT_REF, name, ROUTE_MAP_MATCH_COMMUNITY, ctx.getStart().getLine());
  }

  @Override
  public void exitRms_community(Rms_communityContext ctx) {
    ImmutableList.Builder<StandardCommunity> communities = ImmutableList.builder();
    for (Standard_communityContext communityCtx : ctx.communities) {
      Optional<StandardCommunity> communityOpt = toStandardCommunity(communityCtx);
      if (!communityOpt.isPresent()) {
        return;
      }
      communities.add(communityOpt.get());
    }
    RouteMapSetCommunity old = _currentRouteMapEntry.getSetCommunity();
    boolean additive = ctx.additive != null;
    if (old != null) {
      communities.addAll(old.getCommunities());
      additive = additive || old.getAdditive();
    }
    _currentRouteMapEntry.setSetCommunity(new RouteMapSetCommunity(communities.build(), additive));
  }

  @Override
  public void exitRmsipnh_literal(Rmsipnh_literalContext ctx) {
    // Incompatible with:
    // - peer-address (TODO)
    // - redist-unchanged (TODO)
    // - unchanged
    ImmutableList.Builder<Ip> nextHops = ImmutableList.builder();
    RouteMapSetIpNextHop old = _currentRouteMapEntry.getSetIpNextHop();
    if (old != null) {
      if (!(old instanceof RouteMapSetIpNextHopLiteral)) {
        warn(
            ctx,
            "Cannot mix literal next-hop IP(s) with peer-address, redist-unchanged, nor unchanged");
        return;
      }
      nextHops.addAll(((RouteMapSetIpNextHopLiteral) old).getNextHops());
    }
    ctx.next_hops.stream().map(CiscoNxosControlPlaneExtractor::toIp).forEach(nextHops::add);
    _currentRouteMapEntry.setSetIpNextHop(new RouteMapSetIpNextHopLiteral(nextHops.build()));
  }

  @Override
  public void exitRmsipnh_unchanged(Rmsipnh_unchangedContext ctx) {
    // Incompatible with:
    // - literal IP(s)
    // - peer-address (TODO)
    RouteMapSetIpNextHop old = _currentRouteMapEntry.getSetIpNextHop();
    if (old != null && !(old instanceof RouteMapSetIpNextHopUnchanged)) {
      warn(ctx, "Cannot mix unchanged with literal next-hop IP(s) nor peer-address");
    }
    _currentRouteMapEntry.setSetIpNextHop(RouteMapSetIpNextHopUnchanged.INSTANCE);
  }

  @Override
  public void exitRms_local_preference(Rms_local_preferenceContext ctx) {
    _currentRouteMapEntry.setSetLocalPreference(
        new RouteMapSetLocalPreference(toLong(ctx.local_preference)));
  }

  @Override
  public void exitRms_metric(Rms_metricContext ctx) {
    if (ctx.delay == null) {
      _currentRouteMapEntry.setSetMetric(new RouteMapSetMetric(toLong(ctx.metric)));
    } else {
      _currentRouteMapEntry.setSetMetricEigrp(
          new RouteMapSetMetricEigrp(
              new EigrpMetric(
                  toLong(ctx.metric),
                  toLong(ctx.delay),
                  toInteger(ctx.reliability),
                  toInteger(ctx.load),
                  toLong(ctx.mtu))));
    }
  }

  @Override
  public void exitRms_metric_type(Rms_metric_typeContext ctx) {
    RouteMapMetricType type;
    if (ctx.EXTERNAL() != null) {
      type = RouteMapMetricType.EXTERNAL;
    } else if (ctx.INTERNAL() != null) {
      type = RouteMapMetricType.INTERNAL;
    } else if (ctx.TYPE_1() != null) {
      type = RouteMapMetricType.TYPE_1;
    } else if (ctx.TYPE_2() != null) {
      type = RouteMapMetricType.TYPE_2;
    } else {
      // assume valid but unsupported
      todo(ctx);
      return;
    }
    _currentRouteMapEntry.setSetMetricType(new RouteMapSetMetricType(type));
  }

  @Override
  public void exitRms_origin(Rms_originContext ctx) {
    OriginType type;
    if (ctx.EGP() != null) {
      type = OriginType.EGP;
    } else if (ctx.IGP() != null) {
      type = OriginType.IGP;
    } else if (ctx.INCOMPLETE() != null) {
      type = OriginType.INCOMPLETE;
    } else {
      // Realllly should not get here
      throw new IllegalArgumentException(String.format("Invalid origin type: %s", ctx.getText()));
    }
    _currentRouteMapEntry.setSetOrigin(new RouteMapSetOrigin(type));
  }

  @Override
  public void exitRms_tag(Rms_tagContext ctx) {
    _currentRouteMapEntry.setSetTag(new RouteMapSetTag(toLong(ctx.tag)));
  }

  @Override
  public void exitRms_weight(Rms_weightContext ctx) {
    _currentRouteMapEntry.setSetWeight(new RouteMapSetWeight(toInteger(ctx.weight)));
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    _c.setHostname(ctx.hostname.getText());
  }

  @Override
  public void exitS_vdc(S_vdcContext ctx) {
    Optional<Integer> maybeId = toInteger(ctx, ctx.id);
    if (!maybeId.isPresent()) {
      return;
    }
    int vdcId = maybeId.get();
    if (vdcId != 1) {
      warn(ctx, "Virtual device contexts are not yet supported");
    }
  }

  @Override
  public void exitS_vrf_context(S_vrf_contextContext ctx) {
    _currentVrf = _c.getDefaultVrf();
  }

  @Override
  public void enterSnmps_community(Snmps_communityContext ctx) {
    Optional<String> name = toString(ctx, ctx.community);
    // dummy for invalid name
    _currentSnmpCommunity =
        name.map(s -> _c.getSnmpCommunities().computeIfAbsent(s, SnmpCommunity::new))
            .orElseGet(() -> new SnmpCommunity("dummy"));
  }

  @Override
  public void exitSnmps_community(Snmps_communityContext ctx) {
    _currentSnmpCommunity = null;
  }

  @Override
  public void exitSnmps_community_use_acl(Snmps_community_use_aclContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _currentSnmpCommunity.setAclName(name.get());
    _c.referenceStructure(
        IP_ACCESS_LIST_ABSTRACT_REF,
        name.get(),
        SNMP_SERVER_COMMUNITY_USE_ACL,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitSnmps_community_use_ipv4acl(Snmps_community_use_ipv4aclContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _currentSnmpCommunity.setAclNameV4(name.get());
    _c.referenceStructure(
        IP_ACCESS_LIST,
        name.get(),
        SNMP_SERVER_COMMUNITY_USE_IPV4ACL,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitSnmps_community_use_ipv6acl(Snmps_community_use_ipv6aclContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _currentSnmpCommunity.setAclNameV6(name.get());
    _c.referenceStructure(
        IPV6_ACCESS_LIST,
        name.get(),
        SNMP_SERVER_COMMUNITY_USE_IPV6ACL,
        ctx.name.getStart().getLine());
  }

  @Override
  public void enterSnmps_host(Snmps_hostContext ctx) {
    // CLI completion does not show size limit for DNS name variant of snmp-server host
    _currentSnmpServer = _c.getSnmpServers().computeIfAbsent(ctx.host.getText(), SnmpServer::new);
  }

  @Override
  public void exitSnmps_host(Snmps_hostContext ctx) {
    _currentSnmpServer = null;
  }

  @Override
  public void exitVc_description(Vc_descriptionContext ctx) {
    toString(ctx, ctx.desc).ifPresent(_currentVrf::setDescription);
  }

  @Override
  public void exitVc_no_shutdown(Vc_no_shutdownContext ctx) {
    _currentVrf.setShutdown(false);
  }

  @Override
  public void exitVc_rd(Vc_rdContext ctx) {
    _currentVrf.setRd(toRouteDistinguisher(ctx.rd));
  }

  @Override
  public void exitVc_shutdown(Vc_shutdownContext ctx) {
    _currentVrf.setShutdown(true);
  }

  @Override
  public void exitVc_vni(Vc_vniContext ctx) {
    Optional<Integer> vniOrError = toInteger(ctx, ctx.vni_number());
    if (!vniOrError.isPresent()) {
      return;
    }
    Integer vni = vniOrError.get();
    _currentVrf.setVni(vni);
  }

  @Override
  public void exitVcspt_qos(Vcspt_qosContext ctx) {
    Optional<String> name = toString(ctx, ctx.name);
    if (!name.isPresent()) {
      return;
    }
    _c.referenceStructure(
        POLICY_MAP_QOS, name.get(), VLAN_CONFIGURATION_QOS, ctx.getStart().getLine());
  }

  @Override
  public void exitFf_admin_distance(Ff_admin_distanceContext ctx) {
    // TODO: conversion
    todo(ctx);
    toInteger(ctx, ctx.dist).ifPresent(_c::setFabricForwardingAdminDistance);
  }

  @Override
  public void exitFf_anycast_gateway_mac(Ff_anycast_gateway_macContext ctx) {
    _c.setFabricForwardingAnycastGatewayMac(toMacAddress(ctx.mac));
  }

  private static @Nonnull MacAddress toMacAddress(Mac_address_literalContext ctx) {
    // TODO: support all options.
    //       Option 1: E.E.E
    //       Option 2: EE-EE-EE-EE-EE-EE
    //       Option 3: EE:EE:EE:EE:EE:EE
    //       Option 4: EEEE.EEEE.EEEE (supported, canonical)
    char[] letters = ctx.getText().replace(".", "").toCharArray();
    String datamodelFormat =
        String.format(
            "%c%c:%c%c:%c%c:%c%c:%c%c:%c%c",
            letters[0],
            letters[1],
            letters[2],
            letters[3],
            letters[4],
            letters[5],
            letters[6],
            letters[7],
            letters[8],
            letters[9],
            letters[10],
            letters[11]);
    return MacAddress.parse(datamodelFormat);
  }

  @Override
  public void exitI_fabric_forwarding(I_fabric_forwardingContext ctx) {
    boolean warnInvalid = false;
    for (Interface i : _currentInterfaces) {
      CiscoNxosInterfaceType type = i.getType();
      if (type != CiscoNxosInterfaceType.VLAN) {
        warnInvalid = true;
        continue;
      }
      i.setFabricForwardingModeAnycastGateway(true);
    }
    if (warnInvalid) {
      warn(ctx, "fabric forwarding mode anycast-gateway only valid on Vlan interfaces");
    }
  }

  private static void setRouteTarget(
      Route_target_or_autoContext rtAutoCtx,
      Both_export_importContext direction,
      @Nullable TerminalNode evpn,
      VrfAddressFamily af) {
    ExtendedCommunityOrAuto ecOrAuto = toExtendedCommunityOrAuto(rtAutoCtx);
    boolean setExport = direction.BOTH() != null || direction.EXPORT() != null;
    boolean setImport = direction.BOTH() != null || direction.IMPORT() != null;
    boolean isEvpn = evpn != null;
    if (!isEvpn && setExport) {
      af.setExportRt(ecOrAuto);
    }
    if (isEvpn && setExport) {
      af.setExportRtEvpn(ecOrAuto);
    }
    if (!isEvpn && setImport) {
      af.setImportRt(ecOrAuto);
    }
    if (isEvpn && setImport) {
      af.setImportRtEvpn(ecOrAuto);
    }
  }

  @Override
  public void exitVcaf4u_route_target(Vcaf4u_route_targetContext ctx) {
    VrfAddressFamily af = _currentVrf.getAddressFamily(AddressFamily.IPV4_UNICAST);
    setRouteTarget(ctx.rt, ctx.both_export_import(), ctx.EVPN(), af);
  }

  @Override
  public void exitVcaf6u_route_target(Vcaf6u_route_targetContext ctx) {
    VrfAddressFamily af = _currentVrf.getAddressFamily(AddressFamily.IPV6_UNICAST);
    setRouteTarget(ctx.rt, ctx.both_export_import(), ctx.EVPN(), af);
  }

  @Override
  public void exitVlan_vlan(Vlan_vlanContext ctx) {
    _currentVlans = null;
  }

  @Override
  public void exitVv_vn_segment(Vv_vn_segmentContext ctx) {
    Optional<Integer> vniOrError = toInteger(ctx, ctx.vni_number());
    if (!vniOrError.isPresent()) {
      return;
    }
    Integer vni = vniOrError.get();
    _currentVlans.forEach(v -> v.setVni(vni));
  }

  private @Nonnull Optional<Long> toBandwidthEigrp(
      ParserRuleContext messageCtx, Interface_bandwidth_eigrp_kbpsContext ctx) {
    return toLongInSpace(messageCtx, ctx, BANDWIDTH_EIGRP_RANGE, "bandwidth eigrp");
  }

  private @Nonnull Optional<Long> toBandwidth(
      ParserRuleContext messageCtx, Interface_bandwidth_kbpsContext ctx) {
    return toLongInSpace(messageCtx, ctx, BANDWIDTH_RANGE, "bandwidth");
  }

  private Optional<Long> toPortChannelBandwidth(
      ParserRuleContext messageCtx, Interface_bandwidth_kbpsContext ctx) {
    return toLongInSpace(messageCtx, ctx, BANDWIDTH_PORT_CHANNEL_RANGE, "bandwidth");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Track_object_idContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, TRACK_OBJECT_ID_RANGE, "track object-id");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Dscp_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, DSCP_RANGE, "DSCP number");
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, Dscp_specContext ctx) {
    if (ctx.num != null) {
      return toInteger(messageCtx, ctx.num);
    } else if (ctx.AF11() != null) {
      return Optional.of(DscpType.AF11.number());
    } else if (ctx.AF12() != null) {
      return Optional.of(DscpType.AF12.number());
    } else if (ctx.AF13() != null) {
      return Optional.of(DscpType.AF13.number());
    } else if (ctx.AF21() != null) {
      return Optional.of(DscpType.AF21.number());
    } else if (ctx.AF22() != null) {
      return Optional.of(DscpType.AF22.number());
    } else if (ctx.AF23() != null) {
      return Optional.of(DscpType.AF23.number());
    } else if (ctx.AF31() != null) {
      return Optional.of(DscpType.AF31.number());
    } else if (ctx.AF32() != null) {
      return Optional.of(DscpType.AF32.number());
    } else if (ctx.AF33() != null) {
      return Optional.of(DscpType.AF33.number());
    } else if (ctx.AF41() != null) {
      return Optional.of(DscpType.AF41.number());
    } else if (ctx.AF42() != null) {
      return Optional.of(DscpType.AF42.number());
    } else if (ctx.AF43() != null) {
      return Optional.of(DscpType.AF43.number());
    } else if (ctx.CS1() != null) {
      return Optional.of(DscpType.CS1.number());
    } else if (ctx.CS2() != null) {
      return Optional.of(DscpType.CS2.number());
    } else if (ctx.CS3() != null) {
      return Optional.of(DscpType.CS3.number());
    } else if (ctx.CS4() != null) {
      return Optional.of(DscpType.CS4.number());
    } else if (ctx.CS5() != null) {
      return Optional.of(DscpType.CS5.number());
    } else if (ctx.CS6() != null) {
      return Optional.of(DscpType.CS6.number());
    } else if (ctx.CS7() != null) {
      return Optional.of(DscpType.CS7.number());
    } else if (ctx.DEFAULT() != null) {
      return Optional.of(DscpType.DEFAULT.number());
    } else if (ctx.EF() != null) {
      return Optional.of(DscpType.EF.number());
    } else {
      // assumed to be valid but unsupported
      todo(ctx);
      return Optional.empty();
    }
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ebgp_multihop_ttlContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, BGP_EBGP_MULTIHOP_TTL_RANGE, "BGP ebgp-multihop ttl");
  }

  /** Returns the ASN iff the process tag is a valid EIGRP ASN. */
  private @Nonnull Optional<Integer> toMaybeAsn(String processTag) {
    return Optional.ofNullable(Ints.tryParse(processTag)).filter(EIGRP_ASN_RANGE::contains);
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, Eigrp_asnContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, EIGRP_ASN_RANGE, "EIGRP autonomous-system number");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Inherit_sequence_numberContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx, BGP_INHERIT_RANGE, "BGP neighbor inherit peer-policy seq");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Interface_mtuContext ctx) {
    assert messageCtx != null; // prevent unused warning.
    // TODO: the valid MTU ranges are dependent on interface type.
    return Optional.of(toInteger(ctx.mtu));
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ip_prefix_list_line_prefix_lengthContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx, IP_PREFIX_LIST_PREFIX_LENGTH_RANGE, "ip prefix-list prefix-length bound");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ipv6_prefix_list_line_prefix_lengthContext ctx) {
    return toIntegerInSpace(
        messageCtx,
        ctx,
        IPV6_PREFIX_LIST_PREFIX_LENGTH_RANGE,
        "ipv6 prefix-list prefix-length bound");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Last_as_num_prependsContext ctx) {
    return toIntegerInSpace(
        messageCtx,
        ctx,
        NUM_AS_PATH_PREPENDS_RANGE,
        "set as-path prepend last-as number of prepends");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Maxas_limitContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, BGP_MAXAS_LIMIT_RANGE, "BGP maxas-limit");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Maximum_pathsContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, BGP_MAXIMUM_PATHS_RANGE, "BGP maximum-paths");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ospf_area_default_costContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx, OSPF_AREA_DEFAULT_COST_RANGE, "router ospf area default-cost");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ospf_area_range_costContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx, OSPF_AREA_RANGE_COST_RANGE, "router ospf area range cost");
  }

  private static int toInteger(Ospf_priorityContext ctx) {
    return toInteger(ctx.uint8());
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Packet_lengthContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, PACKET_LENGTH_RANGE, "packet length");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Protocol_distanceContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, PROTOCOL_DISTANCE_RANGE, "distance");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Tcp_flags_maskContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, TCP_FLAGS_MASK_RANGE, "tcp-flags-mask");
  }

  private Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Allowas_in_max_occurrencesContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, BGP_ALLOWAS_IN, "bgp allowas-in");
  }

  private int toInteger(Tcp_port_numberContext ctx) {
    return toInteger(ctx.uint16());
  }

  private @Nonnull Optional<Integer> toInteger(Tcp_portContext ctx) {
    if (ctx.num != null) {
      return Optional.of(toInteger(ctx.num));
    } else if (ctx.BGP() != null) {
      return Optional.of(NamedPort.BGP.number());
    } else if (ctx.CHARGEN() != null) {
      return Optional.of(NamedPort.CHARGEN.number());
    } else if (ctx.CMD() != null) {
      return Optional.of(NamedPort.CMDtcp_OR_SYSLOGudp.number());
    } else if (ctx.DAYTIME() != null) {
      return Optional.of(NamedPort.DAYTIME.number());
    } else if (ctx.DISCARD() != null) {
      return Optional.of(NamedPort.DISCARD.number());
    } else if (ctx.DOMAIN() != null) {
      return Optional.of(NamedPort.DOMAIN.number());
    } else if (ctx.DRIP() != null) {
      return Optional.of(NamedPort.DRIP.number());
    } else if (ctx.ECHO() != null) {
      return Optional.of(NamedPort.ECHO.number());
    } else if (ctx.EXEC() != null) {
      return Optional.of(NamedPort.BIFFudp_OR_EXECtcp.number());
    } else if (ctx.FINGER() != null) {
      return Optional.of(NamedPort.FINGER.number());
    } else if (ctx.FTP() != null) {
      return Optional.of(NamedPort.FTP.number());
    } else if (ctx.FTP_DATA() != null) {
      return Optional.of(NamedPort.FTP_DATA.number());
    } else if (ctx.GOPHER() != null) {
      return Optional.of(NamedPort.GOPHER.number());
    } else if (ctx.HOSTNAME() != null) {
      return Optional.of(NamedPort.HOSTNAME.number());
    } else if (ctx.IDENT() != null) {
      return Optional.of(NamedPort.IDENT.number());
    } else if (ctx.IRC() != null) {
      return Optional.of(NamedPort.IRC.number());
    } else if (ctx.KLOGIN() != null) {
      return Optional.of(NamedPort.KLOGIN.number());
    } else if (ctx.KSHELL() != null) {
      return Optional.of(NamedPort.KSHELL.number());
    } else if (ctx.LOGIN() != null) {
      return Optional.of(NamedPort.LOGINtcp_OR_WHOudp.number());
    } else if (ctx.LPD() != null) {
      return Optional.of(NamedPort.LPD.number());
    } else if (ctx.NNTP() != null) {
      return Optional.of(NamedPort.NNTP.number());
    } else if (ctx.PIM_AUTO_RP() != null) {
      return Optional.of(NamedPort.PIM_AUTO_RP.number());
    } else if (ctx.POP2() != null) {
      return Optional.of(NamedPort.POP2.number());
    } else if (ctx.POP3() != null) {
      return Optional.of(NamedPort.POP3.number());
    } else if (ctx.SMTP() != null) {
      return Optional.of(NamedPort.SMTP.number());
    } else if (ctx.SUNRPC() != null) {
      return Optional.of(NamedPort.SUNRPC.number());
    } else if (ctx.TACACS() != null) {
      return Optional.of(NamedPort.TACACS.number());
    } else if (ctx.TALK() != null) {
      return Optional.of(NamedPort.TALK.number());
    } else if (ctx.TELNET() != null) {
      return Optional.of(NamedPort.TELNET.number());
    } else if (ctx.TIME() != null) {
      return Optional.of(NamedPort.TIME.number());
    } else if (ctx.UUCP() != null) {
      return Optional.of(NamedPort.UUCP.number());
    } else if (ctx.WHOIS() != null) {
      return Optional.of(NamedPort.WHOIS.number());
    } else if (ctx.WWW() != null) {
      return Optional.of(NamedPort.HTTP.number());
    } else {
      // assume valid but unsupported
      todo(ctx);
      return Optional.empty();
    }
  }

  private int toInteger(Udp_port_numberContext ctx) {
    return toInteger(ctx.uint16());
  }

  private @Nonnull Optional<Integer> toInteger(Udp_portContext ctx) {
    if (ctx.num != null) {
      return Optional.of(toInteger(ctx.num));
    } else if (ctx.BIFF() != null) {
      return Optional.of(NamedPort.BIFFudp_OR_EXECtcp.number());
    } else if (ctx.BOOTPC() != null) {
      return Optional.of(NamedPort.BOOTPC.number());
    } else if (ctx.BOOTPS() != null) {
      return Optional.of(NamedPort.BOOTPS_OR_DHCP.number());
    } else if (ctx.DISCARD() != null) {
      return Optional.of(NamedPort.DISCARD.number());
    } else if (ctx.DNSIX() != null) {
      return Optional.of(NamedPort.DNSIX.number());
    } else if (ctx.DOMAIN() != null) {
      return Optional.of(NamedPort.DOMAIN.number());
    } else if (ctx.ECHO() != null) {
      return Optional.of(NamedPort.ECHO.number());
    } else if (ctx.ISAKMP() != null) {
      return Optional.of(NamedPort.ISAKMP.number());
    } else if (ctx.MOBILE_IP() != null) {
      return Optional.of(NamedPort.MOBILE_IP_AGENT.number());
    } else if (ctx.NAMESERVER() != null) {
      return Optional.of(NamedPort.NAMESERVER.number());
    } else if (ctx.NETBIOS_DGM() != null) {
      return Optional.of(NamedPort.NETBIOS_DGM.number());
    } else if (ctx.NETBIOS_NS() != null) {
      return Optional.of(NamedPort.NETBIOS_NS.number());
    } else if (ctx.NETBIOS_SS() != null) {
      return Optional.of(NamedPort.NETBIOS_SSN.number());
    } else if (ctx.NON500_ISAKMP() != null) {
      return Optional.of(NamedPort.NON500_ISAKMP.number());
    } else if (ctx.NTP() != null) {
      return Optional.of(NamedPort.NTP.number());
    } else if (ctx.PIM_AUTO_RP() != null) {
      return Optional.of(NamedPort.PIM_AUTO_RP.number());
    } else if (ctx.RIP() != null) {
      return Optional.of(NamedPort.EFStcp_OR_RIPudp.number());
    } else if (ctx.SNMP() != null) {
      return Optional.of(NamedPort.SNMP.number());
    } else if (ctx.SNMPTRAP() != null) {
      return Optional.of(NamedPort.SNMPTRAP.number());
    } else if (ctx.SUNRPC() != null) {
      return Optional.of(NamedPort.SUNRPC.number());
    } else if (ctx.SYSLOG() != null) {
      return Optional.of(NamedPort.CMDtcp_OR_SYSLOGudp.number());
    } else if (ctx.TACACS() != null) {
      return Optional.of(NamedPort.TACACS.number());
    } else if (ctx.TALK() != null) {
      return Optional.of(NamedPort.TALK.number());
    } else if (ctx.TFTP() != null) {
      return Optional.of(NamedPort.TFTP.number());
    } else if (ctx.TIME() != null) {
      return Optional.of(NamedPort.TIME.number());
    } else if (ctx.WHO() != null) {
      return Optional.of(NamedPort.LOGINtcp_OR_WHOudp.number());
    } else if (ctx.XDMCP() != null) {
      return Optional.of(NamedPort.XDMCP.number());
    } else {
      // assume valid but unsupported
      todo(ctx);
      return Optional.empty();
    }
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, Vdc_idContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, VDC_ID_RANGE, "vdc id");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vni_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, VNI_RANGE, "VNI");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Route_map_sequenceContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx, ROUTE_MAP_ENTRY_SEQUENCE_RANGE, "route-map entry sequence");
  }

  /**
   * Convert a {@link ParserRuleContext} whose text is guaranteed to represent a valid signed 32-bit
   * decimal integer to an {@link Integer} if it is contained in the provided {@code space}, or else
   * {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, ParserRuleContext ctx, IntegerSpace space, String name) {
    int num = Integer.parseInt(ctx.getText());
    if (!space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private @Nonnull Optional<IntegerSpace> toIntegerSpace(
      ParserRuleContext messageCtx, Acllal3o_packet_length_specContext ctx) {
    boolean range = ctx.range != null;
    return toInteger(messageCtx, ctx.arg1)
        .map(
            arg1 ->
                toIntegerSpace(
                        messageCtx,
                        arg1,
                        range ? toInteger(messageCtx, ctx.arg2) : Optional.empty(),
                        ctx.eq != null,
                        ctx.lt != null,
                        ctx.gt != null,
                        ctx.neq != null,
                        range,
                        PACKET_LENGTH_RANGE)
                    .orElse(null));
  }

  private @Nonnull Optional<IntegerSpace> toIntegerSpace(
      ParserRuleContext messageCtx, Acllal4tcp_port_spec_literalContext ctx) {
    boolean range = ctx.range != null;
    return toInteger(ctx.arg1)
        .map(
            arg1 ->
                toIntegerSpace(
                        messageCtx,
                        arg1,
                        range ? toInteger(ctx.arg2) : Optional.empty(),
                        ctx.eq != null,
                        ctx.lt != null,
                        ctx.gt != null,
                        ctx.neq != null,
                        range,
                        TCP_PORT_RANGE)
                    .orElse(null));
  }

  private @Nonnull Optional<IntegerSpace> toIntegerSpace(
      ParserRuleContext messageCtx, Acllal4udp_port_spec_literalContext ctx) {
    boolean range = ctx.range != null;
    return toInteger(ctx.arg1)
        .map(
            arg1 ->
                toIntegerSpace(
                        messageCtx,
                        arg1,
                        range ? toInteger(ctx.arg2) : Optional.empty(),
                        ctx.eq != null,
                        ctx.lt != null,
                        ctx.gt != null,
                        ctx.neq != null,
                        range,
                        UDP_PORT_RANGE)
                    .orElse(null));
  }

  private @Nullable LongSpace toBgpAsnRange(
      ParserRuleContext messageCtx, Bgp_asn_rangeContext ctx) {
    String rangeText = ctx.getText();
    LongSpace value = LongSpace.parse(rangeText);
    if (!BGP_ASN_RANGE.contains(value)) {
      warn(
          messageCtx,
          String.format("Expected BGP ASNs in range %s, but got '%s'", BGP_ASN_RANGE, rangeText));
      return null;
    }
    return value;
  }

  /**
   * Helper for NX-OS integer space specifiers to convert to IntegerSpace if valid, or else {@link
   * Optional#empty}.
   */
  private @Nonnull Optional<IntegerSpace> toIntegerSpace(
      ParserRuleContext messageCtx,
      int arg1,
      Optional<Integer> arg2Optional,
      boolean eq,
      boolean lt,
      boolean gt,
      boolean neq,
      boolean range,
      IntegerSpace space) {
    if (eq) {
      return Optional.of(IntegerSpace.of(arg1));
    } else if (lt) {
      if (arg1 <= space.least()) {
        return Optional.empty();
      }
      return Optional.of(space.intersection(IntegerSpace.of(Range.closed(0, arg1 - 1))));
    } else if (gt) {
      if (arg1 >= space.greatest()) {
        return Optional.empty();
      }
      return Optional.of(
          space.intersection(IntegerSpace.of(Range.closed(arg1 + 1, Integer.MAX_VALUE))));
    } else if (neq) {
      return Optional.of(space.difference(IntegerSpace.of(arg1)));
    } else if (range) {
      // both args guaranteed to be in range
      return arg2Optional.map(arg2 -> IntegerSpace.of(Range.closed(arg1, arg2)));
    } else {
      // assume valid but unsupported by caller
      todo(messageCtx);
      return Optional.empty();
    }
  }

  private @Nonnull Optional<Long> toLong(
      Ip_as_path_access_listContext messageCtx, Ip_as_path_access_list_seqContext ctx) {
    return toLongInSpace(
        messageCtx, ctx, IP_AS_PATH_ACCESS_LIST_SEQ_RANGE, "ip as-path access-list seq");
  }

  private @Nonnull Optional<Long> toLong(
      ParserRuleContext messageCtx, Ip_access_list_line_numberContext ctx) {
    return toLongInSpace(
        messageCtx, ctx, IP_ACCESS_LIST_LINE_NUMBER_RANGE, "ip access-list line number");
  }

  private @Nonnull Optional<Long> toLong(
      ParserRuleContext messageCtx, Ip_community_list_seqContext ctx) {
    return toLongInSpace(
        messageCtx, ctx, IP_COMMUNITY_LIST_LINE_NUMBER_RANGE, "ip community-list line number");
  }

  private @Nonnull Optional<Long> toLong(
      ParserRuleContext messageCtx, Ip_prefix_list_line_numberContext ctx) {
    return toLongInSpace(messageCtx, ctx, IP_PREFIX_LIST_LINE_NUMBER_RANGE, "ip prefix-list seq");
  }

  /**
   * Convert a {@link ParserRuleContext} whose text is guaranteed to represent a valid signed 64-bit
   * decimal integer to a {@link Long} if it is contained in the provided {@code space}, or else
   * {@link Optional#empty}.
   */
  private @Nonnull Optional<Long> toLongInSpace(
      ParserRuleContext messageCtx, ParserRuleContext ctx, LongSpace space, String name) {
    long num = Long.parseLong(ctx.getText());
    if (!space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private @Nullable String toPortChannel(ParserRuleContext messageCtx, Channel_idContext ctx) {
    int id = Integer.parseInt(ctx.getText());
    // not a mistake; range is 1-4096 (not zero-based).
    if (!PORT_CHANNEL_RANGE.contains(id)) {
      warn(
          messageCtx,
          String.format(
              "Expected port-channel id in range %s, but got '%d'", PORT_CHANNEL_RANGE, id));
      return null;
    }
    return getCanonicalInterfaceNamePrefix("port-channel") + id;
  }

  private @Nonnull Optional<PortSpec> toPortSpec(
      ParserRuleContext messageCtx, Acllal4tcp_port_specContext ctx) {
    if (ctx.literal != null) {
      return toIntegerSpace(messageCtx, ctx.literal).map(LiteralPortSpec::new);
    } else if (ctx.group != null) {
      return Optional.of(toPortSpec(ctx.group));
    } else {
      // assume valid but unsupported
      todo(ctx);
      return Optional.empty();
    }
  }

  private @Nonnull Optional<PortSpec> toPortSpec(
      ParserRuleContext messageCtx, Acllal4udp_port_specContext ctx) {
    if (ctx.literal != null) {
      return toIntegerSpace(messageCtx, ctx.literal).map(LiteralPortSpec::new);
    } else if (ctx.group != null) {
      return Optional.of(toPortSpec(ctx.group));
    } else {
      // assume valid but unsupported
      todo(ctx);
      return Optional.empty();
    }
  }

  private @Nonnull StandardCommunity toStandardCommunity(Literal_standard_communityContext ctx) {
    return StandardCommunity.of(toInteger(ctx.high), toInteger(ctx.low));
  }

  private @Nonnull Optional<StandardCommunity> toStandardCommunity(Standard_communityContext ctx) {
    if (ctx.literal != null) {
      return Optional.of(toStandardCommunity(ctx.literal));
    } else if (ctx.INTERNET() != null) {
      return Optional.of(StandardCommunity.INTERNET);
    } else if (ctx.LOCAL_AS() != null) {
      return Optional.of(StandardCommunity.NO_EXPORT_SUBCONFED);
    } else if (ctx.NO_ADVERTISE() != null) {
      return Optional.of(StandardCommunity.NO_ADVERTISE);
    } else if (ctx.NO_EXPORT() != null) {
      return Optional.of(StandardCommunity.NO_EXPORT);
    } else {
      // assume valid but unsupported
      todo(ctx);
      return Optional.empty();
    }
  }

  private @Nonnull Optional<Set<StandardCommunity>> toStandardCommunitySet(
      Iterable<Standard_communityContext> communities) {
    ImmutableSet.Builder<StandardCommunity> builder = ImmutableSet.builder();
    for (Standard_communityContext communityCtx : communities) {
      Optional<StandardCommunity> community = toStandardCommunity(communityCtx);
      if (!community.isPresent()) {
        return Optional.empty();
      }
      builder.add(community.get());
    }
    return Optional.of(builder.build());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, As_path_regexContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx,
        ctx.dqs.text,
        IP_AS_PATH_ACCESS_LIST_REGEX_LENGTH_RANGE,
        "ip as-path access-list line regex");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Class_map_cp_nameContext ctx) {
    return toStringWithLengthInSpace(
            messageCtx,
            ctx,
            CLASS_MAP_CONTROL_PLANE_NAME_LENGTH_RANGE,
            "class-map type control-plane name")
        .map(name -> getPreferredName(name, CLASS_MAP_CONTROL_PLANE));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Class_map_network_qos_nameContext ctx) {
    return toStringWithLengthInSpace(
            messageCtx,
            ctx,
            CLASS_MAP_NETWORK_QOS_NAME_LENGTH_RANGE,
            "class-map type network-qos name")
        .map(name -> getPreferredName(name, CLASS_MAP_NETWORK_QOS));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Class_map_qos_nameContext ctx) {
    return toStringWithLengthInSpace(
            messageCtx, ctx, CLASS_MAP_QOS_NAME_LENGTH_RANGE, "class-map type qos name")
        .map(name -> getPreferredName(name, CLASS_MAP_QOS));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Class_map_queuing_nameContext ctx) {
    return toStringWithLengthInSpace(
            messageCtx, ctx, CLASS_MAP_QUEUING_NAME_LENGTH_RANGE, "class-map type queuing name")
        .map(name -> getPreferredName(name, CLASS_MAP_QUEUING));
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, Fe_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, FLOW_EXPORTER_NAME_LENGTH_RANGE, "flow exporter name");
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, Fm_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, FLOW_MONITOR_NAME_LENGTH_RANGE, "flow monitor name");
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, Fr_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, FLOW_RECORD_NAME_LENGTH_RANGE, "flow record name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Generic_access_list_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, GENERIC_ACCESS_LIST_NAME_LENGTH_RANGE, "access-list name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Interface_descriptionContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, INTERFACE_DESCRIPTION_LENGTH_RANGE, "interface description");
  }

  /**
   * Returns a list of all the valid interface names, or {@link Optional#empty()} if any is invalid.
   */
  private @Nonnull Optional<List<String>> toInterfaceNames(
      ParserRuleContext messageCtx, List<Interface_nameContext> ctx) {
    ImmutableList.Builder<String> names = ImmutableList.builder();
    boolean valid = true;
    for (Interface_nameContext nameCtx : ctx) {
      Optional<String> name = toString(messageCtx, nameCtx);
      if (name.isPresent()) {
        names.add(name.get());
      } else {
        valid = false;
      }
    }
    return valid ? Optional.of(names.build()) : Optional.empty();
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Interface_nameContext ctx) {
    return toString(messageCtx, ctx, null);
  }

  /**
   * Convert specified interface name context into an interface name string, only allowing the
   * interface types specified in {@code allowedTypes}. If {@code allowedTypes} is {@code null},
   * then all known interface types are allowed.
   *
   * <p>If the interface name context cannot be converted, then {@link Optional#empty} is returned.
   */
  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx,
      Interface_nameContext ctx,
      @Nullable Set<CiscoNxosInterfaceType> allowedTypes) {
    String declaredName = getFullText(ctx);
    String prefix = ctx.prefix.getText();
    CiscoNxosInterfaceType type = toType(ctx.prefix);
    if (type == null) {
      warn(messageCtx, String.format("Unsupported interface type: %s", prefix));
      return Optional.empty();
    }
    if (allowedTypes != null && !allowedTypes.contains(type)) {
      warn(
          messageCtx,
          String.format("Unsupported interface type: %s, expected %s", prefix, allowedTypes));
      return Optional.empty();
    }
    String canonicalPrefix = getCanonicalInterfaceNamePrefix(prefix);
    if (canonicalPrefix == null) {
      warn(messageCtx, String.format("Unsupported interface name: %s", declaredName));
      return Optional.empty();
    }
    String middle = ctx.middle != null ? ctx.middle.getText() : "";
    String parentSuffix = ctx.parent_suffix != null ? ctx.parent_suffix.getText() : "";
    String lead = String.format("%s%s%s", canonicalPrefix, middle, parentSuffix);
    int first = toInteger(ctx.first);
    return Optional.of(String.format("%s%d", lead, first));
  }

  private @Nonnull Optional<List<String>> toStrings(
      ParserRuleContext messageCtx, Interface_rangeContext ctx) {
    String declaredName = getFullText(ctx);
    String prefix = ctx.iname.prefix.getText();

    CiscoNxosInterfaceType type = toType(ctx.iname.prefix);
    if (type == null) {
      warn(messageCtx, String.format("Unsupported interface type: %s", prefix));
      return Optional.empty();
    }
    String canonicalPrefix = getCanonicalInterfaceNamePrefix(prefix);
    if (canonicalPrefix == null) {
      warn(messageCtx, String.format("Unsupported interface name/range: %s", declaredName));
      return Optional.empty();
    }

    String middle = ctx.iname.middle != null ? ctx.iname.middle.getText() : "";
    String parentSuffix = ctx.iname.parent_suffix != null ? ctx.iname.parent_suffix.getText() : "";
    String lead = String.format("%s%s%s", canonicalPrefix, middle, parentSuffix);
    String parentInterface =
        parentSuffix.isEmpty()
            ? null
            : String.format(
                "%s%s%s", canonicalPrefix, middle, ctx.iname.parent_suffix.num.getText());
    int first = toInteger(ctx.iname.first);
    int last = ctx.last != null ? toInteger(ctx.last) : first;

    // flip first and last if range is backwards
    if (last < first) {
      int tmp = last;
      last = first;
      first = tmp;
    }

    // disallow subinterfaces except for physical and port-channel interfaces
    if (type != CiscoNxosInterfaceType.ETHERNET
        && type != CiscoNxosInterfaceType.PORT_CHANNEL
        && parentInterface != null) {
      warn(
          messageCtx, String.format("Cannot construct subinterface for interface type '%s'", type));
      return Optional.empty();
    }

    if (type == CiscoNxosInterfaceType.VLAN
        && !_currentValidVlanRange.contains(IntegerSpace.of(Range.closed(first, last)))) {
      warn(messageCtx, String.format("Vlan number(s) outside of range %s", _currentValidVlanRange));
      return Optional.empty();
    }

    // Validate port-channel numbers
    if (type == CiscoNxosInterfaceType.PORT_CHANNEL
        && !PORT_CHANNEL_RANGE.contains(IntegerSpace.of(Range.closed(first, last)))) {
      warn(
          messageCtx,
          String.format("port-channel number(s) outside of range %s", PORT_CHANNEL_RANGE));
      return Optional.empty();
    }

    return Optional.of(
        IntStream.range(first, last + 1)
            .mapToObj(i -> lead + i)
            .collect(ImmutableList.toImmutableList()));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Ip_access_list_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, GENERIC_ACCESS_LIST_NAME_LENGTH_RANGE, "ip access-list name");
  }

  /**
   * Returns a list of all the valid IP as-path access-list names, or {@link Optional#empty()} if
   * any is invalid.
   */
  private @Nonnull Optional<List<String>> toIpAsPathAccessListNames(
      ParserRuleContext messageCtx, List<Ip_as_path_access_list_nameContext> ctx) {
    ImmutableList.Builder<String> names = ImmutableList.builder();
    boolean valid = true;
    for (Ip_as_path_access_list_nameContext nameCtx : ctx) {
      Optional<String> name = toString(messageCtx, nameCtx);
      if (name.isPresent()) {
        names.add(name.get());
      } else {
        valid = false;
      }
    }
    return valid ? Optional.of(names.build()) : Optional.empty();
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Ip_as_path_access_list_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, IP_AS_PATH_ACCESS_LIST_NAME_LENGTH_RANGE, "ip as-path access-list name");
  }

  /**
   * Returns a list of all the valid IP community-list names, or {@link Optional#empty()} if any is
   * invalid.
   */
  private @Nonnull Optional<List<String>> toIpCommunityListNames(
      ParserRuleContext messageCtx, List<Ip_community_list_nameContext> ctx) {
    ImmutableList.Builder<String> names = ImmutableList.builder();
    boolean valid = true;
    for (Ip_community_list_nameContext nameCtx : ctx) {
      Optional<String> name = toString(messageCtx, nameCtx);
      if (name.isPresent()) {
        names.add(name.get());
      } else {
        valid = false;
      }
    }
    return valid ? Optional.of(names.build()) : Optional.empty();
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Ip_community_list_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, IP_COMMUNITY_LIST_NAME_LENGTH_RANGE, "ip community-list name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Ip_prefix_list_descriptionContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, IP_PREFIX_LIST_DESCRIPTION_LENGTH_RANGE, "ip prefix-list description");
  }

  /**
   * Returns a list of all the valid IP prefix-list names, or {@link Optional#empty()} if any is
   * invalid.
   */
  private @Nonnull Optional<List<String>> toIpPrefixListNames(
      ParserRuleContext messageCtx, List<Ip_prefix_list_nameContext> ctx) {
    ImmutableList.Builder<String> names = ImmutableList.builder();
    boolean valid = true;
    for (Ip_prefix_list_nameContext nameCtx : ctx) {
      Optional<String> name = toString(messageCtx, nameCtx);
      if (name.isPresent()) {
        names.add(name.get());
      } else {
        valid = false;
      }
    }
    return valid ? Optional.of(names.build()) : Optional.empty();
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Ip_prefix_list_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, IP_PREFIX_LIST_NAME_LENGTH_RANGE, "ip prefix-list name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Mac_access_list_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, GENERIC_ACCESS_LIST_NAME_LENGTH_RANGE, "mac access-list name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Object_group_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, OBJECT_GROUP_NAME_LENGTH_RANGE, "object-group name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Policy_map_cp_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx,
        ctx,
        POLICY_MAP_CONTROL_PLANE_NAME_LENGTH_RANGE,
        "policy-map type control-plane name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Policy_map_network_qos_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx,
        ctx,
        POLICY_MAP_NETWORK_QOS_NAME_LENGTH_RANGE,
        "policy-map type network-qos name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Policy_map_qos_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, POLICY_MAP_QOS_NAME_LENGTH_RANGE, "policy-map type qos name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Policy_map_queuing_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, POLICY_MAP_QUEUING_NAME_LENGTH_RANGE, "policy-map type queuing name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Route_map_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, ROUTE_MAP_NAME_LENGTH_RANGE, "route-map name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Router_eigrp_process_tagContext ctx) {
    Optional<String> procName =
        toStringWithLengthInSpace(
            messageCtx, ctx, EIGRP_PROCESS_TAG_LENGTH_RANGE, "EIGRP process tag");
    // EIGRP process tag is case-insensitive.
    return procName.map(name -> getPreferredName(name, ROUTER_EIGRP));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Router_isis_process_tagContext ctx) {
    Optional<String> procName =
        toStringWithLengthInSpace(
            messageCtx, ctx, ISIS_PROCESS_TAG_LENGTH_RANGE, "ISIS process tag");
    // ISIS process tag is case-insensitive.
    return procName.map(name -> getPreferredName(name, ROUTER_ISIS));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Router_ospf_nameContext ctx) {
    Optional<String> procName =
        toStringWithLengthInSpace(
            messageCtx, ctx, OSPF_PROCESS_NAME_LENGTH_RANGE, "OSPF process name");
    // OSPF process name is case-insensitive.
    return procName.map(name -> getPreferredName(name, ROUTER_OSPF));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Router_ospfv3_nameContext ctx) {
    Optional<String> procName =
        toStringWithLengthInSpace(
            messageCtx, ctx, OSPFV3_PROCESS_NAME_LENGTH_RANGE, "OSPFv3 process name");
    // OSPF process name is case-insensitive.
    return procName.map(name -> getPreferredName(name, ROUTER_OSPFV3));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Router_rip_process_idContext ctx) {
    Optional<String> procName =
        toStringWithLengthInSpace(messageCtx, ctx, RIP_PROCESS_ID_LENGTH_RANGE, "RIP process ID");
    // RIP process name is case-insensitive.
    return procName.map(name -> getPreferredName(name, ROUTER_RIP));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Snmp_communityContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, SNMP_COMMUNITY_LENGTH_RANGE, "SNMP community");
  }

  private @Nullable String toString(ParserRuleContext messageCtx, Static_route_nameContext ctx) {
    String name = ctx.getText();
    if (name.length() > StaticRoute.MAX_NAME_LENGTH) {
      warn(
          messageCtx,
          String.format(
              "Expected name <= %d characters,but got '%s'", StaticRoute.MAX_NAME_LENGTH, name));
      return null;
    }
    return name;
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Template_nameContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, BGP_TEMPLATE_NAME_LENGTH_RANGE, "bgp template name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Vrf_descriptionContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx, VRF_DESCRIPTION_LENGTH_RANGE, "VRF description");
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, Vrf_nameContext ctx) {
    Optional<String> vrfName =
        toStringWithLengthInSpace(messageCtx, ctx, VRF_NAME_LENGTH_RANGE, "VRF name");
    // VRF names are case-insensitive.
    return vrfName.map(name -> getPreferredName(name, VRF));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Vrf_non_default_nameContext ctx) {
    Optional<String> vrfName =
        toStringWithLengthInSpace(messageCtx, ctx, VRF_NAME_LENGTH_RANGE, "VRF name")
            // VRF names are case-insensitive.
            .map(name -> getPreferredName(name, VRF));

    if (vrfName.isPresent() && vrfName.get().equals(DEFAULT_VRF_NAME)) {
      warn(messageCtx, "Cannot use default VRF in this context");
      return Optional.empty();
    }
    return vrfName;
  }

  /**
   * Return the text of the provided {@code ctx} if its length is within the provided {@link
   * IntegerSpace lengthSpace}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<String> toStringWithLengthInSpace(
      ParserRuleContext messageCtx, ParserRuleContext ctx, IntegerSpace lengthSpace, String name) {
    String text = ctx.getText();
    if (!lengthSpace.contains(text.length())) {
      warn(
          messageCtx,
          String.format(
              "Expected %s with length in range %s, but got '%s'", name, lengthSpace, text));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  static @Nullable CiscoNxosInterfaceType toType(Interface_prefixContext ctx) {
    if (ctx.ETHERNET() != null) {
      return CiscoNxosInterfaceType.ETHERNET;
    } else if (ctx.LOOPBACK() != null) {
      return CiscoNxosInterfaceType.LOOPBACK;
    } else if (ctx.MGMT() != null) {
      return CiscoNxosInterfaceType.MGMT;
    } else if (ctx.PORT_CHANNEL() != null) {
      return CiscoNxosInterfaceType.PORT_CHANNEL;
    } else if (ctx.VLAN() != null) {
      return CiscoNxosInterfaceType.VLAN;
    }
    return null;
  }

  private @Nullable Integer toVlanId(ParserRuleContext messageCtx, Unreserved_vlan_idContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, _currentValidVlanRange, "VLAN id").orElse(null);
  }

  private @Nullable Integer toVlanId(ParserRuleContext messageCtx, Vlan_idContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, VLAN_RANGE, "VLAN id").orElse(null);
  }

  private @Nullable IntegerSpace toVlanIdRange(
      ParserRuleContext messageCtx, Unreserved_vlan_id_rangeContext ctx) {
    String rangeText = ctx.getText();
    IntegerSpace value = IntegerSpace.parse(rangeText);
    if (!_currentValidVlanRange.contains(value)) {
      warn(
          messageCtx,
          String.format(
              "Expected VLANs in range %s, but got '%s'", _currentValidVlanRange, rangeText));
      return null;
    }
    return value;
  }

  private @Nullable IntegerSpace toVlanIdRange(
      ParserRuleContext messageCtx, Vlan_id_rangeContext ctx) {
    String rangeText = ctx.getText();
    IntegerSpace value = IntegerSpace.parse(rangeText);
    if (!VLAN_RANGE.contains(value)) {
      warn(
          messageCtx,
          String.format("Expected VLANs in range %s, but got '%s'", VLAN_RANGE, rangeText));
      return null;
    }
    return value;
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _c.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      String msg = String.format("Unrecognized Line: %d: %s", line, lineText);
      _w.redFlag(msg + " SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY");
    }
  }

  @Override
  public void exitSysvlan_reserve(Sysvlan_reserveContext ctx) {
    Optional<Integer> maybeFirstVlan =
        toIntegerInSpace(ctx, ctx.first, FIRST_RESERVED_VLAN_RANGE, "first reserved VLAN ID");
    if (!maybeFirstVlan.isPresent()) {
      return;
    }
    int firstVlan = maybeFirstVlan.get();
    IntegerSpace newReservedRange = IntegerSpace.of(Range.closed(firstVlan, firstVlan + 128));
    // TODO: warn about/delete any existing VLAN assignments in the new reserved range.
    _c.setReservedVlanRange(newReservedRange);
    _currentValidVlanRange = VLAN_RANGE.difference(_c.getReservedVlanRange());
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }

    private Interface getOrAddInterface(Interface_nameContext ctx) {
        String canonicalIfaceName = getCanonicalInterfaceName(ctx.getText());
        Interface iface = _c.getInterfaces().get(canonicalIfaceName);
        if (iface == null) {
            iface = addInterface(canonicalIfaceName, ctx, false);
        }
        return iface;
    }

    private String getCanonicalInterfaceName(String ifaceName) {
        return _c.canonicalizeInterfaceName(ifaceName);
    }

    private Interface addInterface(String name, Interface_nameContext ctx, boolean explicit) {
        Interface newInterface = _c.getInterfaces().get(name);
        if (newInterface == null) {
            newInterface = Interface.newNonVlanInterface(name, null, CiscoNxosInterfaceType.ETHERNET);
            _c.getInterfaces().put(name, newInterface);
            initInterface(newInterface, ctx);
        } else {
            _w.pedantic("Interface: '" + name + "' altered more than once");
        }
        newInterface.setDeclaredNames(
                new ImmutableSortedSet.Builder<String>(naturalOrder())
                        .addAll(newInterface.getDeclaredNames())
                        .add(ctx.getText())
                        .build());
        if (explicit) {
            _currentInterfaces.add(newInterface);
        }
        return newInterface;
    }

    private void initInterface(Interface iface,Interface_nameContext ctx) {
        String nameAlpha = ctx.prefix.getText();
        String canonicalNamePrefix = CiscoNxosConfiguration.getCanonicalInterfaceNamePrefix(nameAlpha);
        String vrf =
                canonicalNamePrefix.equals(CiscoNxosConfiguration.MANAGEMENT_INTERFACE_PREFIX)
                        ? CiscoNxosConfiguration.MANAGEMENT_VRF_NAME
                        : Configuration.DEFAULT_VRF_NAME;
//        int mtu = Interface.getDefaultMtu();
        iface.setVrf(vrf);
        initVrf(vrf);
//        iface.setMtu(mtu);
    }


    private Vrf initVrf(String vrfName) {
        return _c.getVrfs().computeIfAbsent(vrfName, Vrf::new);
    }
}
