/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.event.EventManager;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class EventListenerMultipleAddonsTest
{
   @Deployment
   @AddonDependencies
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addAsServiceProvider(Service.class, EventListenerMultipleAddonsTest.class, FooEventListener.class);
      return archive;
   }

   @Deployment(name = "test:dep2,2", testable = false, order = 2)
   public static AddonArchive getDeploymentDep2()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClass(PublishedService.class)
               .addAsServiceProvider(Service.class, PublishedService.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple"),
                        AddonDependencyEntry.create("test:dep1"));
      return archive;
   }

   @Deployment(name = "test:dep1,1", testable = false, order = 1)
   public static AddonArchive getDeploymentDep1()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClass(PublishedService.class)
               .addAsServiceProvider(Service.class, PublishedService.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple"));
      return archive;
   }

   @Test
   public void testFireEvent()
   {
      Furnace furnace = SimpleContainer.getFurnace(this.getClass().getClassLoader());
      AddonRegistry addonRegistry = furnace.getAddonRegistry();
      AtomicInteger atomicInteger = new AtomicInteger();
      addonRegistry.getEventManager().fireEvent(atomicInteger);
      Assert.assertEquals(1, atomicInteger.intValue());
   }

   @Test
   public void testFireEventFromAddon()
   {
      Addon addon = SimpleContainer.getAddon(getClass().getClassLoader());
      EventManager eventManager = addon.getEventManager();
      AtomicInteger atomicInteger = new AtomicInteger();
      eventManager.fireEvent(atomicInteger);
      Assert.assertEquals(1, atomicInteger.intValue());
   }

   public static class FooEventListener implements EventListener
   {
      @Override
      public void handleEvent(Object event, Annotation... qualifiers)
      {
         Logger.getGlobal().log(Level.FINE, "##########EVENT: %s %s %n", new Object[] { event.getClass(), event });
         if (event instanceof AtomicInteger)
         {
            ((AtomicInteger) event).incrementAndGet();
         }
      }
   }
}
