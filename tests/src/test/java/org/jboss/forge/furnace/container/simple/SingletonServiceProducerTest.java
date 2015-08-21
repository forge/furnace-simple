/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple;

import static org.hamcrest.CoreMatchers.is;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SingletonServiceProducerTest
{
   @Deployment
   @AddonDependencies
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(SimpleSingletonInstance.class, SimpleSingletonInstanceProducer.class)
               .addAsServiceProvider(SingletonService.class, SingletonServiceProducerTest.class,
                        SimpleSingletonInstanceProducer.class);

      return archive;
   }

   @Test
   public void testProducerLookup()
   {
      Imported<SimpleSingletonInstance> services = SimpleContainer.getServices(getClass().getClassLoader(),
               SimpleSingletonInstance.class);
      Assert.assertThat(services.isUnsatisfied(), is(false));
      Assert.assertThat(services.isAmbiguous(), is(false));
      SimpleSingletonInstance service1 = services.get();
      SimpleSingletonInstance service2 = services.get();
      Assert.assertEquals(service1.getRandomInteger(), service2.getRandomInteger());

   }
}