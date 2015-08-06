/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SingletonServiceTest
{
   @Deployment
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.furnace.container:simple")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(SingletonServiceTest.class)
               .addClasses(SimpleSingletonInstance.class)
               .addAsServiceProvider(SingletonService.class, SimpleSingletonInstance.class)
               .addAsServiceProvider(Service.class, SingletonServiceTest.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple"));

      return archive;
   }

   @Test
   public void testContainerStartup()
   {
      Imported<SimpleSingletonInstance> services = SimpleContainer.getServices(SimpleSingletonInstance.class);
      Imported<SimpleSingletonInstance> services2 = SimpleContainer.getServices(SimpleSingletonInstance.class);
      assertNotNull(services.get());
      assertEquals(services.get().getRandomInteger(), services2.get().getRandomInteger());
   }
}