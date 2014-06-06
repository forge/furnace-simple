package org.jboss.forge.furnace.container.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
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
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:simple")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClasses(SingletonServiceTest.class)
               .addClasses(SimpleSingletonInstance.class)
               .addAsServiceProvider(Service.class, SimpleSingletonInstance.class)
               .addAsServiceProvider(Service.class, SingletonServiceTest.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple")
               );

      return archive;
   }

   @Test
   public void testContainerStartup()
   {
      Imported<SimpleSingletonInstance> services = SimpleContainer.getFurnace(this.getClass().getClassLoader())
               .getAddonRegistry().getServices(SimpleSingletonInstance.class);
      Imported<SimpleSingletonInstance> services2 = SimpleContainer.getFurnace(this.getClass().getClassLoader())
               .getAddonRegistry().getServices(SimpleSingletonInstance.class);
      assertNotNull(services.get());
      assertEquals(services.get().getRandomInteger(), services2.get().getRandomInteger());
   }
}
