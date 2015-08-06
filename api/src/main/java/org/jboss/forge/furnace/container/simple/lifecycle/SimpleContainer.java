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
import org.jboss.forge.furnace.util.Assert;

/**
 * Implements a fast and simple {@link AddonLifecycleProvider} for the {@link Furnace} runtime. Allows Service and
 * {@link EventListener} registration.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SimpleContainer
{
   private static Map<Addon, Furnace> started = new ConcurrentHashMap<>();

   /**
    * Used to retrieve an instance of {@link Furnace}.
    */
   public static Furnace getFurnace(ClassLoader loader)
   {
      return started.get(getAddon(loader));
   }

   /**
    * Returns the {@link Addon} for which the given ClassLoader represents.
    * 
    * @param loader the {@link ClassLoader} this {@link Furnace} runtime can be found
    * @return the {@link Addon} for which the given ClassLoader represents
    */
   public static Addon getAddon(ClassLoader loader)
   {
      Assert.notNull(loader, "ClassLoader must not be null");
      for (Addon addon : started.keySet())
      {
         if (addon.getClassLoader() == loader)
         {
            return addon;
         }
      }
      return null;
   }

   /**
    * Returns the registered services for a given {@link Furnace} Runtime.
    * 
    * @param classloader the {@link ClassLoader} this {@link Furnace} runtime can be found
    * @param service the service {@link Class}
    * @return an {@link Imported} instance, <code>null</code> if no {@link Furnace} container can be found in the given
    *         {@link ClassLoader}
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
      started.put(addon, furnace);
   }

   static void stop(Addon addon)
   {
      started.remove(addon);
   }

}