/**
 *  Copyright 2011 Rapleaf
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.rapleaf.hank.coordinator.zk;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;

import com.rapleaf.hank.coordinator.AbstractHostDomain;
import com.rapleaf.hank.coordinator.HostDomain;
import com.rapleaf.hank.coordinator.HostDomainPartition;
import com.rapleaf.hank.zookeeper.ZooKeeperPlus;

public class ZkHostDomain extends AbstractHostDomain {
  private final ZooKeeperPlus zk;
  private final String root;
  private final int domainId;

  public ZkHostDomain(ZooKeeperPlus zk, String partsRoot, int domainId) {
    this.zk = zk;
    this.domainId = domainId;
    this.root = partsRoot + "/" + (domainId & 0xff);
  }

  @Override
  public int getDomainId() {
    return domainId;
  }

  @Override
  public Set<HostDomainPartition> getPartitions() throws IOException {
    List<String> partStrs;
    try {
      partStrs = zk.getChildren(root, false);
    } catch (Exception e) {
      throw new IOException(e);
    }
    Set<HostDomainPartition> results = new HashSet<HostDomainPartition>();
    for (String partStr : partStrs) {
      results.add(new ZkHostDomainPartition(zk, root + "/" + partStr));
    }
    return results;
  }

  public static HostDomain create(ZooKeeperPlus zk, String partsRoot,
      int domainId) throws IOException {
    try {
      zk.create(partsRoot + "/" + (domainId & 0xff), null, Ids.OPEN_ACL_UNSAFE,
          CreateMode.PERSISTENT);
      return new ZkHostDomain(zk, partsRoot, domainId);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public HostDomainPartition addPartition(int partNum, int initialVersion)
      throws IOException {
    return ZkHostDomainPartition.create(zk, root, partNum, initialVersion);
  }
}