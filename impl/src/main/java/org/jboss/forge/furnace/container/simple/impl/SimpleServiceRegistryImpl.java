/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.exception.ContainerException;
import org.jboss.forge.furnace.spi.ExportedInstance;
import org.jboss.forge.furnace.spi.ServiceRegistry;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.ClassLoaders;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class SimpleServiceRegistryImpl implements ServiceRegistry
{
   private final Logger log = Logger.getLogger(getClass().getName());

   private final Furnace furnace;
   private final Addon addon;
   private final Set<Class<?>> serviceTypes;

   private final Map<String, Class<?>> classCache = new WeakHashMap<>();
   private final Map<String, ExportedInstance<?>> instanceCache = new WeakHashMap<>();
   private final Map<String, Set<ExportedInstance<?>>> instancesCache = new WeakHashMap<>();

   public SimpleServiceRegistryImpl(Furnace furnace, Addon addon, Set<Class<?>> serviceTypes)
   {
      this.furnace = furnace;
      this.addon = addon;
      this.serviceTypes = serviceTypes;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> Set<ExportedInstance<T>> getExportedInstances(String clazz)
   {
      try
      {
         Class<T> type = (Class<T>) loadAddonClass(clazz);
         return getExportedInstances(type);
      }
      catch (ClassNotFoundException e)
      {
         return Collections.emptySet();
      }
   }

   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public <T> Set<ExportedInstance<T>> getExportedInstances(Class<T> clazz)
   {
      Addons.waitUntilStarted(addon);

      final Class<T> actualLoadedType;
      try
      {
         actualLoadedType = loadAddonClass(clazz);
      }
      catch (ClassNotFoundException e)
      {
         log.fine("Class " + clazz.getName() + " is not present in this addon [" + addon + "]");
         return Collections.emptySet();
      }

      Set<ExportedInstance<T>> result = (Set) instancesCache.get(actualLoadedType.getName());

      if (result == null)
      {
         result = new HashSet<>();
         for (Class<?> type : serviceTypes)
         {
            if (actualLoadedType.isAssignableFrom(type))
            {
               result.add(new SimpleExportedInstanceImpl<>(furnace, addon, (Class<T>) type));
            }
         }

         instancesCache.put(actualLoadedType.getName(), (Set) result);
      }
      return result;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> ExportedInstance<T> getExportedInstance(String clazz)
   {
      Class<T> type;
      try
      {
         type = (Class<T>) loadAddonClass(clazz);
         return getExportedInstance(type);
      }
      catch (ClassNotFoundException e)
      {
         return null;
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> ExportedInstance<T> getExportedInstance(final Class<T> clazz)
   {
      Assert.notNull(clazz, "Requested Class type may not be null");
      Addons.waitUntilStarted(addon);

      final Class<T> actualLoadedType;
      try
      {
         actualLoadedType = loadAddonClass(clazz);
      }
      catch (ClassNotFoundException cnfe)
      {
         log.fine("Class " + clazz.getName() + " is not present in this addon [" + addon + "]");
         return null;
      }

      ExportedInstance<T> result = (ExportedInstance<T>) instanceCache.get(actualLoadedType.getName());
      if (result == null)
      {
         try
         {
            result = ClassLoaders.executeIn(addon.getClassLoader(), new Callable<ExportedInstance<T>>()
            {
               @Override
               public ExportedInstance<T> call() throws Exception
               {
                  for (Class<?> type : serviceTypes)
                  {
                     if (actualLoadedType.isAssignableFrom(type))
                     {
                        return new SimpleExportedInstanceImpl<>(furnace, addon, (Class<T>) type);
                     }
                  }

                  if (ClassLoaders.ownsClass(addon.getClassLoader(), clazz))
                     return new SimpleExportedInstanceImpl<>(furnace, addon, actualLoadedType);

                  return null;
               }
            });

            if (result != null)
               instanceCache.put(actualLoadedType.getName(), result);
         }
         catch (Exception e)
         {
            throw new ContainerException("Could not get service of type [" + actualLoadedType + "] from addon ["
                     + addon
                     + "]", e);
         }

      }
      return result;
   }

   @Override
   public Set<Class<?>> getExportedTypes()
   {
      return Collections.unmodifiableSet(serviceTypes);
   }

   @Override
   public <T> Set<Class<T>> getExportedTypes(Class<T> type)
   {
      Set<Class<T>> result = new HashSet<>();
      for (Class<?> serviceType : serviceTypes)
      {
         if (type.isAssignableFrom(serviceType))
         {
            result.add(type);
         }
      }
      return result;
   }

   @Override
   public boolean hasService(Class<?> clazz)
   {
      Addons.waitUntilStarted(addon);
      Class<?> type;
      try
      {
         type = loadAddonClass(clazz);
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }
      return !getExportedTypes(type).isEmpty();
   }

   @Override
   public boolean hasService(String clazz)
   {
      try
      {
         Class<?> type = loadAddonClass(clazz);
         return hasService(type);
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }
   }

   /**
    * Ensures that the returned class is loaded from this {@link Addon}
    */
   @SuppressWarnings("unchecked")
   private <T> Class<T> loadAddonClass(Class<T> actualType) throws ClassNotFoundException
   {
      /*
       * FIXME The need for this method defeats the entire purpose of a true module system. This needs to be fixed by
       * the CLAC.
       */
      final Class<T> type;
      if (actualType.getClassLoader() == addon.getClassLoader())
      {
         type = actualType;
      }
      else
      {
         type = (Class<T>) loadAddonClass(actualType.getName());
      }
      return type;
   }

   private Class<?> loadAddonClass(String className) throws ClassNotFoundException
   {
      Class<?> cached = classCache.get(className);
      if (cached == null)
      {
         Class<?> result = Class.forName(className, false, addon.getClassLoader());
         // potentially not thread-safe
         classCache.put(className, result);
         cached = result;
      }

      return cached;
   }

   @Override
   public String toString()
   {
      return serviceTypes.toString();
   }
}
