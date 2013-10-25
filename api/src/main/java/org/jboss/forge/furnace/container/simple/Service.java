/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple;

/**
 * Reference interface. Service types are not required to extend this class, but may do so for reference purposes.
 * 
 * <p>
 * To register a type as a service, a file must be created with the name
 * <code>META-INF/services/org.jboss.forge.furnace.container.simple.Service</code>, and each service type name must be
 * added on a separate line:
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
 */
public interface Service
{

}
