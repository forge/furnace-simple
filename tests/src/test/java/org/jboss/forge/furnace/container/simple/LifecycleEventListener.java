package org.jboss.forge.furnace.container.simple;

import java.lang.annotation.Annotation;

import org.jboss.forge.furnace.event.PostStartup;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class LifecycleEventListener implements EventListener
{
   private static boolean observedPostStartup;

   public boolean isPostStartupObserved()
   {
      return observedPostStartup;
   }

   @Override
   public void handleEvent(Object event, Annotation... qualifiers)
   {
      if (event instanceof PostStartup)
         observedPostStartup = true;
   }
}
