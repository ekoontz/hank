package com.rapleaf.hank.coordinator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractRing implements Ring {
  private final int ringNum;
  private final RingGroup ringGroupConfig;

  protected AbstractRing(int ringNum, RingGroup ringGroupConfig) {
    this.ringNum = ringNum;
    this.ringGroupConfig = ringGroupConfig;
  }

  @Override
  public void commandAll(HostCommand command) throws IOException {
    for (Host hc : getHosts()) {
      hc.enqueueCommand(command);
    }
  }

  @Override
  public Set<Host> getHostsForDomainPartition(int domainId, int partition) throws IOException {
    Set<Host> results = new HashSet<Host>();
    for (Host hc : getHosts()) {
      HostDomain domainById = hc.getDomainById(domainId);
      for (HostDomainPartition hdpc : domainById.getPartitions()) {
        if (hdpc.getPartNum() == partition) {
          results.add(hc);
          break;
        }
      }
    }
    return results;
  }

  @Override
  public Set<Host> getHostsInState(HostState state) throws IOException {
    Set<Host> results = new HashSet<Host>();
    for (Host hostConfig: getHosts()) {
      if (hostConfig.getState() == state) {
        results.add(hostConfig);
      }
    }
    return results;
  }

  @Override
  public final RingGroup getRingGroup() {
    return ringGroupConfig;
  }

  @Override
  public final int getRingNumber() {
    return ringNum;
  }

  @Override
  public final boolean isUpdatePending() {
    return getUpdatingToVersionNumber() != null;
  }

  @Override
  public Set<Integer> getUnassignedPartitions(Domain domain) throws IOException {
    Integer domainId = getRingGroup().getDomainGroup().getDomainId(domain.getName());

    Set<Integer> unassignedParts = new HashSet<Integer>();
    for (int i = 0; i < domain.getNumParts(); i++) {
      unassignedParts.add(i);
    }

    for (Host hc : getHosts()) {
      HostDomain hdc = hc.getDomainById(domainId);
      if (hdc == null) {
        continue;
      }
      for (HostDomainPartition hdpc : hdc.getPartitions()) {
        unassignedParts.remove(hdpc.getPartNum());
      }
    }

    return unassignedParts;
  }

  @Override
  public int compareTo(Ring other) {
    return Integer.valueOf(ringNum).compareTo(other.getRingNumber());
  }
}
