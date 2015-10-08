/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.impl;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.Producer;
import org.jboss.forge.furnace.exception.ContainerException;
import org.jboss.forge.furnace.proxy.ClassLoaderInterceptor;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.spi.ExportedInstance;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class SimpleProducerExportedInstance<T> implements ExportedInstance<T>
{
   private final Addon addon;
   private final Class<T> type;
   private final Class<? extends Producer<T>> producer;
   private final boolean singleton;
   private T delegate;

   public SimpleProducerExportedInstance(Addon addon, Class<T> type, Class<? extends Producer<T>> producer,
            boolean singleton)
   {
      this.addon = addon;
      this.type = type;
      this.producer = producer;
      this.singleton = singleton;
   }

   @Override
   public T get()
   {
      if (singleton)
      {
         if (delegate == null)
         {
            delegate = newInstance();
         }
         return delegate;
      }
      else
      {
         return newInstance();
      }
   }

   private T newInstance()
   {
      try
      {
         T delegate = producer.newInstance().get();
         delegate = Proxies.enhance(addon.getClassLoader(), delegate,
                  new ClassLoaderInterceptor(addon.getClassLoader(),
                           delegate));
         return delegate;
      }
      catch (Exception e)
      {
         throw new ContainerException("Could not create instance of [" + type.getName() + "] through reflection.",
                  e);
      }
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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((addon == null) ? 0 : addon.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      SimpleProducerExportedInstance<?> other = (SimpleProducerExportedInstance<?>) obj;
      if (addon == null)
      {
         if (other.addon != null)
            return false;
      }
      else if (!addon.equals(other.addon))
         return false;
      if (type == null)
      {
         if (other.type != null)
            return false;
      }
      else if (!type.equals(other.type))
         return false;
      return true;
   }
}
