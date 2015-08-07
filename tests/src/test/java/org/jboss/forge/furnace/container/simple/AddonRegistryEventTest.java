/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class AddonRegistryEventTest
{
   @Deployment
   @AddonDependencies
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addAsServiceProvider(Service.class, AddonRegistryEventTest.class)
               .addAsServiceProvider(EventListener.class, FooEventListener.class);
      return archive;
   }

   @Test
   public void testFireEvent()
   {
      Furnace furnace = SimpleContainer.getFurnace(this.getClass().getClassLoader());
      Assert.assertNotNull("Furnace is not started", furnace);
      AddonRegistry addonRegistry = furnace.getAddonRegistry();
      addonRegistry.getEventManager().fireEvent("Foo");
      Assert.assertTrue("Event was not received", FooEventListener.eventFired);
   }

   public static class FooEventListener implements EventListener
   {
      public static boolean eventFired;

      @Override
      public void handleEvent(Object event, Annotation... qualifiers)
      {
         eventFired = "Foo".equals(event);
      }
   }

}
