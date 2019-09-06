/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 *
 */
package uk.ac.reading.cs.knime.sax;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * This is the eclipse bundle activator.
 * Note: KNIME node developers probably won't have to do anything in here, 
 * as this class is only needed by the eclipse platform/plug-in mechanism.
 * If you want to move/rename this file, make sure to change the plugin.xml
 * file in the project root directory accordingly.
 *
 * @author Ryan Faulkner
 */
public class SAXNodePlugin extends Plugin {
	/** Make sure that this *always* matches the ID in plugin.xml. */
	public static final String PLUGIN_ID = "uk.ac.reading.sse.sempr14.sax";

	// The shared instance.
	private static SAXNodePlugin plugin;

	/**
	 * The constructor.
	 */
	public SAXNodePlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context		The OSGI bundle context
	 * @throws Exception	If this plug-in could not be started
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context		The OSGI bundle context
	 * @throws Exception	If this plug-in could not be stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Singleton	instance of the plug-in
	 */
	public static SAXNodePlugin getDefault() {
		return plugin;
	}
}