/*
 * Copyright 2021 The playce-roro-v3 Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Jan 12, 2022	    First Draft.
 */
package io.playce.roro.app.asmt.java.policy;

import java.util.List;
import java.util.regex.Pattern;

/**
 * <pre>
 * Assessment 기본 정책을 포함하고 있는 클래스
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0.0
 */
public class Policy {

    private static final String EJB = "javax\\.ejb\\.";
    private static final String JCA = "javax\\.resource\\.";
    private static final String JMS = "javax\\.jms\\.";
    private static final String JNDI = "javax\\.naming\\.";
    private static final String JPA = "javax\\.persistence\\.";
    private static final String JTA = "javax\\.transaction\\.";
    private static final String SQL = "java\\.sql\\.";
    private static final String SPRING_EJB = "org\\.springframework\\.ejb\\.";
    private static final String SPRING_JNDI = "org\\.springframework\\.jndi\\.";
    private static final String WEB_LOGIC = "weblogic\\.";
    private static final String WEB_SPHERE1 = "com\\.ibm\\.websphere\\.";
    private static final String WEB_SPHERE2 = "com\\.ibm\\.wsspi\\.";
    private static final String JBOSS = "org\\.jboss\\.";
    private static final String JEUS = "jeus\\.";

    private static final String SERVLET1 = "extends HttpServlet";
    private static final String SERVLET2 = "extends javax\\.servlet\\.http\\.HttpServlet";
    private static final String CONTROLLER = "@Controller";
    private static final String REST_CONTROLLER = "@RestController";

    private static final String JDBC = "jdbc:";
    private static final String JNDI1 = "DataSource.*\\.lookup";
    private static final String JNDI2 = "name.*jndiName.*value";
    private static final String JNDI3 = "\\.getDataSource\\(";
    private static final String NOT_JNDI_PATTERN = ".*\\.getDataSource\\(\\).*";

    //private static final String IP_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    private static final String IP_PATTERN = ".*(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(?!\\d+).*";
    private static final String NOT_IP_PATTERN = ".*(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){4}.*";
    private static final String IP_PORT_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(?!\\d+)(:(?![7-9]\\d{4})(?!6[6-9]\\d{3})(?!65[6-9]\\d{2})(?!655[4-9]\\d)(?!6553[6-9])(?!0+)(\\d{1,5}))?";
    // private static final String HTTP_PATTERN = "(https?|wss?):\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)";
    private static final String HTTP_PATTERN = "(https?|wss?):\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(:(?![7-9]\\d{4})(?!6[6-9]\\d{3})(?!65[6-9]\\d{2})(?!655[4-9]\\d)(?!6553[6-9])(?!0+)(\\d{1,5}))?";
    private static final String LOCALHOST_PORT_PATTERN = "(localhost)(:(?![7-9]\\d{4})(?!6[6-9]\\d{3})(?!65[6-9]\\d{2})(?!655[4-9]\\d)(?!6553[6-9])(?!0+)(\\d{1,5}))?";

    private static Pattern apiPattern;
    private static Pattern servletPattern;
    private static Pattern jdbcPattern;
    private static Pattern jndiPattern;
    private static Pattern notJndiPattern;
    private static Pattern ipPattern;
    private static Pattern notIpPattern;
    private static Pattern ipPortPattern;
    private static Pattern httpPattern;
    private static Pattern localhostPortPattern;

