package com.devonfw.ide.sonarqube.common.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.java.Java;
import org.sonarsource.api.sonarlint.SonarLintSide;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class creates a quality profile containing the rules of this plugin plus additional rules from external repos.
 */
@SonarLintSide
public class DevonfwJavaProfile implements BuiltInQualityProfilesDefinition {

  private static final Set<String> FORBIDDEN_RULE_KEYS = new HashSet<>(Arrays.asList(
      /* squid repo */
      "S2076", "S2078", "S3318", "S2070", "S4142",
      /* findbugs repo */
      "VA_FORMAT_STRING_ARG_MISMATCH", "VA_FORMAT_STRING_MISSING_ARGUMENT", "VA_FORMAT_STRING_EXTRA_ARGUMENTS_PASSED",
      "VA_FORMAT_STRING_BAD_CONVERSION_FROM_ARRAY", "VA_FORMAT_STRING_BAD_ARGUMENT",
      "VA_FORMAT_STRING_BAD_CONVERSION_TO_BOOLEAN", "VA_FORMAT_STRING_NO_PREVIOUS_ARGUMENT", "VA_FORMAT_STRING_ILLEGAL",
      "VA_FORMAT_STRING_BAD_CONVERSION", "VA_FORMAT_STRING_EXPECTED_MESSAGE_FORMAT_SUPPLIED",
      /* fb-contrib repo */
      "SPP_NULL_CHECK_ON_MAP_SUBSET_ACCESSOR", "SPP_NULL_CHECK_ON_OPTIONAL", "SPP_USE_CONTAINSKEY"));

  private static final String DEVONFW_JAVA = "/com/devonfw/ide/sonarqube/common/rules/devon4j/devonfwJava.xml";

  private static final Logger logger = Logger.getGlobal();

  @Override
  public void define(Context context) {

    NewBuiltInQualityProfile devonfwJava = context.createBuiltInQualityProfile("devonfw Java", Java.KEY);
    NodeList ruleList = readQualityProfileXml();
    NodeList childrenOfRule;
    NewBuiltInActiveRule currentRule;
    String repoKey = null;
    String ruleKey = null;
    String severity = null;

    for (int i = 0; i < ruleList.getLength(); i++) {

      childrenOfRule = ruleList.item(i).getChildNodes();

      for (int j = 0; j < childrenOfRule.getLength(); j++) {

        switch (childrenOfRule.item(j).getNodeName()) {
          case "repositoryKey":
            repoKey = childrenOfRule.item(j).getTextContent();
            break;
          case "key":
            ruleKey = childrenOfRule.item(j).getTextContent();
            break;
          case "priority":
            severity = childrenOfRule.item(j).getTextContent();
            break;
        }

      }

      if (!FORBIDDEN_RULE_KEYS.contains(ruleKey)) {
        currentRule = devonfwJava.activateRule(repoKey, ruleKey);
        currentRule.overrideSeverity(severity);
      }

    }

    devonfwJava.done();

  }

  private NodeList readQualityProfileXml() {

    InputStream inputStream = DevonfwJavaProfile.class.getResourceAsStream(DEVONFW_JAVA);

    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = dbFactory.newDocumentBuilder();
      Document document = builder.parse(inputStream);
      return document.getElementsByTagName("rule");
    } catch (ParserConfigurationException pc) {
      logger.log(Level.WARNING, "There was a problem configuring the parser.");
      return null;
    } catch (IOException io) {
      io.printStackTrace();
      logger.log(Level.WARNING, "There was a problem reading the file.");
      return null;
    } catch (SAXException sax) {
      logger.log(Level.WARNING, "There was a problem parsing the file.");
      return null;
    }

  }

  /**
   * @return deprecated or unavailable rule keys that should not be added to the profile
   */
  public static Set<String> getForbiddenRuleKeys() {

    return DevonfwJavaProfile.FORBIDDEN_RULE_KEYS;
  }

}