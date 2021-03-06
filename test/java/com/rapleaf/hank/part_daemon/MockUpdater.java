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
package com.rapleaf.hank.part_daemon;

import java.io.IOException;
import java.util.Set;

import com.rapleaf.hank.storage.Updater;

public class MockUpdater implements Updater {
  private boolean updated = false;
  public Integer updatedToVersion = null;
  public Set<Integer> excludeVersions;

  @Override
  public void update(int toVersion, Set<Integer> excludeVersions) throws IOException {
    updatedToVersion = toVersion;
    this.excludeVersions = excludeVersions;
    setUpdated(true);
  }

  public void setUpdated(boolean updated) {
    this.updated = updated;
  }

  public boolean isUpdated() {
    return updated;
  }
}
