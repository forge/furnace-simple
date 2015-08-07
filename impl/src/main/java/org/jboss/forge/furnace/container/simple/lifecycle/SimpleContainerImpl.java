/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.lifecycle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.container.simple.EventListener;
import org.jboss.forge.furnace.container.simple.Service;
import org.jboss.forge.furnace.container.simple.SingletonService;
import org.jboss.forge.furnace.container.simple.events.SimpleEventManagerImpl;
import org.jboss.forge.furnace.container.simple.impl.SimpleServiceRegistry;
import org.jboss.forge.furnace.event.EventManager;
import org.jboss.forge.furnace.event.PostStartup;
import org.jboss.forge.furnace.event.PreShutdown;
import org.jboss.forge.furnace.lifecycle.AddonLifecycleProvider;
import org.jboss.forge.furnace.lifecycle.ControlType;
import org.jboss.forge.furnace.spi.ServiceRegistry;
import org.jboss.forge.furnace.util.ClassLoaders;

/**
 * Implements a fast and simple {@link AddonLifecycleProvider} for the {@link Furnace} runtime. Allows {@link Service}
 * and {@link EventListener} registration.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SimpleContainerImpl implements AddonLifecycleProvider
{
   private static final Logger log = Logger.getLogger(SimpleContainerImpl.class.getName());

   private Furnace furnace;
   private EventManager eventManager;

   @Override
   public void initialize(Furnace furnace, AddonRegistry registry, Addon self) throws Exception
   {
      this.furnace = furnace;
   }

   @Override
   public void start(Addon addon) throws Exception
   {
      SimpleContainer.start(addon, furnace);
      this.eventManager = new SimpleEventManagerImpl(addon);
   }

   @Override
   public void stop(Addon addon) throws Exception
   {
      SimpleContainer.stop(addon);
   }

   @Override
   public EventManager getEventManager(Addon addon)
   {
      return eventManager;
   }

   @Override
   public ServiceRegistry getServiceRegistry(Addon addon) throws Exception
   {
      Set<Class<?>> serviceTypes = locateServices(addon, Service.class);
      Set<Class<?>> singletonServiceTypes = locateServices(addon, SingletonService.class);
      return new SimpleServiceRegistry(furnace, addon, serviceTypes, singletonServiceTypes);
   }

   private static Set<Class<?>> locateServices(Addon addon, Class<?> serviceType) throws IOException
   {
      Enumeration<URL> resources = addon.getClassLoader().getResources("/META-INF/services/" + serviceType.getName());
      Set<Class<?>> serviceTypes = new HashSet<>();
      while (resources.hasMoreElements())
      {
         URL resource = resources.nextElement();
         try (InputStream stream = resource.openStream();
                  BufferedReader reader = new BufferedReader(new InputStreamReader(stream)))
         {
            String serviceName;
            while ((serviceName = reader.readLine()) != null)
            {
               if (ClassLoaders.containsClass(addon.getClassLoader(), serviceName))
               {
                  Class<?> type = ClassLoaders.loadClass(addon.getClassLoader(), serviceName);
                  if (ClassLoaders.ownsClass(addon.getClassLoader(), type))
                  {
                     serviceTypes.add(type);
                  }
               }
               else
               {
                  log.log(Level.WARNING,
                           "Service class not enabled due to underlying classloading error. If this is unexpected, "
                                    + "enable DEBUG logging to see the full stack trace: "
                                    + getClassLoadingErrorMessage(addon, serviceName));
                  log.log(Level.FINE,
                           "Service class not enabled due to underlying classloading error.",
                           ClassLoaders.getClassLoadingExceptionFor(addon.getClassLoader(), serviceName));
               }
            }
         }
      }

      return serviceTypes;
   }

   private static String getClassLoadingErrorMessage(Addon addon, String serviceType)
   {
      Throwable e = ClassLoaders.getClassLoadingExceptionFor(addon.getClassLoader(), serviceType);
      while (e.getCause() != null && e.getCause() != e)
      {
         e = e.getCause();
      }
      return e.getClass().getName() + ": " + e.getMessage();
   }

   @Override
   public void postStartup(Addon addon) throws Exception
   {
      eventManager.fireEvent(new PostStartup(addon));
   }

   @Override
   public void preShutdown(Addon addon) throws Exception
   {
      eventManager.fireEvent(new PreShutdown(addon));
   }

   @Override
   public ControlType getControlType()
   {
      return ControlType.DEPENDENTS;
   }
}