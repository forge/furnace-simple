/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.events;

import java.lang.annotation.Annotation;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.EventListener;
import org.jboss.forge.furnace.container.simple.impl.SimpleServiceRegistry;
import org.jboss.forge.furnace.event.EventException;
import org.jboss.forge.furnace.event.EventManager;
import org.jboss.forge.furnace.spi.ExportedInstance;

/**
 * {@link EventManager} implementation for Simple container
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SimpleEventManagerImpl implements EventManager
{
   private final SimpleServiceRegistry serviceRegistry;

   public SimpleEventManagerImpl(Addon addon, SimpleServiceRegistry serviceRegistry)
   {
      this.serviceRegistry = serviceRegistry;
   }

   @Override
   public void fireEvent(Object event, Annotation... qualifiers) throws EventException
   {
      for (ExportedInstance<EventListener> instance : serviceRegistry.getExportedInstances(EventListener.class))
      {
         EventListener listener = null;
         try
         {
            listener = instance.get();
            listener.handleEvent(event, qualifiers);
         }
         finally
         {
            // Do not release, otherwise singleton instances will be GC'ed
            // instance.release(listener);
         }
      }
   }
}