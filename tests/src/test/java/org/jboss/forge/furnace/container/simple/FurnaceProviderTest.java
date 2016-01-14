/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class FurnaceProviderTest
{
   @Test
   public void testFurnaceInstance()
   {
      ClassLoader loader = getClass().getClassLoader();
      Furnace furnace = SimpleContainer.getFurnace(loader);
      Assert.assertSame(furnace, Furnace.instance(loader));
   }
}
