/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import java.util.Iterator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.spi.ServiceRegistry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class AddonRegistryInheritanceLookupTest implements Service
{

   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:simple")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple"),
                        AddonDependencyEntry.create("test:dep2"),
                        AddonDependencyEntry.create("test:dep1")

               );

      return archive;
   }

   @Deployment(name = "test:dep2,2", testable = false, order = 2)
   public static ForgeArchive getDeploymentDep2()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClass(PublishedServiceSubtype.class)
               .addAsServiceProvider(Service.class, PublishedServiceSubtype.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple"),
                        AddonDependencyEntry.create("test:dep1")
               );
      return archive;
   }

   @Deployment(name = "test:dep1,1", testable = false, order = 1)
   public static ForgeArchive getDeploymentDep1()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClass(PublishedService.class)
               .addAsServiceProvider(Service.class, PublishedService.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple")
               );

      return archive;
   }

   @Test
   public void testServiceWithExpectedObjectsDifferentClassLoaders() throws Exception
   {
      AddonRegistry addonRegistry = SimpleContainer.getFurnace(this.getClass().getClassLoader()).getAddonRegistry();

      AddonId depOneId = AddonId.from("test:dep1", "1");
      AddonId depTwoId = AddonId.from("test:dep2", "2");

      Addon depOne = addonRegistry.getAddon(depOneId);
      Addon depTwo = addonRegistry.getAddon(depTwoId);

      ServiceRegistry depOneServiceRegistry = depOne.getServiceRegistry();
      ServiceRegistry depTwoServiceRegistry = depTwo.getServiceRegistry();

      Assert.assertTrue(depOneServiceRegistry.hasService(PublishedService.class));
      Assert.assertFalse(depOneServiceRegistry.hasService(PublishedServiceSubtype.class));
      Assert.assertTrue(depTwoServiceRegistry.hasService(PublishedService.class));
      Assert.assertTrue(depTwoServiceRegistry.hasService(PublishedServiceSubtype.class));

      Assert.assertNotNull(depOneServiceRegistry.getExportedInstance(PublishedService.class.getName()));
      Assert.assertNotNull(depTwoServiceRegistry.getExportedInstance(PublishedService.class.getName()));
      Assert.assertNull(depOneServiceRegistry.getExportedInstance(PublishedServiceSubtype.class.getName()));
      Assert.assertNotNull(depTwoServiceRegistry.getExportedInstance(PublishedServiceSubtype.class.getName()));

      Imported<PublishedService> services = addonRegistry.getServices(PublishedService.class);
      Assert.assertFalse("Imported<PublishedService> should have been satisfied", services.isUnsatisfied());
      Assert.assertTrue("Imported<PublishedService> should have been ambiguous", services.isAmbiguous());
      Iterator<PublishedService> iterator = services.iterator();
      Assert.assertTrue(iterator.hasNext());
      Assert.assertThat(iterator.next(), is(instanceOf(PublishedService.class)));
      Assert.assertTrue(iterator.hasNext());
      Assert.assertThat(iterator.next(), is(instanceOf(PublishedService.class)));

      Imported<PublishedServiceSubtype> services2 = addonRegistry.getServices(PublishedServiceSubtype.class);
      Assert.assertFalse("Imported<PublishedServiceSubtype> should have been satisfied", services2.isUnsatisfied());
      Assert.assertFalse("Imported<PublishedServiceSubtype> should not have been ambiguous", services2.isAmbiguous());
      Iterator<PublishedServiceSubtype> iterator2 = services2.iterator();
      Assert.assertTrue(iterator2.hasNext());
      Assert.assertThat(iterator2.next(), is(instanceOf(PublishedServiceSubtype.class)));
      Assert.assertFalse(iterator2.hasNext());
   }

}
