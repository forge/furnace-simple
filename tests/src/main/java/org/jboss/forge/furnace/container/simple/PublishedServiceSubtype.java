package org.jboss.forge.furnace.container.simple;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class PublishedServiceSubtype extends PublishedService
{
   @Override
   public String getMessage()
   {
      return "I am PublishedServiceSubtype.";
   }

   @Override
   public ClassLoader getClassLoader()
   {
      return getClass().getClassLoader();
   }
}
