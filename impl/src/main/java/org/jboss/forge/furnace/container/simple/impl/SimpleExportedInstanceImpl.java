/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.impl;

import java.lang.reflect.Constructor;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.exception.ContainerException;
import org.jboss.forge.furnace.proxy.ClassLoaderInterceptor;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.spi.ExportedInstance;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class SimpleExportedInstanceImpl<T> implements ExportedInstance<T>
{
   private final Furnace furnace;
   private final Addon addon;
   private final Class<T> type;

   public SimpleExportedInstanceImpl(Furnace furnace, Addon addon, Class<T> clazz)
   {
      this.furnace = furnace;
      this.addon = addon;
      this.type = clazz;
   }

   @Override
   public T get()
   {
      T delegate = null;
      try
      {
         try
         {
            Constructor<T> constructor = type.getConstructor(Furnace.class);
            delegate = constructor.newInstance(furnace);
         }
         catch (NoSuchMethodException e)
         {
            delegate = type.newInstance();
         }
      }
      catch (Exception e)
      {
         throw new ContainerException("Could not create instance of [" + type.getName() + "] through reflection.", e);
      }
      return Proxies.enhance(addon.getClassLoader(), delegate, new ClassLoaderInterceptor(addon.getClassLoader(),
               delegate));
   }

   @Override
   public void release(T instance)
   {
      // no action required
   }

   @Override
   public String toString()
   {
      return type.getName() + " from " + addon;
   }

   @Override
   public Class<? extends T> getActualType()
   {
      return type;
   }

   @Override
   public Addon getSourceAddon()
   {
      return addon;
   }
}
