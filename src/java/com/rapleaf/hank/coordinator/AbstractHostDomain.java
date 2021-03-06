package com.rapleaf.hank.coordinator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractHostDomain implements HostDomain {
  @Override
  public int compareTo(HostDomain arg0) {
    return Integer.valueOf(getDomainId()).compareTo(arg0.getDomainId());
  }

  @Override
  public HostDomainPartition getPartitionByNumber(int partNum)
      throws IOException {
    for (HostDomainPartition p : getPartitions()) {
      if (p.getPartNum() == partNum) {
        return p;
      }
    }
    return null;
  }

  @Override
  public Long getAggregateCount(String countID) throws IOException {
    long aggregateCount = 0;
    boolean notNull = false;
    for (HostDomainPartition hdp : getPartitions()) {
      Long currentCount = hdp.getCount(countID);
      if (currentCount != null) {
        notNull = true;
        aggregateCount += currentCount;
      }
    }
    if (notNull) {
      return aggregateCount;
    }
    return null;
  }

  @Override
  public Set<String> getAggregateCountKeys() throws IOException {
    Set<String> aggregateCountKeys = new HashSet<String>();
    for (HostDomainPartition hdp : getPartitions()) {
      aggregateCountKeys.addAll(hdp.getCountKeys());
    }
    return aggregateCountKeys;
  }
}
