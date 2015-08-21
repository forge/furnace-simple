/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple;

/**
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class SimpleSingletonInstanceProducer implements Producer<SimpleSingletonInstance>
{
   @Override
   public SimpleSingletonInstance get()
   {
      return new SimpleSingletonInstance();
   }
}