    static {
        StringBuilder regex = new StringBuilder("(");
        regex.append(".*").append(EJB).append(".*|");
        regex.append(".*").append(JCA).append(".*|");
        regex.append(".*").append(JMS).append(".*|");
        regex.append(".*").append(JNDI).append(".*|");
        regex.append(".*").append(JPA).append(".*|");
        regex.append(".*").append(JTA).append(".*|");
        regex.append(".*").append(SQL).append(".*|");
        regex.append(".*").append(SPRING_EJB).append(".*|");
        regex.append(".*").append(SPRING_JNDI).append(".*|");
        regex.append(".*").append(WEB_LOGIC).append(".*|");
        regex.append(".*").append(WEB_SPHERE1).append(".*|");
        regex.append(".*").append(WEB_SPHERE2).append(".*|");
        regex.append(".*").append(JBOSS).append(".*|");
        regex.append(".*").append(JEUS).append(".*").append(")");

        apiPattern = Pattern.compile(regex.toString());

        regex = new StringBuilder("(");
        regex.append(".*").append(SERVLET1).append(".*|");
        regex.append(".*").append(SERVLET2).append(".*|");
        regex.append(".*").append(CONTROLLER).append(".*|");
        regex.append(".*").append(REST_CONTROLLER).append(".*").append(")");

        servletPattern = Pattern.compile(regex.toString());

        regex = new StringBuilder("(");
        regex.append(".*").append(JDBC).append(".*").append(")");

        jdbcPattern = Pattern.compile(regex.toString());

        regex = new StringBuilder("(");
        regex.append(".*").append(JNDI1).append(".*|");
        regex.append(".*").append(JNDI2).append(".*|");
        regex.append(".*").append(JNDI3).append(".*").append(")");

        jndiPattern = Pattern.compile(regex.toString());

        notJndiPattern = Pattern.compile(NOT_JNDI_PATTERN);

        ipPattern = Pattern.compile(IP_PATTERN);

        notIpPattern = Pattern.compile(NOT_IP_PATTERN);

        ipPortPattern = Pattern.compile(IP_PORT_PATTERN);

        httpPattern = Pattern.compile(HTTP_PATTERN);

        localhostPortPattern = Pattern.compile(LOCALHOST_PORT_PATTERN);
    }

    private Pattern etcPattern;

    /**
     * Instantiates a new Policy.
     *
     * @param patterns the patterns
     */
    public Policy(List<String> patterns) {
        setCustomPloicy(patterns);
    }

    /**
     * Sets custom ploicy.
     *
     * @param patterns the patterns
     */
    public void setCustomPloicy(List<String> patterns) {
        StringBuilder regex = new StringBuilder("(");

        for (String pattern : patterns) {
            regex.append(".*").append(pattern.replaceAll("\\.", "\\\\.")).append(".*|");
        }
        regex.deleteCharAt(regex.toString().length() - 1);
        regex.append(")");

        if (patterns.size() > 0) {
            etcPattern = Pattern.compile(regex.toString());
        }
    }

    /**
     * Gets api pattern.
     *
     * @return the api pattern
     */
    public Pattern getApiPattern() {
        return apiPattern;
    }

    /**
     * Gets servlet pattern.
     *
     * @return the servlet pattern
     */
    public Pattern getServletPattern() {
        return servletPattern;
    }

    /**
     * Gets jdbc pattern.
     *
     * @return the jdbc pattern
     */
    public Pattern getJdbcPattern() {
        return jdbcPattern;
    }

    /**
     * Gets jndi pattern.
     *
     * @return the jndi pattern
     */
    public Pattern getJndiPattern() {
        return jndiPattern;
    }

    /**
     * Gets ip pattern.
     *
     * @return the ip pattern
     */
    public Pattern getIpPattern() {
        return ipPattern;
    }

    /**
     * Gets Not ip pattern.
     *
     * @return the Not ip pattern
     */
    public Pattern getNotIpPattern() {
        return notIpPattern;
    }

    /**
     * Gets etc pattern.
     *
     * @return the etc pattern
     */
    public Pattern getEtcPattern() {
        return etcPattern;
    }

    /**
     * Gets Not jndi pattern.
     *
     * @return the Not jndi pattern
     */
    public Pattern getNotJndiPattern() {
        return notJndiPattern;
    }

    /**
     * Gets IP:Port pattern.
     *
     * @return the IP:Port pattern
     */
    public Pattern getIpPortPattern() {
        return ipPortPattern;
    }

    /**
     * Gets HTTP pattern.
     *
     * @return the HTTP pattern
     */
    public Pattern getHttpPattern() {
        return httpPattern;
    }

    /**
     * Gets localhost port pattern.
     *
     * @return the localhost port pattern
     */
    public Pattern getLocalhostPortPattern() {
        return localhostPortPattern;
    }
}
//end of Policy.java
