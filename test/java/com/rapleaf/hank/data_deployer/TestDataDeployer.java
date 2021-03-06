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
package com.rapleaf.hank.data_deployer;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import com.rapleaf.hank.config.DataDeployerConfigurator;
import com.rapleaf.hank.coordinator.AbstractHostDomain;
import com.rapleaf.hank.coordinator.Coordinator;
import com.rapleaf.hank.coordinator.DomainGroup;
import com.rapleaf.hank.coordinator.DomainGroupVersion;
import com.rapleaf.hank.coordinator.Host;
import com.rapleaf.hank.coordinator.HostDomain;
import com.rapleaf.hank.coordinator.HostDomainPartition;
import com.rapleaf.hank.coordinator.MockDomainGroup;
import com.rapleaf.hank.coordinator.MockDomainGroupVersion;
import com.rapleaf.hank.coordinator.MockHost;
import com.rapleaf.hank.coordinator.MockHostDomainPartition;
import com.rapleaf.hank.coordinator.MockRing;
import com.rapleaf.hank.coordinator.MockRingGroup;
import com.rapleaf.hank.coordinator.PartDaemonAddress;
import com.rapleaf.hank.coordinator.Ring;
import com.rapleaf.hank.coordinator.RingGroup;
import com.rapleaf.hank.coordinator.mock.MockCoordinator;

public class TestDataDeployer extends TestCase {
  public class MockRingGroupUpdateTransitionFunction implements RingGroupUpdateTransitionFunction {
    public RingGroup calledWithRingGroup;

    @Override
    public void manageTransitions(RingGroup ringGroup) {
      calledWithRingGroup = ringGroup;
    }

  }

  public void testTriggersUpdates() throws Exception {
    final MockDomainGroup domainGroup = new MockDomainGroup("myDomainGroup") {
      @Override
      public DomainGroupVersion getLatestVersion() {
        return new MockDomainGroupVersion(null, null, 2);
      }
    };

    final MockHostDomainPartition mockHostDomainPartitionConfig = new MockHostDomainPartition(0, 0, 1);

    final MockHost mockHostConfig = new MockHost(new PartDaemonAddress("locahost", 12345)) {
      @Override
      public Set<HostDomain> getAssignedDomains() throws IOException {
        return Collections.singleton((HostDomain)new AbstractHostDomain() {
          @Override
          public HostDomainPartition addPartition(int partNum, int initialVersion) {return null;}

          @Override
          public int getDomainId() {return 0;}

          @Override
          public Set<HostDomainPartition> getPartitions() {
            return Collections.singleton((HostDomainPartition)mockHostDomainPartitionConfig);
          }
        });
      }
    };

    final MockRing mockRingConfig = new MockRing(null, null, 1, null) {
      @Override
      public Set<Host> getHosts() {
        return Collections.singleton((Host)mockHostConfig);
      }
    };

    final MockRingGroup mockRingGroupConf = new MockRingGroup(null, "myRingGroup", Collections.EMPTY_SET) {
      @Override
      public DomainGroup getDomainGroup() {
        return domainGroup;
      }

      @Override
      public Integer getCurrentVersion() {
        return 1;
      }

      @Override
      public Set<Ring> getRings() {
        return Collections.singleton((Ring)mockRingConfig);
      }
    };

    DataDeployerConfigurator mockConfig = new DataDeployerConfigurator() {
      @Override
      public long getSleepInterval() {
        return 100;
      }

      @Override
      public String getRingGroupName() {
        return "myRingGroup";
      }

      @Override
      public Coordinator getCoordinator() {
        return new MockCoordinator(){
          @Override
          public RingGroup getRingGroupConfig(String ringGroupName) {
            return mockRingGroupConf;
          }
        };
      }
    };
    MockRingGroupUpdateTransitionFunction mockTransFunc = new MockRingGroupUpdateTransitionFunction();
    DataDeployer daemon = new DataDeployer(mockConfig, mockTransFunc);
    daemon.processUpdates(mockRingGroupConf, domainGroup);

    assertNull(mockTransFunc.calledWithRingGroup);
    assertEquals(2, mockRingGroupConf.updateToVersion);
    assertEquals(Integer.valueOf(2), mockRingConfig.updatingToVersion);
    assertEquals(2, mockHostDomainPartitionConfig.updatingToVersion);
  }

  public void testKeepsExistingUpdatesGoing() throws Exception {
    final MockDomainGroup domainGroup = new MockDomainGroup("myDomainGroup") {
      @Override
      public DomainGroupVersion getLatestVersion() {
        return new MockDomainGroupVersion(null, null, 2);
      }
    };

    final MockRingGroup mockRingGroupConf = new MockRingGroup(null, "myRingGroup", Collections.EMPTY_SET) {
      @Override
      public DomainGroup getDomainGroup() {
        return domainGroup;
      }

      @Override
      public Integer getCurrentVersion() {
        return 1;
      }

      @Override
      public boolean isUpdating() {
        return true;
      }
    };

    DataDeployerConfigurator mockConfig = new DataDeployerConfigurator() {
      @Override
      public long getSleepInterval() {
        return 100;
      }

      @Override
      public String getRingGroupName() {
        return "myRingGroup";
      }

      @Override
      public Coordinator getCoordinator() {
        return new MockCoordinator(){
          @Override
          public RingGroup getRingGroupConfig(String ringGroupName) {
            return mockRingGroupConf;
          }
        };
      }
    };
    MockRingGroupUpdateTransitionFunction mockTransFunc = new MockRingGroupUpdateTransitionFunction();
    DataDeployer daemon = new DataDeployer(mockConfig, mockTransFunc);
    daemon.processUpdates(mockRingGroupConf, domainGroup);

    assertEquals(mockRingGroupConf, mockTransFunc.calledWithRingGroup);
  }
}
