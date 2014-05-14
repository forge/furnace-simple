package org.jboss.forge.furnace.container.simple.lifecycle;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.EventListener;
import org.jboss.forge.furnace.container.simple.Service;
import org.jboss.forge.furnace.lifecycle.AddonLifecycleProvider;

/**
 * Implements a fast and simple {@link AddonLifecycleProvider} for the {@link Furnace} runtime. Allows {@link Service}
 * and {@link EventListener} registration.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SimpleContainer
{
   private static Map<ClassLoader, Furnace> started = new ConcurrentHashMap<>(new WeakHashMap<ClassLoader, Furnace>());

   /**
    * Used to retrieve an instance of {@link Furnace}.
    */
   public static Furnace getFurnace(ClassLoader loader)
   {
      return started.get(loader);
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