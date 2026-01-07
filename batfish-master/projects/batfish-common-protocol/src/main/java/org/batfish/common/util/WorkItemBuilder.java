package org.batfish.common.util;

import org.batfish.common.BfConsts;
import org.batfish.common.WorkItem;

import java.util.List;

public class WorkItemBuilder {

  public static WorkItem getWorkItemAnswerQuestion(
      String questionName,
      String containerName,
      String testrigName,
      String deltaTestrig,
      boolean isDifferential) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_ANSWER, "");
    wItem.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
    if (isDifferential) {
      wItem.addRequestParam(BfConsts.ARG_DIFFERENTIAL, "");
    }
    if (deltaTestrig != null) {
      wItem.addRequestParam(BfConsts.ARG_DELTA_TESTRIG, deltaTestrig);
    }
    return wItem;
  }

  public static WorkItem getWorkItemGenerateDataPlane(String network, String snapshot) {
    WorkItem wItem = new WorkItem(network, snapshot);
    wItem.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
    return wItem;
  }


  public static WorkItem getWorkItemVerifySingleReachability(String network, String snapshot, List<String> parameters) {
    WorkItem wItem = new WorkItem(network, snapshot);
    wItem.addRequestParam(BfConsts.VERIFY_SINGLE_REACHABILITY, "true");
    wItem.addRequestParam("src-router", parameters.get(0));
    wItem.addRequestParam("src-vrf", parameters.get(1));
    wItem.addRequestParam("dst-router", parameters.get(2));
    wItem.addRequestParam("dst-vrf", parameters.get(3));
    wItem.addRequestParam("dst-prefix", parameters.get(4));
    return wItem;
  }

  public static WorkItem getWorkItemComputeViolationTree(String network, String snapshot) {
    WorkItem wItem = new WorkItem(network, snapshot);
    wItem.addRequestParam(BfConsts.COMPUTE_VIOLATION_TREE, "true");
    return wItem;
  }

  public static WorkItem getWorkItemVerifyAllReachability(String network, String snapshot) {
    WorkItem wItem = new WorkItem(network, snapshot);
    wItem.addRequestParam(BfConsts.VERIFY_ALL_REACHABILITY, "true");
    return wItem;
  }

  public static WorkItem getWorkItemComputeControlPlane(String network, String snapshot) {
    WorkItem wItem = new WorkItem(network, snapshot);
    wItem.addRequestParam(BfConsts.COMPUTE_CONTROL_PLANE, "true");
    return wItem;
  }

  public static WorkItem getWorkItemParse(String containerName, String testrigName) {
    WorkItem wItem = new WorkItem(containerName, testrigName);
    wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, "");
    wItem.addRequestParam(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, "");
    wItem.addRequestParam(BfConsts.COMMAND_INIT_INFO, "");
    wItem.addRequestParam(BfConsts.ARG_IGNORE_MANAGEMENT_INTERFACES, "");
    return wItem;
  }

  public static boolean isAnsweringWorkItem(WorkItem workItem) {
    return workItem.getRequestParams().containsKey(BfConsts.COMMAND_ANSWER);
  }

  public static boolean isDataplaningWorkItem(WorkItem workItem) {
    return workItem.getRequestParams().containsKey(BfConsts.COMMAND_DUMP_DP);
  }

  public static boolean isDifferential(WorkItem workItem) {
    return workItem.getRequestParams().containsKey(BfConsts.ARG_DIFFERENTIAL);
  }

  public static boolean isParsingWorkItem(WorkItem workItem) {
    return workItem.getRequestParams().containsKey(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC);
  }

  public static String getQuestionName(WorkItem workItem) {
    return workItem.getRequestParams().get(BfConsts.ARG_QUESTION_NAME);
  }
  public static WorkItem getWorkItemGetViolationTree(String network, String snapshot, List<String> parameters) {
    WorkItem wItem = new WorkItem(network, snapshot);
    wItem.addRequestParam(BfConsts.GET_VIOLATION_TREE, "true");
    wItem.addRequestParam("src-router", parameters.get(0));
    wItem.addRequestParam("src-vrf", parameters.get(1));
    wItem.addRequestParam("dst-router", parameters.get(2));
    wItem.addRequestParam("dst-vrf", parameters.get(3));
    wItem.addRequestParam("dst-prefix", parameters.get(4));
    return wItem;
  }
  public static String getReferenceSnapshotName(WorkItem workItem) {
    return workItem.getRequestParams().get(BfConsts.ARG_DELTA_TESTRIG);
  }
}
