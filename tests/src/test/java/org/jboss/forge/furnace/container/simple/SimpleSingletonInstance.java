package org.jboss.forge.furnace.container.simple;

import java.util.Random;

public class SimpleSingletonInstance implements SingletonService
{
   private int randomInteger;
   public SimpleSingletonInstance() {
      Random r = new Random();
      randomInteger = r.nextInt();
   }
   
   public int getRandomInteger() {
      return randomInteger;
   }
}
