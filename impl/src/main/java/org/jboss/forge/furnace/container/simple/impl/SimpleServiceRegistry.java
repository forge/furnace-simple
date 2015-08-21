/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.EventListener;
import org.jboss.forge.furnace.container.simple.Producer;
import org.jboss.forge.furnace.container.simple.Service;
import org.jboss.forge.furnace.container.simple.SingletonService;
import org.jboss.forge.furnace.spi.ExportedInstance;
import org.jboss.forge.furnace.spi.ServiceRegistry;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.ClassLoaders;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SimpleServiceRegistry implements ServiceRegistry
{
   private static final Logger log = Logger.getLogger(SimpleServiceRegistry.class.getName());

   private final Addon addon;

   private final Set<Class<?>> serviceTypes = new HashSet<>();
   private final Set<Class<?>> singletonServiceTypes = new HashSet<>();

   private final Map<String, ExportedInstance<?>> instancesCache = new ConcurrentHashMap<>();

   @SuppressWarnings("unchecked")
   public SimpleServiceRegistry(Addon addon)
   {
      this.addon = addon;
      // Simple services
      for (Class<?> type : locateServices(addon, Service.class, EventListener.class))
      {
         Class<?> serviceType = type;
         ExportedInstance<?> exportedInstance;
         if (Producer.class.isAssignableFrom(type))
         {
            serviceType = extractProducesType(type);
            exportedInstance = new SimpleProducerExportedInstance(addon, serviceType, type, false);
         }
         else
         {
            exportedInstance = new SimpleExportedInstance<>(addon, type);
         }

         serviceTypes.add(serviceType);
         instancesCache.put(serviceType.getName(), exportedInstance);
      }

      // Singleton services
      for (Class<?> type : locateServices(addon, SingletonService.class))
      {
         Class<?> serviceType = type;
         ExportedInstance<?> exportedInstance;
         if (Producer.class.isAssignableFrom(type))
         {
            serviceType = extractProducesType(type);
            exportedInstance = new SimpleProducerExportedInstance(addon, serviceType, type, true);
         }
         else
         {
            exportedInstance = new SimpleSingletonExportedInstance<>(addon, type);
         }
         singletonServiceTypes.add(serviceType);
         instancesCache.put(serviceType.getName(), exportedInstance);
      }
   }

   static Class<?> extractProducesType(Class<?> service)
   {
      Type[] genericInterfaces = service.getGenericInterfaces();
      for (Type type : genericInterfaces)
      {
         if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == Producer.class)
         {
            return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
         }
      }
      return null;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> Set<ExportedInstance<T>> getExportedInstances(String clazz)
   {
      try
      {
         return getExportedInstances((Class<T>) Class.forName(clazz, false, addon.getClassLoader()));
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
      Set<ExportedInstance<T>> result = new HashSet<>();
      for (Class<?> type : singletonServiceTypes)
      {
         if (clazz.isAssignableFrom(type))
         {
            result.add((ExportedInstance<T>) instancesCache.get(type.getName()));
         }
      }

      for (Class<?> type : serviceTypes)
      {
         if (clazz.isAssignableFrom(type))
         {
            result.add((ExportedInstance<T>) instancesCache.get(type.getName()));
         }
      }
      return result;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> ExportedInstance<T> getExportedInstance(String clazz)
   {
      try
      {
         Class<?> type = Class.forName(clazz, false, addon.getClassLoader());
         return getExportedInstance((Class<T>) type);
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

      for (Class<?> type : singletonServiceTypes)
      {
         if (clazz.isAssignableFrom(type))
         {
            return (ExportedInstance<T>) instancesCache.get(type.getName());
         }
      }

      for (Class<?> type : serviceTypes)
      {
         if (clazz.isAssignableFrom(type))
         {
            return (ExportedInstance<T>) instancesCache.get(type.getName());
         }
      }

      return null;
   }

   @Override
   public Set<Class<?>> getExportedTypes()
   {
      final Set<Class<?>> result = new HashSet<>();
      result.addAll(serviceTypes);
      result.addAll(singletonServiceTypes);
      return Collections.unmodifiableSet(result);
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> Set<Class<T>> getExportedTypes(Class<T> type)
   {
      Set<Class<T>> result = new HashSet<>();
      for (Class<?> serviceType : getExportedTypes())
      {
         if (type.isAssignableFrom(serviceType))
         {
            result.add((Class<T>) serviceType);
         }
      }
      return result;
   }

   @Override
   public boolean hasService(Class<?> clazz)
   {
      for (Class<?> service : getExportedTypes())
      {
         if (clazz.isAssignableFrom(service))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean hasService(String clazz)
   {
      try
      {
         return hasService(Class.forName(clazz, false, addon.getClassLoader()));
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }
   }

   @Override
   public void close()
   {
      this.serviceTypes.clear();
      this.singletonServiceTypes.clear();
      this.instancesCache.clear();
   }

   @Override
   public String toString()
   {
      return "SimpleServiceRegistry [serviceTypes=" + serviceTypes + ", singletonServiceTypes=" + singletonServiceTypes
               + "]";
   }

   private static Set<Class<?>> locateServices(Addon addon, Class<?>... serviceTypes)
   {
      Set<Class<?>> allServiceTypes = new HashSet<>();
      try
      {
         for (Class<?> serviceType : serviceTypes)
         {
            Enumeration<URL> resources = addon.getClassLoader()
                     .getResources("/META-INF/services/" + serviceType.getName());
            while (resources.hasMoreElements())
            {
               URL resource = resources.nextElement();
               try (InputStream stream = resource.openStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream)))
               {
                  String serviceName;
                  while ((serviceName = reader.readLine()) != null)
                  {
                     try
                     {
                        Class<?> type = ClassLoaders.loadClass(addon.getClassLoader(), serviceName);
                        if (ClassLoaders.ownsClass(addon.getClassLoader(), type))
                        {
                           log.log(Level.FINE, "Service {0} registered", type);
                           allServiceTypes.add(type);
                        }
                     }
                     catch (Throwable e)
                     {
                        log.log(Level.WARNING,
                                 "Service class not enabled due to underlying classloading error. "
                                          + "This may happen if the class is not yet loaded by Furnace. If this is unexpected, "
                                          + "enable DEBUG logging to see the full stack trace: "
                                          + getClassLoadingErrorMessage(addon, serviceName, e));
                        log.log(Level.FINE,
                                 "Service " + serviceName + " not enabled due to underlying classloading error.", e);
                     }
                  }
               }
            }
         }
      }
      catch (IOException ie)
      {
         log.log(Level.SEVERE, "Error while reading service classes", ie);
      }
      return allServiceTypes;
   }

   private static String getClassLoadingErrorMessage(Addon addon, String serviceType, Throwable e)
   {
      while (e.getCause() != null && e.getCause() != e)
      {
         e = e.getCause();
      }
      return e.getClass().getName() + ": " + e.getMessage();
   }
}
