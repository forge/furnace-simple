/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.lifecycle;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.container.simple.EventListener;
import org.jboss.forge.furnace.container.simple.Service;
import org.jboss.forge.furnace.container.simple.events.SimpleEventManagerImpl;
import org.jboss.forge.furnace.container.simple.impl.SimpleServiceRegistry;
import org.jboss.forge.furnace.event.EventManager;
import org.jboss.forge.furnace.event.PostStartup;
import org.jboss.forge.furnace.event.PreShutdown;
import org.jboss.forge.furnace.lifecycle.AddonLifecycleProvider;
import org.jboss.forge.furnace.lifecycle.ControlType;
import org.jboss.forge.furnace.spi.ServiceRegistry;

/**
 * Implements a fast and simple {@link AddonLifecycleProvider} for the {@link Furnace} runtime. Allows {@link Service}
 * and {@link EventListener} registration.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SimpleAddonLifecycleProvider implements AddonLifecycleProvider
{
   private Furnace furnace;
   private EventManager eventManager;
   private ServiceRegistry serviceRegistry;

   @Override
   public void initialize(Furnace furnace, AddonRegistry registry, Addon self) throws Exception
   {
      this.furnace = furnace;
   }

   @Override
   public void start(Addon addon) throws Exception
   {
      SimpleContainer.start(addon, furnace);
      this.serviceRegistry = new SimpleServiceRegistry(addon);
      this.eventManager = new SimpleEventManagerImpl(addon, serviceRegistry);
   }

   @Override
   public void stop(Addon addon) throws Exception
   {
      SimpleContainer.stop(addon);
      if (this.serviceRegistry != null)
      {
         serviceRegistry.close();
      }
      this.serviceRegistry = null;
      this.eventManager = null;
   }

   @Override
   public EventManager getEventManager(Addon addon)
   {
      return eventManager;
   }

   @Override
   public ServiceRegistry getServiceRegistry(Addon addon)
   {
      return serviceRegistry;
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