package org.jboss.forge.furnace.container.simple;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class PublishedService
{
   public String getMessage()
   {
      return "I am PublishedService.";
   }

   public ClassLoader getClassLoader()
   {
      return getClass().getClassLoader();
   }
}
