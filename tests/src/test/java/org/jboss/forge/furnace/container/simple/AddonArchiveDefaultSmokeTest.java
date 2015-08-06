/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple;

import static org.hamcrest.CoreMatchers.notNullValue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Smoke test for tests without a {@link Deployment} method
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class AddonArchiveDefaultSmokeTest
{
   @Test
   public void testFurnace()
   {
      Furnace furnace = SimpleContainer.getFurnace(getClass().getClassLoader());
      Assert.assertThat(furnace, notNullValue());
   }

   @Test
   public void testAddon()
   {
      Addon addon = SimpleContainer.getAddon(getClass().getClassLoader());
      Assert.assertThat(addon, notNullValue());
      Assert.assertEquals("_DEFAULT_", addon.getId().getName());
   }

}
