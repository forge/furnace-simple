package org.jboss.forge.furnace.container.simple;

/**
 * Interface used to register a Service to be a singleton. Only one instance of the service instance will be created for
 * the whole deployment.
 * 
 * <p>
 * To register a type as a service, a file must be created with the name
 * <code>META-INF/services/org.jboss.forge.furnace.container.simple.SingletonService</code>, and each service type name
 * must be added to this file on a separate line:
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
 * META-INF/services/org.jboss.forge.furnace.container.simple.SingletonService
 * -------
 * org.example.ExampleService
 * org.example.ExampleService2
 * org.my.custom.MyService
 * -------
 * </pre>
 * 
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface SingletonService
{

}