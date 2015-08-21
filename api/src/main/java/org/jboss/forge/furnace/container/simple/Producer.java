/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple;

/**
 * Interface used to register a Service to be a producer.
 * 
 * <p>
 * To register a {@link Producer}, register it as a {@link Service} or {@link SingletonService}.
 * <p>
 * 
 * <pre>
 * public class ExampleService extends ProducerService<MyObject>
 * {
 *    &#64;Override
 *    public MyObject get()
 *    {
 *       // r MyObject
 *    }
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
 * 
 * <h2>Warning: from JDK 1.8.0_20 onwards, javac changed its behavior in order to check for transitive references.
 * Producer services must NEVER be directly accessed outside of the addon in which they are defined. See
 * <a href="https://issues.jboss.org/browse/FORGE-2019">this link</a> for more information</h2>
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface Producer<T>
{
   T get();
}