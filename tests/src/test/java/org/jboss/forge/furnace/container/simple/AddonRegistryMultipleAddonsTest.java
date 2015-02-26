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
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
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
public class AddonRegistryMultipleAddonsTest
{

   @Deployment
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.furnace.container:simple")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addBeansXML()
               .addAsServiceProvider(Service.class, AddonRegistryMultipleAddonsTest.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple"),
                        AddonDependencyEntry.create("test:dep2"),
                        AddonDependencyEntry.create("test:dep1")

               );

      return archive;
   }

   @Deployment(name = "test:dep2,2", testable = false, order = 2)
   public static AddonArchive getDeploymentDep2()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClass(BB.class)
               .addAsServiceProvider(Service.class, BB.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple")
               );
      return archive;
   }

   @Deployment(name = "test:dep1,1", testable = false, order = 1)
   public static AddonArchive getDeploymentDep1()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addClass(Aa.class)
               .addAsServiceProvider(Service.class, Aa.class)
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

      Assert.assertTrue(depOneServiceRegistry.hasService(Aa.class));
      Assert.assertFalse(depOneServiceRegistry.hasService(BB.class));
      Assert.assertFalse(depTwoServiceRegistry.hasService(Aa.class));
      Assert.assertTrue(depTwoServiceRegistry.hasService(BB.class));

      Assert.assertNull(depOneServiceRegistry.getExportedInstance(BB.class.getName()));
      Assert.assertNull(depTwoServiceRegistry.getExportedInstance(Aa.class.getName()));

      Assert.assertNotNull(depOneServiceRegistry.getExportedInstance(Aa.class.getName()));
      Assert.assertNotNull(depTwoServiceRegistry.getExportedInstance(BB.class.getName()));

      Imported<Aa> services = addonRegistry.getServices(Aa.class);
      Assert.assertFalse("Imported<Aa> should have been satisfied", services.isUnsatisfied());
      Assert.assertFalse("Imported<Aa> should not have been ambiguous", services.isAmbiguous());
      Iterator<Aa> iterator = services.iterator();
      Assert.assertTrue(iterator.hasNext());
      Assert.assertThat(iterator.next(), is(instanceOf(Aa.class)));
      Assert.assertFalse(iterator.hasNext());

      Imported<BB> services2 = addonRegistry.getServices(BB.class);
      Assert.assertFalse("Imported<BB> should have been satisfied", services2.isUnsatisfied());
      Assert.assertFalse("Imported<BB> should not have been ambiguous", services2.isAmbiguous());
      Iterator<BB> iterator2 = services2.iterator();
      Assert.assertTrue(iterator2.hasNext());
      Assert.assertThat(iterator2.next(), is(instanceOf(BB.class)));
      Assert.assertFalse(iterator2.hasNext());
   }

}
