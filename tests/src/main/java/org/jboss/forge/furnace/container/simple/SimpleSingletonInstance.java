/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple;

import java.util.Random;

public class SimpleSingletonInstance implements SingletonService
{
   private int randomInteger;

   public SimpleSingletonInstance()
   {
      Random r = new Random();
      randomInteger = r.nextInt();
   }

   public int getRandomInteger()
   {
      return randomInteger;
   }
}