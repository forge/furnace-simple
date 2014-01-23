package org.jboss.forge.furnace.container.simple.lifecycle;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.container.simple.EventListener;
import org.jboss.forge.furnace.container.simple.Service;
import org.jboss.forge.furnace.container.simple.events.SimpleEventManagerImpl;
import org.jboss.forge.furnace.container.simple.impl.SimpleServiceRegistryImpl;
import org.jboss.forge.furnace.event.EventManager;
import org.jboss.forge.furnace.event.PostStartup;
import org.jboss.forge.furnace.event.PreShutdown;
import org.jboss.forge.furnace.lifecycle.AddonLifecycleProvider;
import org.jboss.forge.furnace.lifecycle.ControlType;
import org.jboss.forge.furnace.spi.ServiceRegistry;
import org.jboss.forge.furnace.util.ClassLoaders;
import org.jboss.forge.furnace.util.Streams;

/**
 * Implements a fast and simple {@link AddonLifecycleProvider} for the {@link Furnace} runtime. Allows {@link Service}
 * and {@link EventListener} registration.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SimpleContainer implements AddonLifecycleProvider
{
   private static final String SERVICE_REGISTRATION_FILE_NAME = Service.class.getName();

   private static Map<ClassLoader, Furnace> started = new ConcurrentHashMap<>(
            new WeakHashMap<ClassLoader, Furnace>());

   private Furnace furnace;

   /**
    * Used to retrieve an instance of {@link Furnace}.
    */
   public static Furnace getFurnace(ClassLoader loader)
   {
      return started.get(loader);
   }

   @Override
   public void initialize(Furnace furnace, AddonRegistry registry, Addon self) throws Exception
   {
      this.furnace = furnace;
   }

   @Override
   public void start(Addon addon) throws Exception
   {
      started.put(addon.getClassLoader(), furnace);
   }

   @Override
   public void stop(Addon addon) throws Exception
   {
      started.remove(addon.getClassLoader());
   }

   @Override
   public EventManager getEventManager(Addon addon)
   {
      return new SimpleEventManagerImpl(addon);
   }

   @Override
   public ServiceRegistry getServiceRegistry(Addon addon) throws Exception
   {
      URL resource = addon.getClassLoader().getResource("/META-INF/services/" + SERVICE_REGISTRATION_FILE_NAME);
      Set<Class<?>> serviceTypes = new HashSet<>();
      if (resource != null)
      {
         InputStream stream = resource.openStream();
         String services = Streams.toString(stream);
         for (String serviceType : services.split("\n"))
         {
            if (ClassLoaders.containsClass(addon.getClassLoader(), serviceType))
            {
               Class<?> type = ClassLoaders.loadClass(addon.getClassLoader(), serviceType);
               serviceTypes.add(type);
            }
         }

      }
      return new SimpleServiceRegistryImpl(furnace, addon, serviceTypes);
   }

   @Override
   public void postStartup(Addon addon) throws Exception
   {
      getEventManager(addon).fireEvent(new PostStartup(addon));
   }

   @Override
   public void preShutdown(Addon addon) throws Exception
   {
      getEventManager(addon).fireEvent(new PreShutdown(addon));
   }

   @Override
   public ControlType getControlType()
   {
      return ControlType.DEPENDENTS;
   }
}