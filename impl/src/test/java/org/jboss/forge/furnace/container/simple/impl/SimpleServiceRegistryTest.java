/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple.impl;

import org.jboss.forge.furnace.container.simple.Producer;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class SimpleServiceRegistryTest
{

   @Test
   public void testExtractProducesType()
   {
      Class<?> type = SimpleServiceRegistry.extractProducesType(MyProducer.class);
      Assert.assertSame(String.class, type);
   }

   class MyProducer implements Producer<String>
   {
      @Override
      public String get()
      {
         return null;
      }
   }
}
