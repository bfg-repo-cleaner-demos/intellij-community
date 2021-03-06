/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.gradle.settings;

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Denis Zhdanov
 * @since 4/9/13 1:50 PM
 */
public class GradleExecutionSettings extends ExternalSystemExecutionSettings {

  private static final boolean USE_VERBOSE_GRADLE_API_BY_DEFAULT = Boolean.parseBoolean(System.getProperty("gradle.api.verbose"));

  private static final long serialVersionUID = 1L;

  @NotNull private final List<String> myResolverExtensions = ContainerUtilRt.newArrayList();
  @Nullable private final String myGradleHome;
  @Nullable private final String myServiceDirectory;

  private final int myDaemonXmx;

  private final boolean myUseWrapper;

  @Nullable private String myJavaHome;

  public GradleExecutionSettings(@Nullable String gradleHome, @Nullable String serviceDirectory, boolean wrapper) {
    this(gradleHome, serviceDirectory, wrapper, SystemProperties.getIntProperty("gradle.daemon.xmx.mb", 512));
  }
  
  public GradleExecutionSettings(@Nullable String gradleHome, @Nullable String serviceDirectory, boolean wrapper, int daemonXmx) {
    myGradleHome = gradleHome;
    myServiceDirectory = serviceDirectory;
    myUseWrapper = wrapper;
    myDaemonXmx = daemonXmx;
    setVerboseProcessing(USE_VERBOSE_GRADLE_API_BY_DEFAULT);
  }

  @Nullable
  public String getGradleHome() {
    return myGradleHome;
  }

  @Nullable
  public String getServiceDirectory() {
    return myServiceDirectory;
  }

  public boolean isUseWrapper() {
    return myUseWrapper;
  }

  @Nullable
  public String getJavaHome() {
    return myJavaHome;
  }

  public void setJavaHome(@Nullable String javaHome) {
    myJavaHome = javaHome;
  }

  @NotNull
  public List<String> getResolverExtensions() {
    return myResolverExtensions;
  }

  public void addResolverExtensionClass(@NotNull String className) {
    myResolverExtensions.add(className);
  }

  /**
   * @return    max heap memory limit (in MB) to use for gradle daemon (if any);
   *            non-positive value as an indication that default gradle value (1024) should be used instead
   */
  public int getDaemonXmx() {
    return myDaemonXmx;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (myGradleHome != null ? myGradleHome.hashCode() : 0);
    result = 31 * result + (myServiceDirectory != null ? myServiceDirectory.hashCode() : 0);
    result = 31 * result + (myUseWrapper ? 1 : 0);
    result = 31 * result + (myJavaHome != null ? myJavaHome.hashCode() : 0);
    result = 31 * result + myDaemonXmx;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) return false;

    GradleExecutionSettings that = (GradleExecutionSettings)o;

    if (myUseWrapper != that.myUseWrapper) return false;
    if (myDaemonXmx != that.myDaemonXmx) return false;
    if (myGradleHome != null ? !myGradleHome.equals(that.myGradleHome) : that.myGradleHome != null) return false;
    if (myJavaHome != null ? !myJavaHome.equals(that.myJavaHome) : that.myJavaHome != null) return false;
    if (myServiceDirectory != null ? !myServiceDirectory.equals(that.myServiceDirectory) : that.myServiceDirectory != null) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return "home: " + myGradleHome + ", use wrapper: " + myUseWrapper;
  }
}
