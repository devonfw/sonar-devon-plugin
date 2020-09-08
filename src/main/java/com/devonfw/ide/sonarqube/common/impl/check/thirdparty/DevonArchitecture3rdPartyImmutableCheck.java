package com.devonfw.ide.sonarqube.common.impl.check.thirdparty;

import com.devonfw.ide.sonarqube.common.api.JavaType;
import com.devonfw.ide.sonarqube.common.impl.check.DevonArchitecture3rdPartyCheck;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * {@link DevonArchitecture3rdPartyCheck} verifying that Immutable is used from the proper (org.hibernate.annotations) package.
 */
@Rule(key = "E8", name = "devonfw 3rd Party Immutable Check", //
    priority = Priority.CRITICAL, tags = { "architecture-violation", "devonfw", "thirdparty" })
public class DevonArchitecture3rdPartyImmutableCheck extends DevonArchitecture3rdPartyCheck {

    @Override protected String checkDependency(JavaType source, JavaType target) {

        String sourceSimpleName = source.getSimpleName();
        String targetFullName = target.toString();
        if (sourceSimpleName.contains("Entity") && targetFullName.equals("javax.annotation.concurrent.Immutable")) {
            return "Use Immutable from org.hibernate.annotations.Immutable in Entity class.";
        }
        return null;
    }
}
