package org.jboss.forge.furnace.container.simple;

/**
 * Interface used to register a Service to be a singleton. The service has to follow the registration process defined in
 * the {@link Service}, after which only one instance of the service instance will be created for the whole deployment.
 * 
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 */
public interface SingletonService extends Service
{

}
