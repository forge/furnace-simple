/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple;

import static org.hamcrest.CoreMatchers.everyItem;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
@Ignore("Review this test")
public class AddonRegistryIncompatibleServiceLookupTest
{

   @Deployment
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.furnace.container:simple")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClass(Aa.class)
               .addBeansXML()
               .addAsServiceProvider(Service.class, AddonRegistryIncompatibleServiceLookupTest.class)
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
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple"));
      return archive;
   }

   @Deployment(name = "test:dep1,1", testable = false, order = 1)
   public static AddonArchive getDeploymentDep1()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addClass(BB.class)
               .addAsServiceProvider(Service.class, BB.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple"));

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

      Assert.assertFalse(depOneServiceRegistry.hasService(Aa.class));
      Assert.assertFalse(depOneServiceRegistry.hasService(Aa.class.getName()));
      Assert.assertFalse(depTwoServiceRegistry.hasService(Aa.class));
      Assert.assertFalse(depTwoServiceRegistry.hasService(Aa.class.getName()));

      Assert.assertTrue(depOneServiceRegistry.hasService(depTwo.getClassLoader().loadClass(BB.class.getName())));
      Assert.assertTrue(depOneServiceRegistry.hasService(BB.class));
      Assert.assertTrue(depOneServiceRegistry.hasService(BB.class.getName()));

      Assert.assertTrue(depTwoServiceRegistry.hasService(depTwo.getClassLoader().loadClass(BB.class.getName())));
      Assert.assertTrue(depTwoServiceRegistry.hasService(BB.class));
      Assert.assertTrue(depTwoServiceRegistry.hasService(BB.class.getName()));

      Assert.assertNotNull(depTwoServiceRegistry.getExportedInstance(BB.class));
      Assert.assertNotNull(depTwoServiceRegistry.getExportedInstance(BB.class.getName()));

      Imported<BB> services = addonRegistry.getServices(BB.class);
      Assert.assertFalse("Imported<BB> should have been satisfied", services.isUnsatisfied());
      Assert.assertTrue("Imported<BB> should have been ambiguous", services.isAmbiguous());
      Assert.assertThat(services, everyItem(CoreMatchers.<BB> instanceOf(BB.class)));
   }
}
