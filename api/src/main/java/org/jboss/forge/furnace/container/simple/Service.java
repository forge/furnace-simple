/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple;

/**
 * Reference interface. Users are discouraged to implement this interface in their services, as it may not compile on
 * JDK 1.8.0_20+ due to javac changes (https://issues.jboss.org/browse/FORGE-2019).
 * 
 * <p>
 * To register a type as a service, a file must be created with the name
 * <code>META-INF/services/org.jboss.forge.furnace.container.simple.Service</code>, and each service type name must be
 * added to this file on a separate line:
 * <p>
 * 
 * <pre>
 * public class ExampleService
 * {
 *    // ...
 * }
 * </pre>
 * <p>
 * Example registration file:
 * </p>
 * 
 * <pre>
 * META-INF/services/org.jboss.forge.furnace.container.simple.Service
 * -------
 * org.example.ExampleService
 * org.example.ExampleService2
 * org.my.custom.MyService
 * -------
 * </pre>
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 * @deprecated from JDK 1.8.0_20 onwards, javac changed its behavior in order to check for transitive references.
 *             Implementing this interface is discouraged. See <a href="https://issues.jboss.org/browse/FORGE-2019">this
 *             link</a> for more information
 *             
 */
@Deprecated
public interface Service
{

}
