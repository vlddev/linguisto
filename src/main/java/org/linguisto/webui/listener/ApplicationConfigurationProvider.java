package org.linguisto.webui.listener;

import javax.servlet.ServletContext;

import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.config.Log;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Path;

@RewriteConfiguration
public class ApplicationConfigurationProvider extends HttpConfigurationProvider {
	@Override
	public Configuration getConfiguration(ServletContext context) {
		return ConfigurationBuilder.begin()
               .addRule()
               .when(Direction.isInbound().and(Path.matches("/{path}")))
       .perform(Log.message(Level.INFO, "Client requested path: {path}"))
       .where("path").matches(".*");
	}

	@Override
	public int priority() {
		// TODO Auto-generated method stub
		return 0;
	}
}