# MEV: Scalable and Interpretable Overlay Network Checking via Ensemble Verification

## 🌟 Introduction
MEV is a control plane verifier specifically designed for overlay/underlay networks, as proposed in our [**CoNEXT '25 paper**](https://doi.org/10.1145/3768974) and [**SIGCOMM '24 poster**](https://dl.acm.org/doi/10.1145/3672202.3673722). 

MEV introduces **Ensemble Verification**, a novel paradigm that enables:
- **Independent Reasoning:** Decoupled analysis of routing behaviors within each instance or protocol.
- **Hierarchical Verification:** Independent reasoning of forwarding behaviors within each virtual network.
- **Root Cause Analysis:** Efficient identification of policy violations through violation trees.

This artifact is a partial implementation of MEV, built on top of the [Batfish](https://github.com/batfish/batfish) framework to leverage its robust multi-vendor configuration parsing capabilities.

## 📂 Code Structure
The core MEV logic is integrated into the Batfish framework. Below is the directory structure:

- `experiment-network/` : Partial experiment network used in the paper.
- `batfish-master/projects/batfish-common-protocol/src/main/java/org/batfish/MEVNEW/`
    - `EnsembleModel/` : Code for constructing the ensemble control plane model and control plane reasoning.
    - `EnsembleVerification/` : Code for ensemble reasoning of forwarding behaviors and verification results.
    - `ManiProcedure/` : Code for the main procedures and orchestration of MEV.
- `README.md` : Instructions and usage guide for this artifact.

## ⚙️ Environment Setup
> **Note:** This release is built on top of Batfish. Please ensure you can build and run Batfish first.

1. **Prerequisites:** Please refer to the official [Batfish building and running instructions](https://github.com/batfish/batfish/tree/master/docs/building_and_running) (requires JDK 11+, Maven, etc.).
2. **Compilation:** After the environment is set up, build the project using Maven to ensure all MEV components are compiled correctly.

## 🛠️ Usage Workflow
MEV provides an interactive **Command-Line Interface (CLI)** for end-to-end network verification, from configuration parsing to root-cause analysis.

### Step 1: Initialize Snapshot
Parse vendor-specific configuration files and initialize a network snapshot.
```bash
# Syntax: init-snapshot <path-to-testrig>
MEV> init-snapshot ./testrig/overlay-case
```

### Step 2: Compute Control Plane
Compute the converged control-plane state using the ensemble model.
```bash
MEV> compute-control-plane
```

### Step 3: Verify Data Plane Reachability
MEV supports both global and flow-specific reachability verification.

#### 3.1 Verify All-Pairs Reachability
Check reachability across all source–destination pairs in the network.
```bash
MEV> verify-all-reachability
```

#### 3.2 Verify Specific Reachability
Verify whether a specific packet flow is reachable.
Example: Source `ce1` (VRF: `slice0001`) to Destination `ce2` (VRF: `slice0001`) for prefix `2.2.2.2/32`.
```bash
# Syntax: verify-single-reachability <src_node> <src_ctx> <dst_node> <dst_ctx> <prefix>
MEV> verify-single-reachability ce1 slice0001 ce2 slice0001 2.2.2.2/32
```

### Step 4: Compute Violation Tree
When violations are detected, compute the violation tree to identify root causes.
```bash
MEV> compute-violation-tree
```

### Step 5: Inspect a Specific Violation
Retrieve and inspect the detailed violation tree for a specific problematic flow.
```bash
# Syntax: get-violation-tree <src_node> <src_ctx> <dst_node> <dst_ctx> <prefix>
MEV> get-violation-tree ce1 slice0001 ce2 slice0001 2.2.2.2/32
```

## 📝 Notice
- **Implementation:** The MEV codebase currently exceeds 9,000 lines. We are actively expanding CLI support to provide more comprehensive access to its internal verification functions. Version 2.0, featuring enhanced functionality and performance, is on the way—stay tuned.

---
