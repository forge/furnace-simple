/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.lifecycle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.container.simple.EventListener;
import org.jboss.forge.furnace.lifecycle.AddonLifecycleProvider;
import org.jboss.forge.furnace.services.Imported;

/**
 * Implements a fast and simple {@link AddonLifecycleProvider} for the {@link Furnace} runtime. Allows Service and
 * {@link EventListener} registration.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SimpleContainer
{
   private static Map<ClassLoader, Furnace> started = new ConcurrentHashMap<>();

   /**
    * Used to retrieve an instance of {@link Furnace}.
    */
   public static Furnace getFurnace(ClassLoader loader)
   {
      return started.get(loader);
   }

   /**
    * Returns the registered services for a given {@link Furnace} Runtime.
    * 
    * @param service the service {@link Class}
    * @return an {@link Imported} instance
    */
   public static <T> Imported<T> getServices(Class<T> service)
   {
      return getServices(service.getClassLoader(), service);
   }

   /**
    * Returns the registered services for a given {@link Furnace} Runtime.
    * 
    * @param classloader the {@link ClassLoader} this {@link Furnace} runtime can be found
    * @param service the service {@link Class}
    * @return an {@link Imported} instance
    */
   public static <T> Imported<T> getServices(ClassLoader classloader, Class<T> service)
   {
      Furnace furnace = getFurnace(classloader);
      if (furnace != null)
      {
         AddonRegistry addonRegistry = furnace.getAddonRegistry();
         return addonRegistry.getServices(service);
      }
      return null;
   }

   static void start(Addon addon, Furnace furnace)
   {
      started.put(addon.getClassLoader(), furnace);
   }

   static void stop(Addon addon)
   {
      started.remove(addon.getClassLoader());
   }

}